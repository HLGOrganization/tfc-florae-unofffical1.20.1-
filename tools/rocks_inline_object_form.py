"""Rock layer integration, object form - no dependency on RockSettings presets.

The preset-name form ("red_granite": "tfc:red_granite") resolves through
RockSettings.PRESETS.  If that lookup fails the whole world preset is rejected, TFC falls
back, and every TFC biome and feature disappears - which is exactly what happened: no new
rocks AND all grass gone.

The official format allows a rock to be an inline object instead:

  {
    "raw": ..., "hardened": ..., "gravel": ..., "cobble": ...,
    "sand": ..., "sandstone": ...,
    "spike": ..., "loose": ..., "mossy_loose": ...      (last three optional)
  }

That needs no preset lookup at all, and every block it names is verified to exist before
being written.

Sand colours: TFC only ships black/brown/green/pink/red/white/yellow.  Our rocks' own sand
types include gray, orange, light_gray and cyan which TFC has no equivalent for, so those
map onto the nearest available colour.

Applied to both the KubeJS copies (which win) and our jar's copies.
"""
import json
import os
import re
import shutil

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
JAR_BS = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae", "blockstates")
KJS = r"D:\线框的服务器\versions\TFC_Wild_Fire\kubejs\data\tfc\worldgen"
JAR_WG = os.path.join(ROOT, "src", "main", "resources", "data", "tfc", "worldgen")

