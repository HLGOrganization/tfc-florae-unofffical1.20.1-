"""Remove tag entries that point at ids which no longer exist.

A tag JSON with one unknown id fails to load as a whole, and every tag that
depends on it fails too.  That is how one bogus `minecraft:logs` entry wiped
every leaf block in the game.

Judgement rule: an entry is *certainly* dead when the id has no asset at all
(no blockstate for blocks, no item model for items).  Entries that still have
assets are kept even if the source-level audit did not see them, because those
are usually real registrations the parser missed.

Usage: python tools/prune_dead_tag_ids.py [--apply]
"""
from __future__ import annotations

import argparse
import json
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "src", "main", "resources")
DATA = os.path.join(RES, "data")
ASSETS = os.path.join(RES, "assets", "tfcflorae")

# Families that are no longer registered even though their assets are still on
# disk (asset presence alone would say "alive").  Keep in sync with
# TFCFBlocks.REMOVED_SOILS.
REMOVED_PREFIXES = {
    "grass_path", "dense_grass_path", "sparse_grass_path", "dry_grass_path",
    "packed_mud", "mossy_packed_mud", "loose_mud", "coarse_dirt",
    "dry_grass", "dense_grass", "sparse_grass",
    "dry_clay_grass", "dense_clay_grass", "sparse_clay_grass",
    "dry_earthenware_clay_grass", "dense_earthenware_clay_grass", "sparse_earthenware_clay_grass",
    "dry_stoneware_clay_grass", "dense_stoneware_clay_grass", "sparse_stoneware_clay_grass",
}


def have(ident, kind):
    if ":" not in ident:
        return True  # no namespace = minecraft default, not ours to judge
    ns, path = ident.split(":", 1)
    if ns != "tfcflorae":
        return True  # not ours to judge
    if path.split("/")[0] in REMOVED_PREFIXES:
        return False
    if kind == "blocks":
        return os.path.isfile(os.path.join(ASSETS, "blockstates", path.replace("/", os.sep) + ".json"))
    return os.path.isfile(os.path.join(ASSETS, "models", "item", path.replace("/", os.sep) + ".json"))


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--apply", action="store_true")
    args = ap.parse_args()

    removed = []
    files = 0
    for dirpath, _d, fs in os.walk(DATA):
        for fn in fs:
            if not fn.endswith(".json") or "/tags/" not in dirpath.replace(os.sep, "/") + "/":
                continue
            full = os.path.join(dirpath, fn)
            norm = full.replace(os.sep, "/")
            if "/tags/blocks/" in norm:
                kind = "blocks"
            elif "/tags/items/" in norm:
                kind = "items"
            else:
                continue  # worldgen / entity_types / damage_type tags hold non-block ids
            try:
                data = json.load(open(full, encoding="utf-8-sig"))
            except Exception:
                continue
            vals = data.get("values")
            if not isinstance(vals, list):
                continue
            keep, drop = [], []
            for v in vals:
                ident = v.get("id") if isinstance(v, dict) else v
                if isinstance(ident, str) and not ident.startswith("#") and not have(ident, kind):
                    drop.append(ident)
                else:
                    keep.append(v)
            if drop:
                files += 1
                removed.extend((os.path.relpath(full, DATA), d) for d in drop)
                if args.apply:
                    data["values"] = keep
                    with open(full, "w", encoding="utf-8", newline="\n") as fh:
                        json.dump(data, fh, indent=2, ensure_ascii=False)
                        fh.write("\n")

    print(("APPLIED" if args.apply else "DRY RUN") + ": %d tag files, %d dead entries" % (files, len(removed)))
    for path, ident in removed[:40]:
        print("   %-62s %s" % (path, ident))
    if len(removed) > 40:
        print("   ... %d more" % (len(removed) - 40))


if __name__ == "__main__":
    main()
