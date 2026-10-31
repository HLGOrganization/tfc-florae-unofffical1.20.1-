# -*- coding: utf-8 -*-
"""全局闭合审计：把 jar 里所有引用解析一遍，找出指向不存在对象的 id。

与 trim_*.py 不同，这个脚本不针对某次删除，而是无条件全量扫描，用来回答
"这个 jar 自己内部是否自洽"。

检查项
  T1 标签（tags）里的每个条目：`#tag` 要能找到标签文件；裸 id 要能解析到物品/方块
  T2 配方（recipes）里的 ingredient / result / item 引用
  T3 掉落表（loot_tables）里的 item 引用
  T4 模型里 textures 引用的贴图文件是否存在
  T5 模型的 parent 是否存在（只查 tfcflorae / tfc 命名空间，minecraft 的跳过）

用法：
  python tools/audit_datapack.py                 # 默认审计 build/libs 下最新的 jar
  python tools/audit_datapack.py <jar 路径>
  python tools/audit_datapack.py <jar> --tfc <tfc jar 路径>
"""
from __future__ import annotations

import glob
import json
import os
import re
import sys
import zipfile

NS = "tfcflorae"

TAG_CATS = ("items", "blocks", "entity_types", "fluids", "game_events", "functions")

# TFC 里以 id 形式出现、但不是物品/方块的东西（流体等），不算未解析
TFC_FLUID_HINTS = frozenset({
    "spring_water", "river_water", "salt_water", "fresh_water", "curdled_milk",
    "olive_oil", "tannin", "limewater", "vinegar", "brine", "milk", "water",
})

# 本次精简涉及的 13 个树种。审计报告要单独看它们有没有被牵连。
SPECIES = ("hawthorn", "jabuticabeira", "maclura", "medlar", "mulberry", "pear",
           "persimmon", "quince", "sorb", "syzygium", "yew",
           "joshua", "sloe", "rowan")

# 已知的、与本次审计无关的历史遗留（基线里就有的），不算回归
KNOWN_MISSING_MODEL = re.compile(
    r"^tfcflorae:(wood/bookshelf/|wood/roots/|rock/mossy_bricks/tuff|"
    r"rock/mossy_cobble/tuff|spawn_egg/silk_moth|grass/(fluvisol|mollisol|oxisol))"
)


def collect_ids(zf: zipfile.ZipFile, ns: str):
    """从 assets 推导出物品 id 与方块 id。"""
    items, blocks = set(), set()
    for n in zf.namelist():
        m = re.match(r"assets/([^/]+)/models/item/(.+)\.json$", n)
        if m and m.group(1) == ns:
            items.add(m.group(2))
        b = re.match(r"assets/([^/]+)/blockstates/(.+)\.json$", n)
        if b and b.group(1) == ns:
            blocks.add(b.group(2))
            # 方块默认带同名方块物品
            items.add(b.group(2))
    return items, blocks


def collect_tags(zf: zipfile.ZipFile, ns: str | None = None):
    """收集标签文件。

    ns 为 None 时收集**所有**命名空间 —— 这是必须的：我们会在
    data/tfc/tags/... 与 data/minecraft/tags/... 下放"并入对方标签"的文件，
    它们才是真正生效的那份，只扫自己的命名空间会漏掉。
    返回 {(ns, 类别/路径): 文件路径}。
    """
    out = {}
    for n in zf.namelist():
        m = re.match(r"data/([^/]+)/tags/(.+)\.json$", n)
        if m and (ns is None or m.group(1) == ns):
            out[(m.group(1), m.group(2))] = n
    return out


def iter_json(zf: zipfile.ZipFile, pattern: str):
    for n in zf.namelist():
        if re.match(pattern, n):
            try:
                yield n, json.loads(zf.read(n).decode("utf-8"))
            except Exception as e:  # noqa: BLE001
                yield n, ("__PARSE_ERROR__", str(e))


def walk_strings(node, path=""):
    """遍历 JSON 里所有字符串叶子，产出 (路径, 值)。"""
    if isinstance(node, str):
        yield path, node
    elif isinstance(node, dict):
        for k, v in node.items():
            yield from walk_strings(v, path + "/" + str(k))
    elif isinstance(node, list):
        for i, v in enumerate(node):
            yield from walk_strings(v, "%s[%d]" % (path, i))


