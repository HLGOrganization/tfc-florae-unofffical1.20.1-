"""Purge every remaining trace of anthracite from resources and data."""
import io
import json
import os
import shutil

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "src", "main", "resources")
DATA = os.path.join(RES, "data")
ASSETS = os.path.join(RES, "assets", "tfcflorae")

removed_files = []

# 1) loot tables / models for the ore blocks
for base, sub in ((DATA, "tfcflorae/loot_tables/blocks/ore/anthracite"),
                  (ASSETS, "models/item/ore/anthracite")):
    d = os.path.join(base, *sub.split("/"))
    if os.path.isdir(d):
        removed_files.extend(os.listdir(d))
        shutil.rmtree(d)

for rel in ("models/item/ore/anthracite.json",):
    fp = os.path.join(ASSETS, *rel.split("/"))
    if os.path.isfile(fp):
        os.remove(fp)
        removed_files.append(rel)

# 2) tag files that exist only for anthracite
for dp, _d, fs in os.walk(DATA):
    if "/tags/" not in dp.replace(os.sep, "/"):
        continue
    for fn in fs:
        if fn != "ores" and fn != "anthracite.json":
            continue
        fp = os.path.join(dp, fn)
        if fn == "anthracite.json" and "ores" in dp.split(os.sep):
            os.remove(fp)
            removed_files.append(fp)

# 3) strip anthracite ids from every remaining tag
stripped = 0
for dp, _d, fs in os.walk(DATA):
    if "/tags/" not in dp.replace(os.sep, "/"):
        continue
    for fn in fs:
        if not fn.endswith(".json"):
            continue
        fp = os.path.join(dp, fn)
        try:
            d = json.load(io.open(fp, encoding="utf-8-sig"))
        except Exception:
            continue
        vals = d.get("values")
        if not isinstance(vals, list):
            continue
        keep = []
        for v in vals:
            ident = v.get("id") if isinstance(v, dict) else v
            if isinstance(ident, str) and ("ore/anthracite" in ident or "powder/anthracite" in ident):
                stripped += 1
                continue
            keep.append(v)
        if len(keep) != len(vals):
            if not keep:
                os.remove(fp)
                continue
            d["values"] = keep
            io.open(fp, "w", encoding="utf-8", newline="\n").write(
                json.dumps(d, indent=2, ensure_ascii=False) + "\n")

print("removed files/dirs entries:", len(removed_files))
print("tag ids stripped:", stripped)

# 4) prune now-empty dirs
n = 0
for base in (DATA, ASSETS):
    for dp, _d, _f in os.walk(base, topdown=False):
        if not os.listdir(dp):
            os.rmdir(dp)
            n += 1
print("empty dirs removed:", n)
