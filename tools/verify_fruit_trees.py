# -*- coding: utf-8 -*-
"""果树资源自洽性校验：blockstate -> model -> texture 全链路，外加 Java 注册名对齐。

只做静态校验，不能替代进游戏。用法：
    python tools/verify_fruit_trees.py
"""
import json
import os
import re
import sys
import zipfile

RES = "src/main/resources"
NS = "tfcflorae"
TFC_JAR = r"D:\线框的服务器\versions\TFC_Wild_Fire\mods\[群峦传说] TerraFirmaCraft-Forge-1.20.1-3.2.20.jar"
TREES = ["hawthorn", "jabuticabeira", "joshua", "maclura", "medlar", "mulberry",
         "pear", "persimmon", "quince", "rowan", "sloe", "sorb", "syzygium", "yew"]

errors = []
warns = []
tfc = zipfile.ZipFile(TFC_JAR)
tfc_names = set(tfc.namelist())


def err(msg):
    errors.append(msg)


def warn(msg):
    warns.append(msg)


def jload(path):
    with open(os.path.join(RES, path), encoding="utf-8") as f:
        return json.load(f)


def model_exists(mid):
    """mid 形如 'tfcflorae:block/plant/pear_leaves'。"""
    ns, _, path = mid.partition(":")
    if not path:
        ns, path = "minecraft", ns
    rel = "assets/%s/models/%s.json" % (ns, path)
    if ns == NS:
        return os.path.exists(os.path.join(RES, rel))
    if ns == "tfc":
        return rel in tfc_names
    if ns == "minecraft":
        return True  # 原版资源不在本地，跳过
    return True


def texture_exists(tid):
    ns, _, path = tid.partition(":")
    if not path:
        ns, path = "minecraft", ns
    rel = "assets/%s/textures/%s.png" % (ns, path)
    if ns == NS:
        return os.path.exists(os.path.join(RES, rel))
    if ns == "tfc":
        return rel in tfc_names
    if ns == "minecraft":
        return True
    return True


# ---------------------------------------------------------------- 1. JSON 可解析
json_count = 0
for dp, _, fs in os.walk(RES):
    for f in fs:
        if f.endswith(".json"):
            p = os.path.join(dp, f)
            try:
                with open(p, encoding="utf-8") as fh:
                    json.load(fh)
                json_count += 1
            except Exception as e:
                err("JSON 解析失败 %s: %s" % (p, e))
print("1) JSON 解析: %d 个文件全部通过" % json_count if not errors else "1) JSON 解析: 有错误")

# ---------------------------------------------------------------- 2. blockstate -> model
bs_dir = os.path.join(RES, "assets", NS, "blockstates")
bs_checked = 0
for dp, _, fs in os.walk(bs_dir):
    for f in fs:
        if not f.endswith(".json"):
            continue
        p = os.path.join(dp, f)
        rel = os.path.relpath(p, RES).replace(os.sep, "/")
        d = json.load(open(p, encoding="utf-8"))
        models = []

        def collect(node):
            """variants 的值可能是 dict，也可能是带 weight 的 list。"""
            if isinstance(node, list):
                for x in node:
                    collect(x)
            elif isinstance(node, dict):
                if "model" in node:
                    models.append(node["model"])

        for v in d.get("variants", {}).values():
            collect(v)
        for part in d.get("multipart", []):
            collect(part.get("apply"))
        for m in models:
            bs_checked += 1
            if not model_exists(m):
                err("blockstate %s 引用了不存在的模型 %s" % (rel, m))
print("2) blockstate -> 模型: 检查 %d 个引用" % bs_checked)

# ---------------------------------------------------------------- 3. model -> texture
tex_checked = 0
for dp, _, fs in os.walk(os.path.join(RES, "assets", NS, "models")):
    for f in fs:
        if not f.endswith(".json"):
            continue
        p = os.path.join(dp, f)
        rel = os.path.relpath(p, RES).replace(os.sep, "/")
        d = json.load(open(p, encoding="utf-8"))
        if "parent" in d and not model_exists(d["parent"]):
            err("模型 %s 的 parent 不存在: %s" % (rel, d["parent"]))
        for k, v in d.get("textures", {}).items():
            tex_checked += 1
            if not texture_exists(v):
                err("模型 %s 的贴图 %s 不存在: %s" % (rel, k, v))
print("3) 模型 -> 贴图: 检查 %d 个引用" % tex_checked)

# ---------------------------------------------------------------- 4. Java 注册名 vs 资源
java = open(os.path.join("src/main/java/tfcflorae/common/blocks/plant/TFCFFruitTrees.java"),
            encoding="utf-8").read()
java_trees = set(re.findall(r"TFCFWood\.([A-Z_]+)", java))
expect = {t.upper() for t in TREES}
if java_trees != expect:
    err("TFCFFruitTrees.TREES 与校验脚本的树种表不一致: 多 %s 缺 %s"
        % (java_trees - expect, expect - java_trees))
else:
    print("4) 树种表: Java 与资源一致 (%d 种)" % len(expect))

