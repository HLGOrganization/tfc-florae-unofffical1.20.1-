"""Create tfcflorae:all_bamboo_logs and point the 4 bamboo recipes at it.

TFC has no `tfc:all_bamboo_logs`; the port accepted that tag name from 1.18.  The
four bamboo species this mod adds each have their own <wood>_logs tag, so build
the union and rewrite the references.
"""
import io
import json
import os
import re

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATA = os.path.join(ROOT, "src", "main", "resources", "data")
logs_dir = os.path.join(DATA, "tfcflorae", "tags", "blocks")

bamboos = ["bamboo", "blue_bamboo", "dragon_bamboo", "red_bamboo"]
values = []
for b in bamboos:
    p = os.path.join(logs_dir, "%s_logs.json" % b)
    if not os.path.isfile(p):
        print("!! missing tag", p)
        continue
    d = json.load(io.open(p, encoding="utf-8-sig"))
    vals = [v for v in d.get("values", []) if isinstance(v, str)]
    print("%-16s %d values: %s" % (b, len(vals), vals))
    values.extend(vals)

values = sorted(set(values))
out = os.path.join(logs_dir, "all_bamboo_logs.json")
io.open(out, "w", encoding="utf-8", newline="\n").write(
    json.dumps({"replace": False, "values": values}, indent=2, ensure_ascii=False) + "\n")
print("wrote", out, "with", len(values), "values")

# rewrite references
pat = re.compile(r'"tag"\s*:\s*"tfc:all_bamboo_logs"')
changed = []
for dirpath, _d, files in os.walk(DATA):
    for fn in files:
        if not fn.endswith(".json"):
            continue
        full = os.path.join(dirpath, fn)
        if "/tags/" in full.replace(os.sep, "/"):
            continue
        txt = io.open(full, encoding="utf-8-sig").read()
        new = pat.sub('"tag": "tfcflorae:all_bamboo_logs"', txt)
        if new != txt:
            io.open(full, "w", encoding="utf-8", newline="\n").write(new)
            changed.append(os.path.relpath(full, DATA))
print("recipes rewritten:", len(changed), changed)
