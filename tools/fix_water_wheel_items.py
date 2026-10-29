"""Fix the water wheel item models: they all used minecraft:block/oak_planks.

TFC ships a dedicated icon per wood (tfc:item/wood/water_wheel_oak).  We have no
such icons for the 66 mod woods, so use the wood's own plank texture - at least the
colour matches the block instead of every wheel being oak.

Also fixes the same mistake anywhere else in item models (any layer0 pointing at
minecraft:block/oak_planks).
"""
import io
import json
import os
import re

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
A = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae")
ITEM = os.path.join(A, "models", "item")

woods = sorted(f[:-5] for f in os.listdir(os.path.join(A, "blockstates", "wood", "planks"))
               if f.endswith(".json"))
WOODSET = set(woods)

fixed = 0
other = []
for dirpath, _d, files in os.walk(ITEM):
    for fn in files:
        if not fn.endswith(".json"):
            continue
        full = os.path.join(dirpath, fn)
        txt = io.open(full, encoding="utf-8-sig").read()
        if "oak_planks" not in txt:
            continue
        rel = os.path.relpath(full, ITEM).replace(os.sep, "/")
        # wood name = file stem for these mechanical parts
        stem = fn[:-5]
        if stem in WOODSET:
            d = {"parent": "item/generated",
                 "textures": {"layer0": "tfcflorae:block/wood/planks/%s" % stem}}
            io.open(full, "w", encoding="utf-8", newline="\n").write(
                json.dumps(d, indent=2) + "\n")
            fixed += 1
        else:
            other.append(rel)

print("water-wheel style item models fixed:", fixed)
if other:
    print("other item models still referencing oak_planks:")
    for o in other[:15]:
        print("   ", o)
