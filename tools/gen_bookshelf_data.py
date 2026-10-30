"""Generate loot tables and recipes for the new per-wood chiseled bookshelves.

The old 1.18 ids are gone, so their data files were orphans.  Each new shelf
(wood/planks/<wood>_bookshelf) needs a loot table (drops itself) and a crafting recipe
(vanilla's chiseled bookshelf shape: 6 planks + 3 books).

Also removes the orphaned data files of the old bookshelf ids.
"""
import glob
import io
import json
import os
import shutil

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATA = os.path.join(ROOT, "src", "main", "resources", "data", "tfcflorae")
A = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae")

woods = sorted(f[:-5] for f in os.listdir(os.path.join(A, "blockstates", "wood", "planks"))
               if f.endswith(".json") and not f.endswith("_bookshelf.json"))

# ---- drop old bookshelf data (advancements / recipes / loot tables / tags) --------
removed = 0
for sub in ("advancements", "recipes", "loot_tables", "tags"):
    for path in glob.glob(os.path.join(DATA, sub, "**", "*bookshelf*"), recursive=True):
        if "chiseled_bookshelf" in path:
            continue
        if os.path.isdir(path):
            shutil.rmtree(path)
        else:
            os.remove(path)
        removed += 1
print("old bookshelf data files removed:", removed)

# ---- loot tables -------------------------------------------------------------
loot_dir = os.path.join(DATA, "loot_tables", "blocks", "wood", "planks")
os.makedirs(loot_dir, exist_ok=True)
n = 0
for w in woods:
    ident = "wood/planks/%s_bookshelf" % w
    table = {
        "type": "minecraft:block",
        "pools": [{
            "rolls": 1,
            "entries": [{"type": "minecraft:item", "name": "tfcflorae:" + ident}],
            "conditions": [{"condition": "minecraft:survives_explosion"}],
        }],
    }
    io.open(os.path.join(loot_dir, w + "_bookshelf.json"), "w", encoding="utf-8", newline="\n").write(
        json.dumps(table, indent=2) + "\n")
    n += 1
print("loot tables written:", n)

# ---- recipes (vanilla chiseled bookshelf shape) ------------------------------
rec_dir = os.path.join(DATA, "recipes", "crafting", "wood", "bookshelf")
os.makedirs(rec_dir, exist_ok=True)
n = 0
for w in woods:
    plank = "tfcflorae:wood/planks/" + w
    recipe = {
        "type": "minecraft:crafting_shaped",
        "pattern": ["PPP", "BBB", "PPP"],
        "key": {
            "P": {"item": plank},
            "B": {"item": "minecraft:book"},
        },
        "result": {"item": "tfcflorae:wood/planks/%s_bookshelf" % w, "count": 1},
    }
    io.open(os.path.join(rec_dir, w + "_bookshelf.json"), "w", encoding="utf-8", newline="\n").write(
        json.dumps(recipe, indent=2) + "\n")
    n += 1
print("recipes written:", n)
