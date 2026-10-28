"""Generate the missing TFC collapse / landslide recipes.

TFC drives both mechanics with recipes, not tags:

    tfc:collapse   ingredient = blocks that fall apart when unsupported, result = cobble
    tfc:landslide  ingredient = block that slides, result = block it turns into

tfcflorae only shipped `collapse/arkose_cobble.json`, so every other rock of the
mod could neither collapse nor slide, and the mod's earth/sand variants had no
landslide entry at all.  This generates them from the registered block ids.

Usage: python tools/gen_collapse_landslide.py [--check]
"""
from __future__ import annotations

import argparse
import json
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "src", "main", "resources")
ASSETS = os.path.join(RES, "assets", "tfcflorae")
RECIPES = os.path.join(RES, "data", "tfcflorae", "recipes")

ROCKS = ["arkose", "boninite", "carbonatite", "cataclasite", "jaspillite", "laterite",
         "mudstone", "porphyry", "red_granite", "sandstone", "soapstone", "wackestone"]

# rock block types that fall apart into cobble (mirrors TFC's andesite_cobble.json)
COLLAPSE_TYPES = ["cobble", "raw", "hardened", "smooth", "chiseled", "bricks", "cracked_bricks"]
LANDSLIDE_TYPES = [("cobble", "cobble"), ("gravel", "gravel"), ("mossy_cobble", "mossy_cobble")]

COMMENT = "This file was automatically created by mcresources"


def block_ids():
    base = os.path.join(ASSETS, "blockstates")
    out = set()
    for dirpath, _d, files in os.walk(base):
        for fn in files:
            if fn.endswith(".json"):
                out.add(os.path.relpath(os.path.join(dirpath, fn), base).replace(os.sep, "/")[:-5])
    return out


def write(rel, payload, check):
    path = os.path.join(RECIPES, rel)
    if check:
        return path
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as fh:
        json.dump(payload, fh, indent=2, ensure_ascii=False)
        fh.write("\n")
    return path


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--check", action="store_true")
    args = ap.parse_args()

    ids = block_ids()
    ores = sorted(i for i in ids if i.startswith("ore/"))

    written = []
    for rock in ROCKS:
        # collapse -> cobble
        ingredient = ["tfcflorae:rock/%s/%s" % (t, rock) for t in COLLAPSE_TYPES]
        ingredient += ["tfcflorae:" + o for o in ores if o.endswith("/" + rock)]
        ingredient = sorted(set(i for i in ingredient if i.split(":", 1)[1] in ids))
        if ingredient:
            written.append(write("collapse/%s_cobble.json" % rock, {
                "__comment__": COMMENT,
                "type": "tfc:collapse",
                "ingredient": ingredient,
                "result": "tfcflorae:rock/cobble/" + rock,
            }, args.check))

        # landslide: cobble / gravel / mossy_cobble slide into themselves
        for src, dst in LANDSLIDE_TYPES:
            bid = "rock/%s/%s" % (src, rock)
            if bid in ids:
                written.append(write("landslide/rock/%s_%s.json" % (rock, src), {
                    "__comment__": COMMENT,
                    "type": "tfc:landslide",
                    "ingredient": "tfcflorae:" + bid,
                    "result": "tfcflorae:rock/%s/%s" % (dst, rock),
                }, args.check))

    # soil / sand variants slide into themselves
    soil_prefixes = ("rockier_compact_dirt/", "rocky_compact_dirt/", "rockiest_compact_dirt/",
                     "pebble_compact_dirt/")
    for ident in sorted(ids):
        if any(ident.startswith(p) for p in soil_prefixes):
            written.append(write("landslide/soil/%s.json" % ident.replace("/", "_"), {
                "__comment__": COMMENT,
                "type": "tfc:landslide",
                "ingredient": "tfcflorae:" + ident,
                "result": "tfcflorae:" + ident,
            }, args.check))
        elif ident.startswith("sand/") and ident.count("/") >= 2:
            written.append(write("landslide/sand/%s.json" % ident.replace("/", "_"), {
                "__comment__": COMMENT,
                "type": "tfc:landslide",
                "ingredient": "tfcflorae:" + ident,
                "result": "tfcflorae:" + ident,
            }, args.check))

    print(("CHECK" if args.check else "WROTE"), len(written), "recipes")
    for p in written[:10]:
        print("  " + os.path.relpath(p, ROOT))
    if len(written) > 10:
        print("  ... %d more" % (len(written) - 10))


if __name__ == "__main__":
    main()
