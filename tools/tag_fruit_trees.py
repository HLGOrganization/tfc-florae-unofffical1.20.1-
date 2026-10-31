# -*- coding: utf-8 -*-
"""把果树方块挂进对应的 tag。

TFC 自己的果树会进这些 tag，我们的复刻组必须同样进去，否则：
 - tfc:fruit_tree_branch 不在 -> FruitTreeSaplingBlock.createTree 判不出「种在枝干上」
 - minecraft:mineable/axe 不在 -> 枝干挖不动 / 掉落不对
 - minecraft:leaves|saplings 不在 -> 部分模组与合成判据失效
"""
import json
import os

RES = "src/main/resources"
NS = "tfcflorae"
TREES = ["hawthorn", "jabuticabeira", "joshua", "maclura", "medlar", "mulberry",
         "pear", "persimmon", "quince", "rowan", "sloe", "sorb", "syzygium", "yew"]

changed = []


def add(path, values):
    full = os.path.join(RES, path)
    if os.path.exists(full):
        with open(full, "r", encoding="utf-8") as f:
            data = json.load(f)
    else:
        data = {"replace": False, "values": []}
    cur = data.setdefault("values", [])
    flat = {v if isinstance(v, str) else json.dumps(v, sort_keys=True) for v in cur}
    added = []
    for v in values:
        key = v if isinstance(v, str) else json.dumps(v, sort_keys=True)
        if key not in flat:
            cur.append(v)
            flat.add(key)
            added.append(v)
    if added:
        os.makedirs(os.path.dirname(full), exist_ok=True)
        with open(full, "w", encoding="utf-8", newline="\n") as f:
            json.dump(data, f, indent=2, ensure_ascii=False)
            f.write("\n")
        changed.append((path, len(added)))


def main():
    branches = ["%s:plant/%s_%s" % (NS, t, s) for t in TREES for s in ("branch", "growing_branch")]
    leaves = ["%s:plant/%s_leaves" % (NS, t) for t in TREES]
    saplings = ["%s:plant/%s_sapling" % (NS, t) for t in TREES]

    add("data/minecraft/tags/blocks/mineable/axe.json", branches)
    add("data/minecraft/tags/blocks/leaves.json", leaves)
    add("data/minecraft/tags/blocks/saplings.json", saplings)
    add("data/minecraft/tags/items/leaves.json", leaves)
    add("data/minecraft/tags/items/saplings.json", saplings)
    add("data/tfc/tags/blocks/fruit_tree_branch.json", branches)
    add("data/tfc/tags/blocks/fruit_tree_leaves.json", leaves)
    add("data/tfc/tags/blocks/fruit_tree_sapling.json", saplings)
    add("data/tfc/tags/items/fruit_tree_leaves.json", leaves)

    for p, n in changed:
        print("  +%-3d %s" % (n, p))
    print("共改动 %d 个 tag 文件" % len(changed))


if __name__ == "__main__":
    main()
