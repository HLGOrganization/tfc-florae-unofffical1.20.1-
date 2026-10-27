"""Audit data fields against the real Forge registry dump, never against assets.

This script is read-only. It intentionally leaves external namespaces absent from
the minimal probe alone. Tags/worldgen are handled by separate audit passes.
"""
import argparse
import json
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DATA = ROOT / "src/main/resources/data"


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--dump", type=Path, default=ROOT / "reports/runtime-registries.json")
    parser.add_argument("--report", type=Path, default=ROOT / "reports/data-registry-fields.json")
    args = parser.parse_args()
    dump = json.loads(args.dump.read_text(encoding="utf-8"))
    registries = {key: set(value) for key, value in dump["registries"].items()}
    completeness = {
        "tfc_items": any(value.startswith("tfc:") for value in registries.get("minecraft:item", set())),
        "tfcflorae_items": any(value.startswith("tfcflorae:") for value in registries.get("minecraft:item", set())),
        "tfc_blocks": any(value.startswith("tfc:") for value in registries.get("minecraft:block", set())),
        "tfcflorae_blocks": any(value.startswith("tfcflorae:") for value in registries.get("minecraft:block", set())),
        "tfc_recipe_serializers": any(value.startswith("tfc:") for value in registries.get("minecraft:recipe_serializer", set())),
        "tfcflorae_recipe_serializers": any(value.startswith("tfcflorae:") for value in registries.get("minecraft:recipe_serializer", set())),
    }
    checked_namespaces = {"tfc", "tfcflorae", "minecraft", "forge", "patchouli"}
    errors, checked = [], Counter()

    def field(file, path, value, registry):
        if not isinstance(value, str) or value.startswith("#"):
            return
        value_id = value.split("[", 1)[0]
        if ":" not in value_id:
            value_id = "minecraft:" + value_id
        if value_id.split(":", 1)[0] not in checked_namespaces:
            return
        if registry not in registries:
            return
        checked[registry] += 1
        if value_id not in registries[registry]:
            replacement = "tfc:" + value_id.split(":", 1)[1]
            errors.append({"file": file, "path": path, "value": value,
                           "registry": registry,
                           "existing_tfc_equivalent": replacement if value_id.startswith("tfcflorae:") and replacement in registries[registry] else None})

    def item(value, file, path):
        if isinstance(value, list):
            for i, obj in enumerate(value):
                item(obj, file, path + [i])
        elif isinstance(value, dict):
            if "item" in value:
                field(file, path + ["item"], value["item"], "minecraft:item")
            for key in ("ingredient", "ingredients", "stack"):
                if key in value:
                    item(value[key], file, path + [key])
            if "items" in value:
                for i, obj in enumerate(value["items"]):
                    field(file, path + ["items", i], obj, "minecraft:item")

    def block(value, file, path):
        if isinstance(value, str):
            field(file, path, value, "minecraft:block")
        elif isinstance(value, list):
            for i, obj in enumerate(value):
                block(obj, file, path + [i])
        elif isinstance(value, dict):
            for key in ("block", "Name"):
                if key in value:
                    field(file, path + [key], value[key], "minecraft:block")

    def fluid(value, file, path):
        if isinstance(value, str):
            field(file, path, value, "minecraft:fluid")
        elif isinstance(value, list):
            for i, obj in enumerate(value):
                fluid(obj, file, path + [i])
        elif isinstance(value, dict):
            for key in ("fluid", "ingredient"):
                if key in value:
                    fluid(value[key], file, path + [key])

    def predicates(value, file, path):
        if isinstance(value, list):
            for i, obj in enumerate(value):
                predicates(obj, file, path + [i])
        elif isinstance(value, dict):
            item(value, file, path)
            if "block" in value and isinstance(value["block"], str):
                field(file, path + ["block"], value["block"], "minecraft:block")
            for i, obj in enumerate(value.get("blocks", [])):
                field(file, path + ["blocks", i], obj, "minecraft:block")
            for k, obj in value.items():
                if k == "enchantment":
                    field(file, path + [k], obj, "minecraft:enchantment")
                elif k in ("potion", "effect", "type_specific") and isinstance(obj, str):
                    registry = "minecraft:potion" if k == "potion" else "minecraft:mob_effect"
                    if k != "type_specific":
                        field(file, path + [k], obj, registry)
                elif isinstance(obj, (dict, list)):
                    predicates(obj, file, path + [k])

    def recipe(obj, file, path):
        kind = obj.get("type", "")
        field(file, path + ["type"], kind, "minecraft:recipe_serializer")
        if "recipe" in obj:
            recipe(obj["recipe"], file, path + ["recipe"])
        block_recipe = kind in ("tfc:collapse", "tfc:landslide", "tfc:chisel", "tfcflorae:brushing")
        for k in ("ingredient", "ingredients", "mold", "primary_ingredient", "item_ingredient", "input_item", "catalyst"):
            if k in obj:
                (block if block_recipe and k == "ingredient" else item)(obj[k], file, path + [k])
        for k in ("result", "result_item", "output_item", "extra_drop", "extra_products", "on_seal", "on_unseal", "item_output"):
            if k in obj:
                if block_recipe and k == "result":
                    block(obj[k], file, path + [k])
                elif kind == "minecraft:stonecutting" and k == "result":
                    field(file, path + [k], obj[k], "minecraft:item")
                else:
                    item(obj[k], file, path + [k])
        for k in ("fluid", "fluid_ingredient", "input_fluid", "output_fluid", "fluid_output", "result_fluid"):
            if k in obj:
                fluid(obj[k], file, path + [k])
        if kind == "tfc:blast_furnace":
            fluid(obj.get("result"), file, path + ["result"])
        for key, value in obj.get("key", {}).items():
            item(value, file, path + ["key", key])

    def loot(obj, file, path):
        if isinstance(obj, list):
            for i, value in enumerate(obj):
                loot(value, file, path + [i])
        elif isinstance(obj, dict):
            if obj.get("type") in ("minecraft:item", "item"):
                field(file, path + ["name"], obj.get("name"), "minecraft:item")
            if "block" in obj and isinstance(obj["block"], str):
                field(file, path + ["block"], obj["block"], "minecraft:block")
            if "block" in obj and isinstance(obj["block"], dict):
                block(obj["block"], file, path + ["block"])
            if "predicate" in obj:
                predicates(obj["predicate"], file, path + ["predicate"])
            for key, value in obj.items():
                if key != "predicate" and isinstance(value, (dict, list)):
                    loot(value, file, path + [key])

    counts = Counter()
    for p in sorted(DATA.glob("*/*/**/*.json")):
        rel = p.relative_to(DATA)
        category = rel.parts[1]
        if category not in ("tfc", "recipes", "loot_tables", "advancements"):
            continue
        file = rel.as_posix()
        obj = json.loads(p.read_text(encoding="utf-8"))
        counts[category] += 1
        if category == "recipes":
            recipe(obj, file, [])
        elif category == "loot_tables":
            loot(obj, file, [])
        elif category == "advancements":
            predicates(obj, file, [])
        else:
            manager = rel.parts[2]
            if "ingredient" in obj:
                (block if manager in ("panning", "supports") else item)(obj["ingredient"], file, ["ingredient"])
            if manager == "knapping_types":
                item(obj.get("input"), file, ["input"])
                item(obj.get("jei_icon_item"), file, ["jei_icon_item"])
                field(file, ["click_sound"], obj.get("click_sound"), "minecraft:sound_event")
    unique_errors = {json.dumps(e, sort_keys=True): e for e in errors}
    errors = list(unique_errors.values())
    report = {"dump": str(args.dump), "registry_probe_complete_for_tfc": all(completeness.values()),
              "probe_namespace_presence": completeness, "files_by_category": dict(counts),
              "checked_fields_by_registry": dict(checked), "invalid_fields": len(errors),
              "invalid_by_category": dict(Counter(e["file"].split("/")[1] for e in errors)),
              "errors": errors,
              "limits": "Only explicit registry fields in recipes/loot/advancements/TFC managers. External namespaces absent from the minimal probe are excluded. Tag and dynamic-worldgen checks are separate. Invalid fields are not actionable unless registry_probe_complete_for_tfc is true; a failed mod bootstrap yields vanilla-only false positives."}
    args.report.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps({k: v for k, v in report.items() if k not in ("errors", "limits")}, ensure_ascii=False))


if __name__ == "__main__":
    main()
