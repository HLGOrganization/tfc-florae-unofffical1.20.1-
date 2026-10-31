#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
果树精简：按用户确认的 A 方案删除资源。

默认 dry-run，只打印计划；加 --apply 才真删。

铁律：
  * 只用精确名字列表 + 路径边界判断（`<name>.json` 或 `<name>_*`），禁止裸子串匹配。
  * 删注册内容 = 代码 + 资源 + 标签 三件套（代码已改，这里处理资源与标签）。
  * 写入一律无 BOM、UTF-8、LF 保持原样。
"""
import argparse
import json
import os
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "src", "main", "resources")

# ── 精确名单 ────────────────────────────────────────────────────────────────
# 删掉整套 wood/* 木材形态（保留 plant/* 果树形态）
WOOD_DELETE = [
    "hawthorn", "jabuticabeira", "maclura", "medlar", "mulberry", "pear",
    "persimmon", "quince", "sorb", "syzygium", "yew",   # 11 种纯果树
    "sloe", "rowan",                                    # 整树种删除
]
# 删掉 plant/* 果树形态（joshua 保留木材形态）
FRUIT_TREE_DELETE = ["joshua", "sloe", "rowan"]
# 删掉食物物品
FOOD_DELETE = ["sloe", "rowan_berry"]

def bn(p):
    return os.path.basename(p)

def name_match(path, name, exts=(".json", ".png", ".mcmeta")):
    """路径基名等于 `<name><ext>` 或以 `<name>_` 开头 —— 有边界，不是裸子串。"""
    b = bn(path)
    for ext in exts:
        if b == name + ext:
            return True
    return b.startswith(name + "_")

def walk(*parts):
    base = os.path.join(RES, *parts)
    if not os.path.isdir(base):
        return
    for dp, _, fs in os.walk(base):
        for f in fs:
            yield os.path.join(dp, f)

def rel(p):
    return os.path.relpath(p, RES).replace("\\", "/")

def collect():
    plan = {"files": [], "dirs": [], "ids": set()}

    # 1) 13 个树种的 wood/* 资源（blockstate / 模型 / 贴图 / 掉落表）
    for sp in WOOD_DELETE:
        for p in walk("assets", "tfcflorae", "blockstates", "wood"):
            if name_match(p, sp):
                plan["files"].append(p)
                # 由 blockstate 路径反推方块 id：assets/tfcflorae/blockstates/<id>.json
                rid = rel(p)[len("assets/tfcflorae/blockstates/"):-len(".json")]
                plan["ids"].add("tfcflorae:" + rid)
        for sub in (("assets", "tfcflorae", "models", "block", "wood"),
                    ("assets", "tfcflorae", "models", "item", "wood"),
                    ("assets", "tfcflorae", "textures", "block", "wood"),
                    ("assets", "tfcflorae", "textures", "item", "wood"),
                    ("data", "tfcflorae", "loot_tables", "blocks", "wood")):
            for p in walk(*sub):
                if name_match(p, sp):
                    plan["files"].append(p)
        # 树结构 NBT（只被已失效的 tree configured_feature 引用）
        for suffix in ("", "_dead", "_large"):
            d = os.path.join(RES, "data", "tfcflorae", "structures", sp + suffix)
            if os.path.isdir(d):
                plan["dirs"].append(d)
        # 已失效的树木地物（从未被任何群系放置）
        for sub in (("data", "tfcflorae", "worldgen", "configured_feature", "tree"),
                    ("data", "tfcflorae", "worldgen", "placed_feature", "tree")):
            for p in walk(*sub):
                if name_match(p, sp):
                    plan["files"].append(p)

    # 2) joshua / sloe / rowan 的 plant/* 果树形态
    for sp in FRUIT_TREE_DELETE:
        for sub in (("assets", "tfcflorae", "blockstates", "plant"),
                    ("assets", "tfcflorae", "models", "block", "plant"),
                    ("assets", "tfcflorae", "models", "item", "plant"),
                    ("assets", "tfcflorae", "textures", "block", "fruit_tree"),
                    ("data", "tfcflorae", "loot_tables", "blocks", "plant")):
            for p in walk(*sub):
                if name_match(p, sp):
                    plan["files"].append(p)
                    rid = rel(p)
                    for pre in ("assets/tfcflorae/blockstates/", "data/tfcflorae/loot_tables/blocks/"):
                        if rid.startswith(pre):
                            plan["ids"].add("tfcflorae:" + rid[len(pre):-len(".json")])
        # 花盆
        for p in walk("assets", "tfcflorae", "blockstates", "plant", "potted"):
            if bn(p) == sp + "_sapling.json":
                plan["files"].append(p)
                plan["ids"].add("tfcflorae:plant/potted/" + sp + "_sapling")
        # 果树气候范围 + 果树地物
        for relp in ("data/tfcflorae/tfc/climate_ranges/plant/%s_tree.json" % sp,
                     "data/tfcflorae/worldgen/configured_feature/plant/%s.json" % sp,
                     "data/tfcflorae/worldgen/placed_feature/plant/%s.json" % sp):
            p = os.path.join(RES, relp.replace("/", os.sep))
            if os.path.isfile(p):
                plan["files"].append(p)

    # 3) 食物物品
    for fid in FOOD_DELETE:
        for sub in (("assets", "tfcflorae", "models", "item", "food"),
                    ("assets", "tfcflorae", "textures", "item", "food")):
            for p in walk(*sub):
                if bn(p).startswith(fid + "."):
                    plan["files"].append(p)
        plan["ids"].add("tfcflorae:food/" + fid)

    plan["files"] = sorted(set(plan["files"]))
    plan["dirs"] = sorted(set(plan["dirs"]))
    return plan

def scan_tags(ids):
    """返回 {标签文件: 命中的 id 列表}"""
    hits = {}
    base = os.path.join(RES, "data")
    for dp, _, fs in os.walk(base):
        if os.sep + "tags" + os.sep not in dp + os.sep:
            continue
        for f in fs:
            if not f.endswith(".json"):
                continue
            p = os.path.join(dp, f)
            try:
                txt = open(p, encoding="utf-8").read()
            except Exception:
                continue
            found = [i for i in ids if '"' + i + '"' in txt]
            if found:
                hits[rel(p)] = sorted(found)
    return hits

def scan_recipes(ids):
    hits = {}
    base = os.path.join(RES, "data")
    for dp, _, fs in os.walk(base):
        if os.sep + "recipes" + os.sep not in dp + os.sep:
            continue
        for f in fs:
            if not f.endswith(".json"):
                continue
            p = os.path.join(dp, f)
            try:
                txt = open(p, encoding="utf-8").read()
            except Exception:
                continue
            found = [i for i in ids if '"' + i + '"' in txt]
            if found:
                hits[rel(p)] = sorted(found)
    return hits

def scan_lang(ids):
    """lang 键由 id 推出：block.tfcflorae.<path> / item.tfcflorae.<path>"""
    hits = {}
    keys = set()
    for i in ids:
        path = i.split(":", 1)[1]
        for pre in ("block.", "item."):
            keys.add(pre + "tfcflorae." + path.replace("/", "."))
    for p in walk("assets", "tfcflorae", "lang"):
        if not bn(p).endswith(".json"):
            continue
        try:
            data = json.load(open(p, encoding="utf-8"))
        except Exception:
            continue
        found = sorted(k for k in data if k in keys)
        if found:
            hits[rel(p)] = found
    return hits

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--apply", action="store_true")
    a = ap.parse_args()

    plan = collect()
    ids = plan["ids"]
    tags = scan_tags(ids)
    recipes = scan_recipes(ids)
    langs = scan_lang(ids)

    print("=" * 78)
    print("模式: %s" % ("APPLY（真删）" if a.apply else "DRY-RUN（只看）"))
    print("=" * 78)
    print("待删文件      : %d" % len(plan["files"]))
    print("待删目录      : %d" % len(plan["dirs"]))
    print("涉及方块/物品 id: %d" % len(ids))
    print("命中标签文件  : %d（需清理引用）" % len(tags))
    print("命中配方文件  : %d（需删除）" % len(recipes))
    print("命中 lang 文件: %d（需删键）" % len(langs))
    print()

    by_top = {}
    for p in plan["files"]:
        parts = rel(p).split("/")
        k = "/".join(parts[:4])
        by_top[k] = by_top.get(k, 0) + 1
    print("--- 待删文件按目录分布 ---")
    for k in sorted(by_top):
        print("  %-56s %d" % (k, by_top[k]))
    print()

    print("--- 待删目录（树结构 NBT）---")
    for d in plan["dirs"]:
        n = sum(len(fs) for _, _, fs in os.walk(d))
        print("  %-56s %d 个文件" % (rel(d), n))
    print()

    if tags:
        print("--- 标签引用（前 12）---")
        for k in sorted(tags)[:12]:
            print("  %-60s %s" % (k, tags[k][:3]))
        print()
    if recipes:
        print("--- 配方（前 12）---")
        for k in sorted(recipes)[:12]:
            print("  %-60s %s" % (k, recipes[k][:2]))
        print()
    if langs:
        print("--- lang（前 8）---")
        for k in sorted(langs)[:8]:
            print("  %-40s %d 个键" % (k, len(langs[k])))
        print()

    if not a.apply:
        print("DRY-RUN 结束，未改动任何文件。")
        return

    # ── 执行 ────────────────────────────────────────────────────────────
    removed = 0
    for p in plan["files"]:
        if os.path.isfile(p):
            os.remove(p)
            removed += 1
    for d in plan["dirs"]:
        for dp, _, fs in os.walk(d, topdown=False):
            for f in fs:
                os.remove(os.path.join(dp, f))
                removed += 1
            os.rmdir(dp)

    # 配方：整份删掉
    for r in recipes:
        os.remove(os.path.join(RES, r.replace("/", os.sep)))
        removed += 1

    # 标签：只移除精确 id 字符串，文件保留
    for t, found in tags.items():
        p = os.path.join(RES, t.replace("/", os.sep))
        raw = open(p, "rb").read()
        txt = raw.decode("utf-8")
        data = json.loads(txt)
        if isinstance(data.get("values"), list):
            data["values"] = [v for v in data["values"] if v not in ids]
        out = json.dumps(data, indent=2, ensure_ascii=False) + "\n"
        open(p, "wb").write(out.encode("utf-8"))

    # lang：删键
    for t, found in langs.items():
        p = os.path.join(RES, t.replace("/", os.sep))
        data = json.loads(open(p, encoding="utf-8").read())
        for k in found:
            data.pop(k, None)
        out = json.dumps(data, indent=2, ensure_ascii=False) + "\n"
        open(p, "wb").write(out.encode("utf-8"))

    print("已删除文件 %d 个；清理标签 %d 个、配方 %d 个、lang %d 个。"
          % (removed, len(tags), len(recipes), len(langs)))

if __name__ == "__main__":
    main()
