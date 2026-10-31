"""Regenerate the vein overrides, fixing two mistakes from the first attempt.

1. `weight` was written as JSON null when the TFC template had no weight field.
   `with` entries are sometimes just {"block": ...} with weight defaulting implicitly;
   `null` is not the same as absent and the codec rejects it.  Now weight is only emitted
   when the template actually carries one.

2. Dike / gravel / halite veins were treated as ore veins.  Their `with` is a *rock* block
   (`tfc:rock/raw/diorite`), not an ore (`tfc:ore/diamond/gabbro`), so deriving an ore name
   from them produced nonsense like `tfcflorae:ore/raw/soapstone`.  A vein is only eligible
   when **every** `with.block` of its template matches `tfc:ore/<ore>/<rock>`.

Files written by the previous run are deleted first so nothing stale survives.
"""
import json
import os
import re
import shutil
import zipfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "src", "main", "resources")
TFE_JAR = os.path.join(r"D:\线框的服务器\versions\TFC_Wild_Fire\mods",
                       "Wildfire_TerraFirmaEarth-1.20.1-forge-2.3.0.jar")

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

ORE_RE = re.compile(r'^tfc:ore/([a-z0-9_]+)/[a-z0-9_]+$')

tfe = zipfile.ZipFile(TFE_JAR)

# start clean: drop everything we generated before
vein_dir = os.path.join(RES, "data", "tfc", "worldgen", "configured_feature", "vein")
if os.path.isdir(vein_dir):
    shutil.rmtree(vein_dir)
print("cleared previous vein output")

veins = sorted(n for n in tfe.namelist()
               if "configured_feature/vein/" in n and n.endswith(".json"))

written = 0
entries = 0
skipped = []
for name in veins:
    data = json.loads(tfe.read(name).decode("utf-8"))
    cfg = data.get("config", {})
    blocks = cfg.get("blocks")
    if not isinstance(blocks, list) or not blocks:
        skipped.append((name, "no blocks"))
        continue

    # categories this vein targets
    cats = set()
    for entry in blocks:
        for rep in entry.get("replace", []):
            if isinstance(rep, str):
                rock = rep.rsplit("/", 1)[-1]
                if rock in TFC_CATEGORY:
                    cats.add(TFC_CATEGORY[rock])
    if not cats:
        skipped.append((name, "no rock targets"))
        continue

    # find a template whose `with` is purely ore blocks
    weights = None
    for entry in blocks:
        withs = entry.get("with")
        if not isinstance(withs, list) or not withs:
            continue
        cand = []
        ok = True
        for w in withs:
            if not isinstance(w, dict):
                ok = False
                break
            block = str(w.get("block", ""))
            m = ORE_RE.match(block)
            if not m:
                ok = False
                break
            cand.append((w.get("weight"), m.group(1)))
        if ok and cand:
            weights = cand
            break
    if weights is None:
        skipped.append((name, "template with[] is not ore-based"))
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
        for weight, ore in weights:
            item = {"block": "tfcflorae:ore/%s/%s" % (ore, rock)}
            if weight is not None:                 # never emit "weight": null
                item = {"weight": weight, "block": "tfcflorae:ore/%s/%s" % (ore, rock)}
            withs.append(item)
        blocks.append({"replace": ["tfcflorae:rock/raw/%s" % rock], "with": withs})
        added += 1

    if added:
        p = os.path.join(RES, name.replace("/", os.sep))
        os.makedirs(os.path.dirname(p), exist_ok=True)
        with open(p, "w", encoding="utf-8", newline="\n") as fh:
            fh.write(json.dumps(data, indent=2) + "\n")
        written += 1
        entries += added

print("veins written: %d | rock entries: %d" % (written, entries))
print("skipped: %d" % len(skipped))
for s in skipped[:12]:
    print("   %-30s %s" % (s[0].split("/")[-1], s[1]))

# sanity: no null weight anywhere, no bare strings inside `with`
bad = []
for dirpath, _d, files in os.walk(vein_dir):
    for fn in files:
        p = os.path.join(dirpath, fn)
        d = json.load(open(p, encoding="utf-8"))
        for b in d.get("config", {}).get("blocks", []):
            for w in b.get("with", []):
                if not isinstance(w, dict):
                    bad.append((fn, "bare string in with"))
                elif "weight" in w and w["weight"] is None:
                    bad.append((fn, "null weight"))
print()
print("structural problems in generated veins:", len(bad))
for b in bad[:8]:
    print("   ", b)
