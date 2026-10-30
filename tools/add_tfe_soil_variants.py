"""Add TFE's four soil variants (fluvisol / mollisol / oxisol / podzol) to the rocky
compact dirt family.

TFCFVariant now carries the four new names, so TFCFRockSoilMapper registers
rockier_compact_dirt/<variant>/<rock> for every rock automatically.  This script produces
the matching assets and data:

  textures   textures/block/compact_dirt/<family>/<variant>.png      (8 variants x 4 families)
             textures/block/compact_dirt/<family>/<variant>_top.png  (when redrawn)
  models     models/block/<family>/<variant>/<rock>.json
  blockstates blockstates/<family>/<variant>/<rock>.json
  loot       loot_tables/blocks/<family>/<variant>/<rock>.json
  recipes    crafting / chisel slab+stairs / landslide, same shapes as the existing ones

Textures come from the redrawn set supplied at
D:\\学习资料\\mod解包\\TFCFlorae-1.20.1-2.2.0-mc1.20.1\\assets\\tfcflorae\\textures\\block\\<family>_compact_dirt
"""
import io
import json
import os
import shutil

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
A = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae")
DATA = os.path.join(ROOT, "src", "main", "resources", "data", "tfcflorae")
REDRAWN = r"D:\学习资料\mod解包\TFCFlorae-1.20.1-2.2.0-mc1.20.1\assets\tfcflorae\textures\block"

FAMILIES = ["rockier", "rocky", "rockiest", "pebble"]
NEW_VARIANTS = ["fluvisol", "mollisol", "oxisol", "podzol"]
ROCKS = ["andesite", "arkose", "basalt", "boninite", "carbonatite", "cataclasite",
         "chalk", "chert", "claystone", "conglomerate", "dacite", "diorite", "dolomite",
         "gabbro", "gneiss", "granite", "jaspillite", "laterite", "limestone", "marble",
         "mudstone", "phyllite", "porphyry", "quartzite", "red_granite", "rhyolite",
         "sandstone", "schist", "shale", "slate", "soapstone", "wackestone"]


# ---------------------------------------------------------------- textures
tex_dst_base = os.path.join(A, "textures", "block", "compact_dirt")
copied = 0
for fam in FAMILIES:
    src_dir = os.path.join(REDRAWN, fam + "_compact_dirt")
    dst_dir = os.path.join(tex_dst_base, fam + "_compact_dirt")
    os.makedirs(dst_dir, exist_ok=True)
    for variant in ["loam", "sandy_loam", "silt", "silty_loam"] + NEW_VARIANTS:
        for suffix in ("", "_top"):
            src = os.path.join(src_dir, variant + suffix + ".png")
            if os.path.isfile(src):
                shutil.copy2(src, os.path.join(dst_dir, variant + suffix + ".png"))
                copied += 1
print("textures copied:", copied)

# ---------------------------------------------------------------- models
models = 0
for fam in FAMILIES:
    for variant in NEW_VARIANTS:
        for rock in ROCKS:
            out = os.path.join(A, "models", "block", fam + "_compact_dirt", variant, rock + ".json")
            os.makedirs(os.path.dirname(out), exist_ok=True)
            soil = "tfcflorae:block/compact_dirt/%s_compact_dirt/%s" % (fam, variant)
            model = {
                "parent": "tfcflorae:block/template/soil_over_rock",
                "textures": {
                    "rock": "tfc:block/rock/raw/" + rock,
                    "soil": soil,
                },
            }
            io.open(out, "w", encoding="utf-8", newline="\n").write(json.dumps(model, indent=2) + "\n")
            models += 1
print("models written:", models)

# ---------------------------------------------------------------- blockstates
states = 0
for variant in NEW_VARIANTS:
    for rock in ROCKS:
        out = os.path.join(A, "blockstates", "rockier_compact_dirt", variant, rock + ".json")
        os.makedirs(os.path.dirname(out), exist_ok=True)
        variants = [
            "tfcflorae:block/%s_compact_dirt/%s/%s" % (fam, variant, rock) for fam in FAMILIES
        ]
        state = {
            "__comment__": "This file was automatically created by mcresources",
            "variants": {"": [{"model": m} for m in variants]},
        }
        io.open(out, "w", encoding="utf-8", newline="\n").write(json.dumps(state, indent=2) + "\n")
        states += 1
print("blockstates written:", states)

# ---------------------------------------------------------------- loot
loot = 0
for variant in NEW_VARIANTS:
    for rock in ROCKS:
        out = os.path.join(DATA, "loot_tables", "blocks", "rockier_compact_dirt", variant, rock + ".json")
        os.makedirs(os.path.dirname(out), exist_ok=True)
        table = {
            "type": "minecraft:block",
            "pools": [{
                "rolls": 1,
                "entries": [{"type": "minecraft:item", "name": "tfcflorae:rockier_compact_dirt/%s/%s" % (variant, rock)}],
                "conditions": [{"condition": "minecraft:survives_explosion"}],
            }],
        }
        io.open(out, "w", encoding="utf-8", newline="\n").write(json.dumps(table, indent=2) + "\n")
        loot += 1
print("loot tables written:", loot)

# ---------------------------------------------------------------- recipes
# copy the shape of an existing loam recipe, retargeted at the new variant
recs = 0
def clone_recipe(src_rel, dst_rel, variant):
    global recs
    src = os.path.join(DATA, "recipes", src_rel.replace("/", os.sep))
    if not os.path.isfile(src):
        return
    text = io.open(src, encoding="utf-8-sig").read()
    text = text.replace("/loam/", "/%s/" % variant).replace("/loam\"", "/%s\"" % variant)
    dst = os.path.join(DATA, "recipes", dst_rel.replace("/", os.sep))
    os.makedirs(os.path.dirname(dst), exist_ok=True)
    io.open(dst, "w", encoding="utf-8", newline="\n").write(text)
    recs += 1

for variant in NEW_VARIANTS:
    for rock in ROCKS:
        clone_recipe("crafting/soil/rockier_compact_dirt/loam/%s.json" % rock,
                     "crafting/soil/rockier_compact_dirt/%s/%s.json" % (variant, rock), variant)
        clone_recipe("chisel/slab/soil/rockier_compact_dirt/loam/%s.json" % rock,
                     "chisel/slab/soil/rockier_compact_dirt/%s/%s.json" % (variant, rock), variant)
        clone_recipe("chisel/stair/soil/rockier_compact_dirt/loam/%s.json" % rock,
                     "chisel/stair/soil/rockier_compact_dirt/%s/%s.json" % (variant, rock), variant)
        clone_recipe("landslide/soil/rockier_compact_dirt/loam/%s.json" % rock,
                     "landslide/soil/rockier_compact_dirt/%s/%s.json" % (variant, rock), variant)
print("recipes written:", recs)
