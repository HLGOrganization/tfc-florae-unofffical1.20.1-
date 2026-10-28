"""Generate TFC-facing tags for tfcflorae blocks and items.

Design
------
TFC drives rock collapse, landslides, tool tiers and many recipes through tags in
the `tfc` namespace.  An addon participates by shipping a file with the *same*
path (`data/tfc/tags/blocks/<tag>.json`) containing only its own ids and
`"replace": false`; Forge merges it with TFC's own values.

So this tool takes our registered ids (derived from the blockstate / item model
tree, which mirrors registration) and writes:

    data/tfc/tags/blocks/<tag>.json       our block ids for that TFC tag
    data/tfc/tags/items/<tag>.json        our item ids for that TFC tag
    data/forge/tags/blocks|items/<tag>.json  common tags where applicable
    data/tfcflorae/tags/blocks|items/rock/<category>.json
                                          geological class (TFC has no rock
                                          category tag, so we provide one)

Rock categories mirror TFCFRock's RockCategory (and real geology).

Usage
    python tools/gen_tags.py --check    # report what would be written
    python tools/gen_tags.py            # write files
"""
from __future__ import annotations

import argparse
import collections
import json
import os
import re

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae")
DATA = os.path.join(ROOT, "src", "main", "resources", "data")

# name -> RockCategory, straight from TFCFRock
ROCKS = {
    "red_granite": "igneous_intrusive",
    "porphyry": "igneous_extrusive",
    "boninite": "igneous_extrusive",
    "carbonatite": "igneous_extrusive",
    "tuff": "igneous_extrusive",
    "laterite": "sedimentary",
    "mudstone": "sedimentary",
    "sandstone": "sedimentary",
    "arkose": "sedimentary",
    "jaspillite": "sedimentary",
    "wackestone": "sedimentary",
    "cataclasite": "metamorphic",
    "soapstone": "metamorphic",
}

# block path prefix -> TFC / Forge tags it belongs to
ROCK_BLOCK_RULES = {
    "rock/raw": ["tfc:rock/raw", "tfc:breaks_when_isolated"],
    "rock/hardened": ["tfc:rock/hardened"],
    "rock/smooth": ["tfc:rock/smooth"],
    "rock/cobble": ["tfc:can_landslide", "forge:cobblestone/normal"],
    "rock/mossy_cobble": ["tfc:can_landslide"],
    "rock/gravel": ["tfc:rock/gravel", "tfc:can_landslide", "forge:gravel"],
    "rock/bricks": ["tfc:rock/bricks", "forge:stone_bricks"],
    "rock/mossy_bricks": ["tfc:rock/mossy_bricks", "tfc:rock/bricks"],
    "rock/cracked_bricks": ["tfc:rock/cracked_bricks", "tfc:rock/bricks"],
    "rock/chiseled": ["tfc:rock/chiseled_bricks", "tfc:rock/bricks"],
    "rock/spike": ["tfc:can_collapse"],
    "rock/aqueduct": ["tfc:rock/aqueducts"],
    "rock/anvil": ["tfc:rock_anvils"],
    "rock/loose": ["tfc:loose_rocks"],
    "rock/mossy_loose": ["tfc:loose_rocks"],
    "rock/gravel_layer": ["tfc:can_landslide"],
}

# variant sub-paths that still count as rock products of that type
ROCK_SUBDIRS = ("slab", "stairs", "wall")

# Non-rock families: block/item id prefix -> tags it belongs to.
# Ordered by prefix length (longest first) so `plant/potted` wins over `plant`.
PREFIX_RULES = {
    # --- soil / sand: TFC put dirt, grass, farmland, mud, sand and clay into
    # `tfc:can_landslide`, so the mod's own earth variants belong there too.
    "rockier_compact_dirt": ["tfc:can_landslide"],
    "rocky_compact_dirt": ["tfc:can_landslide"],
    "rockiest_compact_dirt": ["tfc:can_landslide"],
    "pebble_compact_dirt": ["tfc:can_landslide"],
    "sand/rocky": ["tfc:can_landslide", "forge:sand"],
    "sand/sandy_tiles": ["tfc:can_landslide", "forge:sand"],
    "sand/sandier_tiles": ["tfc:can_landslide", "forge:sand"],
    "sand/sandiest_tiles": ["tfc:can_landslide", "forge:sand"],
    "sandstone/layered": ["forge:sandstone", "minecraft:stone_bricks"],
    # --- ores / deposits
    "ore": ["tfc:rock/ores"],
    "deposit": ["tfc:ore_deposits", "tfc:can_landslide"],
    # --- wood
    "wood/log": ["minecraft:logs", "forge:logs", "minecraft:logs_that_log"],
    "wood/wood": ["minecraft:logs", "forge:logs", "minecraft:logs_that_log"],
    "wood/stripped_log": ["forge:stripped_logs"],
    "wood/stripped_wood": ["forge:stripped_logs"],
    "wood/planks": ["minecraft:planks"],
    "wood/leaves": ["minecraft:leaves"],
    "wood/sapling": ["minecraft:saplings"],
    "wood/fallen_leaves": ["tfc:fallen_leaves", "minecraft:leaves"],
    "wood/barrel": ["tfc:barrels"],
    "wood/bookshelf": ["tfc:bookshelves"],
    "wood/chiseled_bookshelf": ["tfc:bookshelves"],
    "wood/chest": ["forge:chests/wooden"],
    "wood/trapped_chest": ["forge:chests/wooden"],
    "wood/trapdoor": ["minecraft:trapdoors"],
    "wood/button": ["minecraft:buttons"],
    "wood/pressure_plate": ["minecraft:pressure_plates"],
    "wood/door": ["minecraft:doors"],
    "wood/fence_gate": ["minecraft:fence_gates"],
    "wood/fence": ["minecraft:fences"],
    "wood/log_fence": ["minecraft:fences"],
    "wood/sign": ["minecraft:signs"],
    "wood/wall_sign": ["minecraft:signs"],
    "wood/slab": ["minecraft:slabs"],
    "wood/stairs": ["minecraft:stairs"],
    "wood/workbench": ["tfc:workbenches"],
    "wood/loom": ["tfc:looms"],
    "wood/sluice": ["tfc:sluices"],
    "wood/lectern": ["tfc:lecterns"],
    "wood/jar_shelf": ["tfc:jar_shelves"],
    "wood/scribing_table": ["tfc:scribing_tables"],
    "wood/sewing_table": ["tfc:sewing_tables"],
    "wood/tool_rack": ["tfc:tool_racks"],
    "wood/water_wheel": ["tfc:water_wheels"],
    "wood/twig": ["tfc:twigs"],
    "wood/potted_sapling": ["minecraft:flower_pots"],
    # --- plants
    "plant/potted": ["minecraft:flower_pots"],
    "plant": ["tfc:plants", "tfc:replaceable_plants"],
}


