"""Copy the user's lossless soil overlays straight into the mod.

Source: the unpacked mod folder the user filled in with hand-drawn overlays
  <base>/<variant>/<soil>.png   (16x16 RGBA, alpha = the soil grain layer)
No re-encoding happens here - shutil.copy2 byte for byte.
"""
import hashlib
import os
import shutil

SRC = r"D:\学习资料\mod解包\TFCFlorae-1.20.1-2.2.0-mc1.20.1\assets\tfcflorae\textures\block"
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DST = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae", "textures", "block", "compact_dirt")

VARIANTS = ["rockier_compact_dirt", "pebble_compact_dirt", "rocky_compact_dirt", "rockiest_compact_dirt"]
SOILS = ["loam", "sandy_loam", "silt", "silty_loam"]

copied = 0
missing = []
for v in VARIANTS:
    out_dir = os.path.join(DST, v)
    os.makedirs(out_dir, exist_ok=True)
    for s in SOILS:
        src = os.path.join(SRC, v, s + ".png")
        if not os.path.isfile(src):
            missing.append("%s/%s" % (v, s))
            continue
        dst = os.path.join(out_dir, s + ".png")
        shutil.copy2(src, dst)
        h = hashlib.sha256(open(dst, "rb").read()).hexdigest()[:12]
        copied += 1
        print("%-24s %-11s -> %s" % (v, s, h))

print("\ncopied:", copied)
if missing:
    print("missing:", missing)
