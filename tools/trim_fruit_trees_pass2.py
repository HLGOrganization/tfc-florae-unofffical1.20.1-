#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
果树精简 · 第二轮：补清理纯物品 id 的标签引用 + 已失效的 advancements。

第一轮只按 blockstate 反推 id，漏掉了没有 blockstate 的纯物品：
  wood/lumber, wood/boat, wood/support, wood/sign, wood/chest_minecart, wood/bookshelf
以及引用已删配方的 advancements。

默认 dry-run，加 --apply 才真改。
"""
import argparse
import json
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "src", "main", "resources")

WOOD_DELETE = [
    "hawthorn", "jabuticabeira", "maclura", "medlar", "mulberry", "pear",
    "persimmon", "quince", "sorb", "syzygium", "yew", "sloe", "rowan",
]
FRUIT_TREE_DELETE = ["joshua", "sloe", "rowan"]
FOOD_DELETE = ["sloe", "rowan_berry"]


def rel(p):
    return os.path.relpath(p, RES).replace("\\", "/")


def subdirs(*parts):
    b = os.path.join(RES, *parts)
    if not os.path.isdir(b):
        return []
    return sorted(d for d in os.listdir(b) if os.path.isdir(os.path.join(b, d)))


def build_ids():
    """完整构造已删 id 集合：方块类型 + 纯物品类型。

    例外：`wood/leaf/mulberry` 是**纯物品**桑叶（TFCFItems.MULBERRY_LEAVES，喂蚕用），
    与木材形态无关，必须保留，所以整体排除 `wood/leaf/*`。
    """
    bs_types = subdirs("assets", "tfcflorae", "blockstates", "wood")
    item_types = [t for t in subdirs("assets", "tfcflorae", "models", "item", "wood") if t != "leaf"]
    ids = set()
    for t in WOOD_DELETE:
        for ty in set(bs_types) | set(item_types):
            ids.add("tfcflorae:wood/%s/%s" % (ty, t))
        ids.add("tfcflorae:wood/planks/%s_bookshelf" % t)
    for t in FRUIT_TREE_DELETE:
        for suf in ("_sapling", "_growing_branch", "_branch", "_leaves"):
            ids.add("tfcflorae:plant/%s%s" % (t, suf))
        ids.add("tfcflorae:plant/potted/%s_sapling" % t)
    for f in FOOD_DELETE:
        ids.add("tfcflorae:food/" + f)
    return ids


def scan_lang(ids):
    keys = set()
    for i in ids:
        path = i.split(":", 1)[1]
        for pre in ("block.", "item."):
            keys.add(pre + "tfcflorae." + path.replace("/", "."))
    hits = {}
    for dp, _, fs in os.walk(os.path.join(RES, "assets", "tfcflorae", "lang")):
        for f in fs:
            if not f.endswith(".json"):
                continue
            p = os.path.join(dp, f)
            data = json.loads(open(p, encoding="utf-8").read())
            found = sorted(k for k in data if k in keys)
            if found:
                hits[rel(p)] = found
    return hits


def scan(ids, subpath):
    hits = {}
    base = os.path.join(RES, "data")
    for dp, _, fs in os.walk(base):
        if subpath not in (dp + os.sep):
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


def name_match(path, name):
    b = os.path.basename(path)
    return b == name + ".json" or b.startswith(name + "_")


def collect_advancements():
    out = []
    base = os.path.join(RES, "data", "tfcflorae", "advancements")
    if not os.path.isdir(base):
        return out
    for dp, _, fs in os.walk(base):
        for f in fs:
            if not f.endswith(".json"):
                continue
            p = os.path.join(dp, f)
            if any(name_match(p, t) for t in WOOD_DELETE):
                out.append(p)
    return sorted(set(out))


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--apply", action="store_true")
    a = ap.parse_args()

    ids = build_ids()
    tags = scan(ids, os.sep + "tags" + os.sep)
    advs = collect_advancements()
    langs = scan_lang(ids)

    print("=" * 78)
    print("第二轮 · 模式: %s" % ("APPLY" if a.apply else "DRY-RUN"))
    print("=" * 78)
    print("构造 id 总数        : %d" % len(ids))
    print("仍引用已删 id 的标签: %d" % len(tags))
    print("待删 advancements   : %d" % len(advs))
    print("待清理 lang 文件    : %d" % len(langs))
    print()
    for k in sorted(langs):
        print("   %-58s %d 个键" % (k, len(langs[k])))
    print()
    if tags:
        print("--- 标签（全部）---")
        for k in sorted(tags):
            print("   %-58s %d 处" % (k, len(tags[k])))
        print()
    if advs:
        by_dir = {}
        for p in advs:
            d = os.path.dirname(rel(p))
            by_dir[d] = by_dir.get(d, 0) + 1
        print("--- advancements 按目录 ---")
        for d in sorted(by_dir):
            print("   %-58s %d" % (d, by_dir[d]))
        print()

    if not a.apply:
        print("DRY-RUN 结束。")
        return

    for t in tags:
        p = os.path.join(RES, t.replace("/", os.sep))
        data = json.loads(open(p, encoding="utf-8").read())
        if isinstance(data.get("values"), list):
            data["values"] = [v for v in data["values"] if v not in ids]
        open(p, "wb").write((json.dumps(data, indent=2, ensure_ascii=False) + "\n").encode("utf-8"))

    for t, found in langs.items():
        p = os.path.join(RES, t.replace("/", os.sep))
        data = json.loads(open(p, encoding="utf-8").read())
        for k in found:
            data.pop(k, None)
        open(p, "wb").write((json.dumps(data, indent=2, ensure_ascii=False) + "\n").encode("utf-8"))

    for p in advs:
        os.remove(p)

    print("已清理标签 %d 个、lang %d 个、删除 advancements %d 个。"
          % (len(tags), len(langs), len(advs)))


if __name__ == "__main__":
    main()
