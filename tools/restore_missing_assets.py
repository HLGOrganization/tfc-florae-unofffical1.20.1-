"""Restore blockstates / item models that orphan-cleanup deleted by mistake.

clean_orphan_assets.py ran with the then-current whitelist; several families (the
ceramic bricks in particular) only appear in the runtime dump, so their assets were
treated as orphans and removed - leaving blocks and items with no model (the pink
and black checkerboard in game).

This lists every whitelisted block/item whose blockstate or item model is missing,
then restores those files from a chosen git revision.
"""
import json
import os
import subprocess
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
A = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae")
FULL = os.path.join(ROOT, "reports", "registered-ids-full.json")
REV = sys.argv[1] if len(sys.argv) > 1 else "c267acc8e"
APPLY = "--apply" in sys.argv

wl = json.load(open(FULL, encoding="utf-8-sig"))
blocks = set(wl["blocks"])
items = set(wl["items"])

def repo_path(rel):
    return "src/main/resources/assets/tfcflorae/" + rel

missing = []
for b in sorted(blocks):
    if not os.path.isfile(os.path.join(A, "blockstates", *b.split("/")) + ".json"):
        missing.append(("blockstates/%s.json" % b, "block"))
for i in sorted(items):
    p = os.path.join(A, "models", "item", *i.split("/")) + ".json"
    if not os.path.isfile(p):
        missing.append(("models/item/%s.json" % i, "item"))

print("whitelist blocks:", len(blocks), "items:", len(items))
print("missing blockstates:", sum(1 for _r, k in missing if k == "block"))
print("missing item models:", sum(1 for _r, k in missing if k == "item"))
for rel, kind in missing[:20]:
    print("   ", kind, rel)

if not APPLY:
    print("\n(dry run - pass --apply <rev> to restore)")
    raise SystemExit

ok = fail = 0
for rel, _kind in missing:
    rp = repo_path(rel)
    res = subprocess.run(["git", "checkout", REV, "--", rp], cwd=ROOT,
                         stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
    if res.returncode == 0:
        ok += 1
    else:
        fail += 1
print("\nrestored:", ok, " not in revision:", fail)
