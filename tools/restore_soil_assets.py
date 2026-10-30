"""Restore the blockstate/model assets of soil blocks that are registered but have none.

25 TFCFSoil values had their blockstate folders (and in some cases models) deleted by the
earlier orphan sweep.  Their blocks are still registered, so anything that places them -
including the cavern features just ported - would show untextured blocks.

Assets are copied 1:1 from the 1.18.2 jar.  rocky/rockiest/pebble compact dirt are skipped
because those variants were merged into rockier.
"""
import io
import json
import os
import shutil

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
A = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae")
SRC = os.path.join(os.path.dirname(ROOT), "tfc_florae", "jar", "assets", "tfcflorae")

SKIP = ("rocky_compact_dirt", "rockiest_compact_dirt", "pebble_compact_dirt", "kaolinite")

# every soil folder that 1.18.2 ships
src_bs = os.path.join(SRC, "blockstates")
candidates = sorted(d for d in os.listdir(src_bs) if os.path.isdir(os.path.join(src_bs, d)))

copied_bs = copied_mb = 0
for name in candidates:
    if any(s in name for s in SKIP):
        continue
    # only soil-ish folders
    if not any(k in name for k in ("grass", "dirt", "mud", "clay", "podzol", "farmland", "bricks")):
        continue
    dst_bs = os.path.join(A, "blockstates", name)
    src_dir = os.path.join(src_bs, name)
    if not os.path.isdir(dst_bs):
        shutil.copytree(src_dir, dst_bs)
        n = sum(len(fs) for _, _, fs in os.walk(src_dir))
        copied_bs += n
        print("blockstates/%-30s +%d" % (name, n))
    # models
    src_mb = os.path.join(SRC, "models", "block", name)
    dst_mb = os.path.join(A, "models", "block", name)
    if os.path.isdir(src_mb) and not os.path.isdir(dst_mb):
        shutil.copytree(src_mb, dst_mb)
        n = sum(len(fs) for _, _, fs in os.walk(src_mb))
        copied_mb += n
        print("models/block/%-28s +%d" % (name, n))

# item models for the same blocks
src_item = os.path.join(SRC, "models", "item")
dst_item = os.path.join(A, "models", "item")
copied_item = 0
for name in candidates:
    if any(s in name for s in SKIP):
        continue
    if not any(k in name for k in ("grass", "dirt", "mud", "clay", "podzol", "farmland", "bricks")):
        continue
    s = os.path.join(src_item, name)
    d = os.path.join(dst_item, name)
    if os.path.isdir(s) and not os.path.isdir(d):
        shutil.copytree(s, d)
        copied_item += sum(len(fs) for _, _, fs in os.walk(s))

print()
print("blockstate files:", copied_bs)
print("block model files:", copied_mb)
print("item model files:", copied_item)

# ---- verify every soil now has a blockstate folder ----------------------
soils = ["GRASS_PATH", "FARMLAND", "ROOTED_DIRT", "DIRT", "GRASS", "CLAY", "CLAY_GRASS",
         "PACKED_MUD", "MOSSY_PACKED_MUD", "LOOSE_MUD", "MYCELIUM_DIRT", "MUD_BRICKS",
         "DRYING_BRICKS", "COARSE_DIRT", "PODZOL", "DRY_GRASS_PATH", "DRY_GRASS",
         "DENSE_GRASS_PATH", "DENSE_GRASS", "SPARSE_GRASS_PATH", "SPARSE_GRASS",
         "DRY_CLAY_GRASS", "DENSE_CLAY_GRASS", "SPARSE_CLAY_GRASS", "EARTHENWARE_CLAY",
         "EARTHENWARE_CLAY_GRASS", "DRY_EARTHENWARE_CLAY_GRASS", "DENSE_EARTHENWARE_CLAY_GRASS",
         "SPARSE_EARTHENWARE_CLAY_GRASS", "STONEWARE_CLAY", "STONEWARE_CLAY_GRASS",
         "DRY_STONEWARE_CLAY_GRASS", "DENSE_STONEWARE_CLAY_GRASS", "SPARSE_STONEWARE_CLAY_GRASS",
         "COMPACT_DIRT"]
missing = [s for s in soils if not os.path.isdir(os.path.join(A, "blockstates", s.lower()))]
print("still missing blockstates:", missing if missing else "none")
