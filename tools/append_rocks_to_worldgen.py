"""Append our nine rocks to TFE's world preset and to every vein, by rock category.

Only *additive* changes: nothing TFC or TFE already defines is modified or removed.

Rock categories (from TFCFRock):

  IGNEOUS_INTRUSIVE  red_granite
  IGNEOUS_EXTRUSIVE  porphyry, boninite, carbonatite
  SEDIMENTARY        laterite, mudstone, jaspillite, wackestone
  METAMORPHIC        soapstone

world_preset
  TFE owns data/tfc/worldgen/world_preset/overworld.json (21 rocks, 11 layers).  Its copy is
  read and our rocks are appended as *new* layers, so no existing layer entry is touched:

    tfcflorae_intrusive    red_granite -> bottom
    tfcflorae_extrusive    porphyry/boninite/carbonatite -> tfcflorae_intrusive
    tfcflorae_sedimentary  laterite/mudstone/jaspillite/wackestone -> marble / quartzite
    tfcflorae_metamorphic  soapstone -> bottom

  and those layers are appended to land / volcanic / uplift, leaving ocean_floor alone.

veins
  TFE owns 57 vein files.  For each one we read its existing `blocks` entries, work out which
  TFC rock categories it targets, and append one entry per our rock of the *same* category:

    replace [tfc:rock/raw/boninite]
    with    poor_/normal_/rich_ variants of the same ore on that rock

  Ores are built from the same weights the existing entry uses, so richness distribution is
  identical.  `disc_vein` (sand/kaolin style) only takes sedimentary/hardened rocks and is left
  alone unless it targets our categories.
"""
import json
import os
import zipfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "src", "main", "resources")
TFE_JAR = os.path.join(r"D:\线框的服务器\versions\TFC_Wild_Fire\mods",
                       "Wildfire_TerraFirmaEarth-1.20.1-forge-2.3.0.jar")

OUR_ROCKS = {
    "red_granite": "IGNEOUS_INTRUSIVE",
    "porphyry": "IGNEOUS_EXTRUSIVE",
    "boninite": "IGNEOUS_EXTRUSIVE",
    "carbonatite": "IGNEOUS_EXTRUSIVE",
    "laterite": "SEDIMENTARY",
    "mudstone": "SEDIMENTARY",
    "jaspillite": "SEDIMENTARY",
    "wackestone": "SEDIMENTARY",
    "soapstone": "METAMORPHIC",
}

# TFC's own rock categories, used to match a vein's existing targets.
TFC_CATEGORY = {
    "granite": "IGNEOUS_INTRUSIVE", "diorite": "IGNEOUS_INTRUSIVE", "gabbro": "IGNEOUS_INTRUSIVE",
    "rhyolite": "IGNEOUS_EXTRUSIVE", "basalt": "IGNEOUS_EXTRUSIVE",
    "andesite": "IGNEOUS_EXTRUSIVE", "dacite": "IGNEOUS_EXTRUSIVE",
    "shale": "SEDIMENTARY", "claystone": "SEDIMENTARY", "limestone": "SEDIMENTARY",
    "conglomerate": "SEDIMENTARY", "dolomite": "SEDIMENTARY", "chert": "SEDIMENTARY",
    "chalk": "SEDIMENTARY",
    "quartzite": "METAMORPHIC", "slate": "METAMORPHIC", "phyllite": "METAMORPHIC",
    "schist": "METAMORPHIC", "gneiss": "METAMORPHIC", "marble": "METAMORPHIC",
    "tuff": "IGNEOUS_EXTRUSIVE",
}

tfe = zipfile.ZipFile(TFE_JAR)


def read_json(name):
    return json.loads(tfe.read(name).decode("utf-8"))


def write_json(rel, data):
    p = os.path.join(RES, rel.replace("/", os.sep))
    os.makedirs(os.path.dirname(p), exist_ok=True)
    with open(p, "w", encoding="utf-8", newline="\n") as fh:
        fh.write(json.dumps(data, indent=2) + "\n")


# ---------------------------------------------------------------- world preset
preset_rel = "data/tfc/worldgen/world_preset/overworld.json"
preset = read_json(preset_rel)
settings = preset["dimensions"]["minecraft:overworld"]["generator"]["tfc_settings"]
rls = settings["rock_layer_settings"]

