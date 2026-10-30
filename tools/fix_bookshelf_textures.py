"""Rebuild the bookshelf side/top/occupied textures the way vanilla's chiseled bookshelf does.

Two reported problems:

1. The side used the 1.18 bookshelf texture (a wall of book spines).  A chiseled bookshelf's
   side is plain timber with the frame shape of vanilla's chiseled_bookshelf_side.
2. Placing a book turned the slot purple-black: the *_occupied_* models ask for
   <wood>_bookshelf_occupied, which was never generated (only _empty was).

Approach: take vanilla's chiseled_bookshelf_{side,top}.png as the shape/luminance template,
recolour them to each wood's plank colour, and copy vanilla's chiseled_bookshelf_occupied.png
for the book spines (those are meant to look the same in every wood).
"""
import io
import json
import os
import zipfile
from statistics import median

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
A = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae")
TEX = os.path.join(A, "textures", "block", "wood", "planks")
CLIENT = r"D:\线框的服务器\versions\1.20.1\1.20.1.jar"

z = zipfile.ZipFile(CLIENT)
vanilla = {}
for name in ("side", "top", "occupied"):
    vanilla[name] = Image.open(io.BytesIO(
        z.read("assets/minecraft/textures/block/chiseled_bookshelf_%s.png" % name)
    )).convert("RGBA")
    print("vanilla %-9s %dx%d" % (name, vanilla[name].width, vanilla[name].height))


def plank_colour(path):
    """Median colour of the plank texture's opaque pixels."""
    im = Image.open(path).convert("RGBA")
    px = [p[:3] for p in im.getdata() if p[3] > 128]
    if not px:
        return (128, 128, 128)
    return tuple(int(median(c[i] for c in px)) for i in range(3))


def recolour(template, target_rgb, strength=1.0):
    """Keep the template's luminance, tint it toward target_rgb."""
    lum_px = []
    for r, g, b, a in template.getdata():
        if a > 0:
            lum_px.append((r + g + b) / (3 * 255.0))
    base_lum = median(lum_px) if lum_px else 0.5
    if base_lum <= 0.01:
        base_lum = 0.5
    tr, tg, tb = [c / 255.0 for c in target_rgb]
    out = template.copy()
    px = out.load()
    for y in range(out.height):
        for x in range(out.width):
            r, g, b, a = px[x, y]
            if a == 0:
                continue
            lum = (r + g + b) / (3 * 255.0)
            factor = lum / base_lum            # relative brightness of this pixel
            nr = min(1.0, tr * factor)
            ng = min(1.0, tg * factor)
            nb = min(1.0, tb * factor)
            # blend toward the recoloured value so we do not lose the original contrast
            nr = (1 - strength) * (r / 255.0) + strength * nr
            ng = (1 - strength) * (g / 255.0) + strength * ng
            nb = (1 - strength) * (b / 255.0) + strength * nb
            px[x, y] = (int(nr * 255), int(ng * 255), int(nb * 255), a)
    return out


woods = sorted(f[:-len("_bookshelf_empty.png")] for f in os.listdir(TEX)
               if f.endswith("_bookshelf_empty.png"))
print("woods:", len(woods))

made = {"side": 0, "top": 0, "occupied": 0}
for w in woods:
    plank = os.path.join(TEX, w + ".png")
    if not os.path.isfile(plank):
        print("  !! no plank texture for", w)
        continue
    rgb = plank_colour(plank)
    recolour(vanilla["side"], rgb, 1.0).save(os.path.join(TEX, w + "_bookshelf_side.png"))
    made["side"] += 1
    recolour(vanilla["top"], rgb, 1.0).save(os.path.join(TEX, w + "_bookshelf_top.png"))
    made["top"] += 1
    vanilla["occupied"].save(os.path.join(TEX, w + "_bookshelf_occupied.png"))
    made["occupied"] += 1

print("written:", made)

# sanity: every referenced bookshelf texture now exists
needed_suffix = ("_bookshelf_side", "_bookshelf_top", "_bookshelf_empty", "_bookshelf_occupied")
missing = []
for w in woods:
    for s in needed_suffix:
        if not os.path.isfile(os.path.join(TEX, w + s + ".png")):
            missing.append(w + s)
print("missing bookshelf textures:", len(missing))
for m in missing[:10]:
    print("   ", m)
