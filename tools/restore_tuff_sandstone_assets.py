"""Restore the assets for these still-registered blocks.

  tfcflorae:rock/{column,polished_column,smoke_block,magma}/tuff
  tfcflorae:sandstone/layered/{black,brown,green,pink,red,white,yellow}
  tfcflorae:sandstone/layered/{slab,stairs,wall}/<same 7 colours>

The blocks themselves were never removed - TFC_COLUMN / TFC_POLISHED_COLUMN / TFC_SMOKE_BLOCK
and the SANDSTONE map still register them, and smoke_block/magma are TFC rocks covered by
TFC_COLUMN's Rock iteration.  Only their resources were deleted earlier, which is why the
models were missing in-game.

Everything is copied verbatim from the 1.18.2 pack, preserving the exact directory layout
that the blockstates/models reference:

  blockstates/rock/<family>/tuff.json                 blockstates/sandstone/layered/**
  models/block/rock/<family>/tuff.json                models/block/sandstone/layered/**
  models/item/rock/<family>/tuff.json                 models/item/sandstone/layered/**
  textures/block/rock/<family>/tuff_*.png             textures/block/sandstone/layered/*.png

Note smoke_block has no block model even in the original: its blockstate points at
minecraft's `tfc:block/rock/raw/conglomerate`, which TFC ships, so nothing is missing.
"""
import json
import os
import shutil

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DST = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae")
SRC = os.path.join(r"D:\学习资料\mod解包\TFCFlorae-1.20.1-2.2.0-mc1.20.1", "assets", "tfcflorae")

TUFF_FAMILIES = ["column", "polished_column", "smoke_block", "magma"]
COLORS = ["black", "brown", "green", "pink", "red", "white", "yellow"]
SHAPES = ["slab", "stairs", "wall"]

copied = 0
missing = []


def copy(src, dst):
    global copied
    os.makedirs(os.path.dirname(dst), exist_ok=True)
    shutil.copy2(src, dst)
    copied += 1


# ---- tuff families --------------------------------------------------------
for fam in TUFF_FAMILIES:
    for sub, name in (("blockstates/rock", "tuff.json"),
                      ("models/block/rock", "tuff.json"),
                      ("models/item/rock", "tuff.json")):
        s = os.path.join(SRC, sub.replace("/", os.sep), fam, name)
        if os.path.isfile(s):
            copy(s, os.path.join(DST, sub.replace("/", os.sep), fam, name))
        else:
            missing.append(sub + "/" + fam + "/" + name)
    # textures: column/polished_column use <rock>_side/_top, magma uses <rock>
    tex_dir = os.path.join(SRC, "textures", "block", "rock", fam)
    if os.path.isdir(tex_dir):
        for fn in os.listdir(tex_dir):
            if fn.startswith("tuff"):
                copy(os.path.join(tex_dir, fn),
                     os.path.join(DST, "textures", "block", "rock", fam, fn))

# ---- sandstone/layered ----------------------------------------------------
for color in COLORS:
    for base, suffix in (("blockstates/sandstone/layered", color + ".json"),
                         ("models/block/sandstone/layered", color + ".json"),
                         ("models/item/sandstone/layered", color + ".json"),
                         ("textures/block/sandstone/layered", color + ".png")):
        s = os.path.join(SRC, base.replace("/", os.sep), suffix)
        if os.path.isfile(s):
            copy(s, os.path.join(DST, base.replace("/", os.sep), suffix))
        else:
            missing.append(base + "/" + suffix)
    for shape in SHAPES:
        for base, ext in (("blockstates/sandstone/layered", ".json"),
                          ("models/item/sandstone/layered", ".json")):
            fn = shape + "/" + color + ext
            s = os.path.join(SRC, base.replace("/", os.sep), fn.replace("/", os.sep))
            if os.path.isfile(s):
                copy(s, os.path.join(DST, base.replace("/", os.sep), fn.replace("/", os.sep)))
            else:
                missing.append(base + "/" + fn)

# whole model sub-trees the slab/stair/wall models parent into
for base in ("models/block/sandstone/layered/slab",
             "models/block/sandstone/layered/stairs",
             "models/block/sandstone/layered/wall"):
    src_dir = os.path.join(SRC, base.replace("/", os.sep))
    dst_dir = os.path.join(DST, base.replace("/", os.sep))
    if not os.path.isdir(src_dir):
        missing.append(base)
        continue
    for dirpath, _d, files in os.walk(src_dir):
        for fn in files:
            if not fn.endswith(".json"):
                continue
            rel = os.path.relpath(os.path.join(dirpath, fn), src_dir)
            copy(os.path.join(dirpath, fn), os.path.join(dst_dir, rel))

print("copied:", copied)
print("missing in source:", len(missing))
for m in missing[:12]:
    print("   ", m)
