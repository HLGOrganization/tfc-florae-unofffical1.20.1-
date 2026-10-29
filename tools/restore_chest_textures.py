"""Restore the wooden chest / trapped chest textures from the 1.18.2 original.

TFCFChestBlockEntityRenderer.getMaterial() resolves

    tfcflorae:textures/entity/chest/<folder>/<wood>.png
    folder = normal | normal_left | normal_right | trapped | trapped_left | trapped_right

and this port had no textures/entity directory at all, so vanilla's oak chest texture was
used for every wood.  The 1.18.2 jar (tfc_florae/jar) still has the full set - 133 files
per folder, which includes rock chests; only the 66 woods this port registers are copied.
"""
import io
import os
import shutil

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC = os.path.join(os.path.dirname(ROOT), "tfc_florae", "jar", "assets", "tfcflorae", "textures", "entity", "chest")
DST = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae", "textures", "entity", "chest")

A = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae")
woods = sorted(f[:-5] for f in os.listdir(os.path.join(A, "blockstates", "wood", "planks"))
               if f.endswith(".json"))
WOODSET = set(woods)
print("woods:", len(woods))

FOLDERS = ["normal", "normal_left", "normal_right", "trapped", "trapped_left", "trapped_right"]

total = 0
missing_total = []
for folder in FOLDERS:
    src_dir = os.path.join(SRC, folder)
    if not os.path.isdir(src_dir):
        print("   !! missing source folder:", folder)
        continue
    out_dir = os.path.join(DST, folder)
    os.makedirs(out_dir, exist_ok=True)
    copied = 0
    missing = []
    for w in woods:
        s = os.path.join(src_dir, w + ".png")
        if not os.path.isfile(s):
            missing.append(w)
            continue
        shutil.copy2(s, os.path.join(out_dir, w + ".png"))
        copied += 1
    total += copied
    print("   %-14s copied %2d  missing %s" % (folder, copied, missing[:5] if missing else "none"))
    missing_total += missing

print("total copies:", total)
if missing_total:
    print("woods with no 1.18.2 chest texture:", sorted(set(missing_total)))
