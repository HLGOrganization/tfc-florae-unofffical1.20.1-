#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""给 tuff 补齐「多石」变体。

背景：TFCFRock.TUFF 存在，但 tuff 的整套岩石方块（raw/gravel/cobble…）被移除、改由 TFE 提供，
注册循环里 `if (rock == TFCFRock.TUFF) continue;` 把 tuff 的混合沙/混合土一起跳过了。
TFE 的 tuff 会生成在世界表层（tfe:rock/raw/tuff），所以这一带接沙/土时需要有过渡方块。

本脚本照现有 9 种岩石的同一套模板，补：
  rockier_compact_dirt/<8种土壤>/tuff   方块状态 + 方块模型 + 物品模型 + 掉落表
  sand/rocky/<7种颜色>/tuff             同上

贴图不拷贝，直接引用 TFE 的 tfe:block/rock/raw/tuff 和 tfe:block/rock/gravel/tuff。
"""
import json
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "src", "main", "resources")

ROCK = "tuff"
ROCK_TEX = "tfe:block/rock/raw/tuff"

SOILS = ["fluvisol", "loam", "mollisol", "oxisol", "podzol", "sandy_loam", "silt", "silty_loam"]
COLORS = ["black", "brown", "green", "pink", "red", "white", "yellow"]

TEMPLATE = "tfcflorae:block/template/soil_over_rock"


def write_json(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        json.dump(obj, f, ensure_ascii=False, indent=2)
        f.write("\n")


def read_json(path):
    with open(path, encoding="utf-8") as f:
        return json.load(f)


def sub_granite(obj, kind, sub):
    """把模板里所有 granite 引用换成 tuff。

    方块状态里除了主模型，还有几个「随机变体」模型（pebble / rocky / rockiest 等），
    它们的前缀跟 kind 不同 —— 例如 sand/rocky 的变体是
    sand/pebble/<色>/granite、sand/rockier/<色>/granite、sand/rockiest/<色>/granite。
    只替换 kind 前缀会漏掉这些，必须在全文中把所有 "/<sub>/granite" 换成
    "/<sub>/tuff"。因为 sub 与 kind 组合在一起是全路径唯一的，不会误伤。
    """
    s = json.dumps(obj)
    # 路径型引用：.../<sub>/granite -> .../<sub>/tuff
    s = s.replace("/%s/granite" % sub, "/%s/%s" % (sub, ROCK))
    # 注册名型引用：tfcflorae:<kind>/<sub>/granite
    s = s.replace("tfcflorae:%s/%s/granite" % (kind, sub),
                  "tfcflorae:%s/%s/%s" % (kind, sub, ROCK))
    return json.loads(s)


def copy_template(kind, sub, ref_rock="granite"):
    """读现有 granite 版本的 blockstate / loot / item model 当作模板"""
    bsp = os.path.join(RES, "assets", "tfcflorae", "blockstates", kind, sub, ref_rock + ".json")
    lootp = os.path.join(RES, "data", "tfcflorae", "loot_tables", "blocks", kind, sub, ref_rock + ".json")
    itp = os.path.join(RES, "assets", "tfcflorae", "models", "item", kind, sub, ref_rock + ".json")
    out = {}
    for label, p in (("blockstate", bsp), ("loot", lootp), ("item", itp)):
        out[label] = read_json(p) if os.path.exists(p) else None
    return out


def do_rocky(created, skipped):
    # ---------- 多石土 rockier_compact_dirt ----------
    for soil in SOILS:
        kind, sub = "rockier_compact_dirt", soil
        tmpl = copy_template(kind, sub)

        # 4 个变体模型：主模型 + pebble / rocky / rockiest（方块状态里随机挑一个）
        for tpl_kind in ("rockier_compact_dirt", "pebble_compact_dirt",
                         "rocky_compact_dirt", "rockiest_compact_dirt"):
            soil_tex = "tfcflorae:block/compact_dirt/" + tpl_kind + "/" + soil
            bp = os.path.join(RES, "assets", "tfcflorae", "models", "block", tpl_kind, sub, ROCK + ".json")
            write_json(bp, {"parent": TEMPLATE, "textures": {"rock": ROCK_TEX, "soil": soil_tex}})
            created.append(bp)

        # 方块状态
        if tmpl["blockstate"] is None:
            skipped.append(("blockstate", kind, sub))
        else:
            bs = sub_granite(tmpl["blockstate"], kind, sub)
            p = os.path.join(RES, "assets", "tfcflorae", "blockstates", kind, sub, ROCK + ".json")
            write_json(p, bs)
            created.append(p)

        # 物品模型
        p = os.path.join(RES, "assets", "tfcflorae", "models", "item", kind, sub, ROCK + ".json")
        write_json(p, {"parent": "tfcflorae:block/%s/%s/%s" % (kind, sub, ROCK)})
        created.append(p)

        # 掉落表
        if tmpl["loot"] is None:
            skipped.append(("loot", kind, sub))
        else:
            lj = sub_granite(tmpl["loot"], kind, sub)
            p = os.path.join(RES, "data", "tfcflorae", "loot_tables", "blocks", kind, sub, ROCK + ".json")
            write_json(p, lj)
            created.append(p)

    # ---------- 多石沙 sand/rocky ----------
    for color in COLORS:
        kind, sub = "sand/rocky", color
        tmpl = copy_template(kind, sub)

        for tpl_kind in ("sand/rocky", "sand/pebble", "sand/rockier", "sand/rockiest"):
            soil_tex = "tfcflorae:block/" + tpl_kind + "/" + color
            bp = os.path.join(RES, "assets", "tfcflorae", "models", "block",
                              *tpl_kind.split("/"), color, ROCK + ".json")
            write_json(bp, {"parent": TEMPLATE, "textures": {"rock": ROCK_TEX, "soil": soil_tex}})
            created.append(bp)

        if tmpl["blockstate"] is None:
            skipped.append(("blockstate", kind, sub))
        else:
            bs = sub_granite(tmpl["blockstate"], kind, sub)
            p = os.path.join(RES, "assets", "tfcflorae", "blockstates", kind, sub, ROCK + ".json")
            write_json(p, bs)
            created.append(p)

        p = os.path.join(RES, "assets", "tfcflorae", "models", "item", kind, sub, ROCK + ".json")
        write_json(p, {"parent": "tfcflorae:block/%s/%s/%s" % (kind, sub, ROCK)})
        created.append(p)

        if tmpl["loot"] is None:
            skipped.append(("loot", kind, sub))
        else:
            lj = sub_granite(tmpl["loot"], kind, sub)
            p = os.path.join(RES, "data", "tfcflorae", "loot_tables", "blocks", kind, sub, ROCK + ".json")
            write_json(p, lj)
            created.append(p)


if __name__ == "__main__":
    created, skipped = [], []
    do_rocky(created, skipped)
    print("写入 %d 个文件" % len(created))
    for p in created:
        print("   ", os.path.relpath(p, ROOT))
    if skipped:
        print("\n跳过（模板缺失）：")
        for s in skipped:
            print("   ", s)
