"""Point recipes at the tags this mod actually defines.

The ported recipes ask for `tfc:<wood>_logs` (TFC's own per-species log tag), but
those species are this mod's woods - TFC has no tag for them, so every plank /
support / lumber recipe requiring them silently did nothing.  This mod does ship
`tfcflorae:<wood>_logs` for all 66 species, so rewrite the references.

Also handles `tfc:all_bamboo_logs`, which TFC does not define either.
"""
import io
import json
import os
import re

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATA = os.path.join(ROOT, "src", "main", "resources", "data")
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae")

woods = sorted(f[:-5] for f in os.listdir(os.path.join(ASSETS, "blockstates", "wood", "planks")) if f.endswith(".json"))
logs_dir = os.path.join(DATA, "tfcflorae", "tags", "blocks")

# tags we can point at
ours = {w for w in os.listdir(logs_dir) if w.endswith("_logs.json")}
ours = {w[:-len("_logs.json")] for w in ours}
print("tfcflorae *_logs tags available:", len(ours))

auto = []
for w in woods:
    if w in ours:
        auto.append((r'"tag"\s*:\s*"tfc:%s_logs"' % re.escape(w), '"tag": "tfcflorae:%s_logs"' % w))
if "all_bamboo" in ours:
    auto.append((r'"tag"\s*:\s*"tfc:all_bamboo_logs"', '"tag": "tfcflorae:all_bamboo_logs"'))
print("rewrite rules:", len(auto))

changed = []
for dirpath, _d, files in os.walk(DATA):
    for fn in files:
        if not fn.endswith(".json"):
            continue
        full = os.path.join(dirpath, fn)
        if "/tags/" in full.replace(os.sep, "/"):
            continue
        txt = io.open(full, encoding="utf-8-sig").read()
        new = txt
        for pat, rep in auto:
            new = re.sub(pat, rep, new)
        if new != txt:
            io.open(full, "w", encoding="utf-8", newline="\n").write(new)
            changed.append(os.path.relpath(full, DATA))

print("files rewritten:", len(changed))
for c in changed[:8]:
    print("   ", c)

# report anything still pointing at tfc:<our wood>_logs
left = {}
pat = re.compile(r'"tag"\s*:\s*"tfc:([a-z_]+)_logs"')
for dirpath, _d, files in os.walk(DATA):
    for fn in files:
        if not fn.endswith(".json"):
            continue
        full = os.path.join(dirpath, fn)
        if "/tags/" in full.replace(os.sep, "/"):
            continue
        txt = io.open(full, encoding="utf-8-sig").read()
        for m in pat.finditer(txt):
            if m.group(1) in woods:
                left[m.group(1)] = left.get(m.group(1), 0) + 1
print("remaining tfc:<our wood>_logs refs:", len(left))
