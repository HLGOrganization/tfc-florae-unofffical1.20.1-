"""Generate the missing item models for wood supports and wall signs.

These 198 items were registered but had no models/item file, so they showed up as
the pink/black checkerboard.  The block models exist, so the item model is just a
parent pointer at them (same approach TFC uses for its own woods).
"""
import io
import json
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
A = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae")

woods = sorted(f[:-5] for f in os.listdir(os.path.join(A, "blockstates", "wood", "planks"))
               if f.endswith(".json"))

# item id -> block model path
def block_model(item_id):
    parts = item_id.split("/")
    if parts[1] in ("horizontal_support", "vertical_support"):
        kind = "horizontal" if parts[1] == "horizontal_support" else "vertical"
        return "tfcflorae:block/wood/support/%s_%s" % (parts[2], kind)
    if parts[1] == "wall_sign":
        return "tfcflorae:block/wood/sign/%s" % parts[2]
    return None

made = 0
skipped = []
for kind in ("horizontal_support", "vertical_support", "wall_sign"):
    for w in woods:
        item_id = "wood/%s/%s" % (kind, w)
        out = os.path.join(A, "models", "item", "wood", kind, w + ".json")
        bm = block_model(item_id)
        if bm is None:
            skipped.append(item_id)
            continue
        # the block model must exist, otherwise we would just move the problem
        rel = bm.split(":", 1)[1]
        if not os.path.isfile(os.path.join(A, "models", rel + ".json")):
            skipped.append(item_id + " (no block model)")
            continue
        os.makedirs(os.path.dirname(out), exist_ok=True)
        io.open(out, "w", encoding="utf-8", newline="\n").write(
            json.dumps({"parent": bm}, indent=2) + "\n")
        made += 1

print("item models written:", made)
print("skipped:", len(skipped), skipped[:5])
