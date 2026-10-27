"""Read-only structure and worldgen block-state audit using real registry data.

Without --dump, collect NBT/schema inventory only. Assets never prove registration.
"""
import argparse
import gzip
import json
import re
import struct
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DATA = ROOT / "src/main/resources/data"


class NbtReader:
    def __init__(self, data):
        self.data = data
        self.offset = 0

    def take(self, count):
        if count < 0 or self.offset + count > len(self.data):
            raise ValueError("NBT payload exceeds file bounds")
        value = self.data[self.offset:self.offset + count]
        self.offset += count
        return value

    def number(self, fmt):
        return struct.unpack(">" + fmt, self.take(struct.calcsize(">" + fmt)))[0]

    def string(self):
        return self.take(self.number("H")).decode("utf-8")

    def count(self):
        count = self.number("i")
        if count < 0:
            raise ValueError("Negative NBT length")
        return count

    def payload(self, kind):
        if kind in range(1, 7):
            return self.number({1: "b", 2: "h", 3: "i", 4: "q", 5: "f", 6: "d"}[kind])
        if kind == 7:
            return self.take(self.count())
        if kind == 8:
            return self.string()
        if kind == 9:
            element, count = self.number("B"), self.count()
            if element == 0 and count:
                raise ValueError("Nonempty TAG_End list")
            return [self.payload(element) for _ in range(count)]
        if kind == 10:
            result = {}
            while True:
                element = self.number("B")
                if not element:
                    return result
                name = self.string()
                if name in result:
                    raise ValueError("Duplicate compound field: " + name)
                result[name] = self.payload(element)
        if kind in (11, 12):
            return [self.number("i" if kind == 11 else "q") for _ in range(self.count())]
        raise ValueError("Unknown NBT tag: " + str(kind))


def read_nbt(path):
    blob = path.read_bytes()
    reader = NbtReader(gzip.decompress(blob) if blob[:2] == b"\x1f\x8b" else blob)
    if reader.number("B") != 10:
        raise ValueError("Structure root must be a compound")
    reader.string()
    result = reader.payload(10)
    if reader.offset != len(reader.data):
        raise ValueError("Trailing NBT data")
    return result


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--dump", type=Path)
    parser.add_argument("--report", type=Path, default=ROOT / "reports/structure-states.json")
    args = parser.parse_args()
    registry = json.loads(args.dump.read_text(encoding="utf-8"))["block_states"] if args.dump else None
    errors, ignored_properties, missing_properties, counts = [], Counter(), Counter(), Counter()
    block_ids = Counter()

    def state(file, location, value):
        counts["block_states_checked"] += 1
        if isinstance(value, str):
            match = re.fullmatch(r"([^\[\]]+)(?:\[([^\]]*)\])?", value)
            if not match:
                errors.append(dict(file=file, location=location, error="Malformed block state", value=value))
                return
            ident = match[1]
            props = dict(part.split("=", 1) for part in match[2].split(",")) if match[2] else {}
        else:
            ident, props = value.get("Name"), value.get("Properties", {})
        if not isinstance(ident, str):
            errors.append(dict(file=file, location=location, error="Missing block ID"))
            return
        if ":" not in ident:
            ident = "minecraft:" + ident
        block_ids[ident] += 1
        if registry is None:
            return
        if ident not in registry:
            errors.append(dict(file=file, location=location, error="Unregistered block", block=ident))
            return
        actual = registry[ident]
        for key, value in props.items():
            if key not in actual:
                ignored_properties[(ident, key, value)] += 1
            elif value not in actual[key]:
                errors.append(dict(file=file, location=location, error="Invalid block property", block=ident, property=key, value=value, allowed=actual.get(key)))
        for key in actual.keys() - props.keys():
            missing_properties[(ident, key)] += 1

    for path in sorted(DATA.glob("*/structures/**/*.nbt")):
        file = path.relative_to(ROOT).as_posix()
        counts["structures"] += 1
        try:
            obj = read_nbt(path)
            palettes = obj.get("palettes", [obj.get("palette", [])])
            if not palettes or not palettes[0]:
                raise ValueError("Missing or empty palette")
            if any(len(p) != len(palettes[0]) for p in palettes):
                raise ValueError("Different palette lengths")
            size = obj["size"]
            if len(size) != 3 or any(not isinstance(x, int) or x <= 0 for x in size):
                raise ValueError("Invalid structure size")
            for p, palette in enumerate(palettes):
                for i, value in enumerate(palette):
                    state(file, f"palette[{p}][{i}]", value)
            for i, entry in enumerate(obj["blocks"]):
                counts["structure_blocks"] += 1
                if not isinstance(entry["state"], int) or not 0 <= entry["state"] < len(palettes[0]):
                    raise ValueError(f"Invalid state index in block {i}")
                pos = entry["pos"]
                if len(pos) != 3 or any(not isinstance(x, int) or not 0 <= x < bound for x, bound in zip(pos, size)):
                    raise ValueError(f"Block {i} outside structure size")
        except Exception as error:
            errors.append(dict(file=file, error=str(error)))

    def nested(value, file, location):
        if isinstance(value, dict):
            if "Name" in value:
                state(file, location, value)
            for key, child in value.items():
                if key == "block" and isinstance(child, str):
                    state(file, location + ".block", child)
                elif key == "replace" and isinstance(child, list):
                    for i, block in enumerate(child):
                        if isinstance(block, str):
                            state(file, f"{location}.replace[{i}]", block)
                else:
                    nested(child, file, location + "." + key)
        elif isinstance(value, list):
            for i, child in enumerate(value):
                nested(child, file, f"{location}[{i}]")

    for path in sorted(DATA.glob("*/worldgen/configured_feature/**/*.json")):
        counts["configured_features"] += 1
        nested(json.loads(path.read_text(encoding="utf-8")), path.relative_to(ROOT).as_posix(), "$")

    report = dict(runtime_registry_used=registry is not None, counts=dict(counts), unique_block_ids=len(block_ids),
                  block_ids=dict(sorted(block_ids.items())), errors=errors,
                  ignored_unknown_properties=[dict(block=k[0], property=k[1], value=k[2], occurrences=v) for k, v in sorted(ignored_properties.items())],
                  omitted_properties_using_registered_defaults=[dict(block=k[0], property=k[1], occurrences=v) for k, v in sorted(missing_properties.items())],
                  limits="Minecraft 1.20.1 NbtUtils.readBlockState ignores property names absent from the registered block, verified from the mapped Forge bytecode. Missing properties use registered defaults and are informational. Does not generate terrain or simulate tree placement.")
    args.report.parent.mkdir(parents=True, exist_ok=True)
    args.report.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(dict(counts=counts, unique_block_ids=len(block_ids), errors=len(errors), runtime_registry_used=registry is not None)))
    return bool(errors)


if __name__ == "__main__":
    raise SystemExit(main())
