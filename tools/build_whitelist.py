"""Build a complete registered-id whitelist for tag generation.

The source-level audit misses some mapper patterns (notably the nested
`mapOfKeys(..., predicate, mapper)` used for ores, plus ceramics, foods, lumber
and layered sandstone).  Those ids are real - the 1.18-era tag files have been
referencing them and the game loads fine - so they are used as a second source.

Whitelist = (audit ids) union (ids referenced by the pre-generation tag files)
            minus (families we deliberately removed)

Removed families are listed explicitly because assets for them still sit on disk:
`REMOVED_SOILS`, flint (whole mod part), and the tuff rock items dropped in the
TFE-duplicate cleanup.

Usage: python tools/build_whitelist.py
Output: reports/registered-ids-full.json
"""
from __future__ import annotations

import json
import os
import subprocess
import tarfile
import tempfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
AUDIT = os.path.join(ROOT, "reports", "registered-ids.json")
OUT = os.path.join(ROOT, "reports", "registered-ids-full.json")
BASE_COMMIT = "ef0e95394"  # state before the tag generator first ran
TAG_DIRS = [
    "src/main/resources/data/tfc/tags",
    "src/main/resources/data/forge/tags",
    "src/main/resources/data/minecraft/tags",
    "src/main/resources/data/tfcflorae/tags",
]

REMOVED_SOILS = {
    "grass_path", "dense_grass_path", "sparse_grass_path", "dry_grass_path",
    "packed_mud", "mossy_packed_mud", "loose_mud", "coarse_dirt",
    "dry_grass", "dense_grass", "sparse_grass",
    "dry_clay_grass", "dense_clay_grass", "sparse_clay_grass",
    "dry_earthenware_clay_grass", "dense_earthenware_clay_grass", "sparse_earthenware_clay_grass",
    "dry_stoneware_clay_grass", "dense_stoneware_clay_grass", "sparse_stoneware_clay_grass",
}
# exact ids removed on purpose
REMOVED_EXACT = {
    "rock/loose/flint", "rock/mossy_loose/flint",
}


def audit_ids():
    d = json.load(open(AUDIT, encoding="utf-8"))
    return set(d["blocks"]), set(d["items"])


def old_tag_ids():
    """Ids referenced by the tag files as they were before my generator ran."""
    blocks, items = set(), set()
    with tempfile.TemporaryDirectory() as tmp:
        arch = os.path.join(tmp, "old.tar")
        with open(arch, "wb") as fh:
            subprocess.run(["git", "archive", BASE_COMMIT, "-o", arch] + TAG_DIRS,
                           cwd=ROOT, check=True, stdout=subprocess.DEVNULL)
        extract = os.path.join(tmp, "x")
        os.makedirs(extract)
        with tarfile.open(arch) as tf:
            tf.extractall(extract)
        for dirpath, _d, files in os.walk(extract):
            for fn in files:
                if not fn.endswith(".json"):
                    continue
                full = os.path.join(dirpath, fn)
                norm = full.replace(os.sep, "/")
                if "/tags/blocks/" in norm:
                    kind = blocks
                elif "/tags/items/" in norm:
                    kind = items
                else:
                    continue
                try:
                    data = json.load(open(full, encoding="utf-8-sig"))
                except Exception:
                    continue
                for v in data.get("values", []):
                    ident = v.get("id") if isinstance(v, dict) else v
                    if isinstance(ident, str) and ident.startswith("tfcflorae:"):
                        kind.add(ident.split(":", 1)[1])
    return blocks, items


def prune(ids):
    return {i for i in ids
            if i.split("/")[0] not in REMOVED_SOILS and i not in REMOVED_EXACT}


def main():
    ab, ai = audit_ids()
    ob, oi = old_tag_ids()
    full_b = prune(ab | ob)
    full_i = prune(ai | oi)
    print("audit:  blocks %d  items %d" % (len(ab), len(ai)))
    print("old tag: blocks %d  items %d" % (len(ob), len(oi)))
    print("full:   blocks %d  items %d  (+%d / +%d over audit)" % (
        len(full_b), len(full_i), len(full_b) - len(ab), len(full_i) - len(ai)))
    with open(OUT, "w", encoding="utf-8") as fh:
        json.dump({"blocks": sorted(full_b), "items": sorted(full_i)}, fh, indent=1)
    print("wrote", OUT)


if __name__ == "__main__":
    main()
