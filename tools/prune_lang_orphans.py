"""Find lang keys whose block/item no longer exists.

A key like block.tfcflorae.wood.planks.african_padauk maps to the id
wood/planks/african_padauk.  If that id is neither registered (whitelist) nor backed
by an asset on disk, the entry is an orphan left behind by removed content.

Dry run by default; pass --apply to strip them from both lang files.
"""
import io
import json
import os
import sys
from collections import Counter

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
LANG = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae", "lang")
A = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae")
FULL = os.path.join(ROOT, "reports", "registered-ids-full.json")

APPLY = "--apply" in sys.argv

wl = json.load(io.open(FULL, encoding="utf-8-sig"))
blocks = set(wl["blocks"])
items = set(wl["items"])

en = json.load(io.open(os.path.join(LANG, "en_us.json"), encoding="utf-8-sig"))

def has_asset(ident, kind):
    rel = ident.replace("/", os.sep) + ".json"
    if kind == "block":
        return os.path.isfile(os.path.join(A, "blockstates", rel))
    return os.path.isfile(os.path.join(A, "models", "item", rel))

orphans = []
other = Counter()
for k in en:
    parts = k.split(".")
    if len(parts) >= 3 and parts[0] in ("block", "item") and parts[1] == "tfcflorae":
        kind = parts[0]
        ident = "/".join(parts[2:])
        pool = blocks if kind == "block" else items
        if ident in pool:
            continue
        if has_asset(ident, kind):
            continue
        orphans.append((k, kind, ident))
    else:
        other[".".join(parts[:2])] += 1

print("total lang keys:", len(en))
print("block/item keys:", sum(1 for k in en if k.startswith(("block.tfcflorae.", "item.tfcflorae."))))
print("orphan block/item keys:", len(orphans))
for k, kind, ident in orphans[:25]:
    print("   %-6s %-58s %s" % (kind, ident, k))
print("\nnon block/item key groups:")
for g, n in other.most_common(10):
    print("   %-40s %d" % (g, n))

if APPLY:
    drop = {k for k, _kind, _id in orphans}
    for name in ("en_us.json", "zh_cn.json"):
        p = os.path.join(LANG, name)
        d = json.load(io.open(p, encoding="utf-8-sig"))
        before = len(d)
        d = {k: v for k, v in d.items() if k not in drop}
        io.open(p, "w", encoding="utf-8", newline="\n").write(
            json.dumps(d, indent=2, ensure_ascii=False) + "\n")
        print("%s: %d -> %d (-%d)" % (name, before, len(d), before - len(d)))
else:
    print("\n(dry run - pass --apply to delete)")
