"""Migrate vein references using the actual rock/ore ownership in 1.20.1.

Asset files are deliberately not treated as registry evidence. The ownership
matrix follows TFCBlocks ORES, Florae ORES/GRADED_ORES and ORES_TFC_ROCK.
The retired 1.18 kaolinite-ore vein is archived; TFC's kaolin_disc stays enabled.
"""
import json
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "src/main/resources"
NATIVE_ROCKS = {
    "granite", "diorite", "gabbro", "shale", "claystone", "limestone", "conglomerate",
    "dolomite", "chert", "chalk", "rhyolite", "basalt", "andesite", "dacite",
    "quartzite", "slate", "phyllite", "schist", "gneiss", "marble",
}
CUSTOM_ROCKS = {
    "red_granite", "porphyry", "boninite", "carbonatite", "laterite", "mudstone",
    "sandstone", "arkose", "jaspillite", "wackestone", "cataclasite", "soapstone",
}
backup = ROOT / "_backup_datapack_registry"
changes = []


def write(path, data):
    target = backup / path.relative_to(RES)
    if not target.exists():
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(path, target)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


for path in sorted((RES / "data/tfcflorae/worldgen/configured_feature/vein").glob("*.json")):
    if path.stem == "kaolinite":
        continue
    data = json.loads(path.read_text(encoding="utf-8"))
    edits = []
    for group in data["config"]["blocks"]:
        for index, ident in enumerate(group["replace"]):
            namespace, location = ident.split(":", 1)
            rock = location.rsplit("/", 1)[1]
            if rock not in NATIVE_ROCKS | CUSTOM_ROCKS:
                raise ValueError("Unrecognized rock: " + rock)
            if namespace == "tfcflorae" and rock in NATIVE_ROCKS:
                updated = "tfc:" + location
                edits.append([ident, updated])
                group["replace"][index] = updated
        for output in group["with"]:
            ident = output["block"]
            namespace, location = ident.split(":", 1)
            category, ore, rock = location.split("/")
            if category != "ore":
                raise ValueError("Unrecognized vein output: " + ident)
            if namespace == "tfcflorae" and rock in NATIVE_ROCKS and ore != "anthracite":
                updated = "tfc:" + location
                edits.append([ident, updated])
                output["block"] = updated
    if edits:
        write(path, data)
        changes.append({"file": path.relative_to(RES).as_posix(), "references": edits})

retired = []
for category in ("configured_feature", "placed_feature"):
    path = RES / f"data/tfcflorae/worldgen/{category}/vein/kaolinite.json"
    if path.exists():
        archived = backup / path.relative_to(RES)
        archived.parent.mkdir(parents=True, exist_ok=True)
        if archived.exists():
            raise FileExistsError("Refusing to overwrite archive: " + str(archived))
        path.rename(archived)
        retired.append(path.relative_to(RES).as_posix())

tag = RES / "data/tfc/tags/worldgen/placed_feature/in_biome/veins.json"
data = json.loads(tag.read_text(encoding="utf-8"))
if "tfc:vein/kaolin_disc" not in data["values"]:
    raise ValueError("TFC native kaolin generation must be retained")
if "tfcflorae:vein/kaolinite" in data["values"]:
    data["values"].remove("tfcflorae:vein/kaolinite")
    write(tag, data)

report = {"changed_veins": len(changes), "changed_references": sum(len(x["references"]) for x in changes),
          "changes": changes, "archived_retired_118_features": retired,
          "kaolin_generation": "tfc:vein/kaolin_disc retained; no new blocks or items registered"}
(ROOT / "reports/vein-registry-fix.json").write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
print(f"Changed veins: {len(changes)}; references: {report['changed_references']}; archived retired features: {len(retired)}")
