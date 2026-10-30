"""Delete every asset / data file belonging to the dropped woods and rocks.

Exact segment matching only - a name must stand alone (not be part of a longer word), so
`rocky` never matches `rockiest` and `hazel` never matches something like `hazelnut_...`.

Woods: yellow_jacaranda, white_mangrove, white_cherry_blossom, whitebeam,
       kauri, hazel, brazilwood, beech
Rocks: arkose, cataclasite, sandstone   (the rock; the sandstone *block* family is not
                                         touched - it has no assets of its own)
"""
import json
import os
import re
import shutil

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "src", "main", "resources")
WOODS = ["yellow_jacaranda", "white_mangrove", "white_cherry_blossom", "whitebeam",
         "kauri", "hazel", "brazilwood", "beech"]
ROCKS = ["arkose", "cataclasite", "sandstone"]
NAMES = WOODS + ROCKS


def matches(rel):
    for n in NAMES:
        if re.search(r'(?<![\w-])' + n + r'(?![\w-])', rel):
            return n
    return None


# ---- 1) delete files ------------------------------------------------------
per_name = {}
deleted = 0
for dirpath, _dirs, files in os.walk(RES):
    for fn in files:
        full = os.path.join(dirpath, fn)
        rel = os.path.relpath(full, RES)
        n = matches(rel)
        if not n:
            continue
        # never touch the lang files here; they are handled key-by-key below
        if rel.endswith(".json") and os.sep + "lang" + os.sep in rel:
            continue
        os.remove(full)
        per_name[n] = per_name.get(n, 0) + 1
        deleted += 1

print("files deleted:", deleted)
for n in NAMES:
    print("   %-22s %d" % (n, per_name.get(n, 0)))

# ---- 2) prune empty dirs --------------------------------------------------
pruned = 0
for _ in range(10):
    for dirpath, dirs, files in os.walk(RES, topdown=False):
        if not dirs and not files:
            os.rmdir(dirpath)
            pruned += 1
print("empty dirs pruned:", pruned)

# ---- 3) strip tag entries -------------------------------------------------
tag_hits = 0
tag_files = 0
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
        kept = []
        removed = 0
        for v in vals:
            s = v if isinstance(v, str) else json.dumps(v)
            if matches(s):
                removed += 1
                continue
            kept.append(v)
        if removed:
            d["values"] = kept
            open(p, "w", encoding="utf-8", newline="\n").write(json.dumps(d, indent=2) + "\n")
            tag_hits += removed
            tag_files += 1
print("tag entries removed:", tag_hits, "in", tag_files, "files")

# ---- 4) strip lang keys ---------------------------------------------------
lang_removed = 0
for langfile in ("en_us.json", "zh_cn.json"):
    p = os.path.join(RES, "assets", "tfcflorae", "lang", langfile)
    if not os.path.isfile(p):
        continue
    d = json.load(open(p, encoding="utf-8-sig"))
    drop = [k for k in d if matches(k)]
    for k in drop:
        del d[k]
    if drop:
        open(p, "w", encoding="utf-8", newline="\n").write(
            json.dumps(d, indent=2, ensure_ascii=False) + "\n")
    print("%s: removed %d keys, %d left" % (langfile, len(drop), len(d)))
    lang_removed += len(drop)

# ---- 5) leftovers ---------------------------------------------------------
left = []
for dirpath, _dirs, files in os.walk(RES):
    for fn in files:
        rel = os.path.relpath(os.path.join(dirpath, fn), RES)
        if os.sep + "lang" + os.sep in rel:
            continue
        if matches(rel):
            left.append(rel)
print("leftover files:", left[:10] if left else "none")
