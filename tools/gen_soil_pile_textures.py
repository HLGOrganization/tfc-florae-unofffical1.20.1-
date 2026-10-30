"""Placeholder item textures and translations for the TFE soil piles.

tfcflorae:soil_pile/<variant> exists for the four new soil variants (the item map covers
TFCFVariant) but there is no texture or name, so they show as purple-black "missing" items.
The piles are the little dirt heaps TFC shows for soil items; a recoloured copy of the
existing loam pile is enough of a placeholder, tinted per soil colour:

  fluvisol  grey-brown (alluvial)
  mollisol  dark brown
  oxisol    red-brown
  podzol    pale grey
"""
import colorsys
import io
import json
import os

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
A = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae")

# target hue/saturation/value multipliers, applied on top of the loam pile
TINTS = {
    "fluvisol": (1.0, 0.85, 1.05),   # slightly lighter, greyer
    "mollisol": (1.0, 1.25, 0.80),   # darker, richer
    "oxisol":   (1.0, 1.30, 0.95),   # redder handled by hue shift below
    "podzol":   (1.0, 0.45, 1.25),   # desaturated, pale
}
HUE_SHIFT = {
    "fluvisol": 0.0,
    "mollisol": 0.0,
    "oxisol": 0.03,    # toward red
    "podzol": 0.0,
}

src = os.path.join(A, "textures", "item", "soil_pile", "loam.png")
dst_dir = os.path.join(A, "textures", "item", "soil_pile")
base = Image.open(src).convert("RGBA")
made = 0
for variant, (hs, ss, vs) in TINTS.items():
    out = base.copy()
    px = out.load()
    hue_shift = HUE_SHIFT[variant]
    for y in range(out.height):
        for x in range(out.width):
            r, g, b, a = px[x, y]
            if a == 0:
                continue
            h, s, v = colorsys.rgb_to_hsv(r / 255.0, g / 255.0, b / 255.0)
            h = (h + hue_shift) % 1.0
            s = min(1.0, s * ss)
            v = min(1.0, v * vs)
            nr, ng, nb = colorsys.hsv_to_rgb(h, s, v)
            px[x, y] = (int(nr * 255), int(ng * 255), int(nb * 255), a)
    out.save(os.path.join(dst_dir, variant + ".png"))
    made += 1
print("soil pile textures:", made)

# ---- item models ----------------------------------------------------------
m_dir = os.path.join(A, "models", "item", "soil_pile")
os.makedirs(m_dir, exist_ok=True)
for variant in TINTS:
    model = {
        "__comment__": "This file was automatically created by mcresources",
        "parent": "item/generated",
        "textures": {"layer0": "tfcflorae:item/soil_pile/" + variant},
    }
    io.open(os.path.join(m_dir, variant + ".json"), "w", encoding="utf-8", newline="\n").write(
        json.dumps(model, indent=2) + "\n")
print("item models written")

# ---- translations ---------------------------------------------------------
NAMES = {
    "fluvisol": ("冲积土堆", "Fluvisol Pile"),
    "mollisol": ("软土堆", "Mollisol Pile"),
    "oxisol": ("氧化土堆", "Oxisol Pile"),
    "podzol": ("灰化土堆", "Podzol Pile"),
}
for lang, idx in (("zh_cn.json", 0), ("en_us.json", 1)):
    p = os.path.join(A, "lang", lang)
    d = json.load(io.open(p, encoding="utf-8-sig"))
    added = 0
    for variant, names in NAMES.items():
        key = "item.tfcflorae.soil_pile." + variant
        if key not in d:
            d[key] = names[idx]
            added += 1
    io.open(p, "w", encoding="utf-8", newline="\n").write(
        json.dumps(d, indent=2, ensure_ascii=False) + "\n")
    print(lang, "added", added, "keys")
