"""Generate a water wheel item icon per mod wood.

TFC's own item model points at tfc:item/wood/water_wheel_oak, which does not exist -
even TFC's wheel items have no icon.  Its generic template
(textures/item/wood/water_wheel.png) *is* a grey wheel though, so recolour it with
each wood's plank colour.

Output: assets/tfcflorae/textures/item/wood/water_wheel_<wood>.png
"""
import io
import os
import statistics
import zipfile

from PIL import Image

TFC = r"D:\学习资料\附属模组工作区\[群峦传说] TerraFirmaCraft-Forge-1.20.1-3.2.20.jar"
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
A = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae")
OUT = os.path.join(A, "textures", "item", "wood")

woods = sorted(f[:-5] for f in os.listdir(os.path.join(A, "blockstates", "wood", "planks"))
               if f.endswith(".json"))

with zipfile.ZipFile(TFC) as z:
    tmpl = Image.open(io.BytesIO(z.read("assets/tfc/textures/item/wood/water_wheel.png"))).convert("RGBA")

opaque = [(r, g, b, a) for r, g, b, a in tmpl.getdata() if a > 128]
ref = statistics.mean((r + g + b) / 3.0 for r, g, b, _ in opaque) or 72.0
print("template greys:", sorted({round((r + g + b) / 3.0) for r, g, b, _ in opaque}), " ref:", round(ref, 1))

os.makedirs(OUT, exist_ok=True)
made = 0
for w in woods:
    plank = os.path.join(A, "textures", "block", "wood", "planks", w + ".png")
    if not os.path.isfile(plank):
        print("   !! no plank texture for", w)
        continue
    pim = Image.open(plank).convert("RGBA")
    pp = [(r, g, b) for r, g, b, a in pim.getdata() if a > 128]
    pr = statistics.mean(c[0] for c in pp)
    pg = statistics.mean(c[1] for c in pp)
    pb = statistics.mean(c[2] for c in pp)

    out = Image.new("RGBA", tmpl.size, (0, 0, 0, 0))
    px = []
    for r, g, b, a in tmpl.getdata():
        if a <= 8:
            px.append((0, 0, 0, 0))
            continue
        lum = (r + g + b) / 3.0
        # map the four greys (32/64/128/192) onto a gentle 0.55 .. 1.15 range so the
        # plank colour keeps its hue instead of blowing out to pink
        scale = 0.55 + 0.60 * (lum / 255.0)
        nr = max(0, min(255, int(pr * scale)))
        ng = max(0, min(255, int(pg * scale)))
        nb = max(0, min(255, int(pb * scale)))
        px.append((nr, ng, nb, a))
    out.putdata(px)
    out.save(os.path.join(OUT, "water_wheel_%s.png" % w))
    made += 1

print("icons written:", made)
