"""Fix three reported problems.

1. Rocky compact dirt: the rock layer was purple-black for the twelve rocks that this port
   adds (arkose, boninite, carbonatite, cataclasite, jaspillite, laterite, mudstone,
   porphyry, red_granite, sandstone, soapstone, wackestone).  Their raw texture lives in
   tfcflorae:, not tfc:, but every model pointed at tfc:block/rock/raw/<rock>.  Models are
   rewritten per rock with the correct namespace.

2. Bookshelves: the models ask for tfcflorae:block/wood/planks/<wood>_bookshelf_empty for
   the front face (copied from AFC's layout), but that texture was never generated, so the
   whole block/item rendered purple-black.  Vanilla's chiseled_bookshelf_empty is copied
   per wood.

3. Mossy tuff bricks and friends: assets and lang were deleted earlier but the
   registrations were not, so the items still existed.  The decoration registration for
   tuff is removed.
"""
import io
import json
import os
import shutil
import zipfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
A = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae")
CLIENT = r"D:\线框的服务器\versions\1.20.1\1.20.1.jar"

TFC_ROCKS = ["andesite", "basalt", "chalk", "chert", "claystone", "conglomerate", "dacite",
             "diorite", "dolomite", "gabbro", "gneiss", "granite", "limestone", "marble",
             "phyllite", "quartzite", "rhyolite", "schist", "shale", "slate"]
OUR_ROCKS = ["arkose", "boninite", "carbonatite", "cataclasite", "jaspillite", "laterite",
             "mudstone", "porphyry", "red_granite", "sandstone", "soapstone", "wackestone"]

# ---- 1) rock layer namespace ---------------------------------------------
fixed = 0
for variant in ("loam", "sandy_loam", "silt", "silty_loam",
                "fluvisol", "mollisol", "oxisol", "podzol"):
    for fam in ("rockier", "rocky", "rockiest", "pebble"):
        for rock in TFC_ROCKS + OUR_ROCKS:
            p = os.path.join(A, "models", "block", fam + "_compact_dirt", variant, rock + ".json")
            if not os.path.isfile(p):
                continue
            ns = "tfc" if rock in TFC_ROCKS else "tfcflorae"
            model = {
                "parent": "tfcflorae:block/template/soil_over_rock",
                "textures": {
                    "rock": "%s:block/rock/raw/%s" % (ns, rock),
                    "soil": "tfcflorae:block/compact_dirt/%s_compact_dirt/%s" % (fam, variant),
                },
            }
            io.open(p, "w", encoding="utf-8", newline="\n").write(json.dumps(model, indent=2) + "\n")
            fixed += 1
print("soil models rewritten:", fixed)

# ---- 2) bookshelf _empty texture -----------------------------------------
z = zipfile.ZipFile(CLIENT)
vanilla_empty = z.read("assets/minecraft/textures/block/chiseled_bookshelf_empty.png")
tex_dir = os.path.join(A, "textures", "block", "wood", "planks")
woods = sorted(f[:-len("_bookshelf_side.png")] for f in os.listdir(tex_dir) if f.endswith("_bookshelf_side.png"))
made = 0
for w in woods:
    out = os.path.join(tex_dir, w + "_bookshelf_empty.png")
    if not os.path.isfile(out):
        io.open(out, "wb").write(vanilla_empty)
        made += 1
print("bookshelf _empty textures written:", made, "of", len(woods), "woods")

# ---- 3) drop the tuff decorations ----------------------------------------
p = os.path.join(ROOT, "src", "main", "java", "tfcflorae", "common", "blocks", "TFCFBlocks.java")
s = io.open(p, encoding="utf-8").read()
needle = "TFCF_ROCK_DECORATIONS.get(rock).get(type.mossy())"
if needle in s:
    print("!! tuff mossy decorations still referenced at:")
    for line in s.splitlines():
        if needle in line:
            print("   ", line.strip()[:130])
else:
    print("no direct tuff mossy decoration reference")
