"""Three crystal geodes (amethyst / glowstone / ruby), modelled on TFC-IE-Crossover.

TFC's own tfc:geode type only has outer / middle / inner, so it can scatter a crystal
block but cannot grow buds on the cavity walls.  TFC-IE-Crossover's
tfc_ie_addon:quartz_geode adds two fields that are exactly what a geode needs:

    filling          what the hollow middle is filled with (air)
    inner_placements the clusters / buds that grow inside

Both mods are present in the pack, so we reuse that feature type instead of shipping a
Java feature of our own.  This means our geodes require TFC-IE-Crossover to be installed.

Shell is identical for all three as requested: hardened basalt outside, raw quartzite
inside.  Placement is a straight copy of tfc:placed_feature/geode so all three generate in
the same places and can be retuned together later.
"""
import json
import os

# crystal -> display name used in the file path
CRYSTALS = ("amethyst", "glowstone", "ruby")

OUTER = "tfc:rock/hardened/basalt"
MIDDLE = "tfc:rock/raw/quartzite"

# identical placement for all three, copied from tfc:placed_feature/geode
PLACEMENT = [
    {"type": "minecraft:rarity_filter", "chance": 500},
    {"type": "minecraft:in_square"},
    {
        "type": "minecraft:height_range",
        "height": {
            "type": "uniform",
            "min_inclusive": {"absolute": -48},
            "max_inclusive": {"absolute": 32},
        },
    },
    {"type": "tfc:biome"},
]

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC = os.path.join(ROOT, "src", "main", "resources", "data", "tfcflorae", "worldgen")


def configured(crystal):
    return {
        "type": "tfc_ie_addon:quartz_geode",
        "config": {
            "outer": OUTER,
            "middle": MIDDLE,
            "inner": [
                {"data": "tfcflorae:crystal/budding/%s" % crystal, "weight": 2},
                {"data": "tfcflorae:crystal/%s" % crystal, "weight": 5},
                {"data": MIDDLE, "weight": 1},
            ],
            "filling": [{"data": "minecraft:air", "weight": 1}],
            "inner_placements": [
                {"data": "tfcflorae:crystal/cluster/%s" % crystal, "weight": 1},
                {"data": "tfcflorae:crystal/large_bud/%s" % crystal, "weight": 3},
                {"data": "tfcflorae:crystal/medium_bud/%s" % crystal, "weight": 5},
                {"data": "tfcflorae:crystal/small_bud/%s" % crystal, "weight": 8},
            ],
        },
    }


def write(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as fh:
        fh.write(json.dumps(obj, indent=2) + "\n")


for c in CRYSTALS:
    write(os.path.join(SRC, "configured_feature", "geode", c + ".json"), configured(c))
    write(os.path.join(SRC, "placed_feature", "geode", c + ".json"),
          {"feature": "tfcflorae:geode/" + c, "placement": PLACEMENT})
    print("wrote", c)

print()
print("geodes:", len(CRYSTALS))