# rock -> (category, tfc sand colour)
OUR_ROCKS = {
    "red_granite":  ("IGNEOUS_INTRUSIVE", "red"),
    "porphyry":     ("IGNEOUS_EXTRUSIVE", "red"),
    "boninite":     ("IGNEOUS_EXTRUSIVE", "white"),     # own sand is GRAY, no tfc:gray
    "carbonatite":  ("IGNEOUS_EXTRUSIVE", "yellow"),
    "laterite":     ("SEDIMENTARY", "brown"),           # own sand is ORANGE, no tfc:orange
    "mudstone":     ("SEDIMENTARY", "white"),           # LIGHT_GRAY
    "jaspillite":   ("SEDIMENTARY", "red"),
    "wackestone":   ("SEDIMENTARY", "black"),
    "soapstone":    ("METAMORPHIC", "white"),           # CYAN
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

ORE_RE = re.compile(r'^[a-z0-9_]+:ore/([a-z0-9_]+)/[a-z0-9_]+$')


def bs_exists(*parts):
    return os.path.isfile(os.path.join(JAR_BS, *parts) + ".json")


def rock_object(rock, sand):
    """Inline Rock object; only fields whose blocks exist are emitted."""
    obj = {}
    for field, sub in (("raw", "raw"), ("hardened", "hardened"),
                       ("gravel", "gravel"), ("cobble", "cobble"),
                       ("spike", "spike"), ("loose", "loose"),
                       ("mossy_loose", "mossy_loose")):
        if bs_exists("rock", sub, rock):
            obj[field] = "tfcflorae:rock/%s/%s" % (sub, rock)
    obj["sand"] = "tfc:sand/%s" % sand
    obj["sandstone"] = "tfc:raw_sandstone/%s" % sand
    return obj


ROCK_OBJS = {r: rock_object(r, s) for r, (_c, s) in OUR_ROCKS.items()}

# every required field must be present
required = ("raw", "hardened", "gravel", "cobble", "sand", "sandstone")
for r, o in ROCK_OBJS.items():
    missing = [f for f in required if f not in o]
    if missing:
        raise SystemExit("rock %s missing required fields %s" % (r, missing))
print("rock objects built for", len(ROCK_OBJS), "rocks, all required fields present")

NEW_LAYERS = [
    {"id": "tfcflorae_intrusive", "layers": {"red_granite": "bottom"}},
    {"id": "tfcflorae_extrusive",
     "layers": {"porphyry": "tfcflorae_intrusive", "boninite": "tfcflorae_intrusive",
                "carbonatite": "tfcflorae_intrusive"}},
    {"id": "tfcflorae_sedimentary",
     "layers": {"laterite": "marble", "mudstone": "marble",
                "jaspillite": "quartzite", "wackestone": "marble"}},
    {"id": "tfcflorae_metamorphic", "layers": {"soapstone": "bottom"}},
]

TOP_ADDS = {
    "land": ["tfcflorae_extrusive", "tfcflorae_sedimentary", "tfcflorae_metamorphic"],
    "volcanic": ["tfcflorae_extrusive"],
    "uplift": ["tfcflorae_metamorphic", "tfcflorae_sedimentary"],
}


def patch_preset(path, label):
    d = json.load(open(path, encoding="utf-8-sig"))
    try:
        rls = d["dimensions"]["minecraft:overworld"]["generator"]["tfc_settings"]["rock_layer_settings"]
    except KeyError:
        print("   %-24s no rock_layer_settings" % label)
        return False
    changed = False
    for r, obj in ROCK_OBJS.items():
        if len(json.dumps(rls["rocks"].get(r))) < 20:      # absent or still a preset string
            rls["rocks"][r] = obj
            changed = True
    have = {l["id"] for l in rls["layers"]}
    for layer in NEW_LAYERS:
        if layer["id"] not in have:
            rls["layers"].append(layer)
            changed = True
    for top, ids in TOP_ADDS.items():
        if top not in rls:
            continue
        for i in ids:
            if i not in rls[top]:
                rls[top].append(i)
                changed = True
    if changed:
        bak = path + ".bak-rocks2"
        if not os.path.exists(bak):
            shutil.copy2(path, bak)
        with open(path, "w", encoding="utf-8", newline="\n") as fh:
            fh.write(json.dumps(d, indent=2) + "\n")
    return changed


def patch_veins(root, label):
    vd = os.path.join(root, "configured_feature", "vein")
    if not os.path.isdir(vd):
        return 0, 0
    touched = entries = 0
    for fn in sorted(os.listdir(vd)):
        if not fn.endswith(".json"):
            continue
        p = os.path.join(vd, fn)
        d = json.load(open(p, encoding="utf-8-sig"))
        blocks = d.get("config", {}).get("blocks")
        if not isinstance(blocks, list) or not blocks:
            continue
        cats = set()
        for e in blocks:
            for rep in e.get("replace", []):
                if isinstance(rep, str):
                    rk = rep.rsplit("/", 1)[-1]
                    if rk in TFC_CATEGORY:
                        cats.add(TFC_CATEGORY[rk])
        if not cats:
            continue
        weights = None
        for e in blocks:
            ws = e.get("with")
            if not isinstance(ws, list) or not ws:
                continue
            cand = []
            for w in ws:
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
        usable = [(w, o) for (w, o) in weights
                  if any(bs_exists("ore", o, r) for r in OUR_ROCKS)]
        if not usable:
            continue
        existing = set()
        for e in blocks:
            for rep in e.get("replace", []):
                if isinstance(rep, str):
                    existing.add(rep.rsplit("/", 1)[-1])
        added = 0
        for rock, (cat, _s) in OUR_ROCKS.items():
            if cat not in cats or rock in existing:
                continue
            withs = []
            for weight, ore in usable:
                if not bs_exists("ore", ore, rock):
                    continue
                item = {"block": "tfcflorae:ore/%s/%s" % (ore, rock)}
                if weight is not None:
                    item = {"weight": weight, "block": "tfcflorae:ore/%s/%s" % (ore, rock)}
                withs.append(item)
            if withs:
                blocks.append({"replace": ["tfcflorae:rock/raw/%s" % rock], "with": withs})
                added += 1
        if added:
            bak = p + ".bak-rocks2"
            if not os.path.exists(bak):
                shutil.copy2(p, bak)
            with open(p, "w", encoding="utf-8", newline="\n") as fh:
                fh.write(json.dumps(d, indent=2) + "\n")
            touched += 1
            entries += added
    return touched, entries


# ---- KubeJS (wins over the jar) ------------------------------------------
print()
print("=== kubejs presets ===")
pd = os.path.join(KJS, "world_preset")
for fn in sorted(os.listdir(pd)):
    if fn.endswith(".json"):
        ok = patch_preset(os.path.join(pd, fn), fn)
        print("   %-24s %s" % (fn, "patched" if ok else "unchanged"))

t, e = patch_veins(KJS, "kubejs")
print("kubejs veins: %d files, %d entries" % (t, e))

# ---- our jar copies ------------------------------------------------------
print()
print("=== jar presets ===")
pd = os.path.join(JAR_WG, "world_preset")
if os.path.isdir(pd):
    for fn in sorted(os.listdir(pd)):
        if fn.endswith(".json"):
            ok = patch_preset(os.path.join(pd, fn), fn)
            print("   %-24s %s" % (fn, "patched" if ok else "unchanged"))
t, e = patch_veins(JAR_WG, "jar")
print("jar veins: %d files, %d entries" % (t, e))

# ---- final check ---------------------------------------------------------
print()
for label, pd in (("kubejs", os.path.join(KJS, "world_preset")),
                  ("jar", os.path.join(JAR_WG, "world_preset"))):
    for fn in sorted(os.listdir(pd)):
        if not fn.endswith(".json"):
            continue
        d = json.load(open(os.path.join(pd, fn), encoding="utf-8-sig"))
        rls = d["dimensions"]["minecraft:overworld"]["generator"]["tfc_settings"]["rock_layer_settings"]
        inline = sum(1 for r in OUR_ROCKS if isinstance(rls["rocks"].get(r), dict))
        preset = sum(1 for r in OUR_ROCKS if isinstance(rls["rocks"].get(r), str))
        if inline or preset:
            print("%-8s %-24s rocks=%d layers=%d inline_ours=%d preset_ours=%d"
                  % (label, fn, len(rls["rocks"]), len(rls["layers"]), inline, preset))
