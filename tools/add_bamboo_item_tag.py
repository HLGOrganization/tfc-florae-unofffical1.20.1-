"""Create the missing ITEM tag tfcflorae:all_bamboo_logs.

Recipes reference {"tag": "tfcflorae:all_bamboo_logs"}, and in 1.20.1 a recipe tag is
matched against ITEM tags (data/<ns>/tags/items/...).  Only a block tag had been created
(data/<ns>/tags/blocks/...), which does nothing for recipes - hence "tag does not exist".

The bamboo block and its BlockItem share the same id, so the item tag lists the same ids.
Both tags are kept: the block one for block tags, the item one for recipes.
"""
import io
import json
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATA = os.path.join(ROOT, "src", "main", "resources", "data", "tfcflorae", "tags")

BLOCK_TAG = os.path.join(DATA, "blocks", "all_bamboo_logs.json")
ITEM_TAG = os.path.join(DATA, "items", "all_bamboo_logs.json")

blocks = json.load(io.open(BLOCK_TAG, encoding="utf-8-sig"))
values = blocks["values"]
print("source block tag values:", len(values))

# an item tag needs the same ids (BlockItem id == block id for these)
os.makedirs(os.path.dirname(ITEM_TAG), exist_ok=True)
item_tag = {"replace": False, "values": list(values)}
io.open(ITEM_TAG, "w", encoding="utf-8", newline="\n").write(
    json.dumps(item_tag, indent=2) + "\n")
print("wrote", os.path.relpath(ITEM_TAG, ROOT))

# sanity: every id must have a blockstate and an item model
A = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae")
missing = []
for v in values:
    ident = v.split(":", 1)[1]
    rel = ident.replace("/", os.sep)
    if not os.path.isfile(os.path.join(A, "blockstates", rel + ".json")):
        missing.append(ident + " (blockstate)")
    if not os.path.isfile(os.path.join(A, "models", "item", rel + ".json")):
        missing.append(ident + " (item model)")
print("ids lacking assets:", missing if missing else "none")
