"""Append our rocks to the KubeJS data pack copies, which outrank the mod jar.

The pack ships its own data/tfc/worldgen overrides under kubejs/data:

  world_preset/           7 files (big_world, drought_world, freezing_world, overworld,
                          small_world, temperate, tropical) - all 21 rocks / 11 layers
  configured_feature/vein 18 files

Because KubeJS data is loaded as a datapack above mod resources, those copies win and the
versions inside our jar never take effect.  So the same additive edits are applied here.

Backups are written next to each file as <name>.bak-rocks before anything is changed.
"""
import json
import os
import re
import shutil

KJS = r"D:\线框的服务器\versions\TFC_Wild_Fire\kubejs\data\tfc\worldgen"

OUR_ROCKS = {
    "red_granite": "IGNEOUS_INTRUSIVE",
    "porphyry": "IGNEOUS_EXTRUSIVE",
    "boninite": "IGNEOUS_EXTRUSIVE",
    "carbonatite": "IGNEOUS_EXTRUSIVE",
    "laterite": "SEDIMENTARY",
    "mudstone": "SEDIMENTARY",
    "jaspillite": "SEDIMENTARY",
    "wackestone": "SEDIMENTARY",
    "soapstone": "METAMORPHIC",
}

TFC_CATEGORY = {
    "granite": "IGNEOUS_INTRUSIVE", "diorite": "IGNEOUS_INTRUSIVE", "gabbro": "IGNEOUS_INTRUSIVE",
    "rhyolite": "IGNEOUS_EXTRUSIVE", "basalt": "IGNEOUS_EXTRUSIVE",
    "andesite": "IGNEOUS_EXTRUSIVE", "dacite": "IGNEOUS_EXTRUSIVE",
    "shale": "SEDIMENTARY", "claystone": "SEDIMENTARY", "limestone": "SEDIMENTARY",
    "conglomerate": "SEDIMENTARY", "dolomite": "SEDIMENTARY", "chert": "SEDIMENTARY",
    "chalk": "SEDIMENTARY",
    "quartzite": "METAMORPHIC", "slate": "METAMORPHIC", "phyllite": "METAMORPHIC",
    "schist": "METAMORPHIC", "gneiss": "METAMORPHIC", "marble": "METAMORPHIC",
    "tuff": "IGNEOUS_EXTRUSIVE",
}

# ores can come from any namespace (tfc, tfc_ie_addon, kubejs...), we only need the ore name
ORE_RE = re.compile(r'^[a-z0-9_]+:ore/([a-z0-9_]+)/[a-z0-9_]+$')

# our ore blocks: which (ore, rock) pairs exist
BS = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                  "src", "main", "resources", "assets", "tfcflorae", "blockstates")


def has_ore(ore, rock):
    return os.path.isfile(os.path.join(BS, "ore", ore, rock + ".json"))


def backup(path):
    b = path + ".bak-rocks"
    if not os.path.exists(b):
        shutil.copy2(path, b)


def save(path, data):
    with open(path, "w", encoding="utf-8", newline="\n") as fh:
        fh.write(json.dumps(data, indent=2) + "\n")


# ---------------------------------------------------------------- presets
new_layers = [
    {"id": "tfcflorae_intrusive", "layers": {"red_granite": "bottom"}},
    {"id": "tfcflorae_extrusive",
     "layers": {"porphyry": "tfcflorae_intrusive", "boninite": "tfcflorae_intrusive",
                "carbonatite": "tfcflorae_intrusive"}},
    {"id": "tfcflorae_sedimentary",
     "layers": {"laterite": "marble", "mudstone": "marble",
                "jaspillite": "quartzite", "wackestone": "marble"}},
    {"id": "tfcflorae_metamorphic", "layers": {"soapstone": "bottom"}},
]

preset_dir = os.path.join(KJS, "world_preset")
print("=== world presets ===")
for fn in sorted(os.listdir(preset_dir)):
    if not fn.endswith(".json"):
        continue
    p = os.path.join(preset_dir, fn)
    d = json.load(open(p, encoding="utf-8-sig"))
    try:
        rls = d["dimensions"]["minecraft:overworld"]["generator"]["tfc_settings"]["rock_layer_settings"]
    except KeyError:
        print("   %-22s no rock_layer_settings, skipped" % fn)
        continue
    added = 0
    for rock in OUR_ROCKS:
        if rock not in rls["rocks"]:
            rls["rocks"][rock] = "tfc:" + rock
            added += 1
    have = {l["id"] for l in rls["layers"]}
    for layer in new_layers:
        if layer["id"] not in have:
            rls["layers"].append(layer)
    for top, extra in (("land", ["tfcflorae_extrusive", "tfcflorae_sedimentary", "tfcflorae_metamorphic"]),
                       ("volcanic", ["tfcflorae_extrusive"]),
                       ("uplift", ["tfcflorae_metamorphic", "tfcflorae_sedimentary"])):
        if top in rls:
            for e in extra:
                if e not in rls[top]:
                    rls[top].append(e)
    if added:
        backup(p)
        save(p, d)
    print("   %-22s rocks %d -> %d, layers -> %d" % (fn, 21, len(rls["rocks"]), len(rls["layers"])))

# ---------------------------------------------------------------- veins
vein_dir = os.path.join(KJS, "configured_feature", "vein")
print()
print("=== veins ===")
touched = 0
entries = 0
for fn in sorted(os.listdir(vein_dir)):
    if not fn.endswith(".json"):
        continue
    p = os.path.join(vein_dir, fn)
    data = json.load(open(p, encoding="utf-8-sig"))
    blocks = data.get("config", {}).get("blocks")
    if not isinstance(blocks, list) or not blocks:
        continue
    cats = set()
    for entry in blocks:
        for rep in entry.get("replace", []):
            if isinstance(rep, str):
                rock = rep.rsplit("/", 1)[-1]
                if rock in TFC_CATEGORY:
                    cats.add(TFC_CATEGORY[rock])
    if not cats:
        continue
    weights = None
    for entry in blocks:
        withs = entry.get("with")
        if not isinstance(withs, list) or not withs:
            continue
        # a vein may mix ores with deposits or gems; we only need the ore entries
        cand = []
        for w in withs:
            if not isinstance(w, dict):
                cand = []
                break
            m = ORE_RE.match(str(w.get("block", "")))
            if m:
                cand.append((w.get("weight"), m.group(1)))
        if cand:
            weights = cand
            break
    if weights is None:
        continue
    usable = [(w, o) for (w, o) in weights if any(has_ore(o, r) for r in OUR_ROCKS)]
    if not usable:
        print("   %-42s skipped (no matching ore)" % fn)
        continue
    existing = set()
    for entry in blocks:
        for rep in entry.get("replace", []):
            if isinstance(rep, str):
                existing.add(rep.rsplit("/", 1)[-1])
    added = 0
    for rock, cat in OUR_ROCKS.items():
        if cat not in cats or rock in existing:
            continue
        withs = []
        for weight, ore in usable:
            if not has_ore(ore, rock):
                continue
            item = {"block": "tfcflorae:ore/%s/%s" % (ore, rock)}
            if weight is not None:
                item = {"weight": weight, "block": "tfcflorae:ore/%s/%s" % (ore, rock)}
            withs.append(item)
        if withs:
            blocks.append({"replace": ["tfcflorae:rock/raw/%s" % rock], "with": withs})
            added += 1
    if added:
        backup(p)
        save(p, data)
        touched += 1
        entries += added
        print("   %-42s +%d" % (fn, added))

print()
print("veins touched: %d | rock entries: %d" % (touched, entries))
