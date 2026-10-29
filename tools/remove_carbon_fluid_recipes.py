"""Delete every recipe that melts/casts/blast-furnaces the removed carbon fluids.

Fluid ids were tfcflorae:ore/<name> (registered as "ore/" + SimpleFluids id), so:
  heating/ore/<name>            item -> molten fluid
  casting/ore/<mold>/<name>     molten fluid -> item
  blast_furnace/coke/<name>     molten coal -> molten coke
All of them point at fluids that no longer exist.
"""
import io
import json
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RECIPES = os.path.join(ROOT, "src", "main", "resources", "data", "tfcflorae", "recipes")

NAMES = ["anthracite", "bituminous_coal", "charcoal", "coke", "lignite"]
MOLDS = ["clay", "earthenware", "stoneware"]

targets = []
for n in NAMES:
    targets.append(("heating/ore/%s.json" % n, None))
for m in MOLDS:
    for n in NAMES:
        targets.append(("casting/ore/%s/%s.json" % (m, n), None))
for n in ["anthracite", "bituminous_coal", "charcoal", "lignite"]:
    targets.append(("blast_furnace/coke/%s.json" % n, None))

removed, kept = [], []
for rel, _ in targets:
    p = os.path.join(RECIPES, *rel.split("/"))
    if not os.path.isfile(p):
        kept.append(rel + " (absent)")
        continue
    d = json.load(io.open(p, encoding="utf-8-sig"))
    txt = json.dumps(d)
    if "tfcflorae:ore/" not in txt:
        kept.append(rel + " (no fluid ref)")
        continue
    os.remove(p)
    removed.append(rel)

print("removed:", len(removed))
for r in removed:
    print("   ", r)
print("skipped:", len(kept))
for k in kept:
    print("   ", k)


def prune_empty(root):
    n = 0
    for dirpath, dirnames, files in os.walk(root, topdown=False):
        if not os.listdir(dirpath):
            os.rmdir(dirpath)
            n += 1
    return n


print("empty dirs removed:", prune_empty(RECIPES))