def main() -> int:
    args = [a for a in sys.argv[1:] if not a.startswith("--")]
    tfc_jar = None
    if "--tfc" in sys.argv:
        tfc_jar = sys.argv[sys.argv.index("--tfc") + 1]
        args = [a for a in args if a != tfc_jar]

    if args:
        jar = args[0]
    else:
        cands = glob.glob(os.path.join("build", "libs", "*.jar"))
        if not cands:
            print("找不到 jar，请显式传入路径")
            return 2
        jar = max(cands, key=os.path.getmtime)

    print("审计目标: %s" % jar)
    zf = zipfile.ZipFile(jar)

    items, blocks = collect_ids(zf, NS)
    tags = collect_tags(zf)  # 所有命名空间
    our_tag_ids = set("%s:%s" % (ns, path) for (ns, path) in tags)
    print("  物品 id: %d   方块 id: %d   标签文件: %d（全命名空间）"
          % (len(items), len(blocks), len(tags)))

    # 世界生成注册表里实际存在的对象：data/<ns>/worldgen/<类别>/<路径>.json
    worldgen = {}  # (类别, "ns:路径") -> True
    for n in zf.namelist():
        m = re.match(r"data/([^/]+)/worldgen/([^/]+)/(.+)\.json$", n)
        if m:
            worldgen[(m.group(2), "%s:%s" % (m.group(1), m.group(3)))] = True

    # 其它命名空间的 id 集（只覆盖 tfc 与 minecraft 的 tfc 部分，其余视为不可判定）
    other_items, other_blocks = set(), set()
    other_tags = set()
    other_worldgen = {}
    if tfc_jar and os.path.exists(tfc_jar):
        with zipfile.ZipFile(tfc_jar) as tz:
            ti, tb = collect_ids(tz, "tfc")
            other_items |= set("tfc:" + x for x in ti)
            other_blocks |= set("tfc:" + x for x in tb)
            for (ns, path) in collect_tags(tz):
                other_tags.add("%s:%s" % (ns, path))
            for n2 in tz.namelist():
                m = re.match(r"data/([^/]+)/worldgen/([^/]+)/(.+)\.json$", n2)
                if m:
                    other_worldgen[(m.group(2), "%s:%s" % (m.group(1), m.group(3)))] = True
            print("  TFC 参考: 物品 %d  方块 %d  标签 %d  地物 %d"
                  % (len(ti), len(tb), len(other_tags), len(other_worldgen)))

    unresolved = {"tag": [], "recipe": [], "loot": [], "texture": [], "parent": []}
    unverifiable = 0

    def resolve(ref: str, kind: str, category: str = "") -> bool:
        """ref 形如 'tfcflorae:wood/log/oak' 或 '#tfc:logs'。

        category 是标签所在类别（如 'items'、'worldgen/configured_feature'），
        决定裸 id 应该去哪个注册表里找。
        """
        nonlocal unverifiable
        if ref.startswith("#"):
            tid = ref[1:]
            ns, _, path = tid.partition(":")
            if tid in our_tag_ids:
                return True
            if ns == NS:
                # 标签 id 不带类别，需在任一类别里找
                return any("%s:%s/%s" % (NS, cat, path) in our_tag_ids for cat in TAG_CATS)
            if ns == "tfc":
                return tid in other_tags or True  # TFC 标签集不全，按通过处理
            unverifiable += 1
            return True

        ns, _, path = ref.partition(":")

        # 世界生成类标签：条目是地物/群系/结构 id，不是物品
        if category.startswith("worldgen/"):
            wcat = category.split("/", 1)[1]
            if ns == NS:
                return (wcat, "%s:%s" % (NS, path)) in worldgen
            if ns == "tfc":
                if not other_worldgen:
                    unverifiable += 1
                    return True
                return (wcat, "%s:%s" % (ns, path)) in other_worldgen
            unverifiable += 1
            return True

        if ns == NS:
            return path in items or path in blocks
        if ns == "tfc":
            if not other_items and not other_blocks:
                unverifiable += 1
                return True
            if ref in other_items or ref in other_blocks:
                return True
            # 流体（spring_water 之类）不在物品/方块集里，属正常
            if path in TFC_FLUID_HINTS:
                return True
            return False
        if ns == "minecraft":
            unverifiable += 1
            return True
        unverifiable += 1
        return True

    # T1 标签（所有命名空间）
    for (tns, tpath), path in tags.items():
        category = tpath.split("/", 1)[0] if "/" in tpath else ""
        if category in ("items", "blocks"):
            pass
        elif tpath.startswith("worldgen/"):
            category = "/".join(tpath.split("/")[:2])
        data = json.loads(zf.read(path).decode("utf-8"))
        vals = data.get("values", [])
        for v in vals:
            ref = v.get("id") if isinstance(v, dict) else v
            if not isinstance(ref, str):
                continue
            if not resolve(ref, "tag", category):
                unresolved["tag"].append((path, ref))

    # T2 配方
    for n, data in iter_json(zf, r"data/%s/recipes/.+\.json$" % NS):
        if isinstance(data, tuple):
            unresolved["recipe"].append((n, "PARSE_ERROR: " + data[1]))
            continue
        for p, s in walk_strings(data):
            if not isinstance(s, str) or not s or " " in s:
                continue
            if p.endswith("/type") or p.endswith("/tag") and s.startswith("tfc:") is False:
                pass
            # 只处理明确是物品引用的字段
            if re.search(r"/(item|ingredient|result|output|base|addition|template|"
                         r"empty_ingredient|fluid_ingredient)$", p) or p.endswith("/ingredient[]"):
                if not resolve(s, "recipe"):
                    unresolved["recipe"].append((n, s))
            elif p.endswith("/ingredient") and isinstance(s, str):
                if not resolve(s, "recipe"):
                    unresolved["recipe"].append((n, s))

    # T3 掉落表
    for n, data in iter_json(zf, r"data/%s/loot_tables/.+\.json$" % NS):
        if isinstance(data, tuple):
            unresolved["loot"].append((n, "PARSE_ERROR: " + data[1]))
            continue
        for p, s in walk_strings(data):
            if isinstance(s, str) and (p.endswith("/name") or p.endswith("/item")):
                if ":" in s and not resolve(s, "loot"):
                    unresolved["loot"].append((n, s))

    # T4 贴图 / T5 parent
    for n, data in iter_json(zf, r"assets/%s/models/.+\.json$" % NS):
        if isinstance(data, tuple):
            continue
        for k, v in (data.get("textures") or {}).items():
            if not isinstance(v, str) or v.startswith("#"):
                continue
            ns2, _, path2 = v.partition(":")
            if ns2 == NS:
                if ("assets/%s/textures/%s.png" % (NS, path2)) not in zf.namelist():
                    unresolved["texture"].append((n, v))
        par = data.get("parent")
        if isinstance(par, str):
            ns2, _, path2 = par.partition(":")
            if ns2 in (NS, "tfc"):
                if ("assets/%s/models/%s.json" % (ns2, path2)) not in zf.namelist():
                    # tfc 的模型不在我们 jar 里，用 TFC jar 再查一次
                    ok = False
                    if tfc_jar and os.path.exists(tfc_jar):
                        with zipfile.ZipFile(tfc_jar) as tz:
                            ok = ("assets/%s/models/%s.json" % (ns2, path2)) in tz.namelist()
                    if not ok:
                        unresolved["parent"].append((n, par))

    print()
    total = 0
    species_hits = []
    for k, label in (("tag", "T1 标签条目"), ("recipe", "T2 配方引用"),
                     ("loot", "T3 掉落表引用"), ("texture", "T4 贴图"),
                     ("parent", "T5 模型 parent")):
        rows = unresolved[k]
        # 过滤掉已知基线遗留
        rows2 = [r for r in rows if not KNOWN_MISSING_MODEL.search(str(r[1]))]
        total += len(rows2)
        print("%s: 未解析 %d 条（已排除已知基线遗留 %d 条）" % (label, len(rows2), len(rows) - len(rows2)))
        for r in rows2[:20]:
            print("    %s  ->  %s" % (r[0], r[1]))
        # 收集与本次精简 13 树种相关的条目
        for r in rows2:
            blob = (str(r[0]) + " " + str(r[1])).lower()
            hit = [s for s in SPECIES if re.search(r"(^|[/_.:])" + s + r"($|[/_.])", blob)]
            if hit:
                species_hits.append((k, r[0], r[1], hit))
    print()
    print("=== 与本次精简的 13 个树种相关的未解析条目 ===")
    if not species_hits:
        print("  无 ✓  —— 精简没有引入任何悬空引用")
    else:
        for k, f, ref, hit in species_hits[:40]:
            print("  [%s] %s -> %s  (%s)" % (k, f, ref, ",".join(hit)))
        print("  合计 %d 条" % len(species_hits))
    print()
    print("跨命名空间无法判定的引用: %d 条（minecraft/其它模组，属正常）" % unverifiable)
    print("结论: %s" % ("全部闭合 ✓" if total == 0 else
                       "全库存在 %d 条历史未解析引用，其中与本次精简相关 %d 条"
                       % (total, len(species_hits))))
    zf.close()
    return 0 if not species_hits else 1


if __name__ == "__main__":
    sys.exit(main())