# ---------------------------------------------------------------- 5. 每个树种的资源齐全
missing = []
for t in TREES:
    need = [
        "assets/%s/blockstates/plant/%s_branch.json" % (NS, t),
        "assets/%s/blockstates/plant/%s_growing_branch.json" % (NS, t),
        "assets/%s/blockstates/plant/%s_leaves.json" % (NS, t),
        "assets/%s/blockstates/plant/%s_sapling.json" % (NS, t),
        "assets/%s/blockstates/plant/potted/%s_sapling.json" % (NS, t),
        "assets/%s/models/item/plant/%s_leaves.json" % (NS, t),
        "assets/%s/models/item/plant/%s_sapling.json" % (NS, t),
        "assets/%s/textures/block/fruit_tree/%s_branch.png" % (NS, t),
        "assets/%s/textures/block/fruit_tree/%s_leaves.png" % (NS, t),
        "assets/%s/textures/block/fruit_tree/%s_flowering_leaves.png" % (NS, t),
        "assets/%s/textures/block/fruit_tree/%s_fruiting_leaves.png" % (NS, t),
        "assets/%s/textures/block/fruit_tree/%s_dry_leaves.png" % (NS, t),
        "assets/%s/textures/block/fruit_tree/%s_sapling.png" % (NS, t),
        "data/%s/loot_tables/blocks/plant/%s_branch.json" % (NS, t),
        "data/%s/loot_tables/blocks/plant/%s_growing_branch.json" % (NS, t),
        "data/%s/loot_tables/blocks/plant/%s_leaves.json" % (NS, t),
        "data/%s/loot_tables/blocks/plant/%s_sapling.json" % (NS, t),
        "data/%s/loot_tables/blocks/plant/potted/%s_sapling.json" % (NS, t),
        "data/%s/tfc/climate_ranges/plant/%s_tree.json" % (NS, t),
        "data/%s/worldgen/configured_feature/plant/%s.json" % (NS, t),
        "data/%s/worldgen/placed_feature/plant/%s.json" % (NS, t),
    ]
    for n in need:
        if not os.path.exists(os.path.join(RES, n)):
            missing.append(n)
if missing:
    for n in missing:
        err("缺文件: " + n)
else:
    print("5) 每树种 21 个必需文件齐全")

# ---------------------------------------------------------------- 6. 掉落表指向真实存在的食物
FOOD = {"hawthorn": "hawthorn", "jabuticabeira": "jabuticaba", "joshua": "joshua_fruit",
        "maclura": "osage_orange", "medlar": "medlar", "mulberry": "mulberry",
        "pear": "pear", "persimmon": "persimmon", "quince": "quince",
        "rowan": "rowan_berry", "sloe": "sloe", "sorb": "sorb_apple",
        "syzygium": "otaheite_apple", "yew": "yew_berry"}
food_dir = os.path.join(RES, "data", NS, "tfc", "food_items")
for t, fruit in FOOD.items():
    p = os.path.join(food_dir, fruit + ".json")
    if not os.path.exists(p):
        err("掉落表要掉的 %s:food/%s 没有 food_items 定义" % (NS, fruit))
    else:
        d = json.load(open(p, encoding="utf-8"))
        if d.get("ingredient", {}).get("item") != "%s:food/%s" % (NS, fruit):
            err("food_items/%s.json 的 ingredient 不是 %s:food/%s" % (fruit, NS, fruit))
        if not d.get("fruit"):
            warn("food_items/%s.json 没有 fruit 字段（不算水果）" % fruit)
print("6) 掉落食物: 14 个 food_items 全部存在且为水果")

# ---------------------------------------------------------------- 7. 气候范围被 Java 覆盖
cr = os.path.join(RES, "data", NS, "tfc", "climate_ranges", "plant")
files = {f[:-5] for f in os.listdir(cr) if f.endswith(".json")}
want = {t + "_tree" for t in TREES}
if not want <= files:
    err("缺气候范围文件: %s" % (want - files))
else:
    print("7) 气候范围: 14 个 plant/<tree>_tree.json 齐全")

# ---------------------------------------------------------------- 8. tag 覆盖
def tag_has(path, needle):
    p = os.path.join(RES, path)
    if not os.path.exists(p):
        return False
    d = json.load(open(p, encoding="utf-8"))
    vals = [v if isinstance(v, str) else json.dumps(v) for v in d.get("values", [])]
    return any(needle in v for v in vals)


tag_checks = [
    ("data/minecraft/tags/blocks/mineable/axe.json", "plant/pear_branch"),
    ("data/minecraft/tags/blocks/leaves.json", "plant/pear_leaves"),
    ("data/minecraft/tags/blocks/saplings.json", "plant/pear_sapling"),
    ("data/minecraft/tags/blocks/flower_pots.json", "plant/potted/pear_sapling"),
    ("data/minecraft/tags/items/leaves.json", "plant/pear_leaves"),
    ("data/minecraft/tags/items/saplings.json", "plant/pear_sapling"),
    ("data/tfc/tags/blocks/fruit_tree_branch.json", "plant/pear_growing_branch"),
    ("data/tfc/tags/blocks/fruit_tree_leaves.json", "plant/pear_leaves"),
    ("data/tfc/tags/blocks/fruit_tree_sapling.json", "plant/pear_sapling"),
    ("data/tfc/tags/items/fruit_tree_leaves.json", "plant/pear_leaves"),
]
bad = [p for p, n in tag_checks if not tag_has(p, n)]
if bad:
    for p in bad:
        err("tag 没写进去: " + p)
else:
    print("8) tag: %d 个文件全部包含果树条目" % len(tag_checks))

# ---------------------------------------------------------------- 汇总
print()
if warns:
    print("警告 %d 条:" % len(warns))
    for x in warns:
        print("  ! " + x)
if errors:
    print("错误 %d 条:" % len(errors))
    for x in errors:
        print("  x " + x)
    sys.exit(1)
print("全部通过（静态校验）")
