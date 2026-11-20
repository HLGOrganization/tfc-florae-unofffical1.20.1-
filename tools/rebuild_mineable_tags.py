# -*- coding: utf-8 -*-
"""按「已注册 id 全集」重建 data/minecraft/tags/blocks/mineable/{shovel,pickaxe,axe,hoe}.json

判定「已注册」的三条证据（**不看 blockstate 是否存在**）：
  1. reports/expander-ids.json —— tools/audit_register_assets.py 从**当前源码**展开注册循环得到的 id 全集
     （`python tools/audit_register_assets.py --dump-ids reports/expander-ids.json` 生成）
  2. reports/runtime-registries.json —— 开发环境里跑出来的真实 Forge 注册表转储（旧版，可能含已删内容）
  3. VERIFIED_SITES —— 源码里能定位到、但展开器解析不了的注册点（`ore/<ore>/<rock>` 这类
     `register("ore/" + ore.name() + "/" + rock.name())` 字符串拼接），用正则 + 枚举值校验

规则：alive = (1) ∪ ((2) ∩ (3)) − DEAD_WOODS。
即「转储里有、但当前源码已找不到注册点」的 id 会被剔除 —— 这正是历史上
`clay/humus`、`dry_grass/bog_iron`、`mud_bricks/humus_*` 三次事故的形态。

用法：
  python tools/rebuild_mineable_tags.py            # 只报告
  python tools/rebuild_mineable_tags.py --apply    # 写回文件
"""
from __future__ import annotations

import argparse
import io
import json
import os
import re

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "src", "main", "resources")
TAG_DIR = os.path.join(RES, "data", "minecraft", "tags", "blocks", "mineable")
EXPANDER = os.path.join(ROOT, "reports", "expander-ids.json")
DUMP = os.path.join(ROOT, "reports", "runtime-registries.json")
TAGS = ["shovel", "pickaxe", "axe", "hoe"]

# 已彻底删除的 22 种木材（TFCFWood 里已不存在的常量）
DEAD_WOODS = {
    "rubber_fig", "nordmann_fir", "laurel", "buxus", "butternut", "fever", "white_ipe", "purple_ipe",
    "black_willow", "locust", "mountain_ash", "limba", "african_padauk", "white_cedar", "sweetgum",
    "red_elm", "hornbeam", "joshua", "bald_cypress", "white_elm", "pink_cherry_blossom", "common_oak",
}

# 源码里存在、但展开器解析不了的注册点（字符串拼接型）。每条 = (正则, 说明, 源码位置)
TFCF_ROCKS = ("andesite basalt boninite carbonatite chalk chert claystone conglomerate dacite diorite "
              "dolomite gabbro gneiss granite jaspillite laterite limestone marble mudstone phyllite "
              "porphyry quartzite red_granite rhyolite schist shale slate soapstone tuff wackestone").split()
FRUIT_TREES = ("hawthorn jabuticabeira maclura medlar mulberry pear persimmon quince sorb syzygium yew").split()
CLAYS = ("earthenware stoneware kaolinite").split()

VERIFIED_SITES = [
    (re.compile(r"^ore/(?!.*//)([a-z0-9_]+)/(%s)$" % "|".join(TFCF_ROCKS)),
     "TFCFBlocks.java:386 mapOfKeys(Ore/TFCFOre × Rock) -> register(\"ore/\" + ore + \"/\" + rock)"),
    (re.compile(r"^plant/(%s)_(growing_branch|branch)$" % "|".join(FRUIT_TREES)),
     "TFCFBlocks.java:840,842 fruitTreeBlocks(\"plant/%s_branch\" / \"plant/%s_growing_branch\")"),
    (re.compile(r"^ceramic/(%s)/(clay_block|bricks|brick_slab|brick_stairs|brick_wall)$" % "|".join(CLAYS)),
     "TFCFBlocks.java:1142-1189 register(\"ceramic/\" + clay + \"/...\")"),
]


def load_sources():
    exp = set(json.load(io.open(EXPANDER, encoding="utf-8"))["blocks"])
    raw = json.load(io.open(DUMP, encoding="utf-8-sig"))
    dump = {x.split(":", 1)[1] for x in raw["block_states"] if x.startswith("tfcflorae:")}
    return exp, dump


def dead_wood(path: str) -> str | None:
    seg = path.split("/")
    for s in seg:
        if s in DEAD_WOODS:
            return s
        # <wood>_bookshelf / <wood>_large / <wood>_tall / <wood>_entry 这类带后缀的
        for w in DEAD_WOODS:
            if s.endswith("_" + w) or s.startswith(w + "_"):
                return w
    return None


def verified_site(path: str) -> str | None:
    for rx, why in VERIFIED_SITES:
        if rx.match(path):
            return why
    return None


def classify(path, exp, dump):
    if dead_wood(path):
        return "dead", "已删除的木材 %s" % dead_wood(path)
    if path in exp:
        return "ok", "源码展开器"
    if path in dump:
        why = verified_site(path)
        if why:
            return "ok", "转储 + 源码注册点：" + why
        return "dead", "只在旧转储里，当前源码找不到注册点"
    why = verified_site(path)
    if why:
        return "ok", "源码注册点（展开器漏解析）：" + why
    return "dead", "两份证据里都没有"


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--apply", action="store_true")
    args = ap.parse_args()

    exp, dump = load_sources()
    print("源码展开器 %d 个 id；注册转储 %d 个 id" % (len(exp), len(dump)))

    # 自检：三次事故里的 id 必须被判死
    selftest = ["clay/humus", "dry_grass/bog_iron", "mud_bricks/humus_0", "mud_bricks/humus"]
    bad = [x for x in selftest if classify(x, exp, dump)[0] != "dead"]
    print("自检（历史事故 id 必须判死）: %s" % ("通过" if not bad else "失败 " + str(bad)))

    total_removed = 0
    for tag in TAGS:
        p = os.path.join(TAG_DIR, tag + ".json")
        d = json.load(open(p, encoding="utf-8"))
        vals = [v["id"] if isinstance(v, dict) else v for v in d["values"]]
        keep, removed = [], []
        for v in vals:
            if not v.startswith("tfcflorae:"):
                keep.append(v)                       # 其它命名空间一律不动
                continue
            verdict, why = classify(v.split(":", 1)[1], exp, dump)
            (keep if verdict == "ok" else removed).append((v, why) if verdict != "ok" else v)
        dupes = len(keep) - len(set(keep))
        print("\n== mineable/%s：原 %d 条 → 保留 %d（其中本模组 %d）｜ 剔除 %d ｜ 重复 %d"
              % (tag, len(vals), len(keep), sum(1 for x in keep if x.startswith("tfcflorae:")),
                 len(removed), dupes))
        for v, why in removed:
            print("   ✗ %-46s %s" % (v, why))
        total_removed += len(removed)
        if args.apply:
            out = {"replace": False, "values": keep}
            open(p, "w", encoding="utf-8", newline="\n").write(
                json.dumps(out, indent=2, ensure_ascii=False) + "\n")
    print("\n合计剔除 %d 条%s" % (total_removed, "" if args.apply else "（未写回，加 --apply 生效）"))


if __name__ == "__main__":
    main()