def load_ids(kind):
    """Ids from assets (blockstates for blocks, models/item for items)."""
    base = os.path.join(ASSETS, "blockstates" if kind == "blocks" else "models/item")
    ids = []
    for dirpath, _d, files in os.walk(base):
        for fn in files:
            if fn.endswith(".json"):
                ids.append(os.path.relpath(os.path.join(dirpath, fn), base).replace(os.sep, "/")[:-5])
    return sorted(set(ids))


def write_tag(ns, kind, tag, ids, check):
    path = os.path.join(DATA, ns, "tags", kind, tag + ".json")
    payload = {"replace": False, "values": sorted(ids)}
    if check:
        return path, len(ids)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as fh:
        json.dump(payload, fh, indent=2, ensure_ascii=False)
        fh.write("\n")
    return path, len(ids)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--check", action="store_true")
    args = ap.parse_args()

    blocks = load_ids("blocks")
    items = load_ids("items")
    print("our blocks: %d   our items: %d" % (len(blocks), len(items)))

    # tag -> [(id, kind)] ; kind is 'blocks' or 'items'
    collected = collections.defaultdict(set)
    cats = collections.defaultdict(set)

    def add(tag, ident, kind):
        collected[(kind, tag)].add(ident)

    rock_pat = re.compile(r"^rock/([a-z_]+)/(%s)$" % "|".join(ROCKS))
    rock_variant = re.compile(r"^rock/([a-z_]+)/(%s)/(%s)$" % ("|".join(ROCKS), "|".join(ROCK_SUBDIRS)))

    matched = collections.Counter()
    for ident in blocks:
        m = rock_pat.match(ident)
        mv = rock_variant.match(ident)
        if m:
            rtype, rock = m.group(1), m.group(2)
            tags = ROCK_BLOCK_RULES.get("rock/" + rtype)
            if tags:
                for t in tags:
                    add(t, "tfcflorae:" + ident, "blocks")
                    matched["rock/" + rtype] += 1
            cats["blocks"].add((ROCKS[rock], "tfcflorae:" + ident))
        elif mv:
            rtype, rock, _sub = mv.group(1), mv.group(2), mv.group(3)
            if ("rock/" + rtype) in ROCK_BLOCK_RULES:
                cats["blocks"].add((ROCKS[rock], "tfcflorae:" + ident))

    for ident in items:
        m = rock_pat.match(ident)
        mv = rock_variant.match(ident)
        if m:
            rtype, rock = m.group(1), m.group(2)
            tags = ROCK_BLOCK_RULES.get("rock/" + rtype)
            if tags:
                for t in tags:
                    add(t, "tfcflorae:" + ident, "items")
            cats["items"].add((ROCKS[rock], "tfcflorae:" + ident))
        elif mv:
            rtype, rock = mv.group(1), mv.group(2)
            if ("rock/" + rtype) in ROCK_BLOCK_RULES:
                cats["items"].add((ROCKS[rock], "tfcflorae:" + ident))

    written = []
    # prefix families (longest prefix first)
    prefix_keys = sorted(PREFIX_RULES, key=len, reverse=True)
    for kind, idlist in (("blocks", blocks), ("items", items)):
        for ident in idlist:
            if ident.startswith("rock/"):
                continue  # handled by the rock rules above
            for prefix in prefix_keys:
                if ident == prefix or ident.startswith(prefix + "/"):
                    for t in PREFIX_RULES[prefix]:
                        add(t, "tfcflorae:" + ident, kind)
                    break

    for (kind, tag) in sorted(collected):
        ids = collected[(kind, tag)]
        ns = tag.split(":")[0]
        path = tag.split(":", 1)[1]
        written.append(write_tag(ns, kind, path, ids, args.check))

    for kind in ("blocks", "items"):
        by_cat = collections.defaultdict(set)
        for cat, ident in cats[kind]:
            by_cat[cat].add(ident)
        for cat, ids in sorted(by_cat.items()):
            written.append(write_tag("tfcflorae", kind, "rock/" + cat, ids, args.check))

    print()
    for path, n in written:
        print("  %-78s %d values" % (os.path.relpath(path, ROOT), n))
    print()
    print("rock rule matches:", dict(matched))
    print(("CHECK" if args.check else "WROTE") + " %d tag files" % len(written))


if __name__ == "__main__":
    main()
