"""Add TFE soil variants (fluvisol / mollisol / oxisol / podzol) to the two clays.

TFC only has four soil variants (silt / loam / sandy_loam / silty_loam).  TFE adds four
more soil orders on top: fluvisol (冲积土), mollisol (黑土), oxisol (氧化土) and
podzol (灰化土).  For the earthenware and stoneware clays to look and behave like the
rest of a TFE world, they need those four variants too - both the clay block and its
grass block.

Resources generated here, per clay:

    textures/block/<clay>/<family>.png                  1
    models/block/<clay>/<family>.json                   1   cube_all
    models/block/<clay>_grass/<family>/{5 faces}.json   5
    blockstates/<clay>/<family>.json                    1
    blockstates/<clay>_grass/<family>.json              1

Models and blockstates are cloned from the existing `silt` ones so the structure stays
identical to what the rest of the mod already ships.

Textures are derived from the clay's own `silt` texture, so the noise pattern matches the
four existing variants.  Each family's target mean colour is taken from TFE's own
`tfe:block/clay/<family>` texture, but expressed as a ratio against TFE's clay texture set
rather than copied, so the earthenware stays terracotta and the stoneware stays pale
porcelain.  Per-pixel multiply keeps the original grain.
"""
import io
import json
import os
import re
import zipfile

import numpy as np
from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae")

CLAWS = ("earthenware_clay", "stoneware_clay")

# family -> target mean RGB for each clay.
# Derived from TFE's addon clay textures, then pulled back toward the clay's own palette:
#   fluvisol  lighter and greyer than silt (alluvial)
#   mollisol  much darker (black soil)
#   oxisol    pushed red/orange (weathered tropical)
#   podzol    light ashy grey-brown
TARGETS = {
    "earthenware_clay": {
        "fluvisol": (123, 87, 73),
        "mollisol": (69, 46, 38),
        "oxisol": (143, 63, 42),
        "podzol": (135, 73, 54),
    },
    "stoneware_clay": {
        "fluvisol": (107, 103, 100),
        "mollisol": (56, 57, 58),
        "oxisol": (117, 77, 63),
        "podzol": (110, 90, 82),
    },
}

FAMILIES = ("fluvisol", "mollisol", "oxisol", "podzol")


def path(*parts):
    return os.path.join(RES, *parts)


def make_texture(clay, family, target):
    src = path("textures", "block", clay, "silt.png")
    img = Image.open(src).convert("RGBA")
    a = np.array(img).astype(np.float64)
    rgb = a[..., :3]
    base_mean = rgb.reshape(-1, 3).mean(0)
    ratio = np.array(target, dtype=np.float64) / base_mean
    out = rgb * ratio
    out = np.clip(out, 0, 255)
    a[..., :3] = out
    res = Image.fromarray(a.round().astype(np.uint8), "RGBA")
    dst = path("textures", "block", clay, family + ".png")
    res.save(dst)
    new_mean = np.array(res)[..., :3].reshape(-1, 3).mean(0)
    uniq = len(set(map(tuple, np.array(res)[..., :3].reshape(-1, 3))))
    return dst, base_mean, new_mean, uniq


def clone_json(src_rel, dst_rel, family):
    with io.open(path(*src_rel.split("/")), encoding="utf-8") as fh:
        text = fh.read()
    text = re.sub(r"(?<=/)silt\b", family, text)
    dst = path(*dst_rel.split("/"))
    os.makedirs(os.path.dirname(dst), exist_ok=True)
    with io.open(dst, "w", encoding="utf-8", newline="\n") as fh:
        fh.write(text)
    return dst


def main():
    written = 0
    print("=== textures ===")
    for clay in CLAWS:
        for fam in FAMILIES:
            dst, base, new, uniq = make_texture(clay, fam, TARGETS[clay][fam])
            print("  %-24s base=%s -> %s uniq=%d"
                  % ("%s/%s.png" % (clay, fam),
                     np.round(base, 1).tolist(), np.round(new, 1).tolist(), uniq))
            written += 1

    print()
    print("=== models / blockstates ===")
    for clay in CLAWS:
        for fam in FAMILIES:
            # clay model + blockstate
            clone_json("models/block/%s/silt.json" % clay,
                       "models/block/%s/%s.json" % (clay, fam), fam)
            clone_json("blockstates/%s/silt.json" % clay,
                       "blockstates/%s/%s.json" % (clay, fam), fam)
            # grass model faces + blockstate
            for face in ("bottom", "side", "snowy_side", "snowy_top", "top"):
                clone_json("models/block/%s_grass/silt/%s.json" % (clay, face),
                           "models/block/%s_grass/%s/%s.json" % (clay, fam, face), fam)
            clone_json("blockstates/%s_grass/silt.json" % clay,
                       "blockstates/%s_grass/%s.json" % (clay, fam), fam)
            written += 9
        print("  %-18s 4 variants -> 4 clay models + 20 grass models + 8 blockstates" % clay)

    print()
    print("total files written:", written)


if __name__ == "__main__":
    main()
