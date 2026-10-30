"""Recolour the bookshelf front (slot) textures per wood.

Reported: every wood shows the same front face because _bookshelf_empty and
_bookshelf_occupied were copied straight from vanilla, i.e. one fixed wood tone.

Both textures are a wood backplate plus either dark grooves (empty) or coloured book
spines (occupied).  Only the wood-coloured, reasonably bright pixels are retinted to the
wood's plank colour; dark grooves and saturated spines are left untouched so the slots
still read as slots and the books stay multicoloured.

Wood detection: hue in the orange/brown band and not overly dark.  Book spines are cyan,
green, purple, pink and blue - all far outside that band.
"""
import colorsys
import io
import os
import zipfile
from statistics import median

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
A = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae")
TEX = os.path.join(A, "textures", "block", "wood", "planks")
CLIENT = r"D:\线框的服务器\versions\1.20.1\1.20.1.jar"

z = zipfile.ZipFile(CLIENT)
templates = {}
for n in ("empty", "occupied"):
    templates[n] = Image.open(io.BytesIO(
        z.read("assets/minecraft/textures/block/chiseled_bookshelf_%s.png" % n)
    )).convert("RGBA")

# wood band and brightness floor, tuned against vanilla's textures
HUE_MIN, HUE_MAX = 0.015, 0.20
MIN_VALUE = 0.0           # grooves are wood-toned too; keep only their darkness
MAX_SAT = 0.80            # above this it is a book spine, keep it


def plank_colour(path):
    im = Image.open(path).convert("RGBA")
    px = [p[:3] for p in im.getdata() if p[3] > 128]
    return tuple(int(median(c[i] for c in px)) for i in range(3)) if px else (140, 110, 70)


def wood_luminance(template):
    """Average brightness of the pixels we consider wood, for relative tinting."""
    vals = []
    for r, g, b, a in template.getdata():
        if a == 0:
            continue
        h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
        if HUE_MIN <= h <= HUE_MAX and s <= MAX_SAT and v >= MIN_VALUE:
            vals.append(v)
    return median(vals) if vals else 0.6


def retint(template, target_rgb):
    base = wood_luminance(template)
    tr, tg, tb = [c / 255.0 for c in target_rgb]
    out = template.copy()
    px = out.load()
    changed = 0
    for y in range(out.height):
        for x in range(out.width):
            r, g, b, a = px[x, y]
            if a == 0:
                continue
            h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
            if not (HUE_MIN <= h <= HUE_MAX and s <= MAX_SAT):
                continue                      # book spine: leave its colour alone
            # keep the pixel's relative darkness so grooves stay grooves
            factor = v / base if base > 0.01 else 1.0
            factor = max(0.15, min(1.6, factor))
            nr = min(1.0, tr * factor)
            ng = min(1.0, tg * factor)
            nb = min(1.0, tb * factor)
            px[x, y] = (int(nr * 255), int(ng * 255), int(nb * 255), a)
            changed += 1
    return out, changed


woods = sorted(f[:-len("_bookshelf_side.png")] for f in os.listdir(TEX)
               if f.endswith("_bookshelf_side.png"))
print("woods:", len(woods))

stats = {"empty": 0, "occupied": 0}
sample = {}
for w in woods:
    plank = os.path.join(TEX, w + ".png")
    if not os.path.isfile(plank):
        print("  !! no plank for", w)
        continue
    rgb = plank_colour(plank)
    for kind in ("empty", "occupied"):
        img, n = retint(templates[kind], rgb)
        img.save(os.path.join(TEX, w + "_bookshelf_" + kind + ".png"))
        stats[kind] += n
        if w in ("african_padauk", "bamboo", "white_mangrove"):
            sample[(w, kind)] = n
    print("  %-18s plank=%s  tinted empty+occupied" % (w, rgb))

print()
print("total tinted pixels:", stats)
for k, v in sample.items():
    print("   %-18s %-9s %d px" % (k[0], k[1], v))
