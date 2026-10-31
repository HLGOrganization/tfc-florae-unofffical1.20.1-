"""Vein pass, take three: only reference ore blocks that actually exist.

The previous pass produced `tfcflorae:ore/diamond/red_granite` etc.  Those blocks do not
exist - our ore set only covers twelve ores (amethyst, bituminous_coal, borax, cinnabar,
cryolite, graphite, gypsum, halite, lignite, ruby, sulfur, sylvite) plus the poor/normal/rich
metal variants.  Diamond, emerald, pyrite, lapis_lazuli and saltpeter were never ported onto
our rocks, so a vein entry referencing them fails to resolve and TFC reports it as
"Not a JSON object: <id>", which is a misleading message for a missing block.

This pass checks every (ore, rock) pair against the blockstate files we actually ship and
skips the pair when missing.  If a vein ends up with no usable pair it is left alone.
"""
import json
import os
import re
import shutil
import zipfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "src", "main", "resources")
BS = os.path.join(RES, "assets", "tfcflorae", "blockstates")
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


def has_ore(ore, rock):
    return os.path.isfile(os.path.join(BS, "ore", ore, rock + ".json"))


tfe = zipfile.ZipFile(TFE_JAR)

vein_dir = os.path.join(RES, "data", "tfc", "worldgen", "configured_feature", "vein")
if os.path.isdir(vein_dir):
    shutil.rmtree(vein_dir)

veins = sorted(n for n in tfe.namelist()
               if "configured_feature/vein/" in n and n.endswith(".json"))

written = 0
entries = 0
skipped = []
missing_pairs = {}

for name in veins:
    data = json.loads(tfe.read(name).decode("utf-8"))
    blocks = data.get("config", {}).get("blocks")
    if not isinstance(blocks, list) or not blocks:
        skipped.append((name, "no blocks"))
        continue

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
            m = ORE_RE.match(str(w.get("block", "")))
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

    # drop ore kinds we do not have for ANY of our rocks
    usable = [(w, o) for (w, o) in weights if any(has_ore(o, r) for r in OUR_ROCKS)]
    if not usable:
        skipped.append((name, "none of its ores exist on our rocks (" +
                        ",".join(sorted({o for _, o in weights})) + ")"))
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
                missing_pairs[(ore, rock)] = True
                continue
            item = {"block": "tfcflorae:ore/%s/%s" % (ore, rock)}
            if weight is not None:
                item = {"weight": weight, "block": "tfcflorae:ore/%s/%s" % (ore, rock)}
            withs.append(item)
        if not withs:
            continue
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
for s in skipped:
    print("   %-32s %s" % (s[0].split("/")[-1], s[1]))

# final check: every tfcflorae ore id referenced must exist
bad = []
for dirpath, _d, files in os.walk(vein_dir):
    for fn in files:
        d = json.load(open(os.path.join(dirpath, fn), encoding="utf-8"))
        for b in d.get("config", {}).get("blocks", []):
            for w in b.get("with", []):
                bid = w.get("block") if isinstance(w, dict) else w
                if not isinstance(bid, str):
                    bad.append((fn, "not a string", bid))
                    continue
                if not bid.startswith("tfcflorae:"):
                    continue                      # TFC/TFE's own entry, not ours to check
                parts = bid.split("/")
                if len(parts) != 3 or not parts[0].endswith(":ore"):
                    bad.append((fn, "malformed", bid))
                    continue
                ore, rock = parts[1], parts[2]
                if not has_ore(ore, rock):
                    bad.append((fn, "missing block", bid))
print()
print("dangling ore references in generated veins:", len(bad))
for b in bad[:10]:
    print("   ", b)
