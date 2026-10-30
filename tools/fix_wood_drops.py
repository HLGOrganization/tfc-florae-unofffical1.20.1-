"""Make `wood` blocks drop `log` instead of themselves (and stripped_wood -> stripped_log).

`wood` is the six-sided bark block; TFC's own loot table drops wood/wood/<wood>, but in
this pack wood is only ever a crafting intermediate, so dropping the log keeps the log
economy intact.  Same treatment for stripped variants.
"""
import io
import json
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATA = os.path.join(ROOT, "src", "main", "resources", "data", "tfcflorae", "loot_tables", "blocks", "wood")

PAIRS = [
    ("wood", "log"),
    ("stripped_wood", "stripped_log"),
]

changed = 0
for src_kind, dst_kind in PAIRS:
    d = os.path.join(DATA, src_kind)
    if not os.path.isdir(d):
        print("missing dir:", src_kind)
        continue
    for fn in sorted(os.listdir(d)):
        if not fn.endswith(".json"):
            continue
        p = os.path.join(d, fn)
        table = json.load(io.open(p, encoding="utf-8-sig"))
        wood = fn[:-5]
        old = "tfcflorae:wood/%s/%s" % (src_kind, wood)
        new = "tfcflorae:wood/%s/%s" % (dst_kind, wood)

        def walk(node):
            n = 0
            if isinstance(node, dict):
                if node.get("name") == old:
                    node["name"] = new
                    n += 1
                for v in node.values():
                    n += walk(v)
            elif isinstance(node, list):
                for v in node:
                    n += walk(v)
            return n

        if walk(table):
            io.open(p, "w", encoding="utf-8", newline="\n").write(json.dumps(table, indent=2) + "\n")
            changed += 1

print("loot tables changed:", changed)
