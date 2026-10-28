"""Generate the missing knapping-panel textures.

How TFC resolves them (`KnappingScreen.getButtonLocation`):

    tfc:textures/gui/knapping/<item registry path>.png
    tfc:textures/gui/knapping/<item registry path>_disabled.png   (if the type asks for it)

The path is hard-coded to the `tfc` namespace, so an addon must ship its sprites
under `assets/tfc/textures/gui/knapping/...`.  tfcflorae shipped none, which is
why earthenware clay, stoneware clay and the mod's loose rocks showed an empty
knapping panel.

Each sprite is taken from the source item's own texture (the panel cell should
look like the material you are knapping), and `_disabled` is the TFC-style dimmed
copy (luminance x 0.772, measured from TFC's own clay_ball/clay_ball_disabled).
"""
from __future__ import annotations

import json
import os
import re

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "src", "main", "resources")
ASSETS = os.path.join(RES, "assets", "tfcflorae")
DATA = os.path.join(RES, "data", "tfcflorae")
OUT_ROOT = os.path.join(RES, "assets", "tfc", "textures", "gui", "knapping")

DISABLED_SCALE = 0.772


def read_json(path):
    with open(path, encoding="utf-8-sig") as fh:
        return json.load(fh)


def tag_items(tag):
    """Resolve a `#namespace:tag` item tag to concrete item ids (local files only)."""
    ns, path = tag.split(":", 1)
    p = os.path.join(RES, "data", ns, "tags", "items", path + ".json")
    if not os.path.isfile(p):
        return []
    out = []
    for v in read_json(p).get("values", []):
        if isinstance(v, dict):
            v = v.get("id", "")
        if not isinstance(v, str) or not v:
            continue
        if v.startswith("#"):
            out.extend(tag_items(v[1:]))
        else:
            out.append(v)
    return out


def source_texture(item_id):
    """Find the 16x16 sprite of an item from its item model."""
    ns, path = item_id.split(":", 1)
    model = os.path.join(ASSETS, "models", "item", path + ".json")
    if not os.path.isfile(model):
        return None
    tex = (read_json(model).get("textures") or {}).get("layer0")
    if not isinstance(tex, str):
        return None
    tns, tpath = tex.split(":", 1)
    if tns != "tfcflorae":
        return ("external", tex)
    p = os.path.join(ASSETS, "textures", tpath.replace("/", os.sep) + ".png")
    return p if os.path.isfile(p) else None


def dim(src, dst):
    im = Image.open(src).convert("RGBA")
    out = Image.new("RGBA", im.size)
    px = []
    for c in im.getdata():
        if c[3] == 0:
            px.append(c)
        else:
            px.append((int(c[0] * DISABLED_SCALE), int(c[1] * DISABLED_SCALE),
                       int(c[2] * DISABLED_SCALE), c[3]))
    out.putdata(px)
    os.makedirs(os.path.dirname(dst), exist_ok=True)
    out.save(dst)


def main():
    jobs = []  # (item_id, needs_disabled)

    # 1. everything registered as a knapping type by this mod
    kt_dir = os.path.join(DATA, "tfc", "knapping_types")
    for fn in sorted(os.listdir(kt_dir)) if os.path.isdir(kt_dir) else []:
        if not fn.endswith(".json"):
            continue
        d = read_json(os.path.join(kt_dir, fn))
        tag = ((d.get("input") or {}).get("ingredient") or {}).get("tag")
        if not tag:
            continue
        disabled = bool(d.get("use_disabled_texture"))
        for item in tag_items(tag):
            jobs.append((item, disabled))

    # 2. our loose rocks fed into TFC's rock knapping
    rock_tag = os.path.join(RES, "data", "tfc", "tags", "items", "rock_knapping.json")
    if os.path.isfile(rock_tag):
        for item in read_json(rock_tag).get("values", []):
            if isinstance(item, str) and item.startswith("tfcflorae:"):
                jobs.append((item, False))

    seen = set()
    written = 0
    skipped = []
    for item, disabled in jobs:
        if item in seen:
            continue
        seen.add(item)
        src = source_texture(item)
        if isinstance(src, tuple) and src and src[0] == "external":
            skipped.append((item, "external texture " + src[1]))
            continue
        if not src:
            skipped.append((item, "no item texture"))
            continue
        rel = item.split(":", 1)[1]
        dst = os.path.join(OUT_ROOT, rel.replace("/", os.sep) + ".png")
        os.makedirs(os.path.dirname(dst), exist_ok=True)
        open(dst, "wb").write(open(src, "rb").read())
        written += 1
        print("  %-56s <- %s" % (os.path.relpath(dst, RES), os.path.relpath(src, ASSETS)))
        if disabled:
            dpath = dst[:-4] + "_disabled.png"
            dim(src, dpath)
            written += 1
            print("  %-56s <- dimmed" % os.path.relpath(dpath, RES))

    print()
    print("written:", written, "files")
    if skipped:
        print("skipped:")
        for item, why in skipped:
            print("  %-52s %s" % (item, why))


if __name__ == "__main__":
    main()
