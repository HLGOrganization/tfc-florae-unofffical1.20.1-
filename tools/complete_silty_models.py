"""Complete missing silty-loam models using the existing loam model structure.

Only exact soil path components change. Reuse existing silt composite textures
where the port has no silty-loam texture, consistent with its existing assets.
Never overwrite an existing resource. Use --apply to write the planned files.
"""
import argparse
import json
import re
import zipfile
from pathlib import Path

root = Path(__file__).resolve().parents[1]
resources = root / "src/main/resources"
assets = resources / "assets/tfcflorae"
pattern = re.compile(r"(?<=/)loam(?=/|_|$)")
silty = re.compile(r"(?<=/)silty_loam(?=/|_|$)")


def model_path(ident):
    namespace, path = ident.split(":", 1)
    return resources / f"assets/{namespace}/models/{path}.json"


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--apply", action="store_true")
    args = parser.parse_args()
    references = set()

    def collect(value):
        if isinstance(value, dict):
            if isinstance(value.get("model"), str):
                references.add(value["model"])
            for child in value.values():
                collect(child)
        elif isinstance(value, list):
            for child in value:
                collect(child)

    for path in (assets / "blockstates").rglob("*.json"):
        collect(json.loads(path.read_text(encoding="utf-8")))
    textures = {path.relative_to(resources).as_posix()
                for path in (resources / "assets").rglob("*.png")}
    tfc = next(root.parent.glob("*TerraFirmaCraft*jar"))
    with zipfile.ZipFile(tfc) as archive:
        textures.update(archive.namelist())
    planned = {}
    reused_textures = []
    for ident in sorted(references):
        if not ident.startswith("tfcflorae:") or model_path(ident).exists():
            continue
        if not silty.search(ident):
            raise ValueError("Missing model is outside silty-loam scope: " + ident)
        template_id = silty.sub("loam", ident)
        template = model_path(template_id)
        data = json.loads(template.read_text(encoding="utf-8"))

        def convert(value):
            if isinstance(value, str):
                return pattern.sub("silty_loam", value)
            if isinstance(value, dict):
                return {key: convert(child) for key, child in value.items()}
            if isinstance(value, list):
                return [convert(child) for child in value]
            return value

        data = convert(data)
        for key, texture in data.get("textures", {}).items():
            if texture.startswith("#"):
                continue
            namespace, path = texture.split(":", 1)
            full = f"assets/{namespace}/textures/{path}.png"
            if full not in textures:
                fallback = silty.sub("silt", texture)
                ns, location = fallback.split(":", 1)
                if f"assets/{ns}/textures/{location}.png" not in textures:
                    raise ValueError("No existing texture for " + texture)
                data["textures"][key] = fallback
                reused_textures.append({"model": ident, "texture": fallback})
        planned[ident] = data

    for ident, data in planned.items():
        parent = data.get("parent", "")
        if parent.startswith("tfcflorae:") and not model_path(parent).exists() and parent not in planned:
            raise ValueError("Missing parent: " + parent)
    if args.apply:
        for ident, data in planned.items():
            target = model_path(ident)
            target.parent.mkdir(parents=True, exist_ok=True)
            with target.open("x", encoding="utf-8", newline="\n") as stream:
                stream.write(json.dumps(data, ensure_ascii=False, indent=2) + "\n")
            if json.loads(target.read_text(encoding="utf-8")) != data:
                raise ValueError("Written model differs: " + ident)
    report = {"applied": args.apply, "models": sorted(planned),
              "existing_silt_textures_reused": reused_textures}
    (root / "reports/silty-model-completion.json").write_text(
        json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"Models: {len(planned)}; existing silt textures reused: {len(reused_textures)}; applied: {args.apply}")


if __name__ == "__main__":
    main()
