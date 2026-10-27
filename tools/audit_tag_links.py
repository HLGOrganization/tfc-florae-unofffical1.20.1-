"""Check 1.20.1 tag links by registry, including worldgen's nested paths.

Read-only with respect to resources. Writes an evidence report under reports.
External packs are indexed for definitions, not declared to be runtime-validated.
The installed Florae jar is excluded so it cannot hide missing source files.
"""
import argparse
import json
import re
import zipfile
from collections import defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
REGISTRIES = (
    "worldgen/configured_carver", "worldgen/configured_feature",
    "worldgen/placed_feature", "worldgen/structure_set", "worldgen/structure",
    "worldgen/biome", "worldgen/noise_settings", "worldgen/noise",
    "worldgen/density_function", "worldgen/processor_list", "worldgen/template_pool",
    "worldgen/flat_level_generator_preset", "worldgen/world_preset", "worldgen/multi_noise_biome_source_parameter_list",
    "blocks", "items", "fluids", "entity_types", "game_events", "damage_type",
    "banner_pattern", "cat_variant", "instrument", "painting_variant", "point_of_interest_type",
)


def tag_key(name, strict=False):
    match = re.fullmatch(r"data/([^/]+)/tags/(.+)\.json", name)
    if match:
        namespace, rest = match.groups()
        for registry in REGISTRIES:
            if rest.startswith(registry + "/"):
                return registry, namespace + ":" + rest[len(registry) + 1:]
        if strict:
            raise ValueError("Unknown source tag registry: " + name)
    return None


def entry(value):
    ident = value if isinstance(value, str) else value["id"]
    required = True if isinstance(value, str) else value.get("required", True)
    return ident, required


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--mods", type=Path, required=True)
    args = parser.parse_args()
    definitions = defaultdict(list)
    source = {}
    resource_names = set()
    skipped = []
    jars = [ROOT / "mc-cache/client-1.20.1.jar"] + sorted(args.mods.glob("*.jar"))
    for jar in jars:
        with zipfile.ZipFile(jar) as archive:
            names = archive.namelist()
            if "META-INF/mods.toml" in names and re.search(
                r'modId\s*=\s*[\"\x27]tfcflorae[\"\x27]',
                archive.read("META-INF/mods.toml").decode("utf-8-sig"),
            ):
                skipped.append(jar.name)
                continue
            for name in names:
                resource_names.add(name)
                key = tag_key(name)
                if key:
                    definitions[key].append((jar.name, name, json.loads(archive.read(name))))

    resources = ROOT / "src/main/resources"
    for path in sorted((resources / "data").rglob("*.json")):
        name = path.relative_to(resources).as_posix()
        resource_names.add(name)
        key = tag_key(name, strict=True)
        if key:
            data = json.loads(path.read_text(encoding="utf-8"))
            source[key] = (name, data)
            definitions[key].append(("source", name, data))

    missing = []
    optional = []
    absent_features = []
    edges = defaultdict(set)
    checked = 0
    for key, (name, data) in source.items():
        for value in data.get("values", []):
            ident, required = entry(value)
            if ident.startswith("#"):
                checked += 1
                target = key[0], ident[1:]
                if target not in definitions:
                    (missing if required else optional).append({
                        "file": name, "registry": key[0], "target": ident,
                    })
            elif key[0].startswith("worldgen/") and ident.startswith(("tfc:", "tfcflorae:")):
                namespace, location = ident.split(":", 1)
                target = f"data/{namespace}/{key[0]}/{location}.json"
                if required and target not in resource_names:
                    absent_features.append({"file": name, "target": ident, "registry": key[0]})

    # Index dependency edges too, so cycles through another pack are visible.
    # Pack ordering and replace/remove directives are reported separately below.
    directives = []
    for key, packs in definitions.items():
        for pack, name, data in packs:
            if key in source and (data.get("replace") or data.get("remove")):
                directives.append({"pack": pack, "file": name,
                                   "replace": data.get("replace", False),
                                   "remove": data.get("remove", [])})
            for value in data.get("values", []):
                ident, _ = entry(value)
                if ident.startswith("#"):
                    target = key[0], ident[1:]
                    if target in definitions:
                        edges[key].add(target)

    active, finished, stack, cycles = set(), set(), [], []

    def visit(key):
        if key in active:
            cycle = stack[stack.index(key):] + [key]
            cycles.append([f"{reg}|{ident}" for reg, ident in cycle])
            return
        if key in finished:
            return
        active.add(key)
        stack.append(key)
        for target in sorted(edges[key]):
            visit(target)
        stack.pop()
        active.remove(key)
        finished.add(key)

    for key in sorted(source):
        visit(key)

    pending = ["tfc:feature/" + name for name in (
        "berry_bushes", "boulders", "crops", "forest_plants", "icebergs", "land_plants",
        "ocean_plants", "ore_deposits", "shore_decorations", "soil_discs", "volcanoes",
    )] + ["tfc:is_lake", "tfc:is_river", "tfc:planks", "tfcflorae:feature/steppe_plants"]
    report = {
        "source_tags": len(source), "source_tag_links": checked,
        "dependency_jars": len(jars) - len(skipped), "excluded_florae_jars": skipped,
        "missing_required": missing, "missing_optional": optional,
        "missing_worldgen_resources": absent_features, "reachable_cycles": cycles,
        "pack_directives_requiring_runtime_order": directives,
        "handoff_15": {ident: [
            {"registry": key[0], "files": [f"{pack}: {name}" for pack, name, _ in packs]}
            for key, packs in definitions.items() if key[1] == ident
        ] for ident in pending},
        "limits": "Checks tag links and TFC/Florae worldgen resource existence. Does not "
                  "verify runtime block/item registries, code-registered biomes, conditions, "
                  "unknown external registry types, pack order or gameplay. Dependency directives "
                  "are warnings: this tool cannot infer their runtime ordering.",
    }
    output = ROOT / "reports/tag-links.json"
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"Source tags: {len(source)}; tag links: {checked}; dependency jars: {len(jars) - len(skipped)}")
    for label, issues in (("MISSING REQUIRED", missing), ("MISSING WORLDGEN", absent_features),
                          ("CYCLES", cycles), ("PACK DIRECTIVES", directives)):
        print(f"{label}: {len(issues)}")
        for issue in issues[:30]:
            print(json.dumps(issue, ensure_ascii=True))
    print("Report:", output)
    return int(bool(missing or absent_features or cycles))


if __name__ == "__main__":
    raise SystemExit(main())
