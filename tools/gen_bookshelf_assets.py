"""Generate 1.20 chiseled bookshelf assets by cloning AFC's structure.

TFC's BookshelfBlock extends vanilla ChiseledBookShelfBlock, so each wood shelf needs the
full vanilla model set (facing/slot states).  AFC already ships exactly that layout, so
its files are used as templates: paths and ids are rewritten from afc:<wood> to
tfcflorae:<wood>.

Textures:
  <wood>_bookshelf_side      <- 1.18.2 textures/block/wood/bookshelf/<wood>.png
  <wood>_bookshelf_stage0..6 <- 1.18.2 textures/block/wood/chiseled_bookshelf/<i>/<wood>.png
  <wood>_bookshelf_top       <- our own plank texture
"""
import io
import json
import os
import re
import shutil
import zipfile

AFC = r"D:\线框的服务器\versions\TFC_Wild_Fire\mods\[林海物语] afc-1.0.23-1.20.1.jar"
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
A = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae")
SRC118 = os.path.join(os.path.dirname(ROOT), "tfc_florae", "jar", "assets", "tfcflorae")

woods = sorted(f[:-5] for f in os.listdir(os.path.join(A, "blockstates", "wood", "planks"))
               if f.endswith(".json"))
print("woods:", len(woods))

z = zipfile.ZipFile(AFC)
# collect afc templates for one reference wood (baobab)
tmpl_models = {}
for n in z.namelist():
    if "models/block/wood/planks/" in n and "baobab_bookshelf" in n:
        tmpl_models[n.split("/")[-1]] = z.read(n).decode()
tmpl_state = z.read("assets/afc/blockstates/wood/planks/baobab_bookshelf.json").decode()
tmpl_item = z.read("assets/afc/models/item/wood/planks/baobab_bookshelf.json").decode()
print("template models:", len(tmpl_models))

def rewrite(text, wood):
    text = text.replace("afc:", "tfcflorae:")
    text = text.replace("baobab_bookshelf", wood + "_bookshelf")
    text = text.replace("baobab", wood)
    return text

# ---- models ---------------------------------------------------------------
models_out = os.path.join(A, "models", "block", "wood", "planks")
os.makedirs(models_out, exist_ok=True)
made = 0
for wood in woods:
    for name, body in tmpl_models.items():
        out = os.path.join(models_out, rewrite(name, wood))
        io.open(out, "w", encoding="utf-8", newline="\n").write(rewrite(body, wood))
        made += 1
print("models written:", made)

# ---- blockstates ----------------------------------------------------------
state_out = os.path.join(A, "blockstates", "wood", "planks")
os.makedirs(state_out, exist_ok=True)
made = 0
for wood in woods:
    out = os.path.join(state_out, wood + "_bookshelf.json")
    io.open(out, "w", encoding="utf-8", newline="\n").write(rewrite(tmpl_state, wood))
    made += 1
print("blockstates written:", made)

# ---- item models ----------------------------------------------------------
item_out = os.path.join(A, "models", "item", "wood", "planks")
os.makedirs(item_out, exist_ok=True)
made = 0
for wood in woods:
    out = os.path.join(item_out, wood + "_bookshelf.json")
    io.open(out, "w", encoding="utf-8", newline="\n").write(rewrite(tmpl_item, wood))
    made += 1
print("item models written:", made)

# ---- textures -------------------------------------------------------------
tex_out = os.path.join(A, "textures", "block", "wood", "planks")
os.makedirs(tex_out, exist_ok=True)
made = 0
for wood in woods:
    # side = 1.18 bookshelf texture (contains the books)
    side = os.path.join(SRC118, "textures", "block", "wood", "bookshelf", wood + ".png")
    if os.path.isfile(side):
        shutil.copy2(side, os.path.join(tex_out, wood + "_bookshelf_side.png"))
        made += 1
    # stage0..6 = 1.18 chiseled bookshelf textures
    for i in range(7):
        stage = os.path.join(SRC118, "textures", "block", "wood", "chiseled_bookshelf", str(i), wood + ".png")
        if os.path.isfile(stage):
            shutil.copy2(stage, os.path.join(tex_out, "%s_bookshelf_stage%d.png" % (wood, i)))
            made += 1
    # top = our plank texture
    plank = os.path.join(A, "textures", "block", "wood", "planks", wood + ".png")
    if os.path.isfile(plank):
        shutil.copy2(plank, os.path.join(tex_out, wood + "_bookshelf_top.png"))
        made += 1
print("textures written:", made)
