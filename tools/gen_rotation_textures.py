"""Textures the rotation machines read from TFC's namespace.

Two sets, both generated from data already in the repo:

1. assets/tfc/textures/block/wood/planks/<wood>.png
   TFC's planksTexture() is hardcoded to tfc:block/wood/planks/<name>, and the axles,
   gear boxes, clutches and windmills use it.  Our woods have no such file, which is why
   they showed up untextured.  Copies our own plank texture (tfcflorae:block/wood/planks).

2. assets/tfc/textures/entity/water_wheel/<wood>.png
   The wheel model samples tfc:textures/entity/water_wheel/<name>.png.  These existed but
   were plain copies of oak's texture; here they are recoloured per wood from that wood's
   average plank colour, keeping the shading of the original.
"""
import io
import os
import shutil
import statistics
import zipfile

from PIL import Image

TFC = r"D:\学习资料\附属模组工作区\[群峦传说] TerraFirmaCraft-Forge-1.20.1-3.2.20.jar"
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "src", "main", "resources")
OURS = os.path.join(RES, "assets", "tfcflorae")
TFC_NS = os.path.join(RES, "assets", "tfc")

woods = sorted(f[:-5] for f in os.listdir(os.path.join(OURS, "blockstates", "wood", "planks"))
               if f.endswith(".json"))

# ---- 1) plank textures in the tfc namespace --------------------------------
planks_out = os.path.join(TFC_NS, "textures", "block", "wood", "planks")
os.makedirs(planks_out, exist_ok=True)
copied = 0
for w in woods:
    src = os.path.join(OURS, "textures", "block", "wood", "planks", w + ".png")
    if not os.path.isfile(src):
        print("   !! missing plank texture:", w)
        continue
    shutil.copy2(src, os.path.join(planks_out, w + ".png"))
    copied += 1
print("plank textures for tfc namespace:", copied)

# ---- 2) water wheel entity textures, recoloured per wood -------------------
with zipfile.ZipFile(TFC) as z:
    base = Image.open(io.BytesIO(z.read("assets/tfc/textures/entity/water_wheel/oak.png"))).convert("RGBA")
print("wheel texture size:", base.size)
opaque = [(r, g, b) for r, g, b, a in base.getdata() if a > 128]
ref = statistics.mean((r + g + b) / 3.0 for r, g, b in opaque) or 72.0

wheel_out = os.path.join(TFC_NS, "textures", "entity", "water_wheel")
os.makedirs(wheel_out, exist_ok=True)
made = 0
for w in woods:
    plank = os.path.join(OURS, "textures", "block", "wood", "planks", w + ".png")
    if not os.path.isfile(plank):
        continue
    pim = Image.open(plank).convert("RGBA")
    pp = [(r, g, b) for r, g, b, a in pim.getdata() if a > 128]
    if not pp:
        continue
    pr = statistics.mean(c[0] for c in pp)
    pg = statistics.mean(c[1] for c in pp)
    pb = statistics.mean(c[2] for c in pp)

    px = []
    for r, g, b, a in base.getdata():
        if a <= 8:
            px.append((0, 0, 0, 0))
            continue
        lum = (r + g + b) / 3.0
        scale = 0.55 + 0.60 * (lum / 255.0)
        px.append((max(0, min(255, int(pr * scale))),
                   max(0, min(255, int(pg * scale))),
                   max(0, min(255, int(pb * scale))),
                   a))
    out = Image.new("RGBA", base.size, (0, 0, 0, 0))
    out.putdata(px)
    out.save(os.path.join(wheel_out, w + ".png"))
    made += 1
print("water wheel entity textures:", made)

# preview strip
try:
    names = ["african_padauk", "joshua", "white_mangrove", "red_cedar"]
    ims = []
    for n in names:
        p = os.path.join(wheel_out, n + ".png")
        if os.path.isfile(p):
            ims.append(Image.open(p).convert("RGBA").resize((96, 96), Image.NEAREST))
    if ims:
        strip = Image.new("RGBA", (96 * len(ims), 96), (25, 25, 25, 255))
        for i, im in enumerate(ims):
            strip.paste(im, (i * 96, 0), im)
        strip.save(os.path.join(ROOT, "reports", "_wheel_textures_preview.png"))
        print("preview saved")
except Exception as e:
    print("preview failed:", e)
