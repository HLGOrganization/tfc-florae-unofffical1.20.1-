"""Strip dropped woods/rocks out of TFC data files' `ingredient` arrays.

These files are one object per file:

  {"ingredient": [{"item": "tfc:ore/normal_bismuthinite/arkose"}, ...],
   "heat_capacity": 2.857, ...}

so the entries live inside an array rather than as object keys.  If the array becomes empty
the file is deleted, otherwise TFC would fail to load a data entry with no ingredients.
"""
import json
import os
import re

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "src", "main", "resources")
NAMES = ["yellow_jacaranda", "white_mangrove", "white_cherry_blossom", "whitebeam",
         "kauri", "hazel", "brazilwood", "beech", "arkose", "cataclasite", "sandstone"]
PATS = [re.compile(r'(?<![a-z0-9])(?:[a-z0-9_]*_)?' + re.escape(n) + r'(?:_[a-z0-9_]+)*(?![a-z0-9])')
        for n in NAMES]


def hits(text):
    return any(p.search(text) for p in PATS)


stripped = 0
emptied = []
for dirpath, _dirs, files in os.walk(os.path.join(RES, "data")):
    for fn in files:
        if not fn.endswith(".json"):
            continue
        p = os.path.join(dirpath, fn)
        try:
            d = json.load(open(p, encoding="utf-8-sig"))
        except Exception:
            continue
        if not isinstance(d, dict):
            continue
        ing = d.get("ingredient")
        if not isinstance(ing, list):
            continue
        kept = [e for e in ing if not hits(json.dumps(e))]
        if len(kept) == len(ing):
            continue
        if not kept:
            os.remove(p)
            emptied.append(os.path.relpath(p, RES).replace(os.sep, "/"))
            continue
        d["ingredient"] = kept
        with open(p, "w", encoding="utf-8", newline="\n") as fh:
            fh.write(json.dumps(d, indent=2) + "\n")
        stripped += len(ing) - len(kept)

print("ingredient entries stripped:", stripped)
print("files deleted because the ingredient list emptied:", len(emptied))
for e in emptied[:10]:
    print("   ", e)

# final content sweep
left = []
for dirpath, _dirs, files in os.walk(RES):
    if os.sep + "lang" + os.sep in dirpath:
        continue
    for fn in files:
        if not fn.endswith(".json"):
            continue
        p = os.path.join(dirpath, fn)
        try:
            t = open(p, encoding="utf-8", errors="ignore").read()
        except Exception:
            continue
        if hits(t):
            left.append(os.path.relpath(p, RES).replace(os.sep, "/"))
print("files still referencing a dropped name:", len(left))
for l in left[:10]:
    print("   ", l)
