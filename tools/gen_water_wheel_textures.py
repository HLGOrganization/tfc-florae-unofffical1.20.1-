"""Give every mod wood its own water wheel entity texture.

TFC's WaterWheelBlock builds its texture path from the wood name, so our woods asked
for textures/entity/water_wheel/<wood>.png which does not exist.  The renderer then
had nothing to draw even once the block entity existed.

They are written into assets/tfc/ on purpose: the block hardcodes the tfc namespace
for that path, and a mod may contribute files to another mod's asset namespace.
TFC's own 20 files are left untouched (we only create the 66 new names).
"""
import os
import shutil
import zipfile

TFC = r"D:\学习资料\附属模组工作区\[群峦传说] TerraFirmaCraft-Forge-1.20.1-3.2.20.jar"
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "src", "main", "resources")
A = os.path.join(RES, "assets", "tfcflorae")

woods = sorted(f[:-5] for f in os.listdir(os.path.join(A, "blockstates", "wood", "planks"))
               if f.endswith(".json"))

out_dir = os.path.join(RES, "assets", "tfc", "textures", "entity", "water_wheel")
os.makedirs(out_dir, exist_ok=True)

with zipfile.ZipFile(TFC) as z:
    # pick a base texture: prefer oak
    base = None
    for cand in ("assets/tfc/textures/entity/water_wheel/oak.png",
                 "assets/tfc/textures/entity/water_wheel/birch.png"):
        if cand in z.namelist():
            base = z.read(cand)
            break
    if base is None:
        tfc_tex = [n for n in z.namelist() if "textures/entity/water_wheel/" in n and n.endswith(".png")]
        base = z.read(sorted(tfc_tex)[0])

made = 0
for w in woods:
    dst = os.path.join(out_dir, w + ".png")
    if os.path.isfile(dst):
        continue
    with open(dst, "wb") as fh:
        fh.write(base)
    made += 1

print("woods:", len(woods), " water wheel entity textures written:", made)