added_rocks = 0
for rock in OUR_ROCKS:
    key = rock
    if key not in rls["rocks"]:
        rls["rocks"][key] = "tfc:" + rock          # RockSettings presets registered by us
        added_rocks += 1

new_layers = [
    {"id": "tfcflorae_intrusive", "layers": {"red_granite": "bottom"}},
    {"id": "tfcflorae_extrusive",
     "layers": {"porphyry": "tfcflorae_intrusive", "boninite": "tfcflorae_intrusive",
                "carbonatite": "tfcflorae_intrusive"}},
    {"id": "tfcflorae_sedimentary",
     "layers": {"laterite": "marble", "mudstone": "marble",
                "jaspillite": "quartzite", "wackestone": "marble"}},
    {"id": "tfcflorae_metamorphic", "layers": {"soapstone": "bottom"}},
]
have = {l["id"] for l in rls["layers"]}
for layer in new_layers:
    if layer["id"] not in have:
        rls["layers"].append(layer)

for top, extra in (("land", ["tfcflorae_extrusive", "tfcflorae_sedimentary", "tfcflorae_metamorphic"]),
                   ("volcanic", ["tfcflorae_extrusive"]),
                   ("uplift", ["tfcflorae_metamorphic", "tfcflorae_sedimentary"])):
    for e in extra:
        if e not in rls[top]:
            rls[top].append(e)

write_json(preset_rel, preset)
print("world_preset: rocks +%d (now %d), layers +%d (now %d)"
      % (added_rocks, len(rls["rocks"]), len(new_layers), len(rls["layers"])))
for top in ("ocean_floor", "volcanic", "land", "uplift"):
    print("   %-12s %s" % (top, rls[top]))

# ---------------------------------------------------------------------- veins
veins = sorted(n for n in tfe.namelist()
               if "configured_feature/vein/" in n and n.endswith(".json"))
n_veins = 0
n_entries = 0
skipped = []
for name in veins:
    data = read_json(name)
    cfg = data.get("config", {})
    blocks = cfg.get("blocks")
    if not isinstance(blocks, list) or not blocks:
        skipped.append((name, "no blocks"))
        continue
    # which categories does this vein already target?
    cats = set()
    for entry in blocks:
        for rep in entry.get("replace", []):
            if not isinstance(rep, str):
                continue
            rock = rep.rsplit("/", 1)[-1]
            cat = TFC_CATEGORY.get(rock)
            if cat:
                cats.add(cat)
    if not cats:
        skipped.append((name, "no known rock targets"))
        continue
    # copy the weight/richness shape from a representative entry of the same vein
    # find an entry whose `with` blocks are rock-specific ores (ns:ore/<ore>/<rock>)
    weights = []
    for entry in blocks:
        cand = []
        for w in entry.get("with", []):
            if not isinstance(w, dict):
                continue
            block = str(w.get("block", ""))
            parts = block.split("/")
            if block.startswith("tfc:ore/") and len(parts) == 3:
                cand.append((w.get("weight"), parts[1]))
        if cand:
            weights = cand
            break
    if not weights:
        skipped.append((name, "no rock-specific ore template"))
        continue
    existing = set()
    for entry in blocks:
        for rep in entry.get("replace", []):
            if isinstance(rep, str):
                existing.add(rep.rsplit("/", 1)[-1])
    added = 0
    for rock, cat in OUR_ROCKS.items():
        if cat not in cats:
            continue
        if rock in existing:
            continue
        # ore ids keep TFC's shape: tfcflorae:ore/<ore>/<rock>
        withs = []
        for weight, prefix in weights:
            withs.append({"weight": weight, "block": "tfcflorae:ore/%s/%s" % (prefix, rock)})
        if not withs:
            continue
        blocks.append({"replace": ["tfcflorae:rock/raw/%s" % rock], "with": withs})
        added += 1
    if added:
        write_json(name, data)
        n_veins += 1
        n_entries += added

print()
print("veins touched: %d, rock entries appended: %d" % (n_veins, n_entries))
print("skipped: %d" % len(skipped))
for s in skipped[:10]:
    print("   ", s[0].split("/")[-1], "-", s[1])
