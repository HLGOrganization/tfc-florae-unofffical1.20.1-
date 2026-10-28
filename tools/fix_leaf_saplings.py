"""Fix leaf loot tables so every tree drops its sapling like TFC does.

Two problems were found in data/tfcflorae/loot_tables/blocks/wood/leaves:

1. every table used `"chance": 0.0083` for the sapling, while TFC uses
   0.011-0.078 (most around 0.03-0.04).  At 0.83% the player effectively never
   sees a sapling; this raises them to 0.04.
2. `bald_cypress` and `red_cypress` had no sapling entry at all: their second
   alternative dropped the leaves item again (a copy/paste error), so those two
   trees could never drop a sapling.

Usage: python tools/fix_leaf_saplings.py [--check]
"""
from __future__ import annotations

import argparse
import glob
import json
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
LEAVES = os.path.join(ROOT, "src", "main", "resources", "data", "tfcflorae",
                      "loot_tables", "blocks", "wood", "leaves")

OLD_CHANCE = '"chance": 0.0083'
NEW_CHANCE = '"chance": 0.04'


def patch_wrong_entry(data, wood):
    """Turn a duplicated `wood/leaves/<wood>` drop into `wood/sapling/<wood>`."""
    fixed = 0
    for pool in data.get("pools", []):
        for entry in pool.get("entries", []):
            for child in entry.get("children", []):
                name = child.get("name", "")
                if name == "tfcflorae:wood/leaves/" + wood:
                    conds = [c.get("condition") for c in child.get("conditions", [])]
                    if "minecraft:random_chance" in conds:
                        child["name"] = "tfcflorae:wood/sapling/" + wood
                        fixed += 1
    return fixed


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--check", action="store_true")
    args = ap.parse_args()

    files = sorted(glob.glob(os.path.join(LEAVES, "*.json")))
    print("loot tables:", len(files))

    chance_fixed = 0
    entry_fixed = []
    no_sapling = []

    for path in files:
        wood = os.path.basename(path)[:-5]
        txt = open(path, encoding="utf-8").read()

        if OLD_CHANCE in txt:
            chance_fixed += 1
            if not args.check:
                txt = txt.replace(OLD_CHANCE, NEW_CHANCE)
                open(path, "w", encoding="utf-8", newline="\n").write(txt)

        if "wood/sapling/" in txt:
            continue

        data = json.loads(txt)
        n = patch_wrong_entry(data, wood)
        if n:
            entry_fixed.append((wood, n))
            if not args.check:
                open(path, "w", encoding="utf-8", newline="\n").write(
                    json.dumps(data, indent=2, ensure_ascii=False) + "\n")
        else:
            no_sapling.append(wood)

    print("chance 0.0083 -> 0.04:", chance_fixed, "files")
    print("leaves entry patched to sapling:", entry_fixed)
    print("still without sapling:", no_sapling)
    if args.check:
        print("CHECK ONLY, nothing written")


if __name__ == "__main__":
    main()
