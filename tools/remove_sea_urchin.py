"""Remove the sea urchin groundcover block entirely.

Covers: the enum value in TFCFGroundcoverBlockType, its blockstate/model/item model/loot
table, every tag entry that references it (our own tags plus the tfc:* tags this port
contributes to), and the lang keys.
"""
import glob
import io
import json
import os
import re

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "src", "main", "resources")
JAVA = os.path.join(ROOT, "src", "main", "java", "tfcflorae")
ID = "tfcflorae:groundcover/sea_urchin"

# ---- 1) java enum value ---------------------------------------------------
p = os.path.join(JAVA, "common", "blocks", "TFCFGroundcoverBlockType.java")
s = io.open(p, encoding="utf-8").read()
# enum constants look like:  SEA_URCHIN(...),
m = re.search(r'\n[ \t]*SEA_URCHIN\([^;]*?\),', s, re.S)
if m:
    s = s[:m.start()] + s[m.end():]
    io.open(p, "w", encoding="utf-8", newline="\n").write(s)
    print("removed SEA_URCHIN enum value")
else:
    print("!! SEA_URCHIN enum value not matched")

# ---- 2) assets ------------------------------------------------------------
for rel in ("blockstates/groundcover/sea_urchin.json",
            "models/block/groundcover/sea_urchin.json",
            "models/item/groundcover/sea_urchin.json",
            "textures/block/groundcover/sea_urchin.png"):
    f = os.path.join(RES, "assets", "tfcflorae", rel)
    if os.path.isfile(f):
        os.remove(f)
        print("deleted asset", rel)

# ---- 3) loot table --------------------------------------------------------
lt = os.path.join(RES, "data", "tfcflorae", "loot_tables", "blocks", "groundcover", "sea_urchin.json")
if os.path.isfile(lt):
    os.remove(lt)
    print("deleted loot table")

# ---- 4) tag entries -------------------------------------------------------
tag_files = glob.glob(os.path.join(RES, "data", "**", "tags", "**", "*.json"), recursive=True)
changed = 0
for f in tag_files:
    try:
        d = json.load(io.open(f, encoding="utf-8-sig"))
    except Exception:
        continue
    vals = d.get("values")
    if not isinstance(vals, list):
        continue
    before = len(vals)
    d["values"] = [v for v in vals if not (isinstance(v, str) and v == ID)]
    if len(d["values"]) != before:
        io.open(f, "w", encoding="utf-8", newline="\n").write(json.dumps(d, indent=2) + "\n")
        changed += 1
        print("cleaned tag:", os.path.relpath(f, RES))
print("tag files changed:", changed)

# ---- 5) lang --------------------------------------------------------------
for name in ("en_us.json", "zh_cn.json"):
    f = os.path.join(RES, "assets", "tfcflorae", "lang", name)
    d = json.load(io.open(f, encoding="utf-8-sig"))
    drop = [k for k in d if "sea_urchin" in k]
    for k in drop:
        del d[k]
    if drop:
        io.open(f, "w", encoding="utf-8", newline="\n").write(
            json.dumps(d, indent=2, ensure_ascii=False) + "\n")
    print(name, "removed", len(drop), "keys")

# ---- 6) leftovers ---------------------------------------------------------
left = []
for dirpath, _d, files in os.walk(RES):
    for fn in files:
        if "sea_urchin" in fn:
            left.append(os.path.relpath(os.path.join(dirpath, fn), RES))
print("remaining files with sea_urchin in name:", left if left else "none")
