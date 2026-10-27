"""Validate block/item/fluid/entity/game-event tag members using actual registries.

Never use assets as evidence that a block or item is registered. With --apply,
only replace an absent tfcflorae member with the exact same path in tfc when that
replacement exists in the matching runtime registry. Keep all unresolved entries
for human review; optional entries remain optional. External namespaces not loaded
by the runtime snapshot are reported as unverified, never removed.
"""
import argparse
import hashlib
import json
import shutil
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
REGISTRIES = {
    "blocks": "minecraft:block",
    "items": "minecraft:item",
    "fluids": "minecraft:fluid",
    "entity_types": "minecraft:entity_type",
    "game_events": "minecraft:game_event",
}


def registry_ids(value):
    if isinstance(value, list):
        return set(value)
    if isinstance(value, dict):
        for key in ("entries", "ids", "values"):
            if key in value:
                return registry_ids(value[key])
        return set(value)
    raise TypeError("Unexpected registry snapshot value: " + str(type(value)))


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--dump", type=Path, default=ROOT / "reports/runtime-registries.json")
    parser.add_argument("--apply", action="store_true")
    parser.add_argument("--allow-partial", action="store_true",
                        help="Audit positive matches but classify absent mod IDs as unverified; never edit files")
    parser.add_argument("--trust-registry-dump", action="store_true",
                        help="Use initialized registry IDs even when unrelated datapack parsers failed")
    args = parser.parse_args()
    snapshot = json.loads(args.dump.read_text(encoding="utf-8-sig"))
    snapshot = snapshot.get("registries", snapshot)
    registries = {}
    for registry in REGISTRIES.values():
        value = snapshot.get(registry, snapshot.get(registry.split(":", 1)[1]))
        if value is None:
            raise ValueError("Required registry is absent: " + registry)
        registries[registry] = registry_ids(value)
    loaded_namespaces = set().union(*(
        {ident.split(":", 1)[0] for ident in values}
        for values in registries.values()
    ))
    block_item_namespaces = set().union(*(
        {ident.split(":", 1)[0] for ident in registries[registry]}
        for registry in ("minecraft:block", "minecraft:item")
    ))
    probe_incomplete = False
    probe_path = ROOT / "reports/runtime-probe-fixed.json"
    probe_status = None
    if probe_path.exists():
        probe_status = json.loads(probe_path.read_text(encoding="utf-8-sig"))
        if probe_status.get("mod_load_error") or probe_status.get("worldgen_passed") is False:
            probe_incomplete = True
    partial = args.allow_partial or (probe_incomplete and not args.trust_registry_dump)
    if args.trust_registry_dump and not {"minecraft", "tfc", "tfcflorae"} <= block_item_namespaces:
        raise ValueError("Cannot trust registry dump without initialized Minecraft, TFC and Florae block/item IDs")
    if not partial and not {"minecraft", "tfc", "tfcflorae"} <= block_item_namespaces:
        raise ValueError("Snapshot must include initialized Minecraft, TFC and Florae registries")
    if partial and args.apply:
        raise ValueError("--apply is disabled for a partial runtime snapshot")

    checked = Counter()
    replacements, unresolved, unverified_required, optional_missing, external_unknown = [], [], [], [], []
    changed_files = []
    resources = ROOT / "src/main/resources"
    for path in sorted((resources / "data").glob("*/tags/*/**/*.json")):
        relative = path.relative_to(resources)
        parts = relative.parts
        tag_type = parts[3]
        if tag_type not in REGISTRIES:
            continue
        registry = REGISTRIES[tag_type]
        ids = registries[registry]
        data = json.loads(path.read_text(encoding="utf-8"))
        changed = False
        for field in ("values", "remove"):
            values = data.get(field, [])
            for index, value in enumerate(values):
                ident = value if isinstance(value, str) else value["id"]
                required = field != "remove" and (True if isinstance(value, str) else value.get("required", True))
                if ident.startswith("#"):
                    continue
                ident = ident if ":" in ident else "minecraft:" + ident
                checked[registry] += 1
                if ident in ids:
                    continue
                issue = {"file": relative.as_posix(), "registry": registry,
                         "field": field, "index": index, "id": ident, "required": required}
                namespace = ident.split(":", 1)[0]
                if not required:
                    optional_missing.append(issue)
                    continue
                if partial and namespace != "minecraft":
                    unverified_required.append(issue)
                    continue
                if namespace not in ("minecraft", "tfc", "tfcflorae") and namespace not in loaded_namespaces:
                    external_unknown.append(issue)
                    continue
                replacement = "tfc:" + ident.split(":", 1)[1]
                if namespace == "tfcflorae" and replacement in ids:
                    replacements.append({**issue, "replacement": replacement})
                    if args.apply:
                        if isinstance(value, str):
                            values[index] = replacement
                        else:
                            values[index] = {**value, "id": replacement}
                        changed = True
                else:
                    unresolved.append(issue)
        if changed:
            backup = ROOT / "_backup_datapack_registry" / relative
            backup.parent.mkdir(parents=True, exist_ok=True)
            if not backup.exists():
                shutil.copy2(path, backup)
            original = path.read_bytes()
            path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
            if json.loads(path.read_text(encoding="utf-8")) != data:
                raise ValueError("Written JSON differs: " + str(path))
            changed_files.append({"file": relative.as_posix(), "backup": str(backup),
                                  "before_sha256": hashlib.sha256(original).hexdigest(),
                                  "after_sha256": hashlib.sha256(path.read_bytes()).hexdigest()})
    report = {"snapshot": str(args.dump), "applied": args.apply,
              "snapshot_partial": partial, "trust_registry_dump": args.trust_registry_dump,
              "probe_status": ({"baseline": probe_status.get("baseline"),
                                "data_parsers_passed": probe_status.get("data_parsers_passed"),
                                "worldgen_passed": probe_status.get("worldgen_passed"),
                                "mod_load_error": probe_status.get("mod_load_error"),
                                "error": probe_status.get("error")}
                               if probe_status else None),
              "registry_sizes": {key: len(value) for key, value in registries.items()},
              "checked_members": dict(checked), "replacements": replacements,
              "unresolved_required": unresolved, "unverified_required": unverified_required,
              "optional_missing": optional_missing,
              "external_unverified": external_unknown, "changed_files": changed_files,
              "limits": "Static inspection of real registry IDs. Does not model pack ordering, "
                        "conditional tags or missing nested tag dependencies; use audit_tag_links too."}
    output = ROOT / "reports" / ("runtime-tags-applied.json" if args.apply else "runtime-tags-audit.json")
    output.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps({"registry_sizes": report["registry_sizes"], "checked": dict(checked),
                      "replacements": len(replacements), "unresolved_required": len(unresolved),
                      "unverified_required": len(unverified_required), "snapshot_partial": partial,
                      "optional_missing": len(optional_missing), "external_unverified": len(external_unknown),
                      "changed_files": len(changed_files)}, ensure_ascii=True))
    return int(bool(unresolved or unverified_required or replacements and not args.apply))


if __name__ == "__main__":
    raise SystemExit(main())
