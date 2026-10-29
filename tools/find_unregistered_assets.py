"""Find ids that have an asset but are NOT registered, using family consistency.

`sandstone/layered/slab/gray` has a blockstate but was never registered (only 7 of
the 12 colours exist), and that single id killed the whole minecraft:slabs tag -
which is why it "disappeared" in game.

Heuristic: an id with assets that the source audit does not know about is
  * suspicious  -> if some of its siblings (same parent folder) ARE in the audit,
                   then the folder is partially registered and this one is dead
  * audit gap   -> if none of its siblings are in the audit, the whole family was
                   missed by the parser (ores, ceramics...) and we keep it

Output: reports/suspect-unregistered.json (list of ids to never put in a tag)
"""
import io
import json
import os
from collections import defaultdict

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae")
AUDIT = os.path.join(ROOT, "reports", "registered-ids.json")
OUT = os.path.join(ROOT, "reports", "suspect-unregistered.json")

audit = json.load(io.open(AUDIT, encoding="utf-8-sig"))
known = set(audit["blocks"]) | set(audit["items"])

# every id that has an asset on disk
asset_blocks = set()
for dirpath, _d, files in os.walk(os.path.join(ASSETS, "blockstates")):
    for fn in files:
        if fn.endswith(".json"):
            rel = os.path.relpath(os.path.join(dirpath, fn), os.path.join(ASSETS, "blockstates"))
            asset_blocks.add(rel.replace(os.sep, "/")[:-5])
asset_items = set()
for dirpath, _d, files in os.walk(os.path.join(ASSETS, "models", "item")):
    for fn in files:
        if fn.endswith(".json"):
            rel = os.path.relpath(os.path.join(dirpath, fn), os.path.join(ASSETS, "models", "item"))
            asset_items.add(rel.replace(os.sep, "/")[:-5])

def families(ids):
    fam = defaultdict(list)
    for i in ids:
        fam["/".join(i.split("/")[:-1])].append(i)
    return fam

suspects = set()
gaps = 0
for pool in (asset_blocks, asset_items):
    for parent, members in families(pool).items():
        inside = [m for m in members if m in known]
        outside = [m for m in members if m not in known]
        if not outside:
            continue
        if inside:
            suspects.update(outside)
        else:
            gaps += len(outside)

print("asset ids:", len(asset_blocks), "blockstates /", len(asset_items), "item models")
print("known (audit):", len(known))
print("suspect unregistered (family partially registered):", len(suspects))
print("kept as audit gap (whole family unknown):", gaps)
from collections import Counter
c = Counter("/".join(s.split("/")[:2]) for s in suspects)
for k, v in c.most_common(15):
    print("   %-34s %d" % (k, v))

io.open(OUT, "w", encoding="utf-8", newline="\n").write(
    json.dumps(sorted(suspects), indent=1, ensure_ascii=False) + "\n")
print("wrote", OUT)
