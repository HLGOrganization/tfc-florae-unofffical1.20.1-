"""Delete recipes / loot / tags that reference content removed on purpose.

kubejs server.log reported 496 failing tfcflorae recipes.  Grouped by cause:

  crafting/soil/dirty*_stone_tiles/*      192  blocks skipped in TFCFRockSoilMapper
  crafting/wood/bookshelf/*                65  fixed by adding the BlockItem (kept)
  chisel/smooth/rock/flagstone_bricks/*    32  flagstone blocks were deleted
  sandstone/layered/*                      45  layered sandstone blocks were deleted
  landslide / collapse / rock tiles        43  blocks skipped via REMOVED_SOILS / removed rocks

Everything below removes the data files whose target block is not registered, so the
datapack stops emitting "Unknown block" / "ItemStack result can't be empty".
"""
import glob
import io
import json
import os
import shutil

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATA = os.path.join(ROOT, "src", "main", "resources", "data")

# path fragments that are gone from the registry
DEAD = [
    "stone_tiles",          # dirty / dirtier / dirtiest stone tiles
    "flagstone",            # flagstone + flagstone_bricks (blocks deleted earlier)
    "sandstone/layered",    # layered sandstone colours (deleted)
    "coarse_dirt",          # REMOVED_SOILS
    "packed_mud",           # REMOVED_SOILS
    "mossy_packed_mud",     # REMOVED_SOILS
    "loose_mud",            # REMOVED_SOILS
    "dry_clay_grass", "dense_clay_grass", "sparse_clay_grass",
    "dry_earthenware_clay_grass", "dense_earthenware_clay_grass", "sparse_earthenware_clay_grass",
    "dry_stoneware_clay_grass", "dense_stoneware_clay_grass", "sparse_stoneware_clay_grass",
    "dry_grass", "dense_grass", "sparse_grass",
    "grass_path",
]

removed = {"recipes": 0, "loot_tables": 0, "tags": 0}
for sub in ("recipes", "loot_tables", "tags"):
    base = os.path.join(DATA, "tfcflorae", sub)
    if not os.path.isdir(base):
        continue
    for path in glob.glob(os.path.join(base, "**", "*.json"), recursive=True):
        rel = os.path.relpath(path, base).replace(os.sep, "/")
        # never touch wood/bookshelf recipes - those are now valid
        if "wood/bookshelf" in rel or "wood/planks" in rel:
            continue
        if any(frag in rel for frag in DEAD):
            os.remove(path)
            removed[sub] += 1

print("removed:", removed)

# prune now-empty directories
pruned = 0
for _ in range(8):
    for sub in ("recipes", "loot_tables", "tags"):
        base = os.path.join(DATA, "tfcflorae", sub)
        if not os.path.isdir(base):
            continue
        for dirpath, dirnames, filenames in os.walk(base, topdown=False):
            if not dirnames and not filenames:
                os.rmdir(dirpath)
                pruned += 1
print("empty dirs pruned:", pruned)

# report what still references the removed blocks
import re
leftovers = []
for path in glob.glob(os.path.join(DATA, "**", "*.json"), recursive=True):
    try:
        text = io.open(path, encoding="utf-8", errors="ignore").read()
    except Exception:
        continue
    for frag in ("flagstone", "stone_tiles", "sandstone/layered"):
        if frag in text:
            leftovers.append(os.path.relpath(path, DATA).replace(os.sep, "/"))
            break
print("files still mentioning removed block families:", len(leftovers))
for x in leftovers[:10]:
    print("   ", x)
