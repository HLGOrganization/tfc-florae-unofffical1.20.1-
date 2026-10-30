"""Second pass: loosen the matcher so underscore-joined names are caught too.

The first pass only matched a name standing completely alone, so files like
`rock/boulder/arkose_mossy.json`, `recipes/landslide/arkose_cobble.json` and
`tfc/fuels/beech_log.json` survived.

This pass:
  1. deletes any file whose PATH carries the name as a token (underscore separators allowed)
  2. removes tag members referencing such ids
  3. removes lang keys mentioning them
  4. strips entries from TFC data files (item_heats, panning, sluicing, fuels, item_sizes,
     supports) whose keys/values reference a dropped rock or wood
  5. prunes recipes that reference a dropped block in their ingredients/result
"""
import json
import os
import re

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "src", "main", "resources")
WOODS = ["yellow_jacaranda", "white_mangrove", "white_cherry_blossom", "whitebeam",
         "kauri", "hazel", "brazilwood", "beech"]
ROCKS = ["arkose", "cataclasite", "sandstone"]
NAMES = WOODS + ROCKS

PATTERNS = [re.compile(r'(?<![a-z0-9])(?:[a-z0-9_]*_)?' + re.escape(n) + r'(?:_[a-z0-9_]+)*(?![a-z0-9])')
            for n in NAMES]


def hits(text):
    return any(p.search(text) for p in PATTERNS)


def name_of(text):
    for n, p in zip(NAMES, PATTERNS):
        if p.search(text):
            return n
    return None


# ---- 1) delete files ------------------------------------------------------
per = {}
deleted = 0
for dirpath, _dirs, files in os.walk(RES):
    for fn in files:
        full = os.path.join(dirpath, fn)
        rel = os.path.relpath(full, RES)
        if os.sep + "lang" + os.sep in rel:
            continue
        n = name_of(rel)
        if n:
            os.remove(full)
            per[n] = per.get(n, 0) + 1
            deleted += 1
print("files deleted (pass 2):", deleted)
for n in NAMES:
    if per.get(n):
        print("   %-22s %d" % (n, per[n]))

# ---- 2) prune empty dirs --------------------------------------------------
pruned = 0
for _ in range(10):
    for dirpath, dirs, files in os.walk(RES, topdown=False):
        if not dirs and not files:
            os.rmdir(dirpath)
            pruned += 1
print("empty dirs pruned:", pruned)

# ---- 3) tags --------------------------------------------------------------
tag_removed = 0
for dirpath, _dirs, files in os.walk(os.path.join(RES, "data")):
    if os.sep + "tags" + os.sep not in dirpath:
        continue
    for fn in files:
        if not fn.endswith(".json"):
            continue
        p = os.path.join(dirpath, fn)
        try:
            d = json.load(open(p, encoding="utf-8-sig"))
        except Exception:
            continue
        vals = d.get("values")
        if not isinstance(vals, list):
            continue
        kept = [v for v in vals if not hits(v if isinstance(v, str) else json.dumps(v))]
        if len(kept) != len(vals):
            d["values"] = kept
            open(p, "w", encoding="utf-8", newline="\n").write(json.dumps(d, indent=2) + "\n")
            tag_removed += len(vals) - len(kept)
print("tag entries removed (pass 2):", tag_removed)

# ---- 4) lang --------------------------------------------------------------
for langfile in ("en_us.json", "zh_cn.json"):
    p = os.path.join(RES, "assets", "tfcflorae", "lang", langfile)
    if not os.path.isfile(p):
        continue
    d = json.load(open(p, encoding="utf-8-sig"))
    drop = [k for k in d if hits(k)]
    for k in drop:
        del d[k]
    if drop:
        open(p, "w", encoding="utf-8", newline="\n").write(
            json.dumps(d, indent=2, ensure_ascii=False) + "\n")
    print("%s: removed %d keys, %d left" % (langfile, len(drop), len(d)))

# ---- 5) TFC data files: strip per-entry references ------------------------
DATA_FILES = [
    "data/tfcflorae/tfc/item_heats",
    "data/tfcflorae/tfc/panning",
    "data/tfcflorae/tfc/sluicing",
    "data/tfcflorae/tfc/fuels",
    "data/tfcflorae/tfc/item_sizes",
    "data/tfcflorae/tfc/supports",
    "data/tfcflorae/tfc/item_damage_resistance",
    "data/tfcflorae/tfc/heat_capacity",
]
stripped = 0
for sub in DATA_FILES:
    d0 = os.path.join(RES, sub.replace("/", os.sep))
    if not os.path.isdir(d0):
        continue
    for dirpath, _dirs, files in os.walk(d0):
        for fn in files:
            if not fn.endswith(".json"):
                continue
            p = os.path.join(dirpath, fn)
            try:
                data = json.load(open(p, encoding="utf-8-sig"))
            except Exception:
                continue
            if not isinstance(data, dict):
                continue
            drop = [k for k in data if hits(k)]
            for k in drop:
                del data[k]
            if drop:
                open(p, "w", encoding="utf-8", newline="\n").write(
                    json.dumps(data, indent=2) + "\n")
                stripped += len(drop)
print("tfc data entries stripped:", stripped)

# ---- 6) leftovers ---------------------------------------------------------
left = []
for dirpath, _dirs, files in os.walk(RES):
    for fn in files:
        rel = os.path.relpath(os.path.join(dirpath, fn), RES)
        if os.sep + "lang" + os.sep in rel:
            continue
        if hits(rel):
            left.append(rel)
print("leftover files:", left[:8] if left else "none")
