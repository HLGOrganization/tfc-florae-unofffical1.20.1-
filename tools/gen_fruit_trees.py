# -*- coding: utf-8 -*-
"""为 TFCFlorae 生成 TFC 3.2 风格「果树」(fruit tree) 的整套资源。

参考实现：Firmalife 1.20.1 的可可树 (FLFruitBlocks.Tree)。
方块由 TFC 的 FruitTree*Block 直接实例化，这里只生成资源与数据。
"""
import json
import os
import shutil
import zipfile

RES = "src/main/resources"
TFC_JAR = r"D:\线框的服务器\versions\TFC_Wild_Fire\mods\[群峦传说] TerraFirmaCraft-Forge-1.20.1-3.2.20.jar"
NS = "tfcflorae"

# 树种 -> (产物食物, climate_range(min_hyd,min_t,max_t), placed(min_t,max_t,min_r,max_r))
TREES = {
    "hawthorn":      ("hawthorn",       (12, -6, 32), (1, 25, 100, 280)),
    "jabuticabeira": ("jabuticaba",     (30,  8, 38), (15, 31, 250, 400)),
    "joshua":        ("joshua_fruit",   (0,   5, 45), (12, 40, 0, 150)),
    "maclura":       ("osage_orange",   (18, -2, 37), (5, 30, 150, 500)),
    "medlar":        ("medlar",         (12, -6, 32), (1, 25, 100, 280)),
    "mulberry":      ("mulberry",       (13, -6, 32), (1, 25, 110, 280)),
    "pear":          ("pear",           (13, -6, 32), (1, 25, 110, 280)),
    "persimmon":     ("persimmon",      (15, -3, 34), (4, 27, 90, 300)),
    "quince":        ("quince",         (12, -6, 32), (1, 25, 100, 280)),
    "rowan":         ("rowan_berry",    (12, -8, 28), (-2, 22, 90, 260)),
    "sloe":          ("sloe",           (12, -8, 30), (-2, 24, 90, 260)),
    "sorb":          ("sorb_apple",     (12, -6, 32), (1, 25, 100, 280)),
    "syzygium":      ("otaheite_apple", (30,  8, 43), (15, 36, 250, 500)),
    "yew":           ("yew_berry",      (12, -8, 30), (-2, 24, 100, 300)),
}

created = []


def w(path, obj):
    """写 JSON（仓库统一 indent=2, 不转义中文）。"""
    full = os.path.join(RES, path)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    with open(full, "w", encoding="utf-8", newline="\n") as f:
        json.dump(obj, f, indent=2, ensure_ascii=False)
        f.write("\n")
    created.append(path)


def copy_tex(src, dst):
    s = os.path.join(RES, "assets", NS, "textures", "block", src)
    d = os.path.join(RES, "assets", NS, "textures", "block", "fruit_tree", dst)
    if not os.path.exists(s):
        raise SystemExit("贴图缺失: " + s)
    os.makedirs(os.path.dirname(d), exist_ok=True)
    shutil.copyfile(s, d)
    created.append("textures/" + dst)


def _avg_rgb(img):
    """不透明像素的平均 RGB。"""
    import numpy as np
    a = np.asarray(img.convert("RGBA"), dtype=np.float64)
    m = a[..., 3] > 0
    return a[m][:, :3].mean(axis=0)


def dry_ratio():
    """从 TFC 自己的 cherry 一对贴图推出「枯萎 / 健康」的色调比例。"""
    import io
    from PIL import Image
    z = zipfile.ZipFile(TFC_JAR)
    live = Image.open(io.BytesIO(z.read("assets/tfc/textures/block/fruit_tree/cherry_leaves.png")))
    dry = Image.open(io.BytesIO(z.read("assets/tfc/textures/block/fruit_tree/cherry_dry_leaves.png")))
    ratio = _avg_rgb(dry) / _avg_rgb(live)
    print("TFC cherry 枯萎/健康 色调比例: R=%.3f G=%.3f B=%.3f" % tuple(ratio))
    return ratio


def make_dry(leaves_src, dst, ratio):
    """把健康树叶贴图按上面的比例做成枯黄色，保留 alpha 与叶形。"""
    import numpy as np
    from PIL import Image
    src = Image.open(os.path.join(RES, "assets", NS, "textures", "block", leaves_src)).convert("RGBA")
    a = np.asarray(src, dtype=np.float64)
    rgb, alpha = a[..., :3], a[..., 3]
    gray = rgb @ np.array([0.299, 0.587, 0.114])
    m = alpha > 0
    g_avg = gray[m].mean()
    k = (rgb[m].mean(axis=0) * ratio) / g_avg          # 让结果均值 = 原均值 * 比例
    out_rgb = np.clip(gray[..., None] * k, 0, 255)
    out = np.dstack([out_rgb, alpha]).astype(np.uint8)
    d = os.path.join(RES, "assets", NS, "textures", "block", "fruit_tree", dst)
    os.makedirs(os.path.dirname(d), exist_ok=True)
    Image.fromarray(out, "RGBA").save(d)
    created.append("textures/" + dst)



# ---------------------------------------------------------------- 模板
def blockstate_branch(t):
    def part(model, when=None, y=None):
        e = {"model": "tfcflorae:block/plant/" + model}
        if y is not None:
            e["y"] = y
        d = {"apply": e}
        if when:
            d = {"when": when, "apply": e}
        return d
    return {"multipart": [
        part(t + "_branch_core"),
        part(t + "_branch_down", {"down": True}),
        part(t + "_branch_up", {"up": True}),
        part(t + "_branch_side", {"north": True}, 90),
        part(t + "_branch_side", {"south": True}, 270),
        part(t + "_branch_side", {"west": True}),
        part(t + "_branch_side", {"east": True}, 180),
    ]}


def blockstate_leaves(t):
    return {"variants": {
        "lifecycle=flowering": {"model": "tfcflorae:block/plant/%s_flowering_leaves" % t},
        "lifecycle=fruiting": {"model": "tfcflorae:block/plant/%s_fruiting_leaves" % t},
        "lifecycle=dormant": {"model": "tfcflorae:block/plant/%s_dry_leaves" % t},
        "lifecycle=healthy": {"model": "tfcflorae:block/plant/%s_leaves" % t},
    }}


def blockstate_sapling(t):
    return {"variants": {"saplings=%d" % i: {"model": "tfcflorae:block/plant/%s_sapling_%d" % (t, i)} for i in range(1, 5)}}


def loot_sticks():
    return {"type": "minecraft:block", "pools": [{
        "name": "loot_pool", "rolls": 1,
        "entries": [{"type": "minecraft:item", "name": "minecraft:stick", "functions": [
            {"function": "minecraft:set_count", "count": {"min": 1, "max": 4, "type": "minecraft:uniform"}}]}],
        "conditions": [{"condition": "minecraft:survives_explosion"}]}]}


def loot_leaves(t, fruit):
    shear = {"condition": "minecraft:any_of", "terms": [
        {"condition": "minecraft:match_tool", "predicate": {"tag": "forge:shears"}},
        {"condition": "minecraft:match_tool", "predicate": {"enchantments": [
            {"enchantment": "minecraft:silk_touch", "levels": {"min": 1}}]}}]}
    return {"type": "minecraft:block", "pools": [
        {"name": "loot_pool", "rolls": 1, "entries": [
            {"type": "minecraft:item", "name": "%s:food/%s" % (NS, fruit), "conditions": [
                {"condition": "minecraft:block_state_property",
                 "block": "%s:plant/%s_leaves" % (NS, t), "properties": {"lifecycle": "fruiting"}}]}],
         "conditions": [{"condition": "minecraft:survives_explosion"}]},
        {"name": "loot_pool", "rolls": 1, "entries": [
            {"type": "minecraft:item", "name": "%s:plant/%s_leaves" % (NS, t), "conditions": [shear]}],
         "conditions": [{"condition": "minecraft:survives_explosion"}]},
        {"name": "loot_pool", "rolls": 1, "entries": [
            {"type": "minecraft:alternatives", "children": [
                {"type": "minecraft:item", "name": "minecraft:stick", "conditions": [
                    {"condition": "minecraft:match_tool", "predicate": {"tag": "tfc:sharp_tools"}},
                    {"condition": "minecraft:random_chance", "chance": 0.2}],
                 "functions": [{"function": "minecraft:set_count",
                                "count": {"min": 1, "max": 2, "type": "minecraft:uniform"}}]},
                {"type": "minecraft:item", "name": "minecraft:stick", "conditions": [
                    {"condition": "minecraft:random_chance", "chance": 0.05}],
                 "functions": [{"function": "minecraft:set_count",
                                "count": {"min": 1, "max": 2, "type": "minecraft:uniform"}}]}]}],
         "conditions": [{"condition": "minecraft:survives_explosion"},
                        {"condition": "minecraft:inverted", "term": shear}]}]}


def loot_sapling(t):
    funcs = []
    for i in range(1, 5):
        funcs.append({"function": "minecraft:set_count", "count": i, "conditions": [
            {"condition": "minecraft:block_state_property",
             "block": "%s:plant/%s_sapling" % (NS, t), "properties": {"saplings": str(i)}}]})
    funcs.append({"function": "minecraft:explosion_decay"})
    return {"type": "minecraft:block", "pools": [{
        "name": "loot_pool", "rolls": 1,
        "entries": [{"type": "minecraft:item", "name": "%s:plant/%s_sapling" % (NS, t), "functions": funcs}],
        "conditions": [{"condition": "minecraft:survives_explosion"}]}]}


def loot_potted(t):
    return {"type": "minecraft:block", "pools": [
        {"name": "loot_pool", "rolls": 1, "entries": [
            {"type": "minecraft:item", "name": "%s:plant/%s_sapling" % (NS, t)}],
         "conditions": [{"condition": "minecraft:survives_explosion"}]},
        {"name": "loot_pool", "rolls": 1, "entries": [
            {"type": "minecraft:item", "name": "minecraft:flower_pot"}],
         "conditions": [{"condition": "minecraft:survives_explosion"}]}]}


def main():
    # 先确认 TFC 的父模型都在
    z = zipfile.ZipFile(TFC_JAR)
    need = ["assets/tfc/models/block/plant/branch_core.json",
            "assets/tfc/models/block/plant/branch_down.json",
            "assets/tfc/models/block/plant/branch_side.json",
            "assets/tfc/models/block/plant/branch_up.json",
            "assets/tfc/models/block/plant/cross_2.json",
            "assets/tfc/models/block/plant/cross_3.json",
            "assets/tfc/models/block/plant/cross_4.json",
            "assets/tfc/textures/block/dirt/loam.png"]
    for n in need:
        if n not in z.namelist():
            raise SystemExit("TFC 缺少父资源: " + n)
    print("TFC 父模型/贴图 全部存在 (%d 项)" % len(need))

    ratio = dry_ratio()

    for t, (fruit, cr, pf) in TREES.items():
        # ---------- 贴图 ----------
        copy_tex("wood/log/%s.png" % t, "%s_branch.png" % t)
        copy_tex("wood/leaves/%s.png" % t, "%s_leaves.png" % t)
        copy_tex("wood/flowers/%s.png" % t, "%s_flowering_leaves.png" % t)
        copy_tex("wood/fruits/%s.png" % t, "%s_fruiting_leaves.png" % t)
        copy_tex("wood/sapling/%s.png" % t, "%s_sapling.png" % t)
        make_dry("wood/leaves/%s.png" % t, "%s_dry_leaves.png" % t, ratio)

        # ---------- blockstate ----------
        w("assets/%s/blockstates/plant/%s_branch.json" % (NS, t), blockstate_branch(t))
        w("assets/%s/blockstates/plant/%s_growing_branch.json" % (NS, t), blockstate_branch(t))
        w("assets/%s/blockstates/plant/%s_leaves.json" % (NS, t), blockstate_leaves(t))
        w("assets/%s/blockstates/plant/%s_sapling.json" % (NS, t), blockstate_sapling(t))
        w("assets/%s/blockstates/plant/potted/%s_sapling.json" % (NS, t),
          {"variants": {"": {"model": "tfcflorae:block/plant/flowerpot/%s_sapling" % t}}})

        # ---------- block 模型 ----------
        for part, parent in (("core", "branch_core"), ("down", "branch_down"),
                             ("side", "branch_side"), ("up", "branch_up")):
            w("assets/%s/models/block/plant/%s_branch_%s.json" % (NS, t, part),
              {"parent": "tfc:block/plant/" + parent,
               "textures": {"bark": "%s:block/fruit_tree/%s_branch" % (NS, t)}})
        for suffix in ("leaves", "dry_leaves"):
            w("assets/%s/models/block/plant/%s_%s.json" % (NS, t, suffix),
              {"parent": "block/leaves",
               "textures": {"all": "%s:block/fruit_tree/%s_%s" % (NS, t, suffix)}})
        # 开花 / 结果态沿用原模组 wood/leaves 的叠加做法：
        # leaves_overlay 里 #all 带 tintindex 0（会被 BlockColor 染成绿色），
        # #overlay 不带 tintindex（保持花/果本身的颜色）。
        for suffix in ("flowering_leaves", "fruiting_leaves"):
            w("assets/%s/models/block/plant/%s_%s.json" % (NS, t, suffix),
              {"parent": "%s:block/leaves_overlay" % NS,
               "textures": {"all": "%s:block/fruit_tree/%s_leaves" % (NS, t),
                            "overlay": "%s:block/fruit_tree/%s_%s" % (NS, t, suffix)}})
        for i, parent in ((1, "block/cross"), (2, "tfc:block/plant/cross_2"),
                          (3, "tfc:block/plant/cross_3"), (4, "tfc:block/plant/cross_4")):
            w("assets/%s/models/block/plant/%s_sapling_%d.json" % (NS, t, i),
              {"parent": parent,
               "textures": {"cross": "%s:block/fruit_tree/%s_sapling" % (NS, t)}})
        w("assets/%s/models/block/plant/flowerpot/%s_sapling.json" % (NS, t),
          {"parent": "minecraft:block/flower_pot_cross",
           "textures": {"plant": "%s:block/fruit_tree/%s_sapling" % (NS, t),
                        "dirt": "tfc:block/dirt/loam"}})

        # ---------- item 模型 ----------
        w("assets/%s/models/item/plant/%s_leaves.json" % (NS, t),
          {"parent": "%s:block/plant/%s_leaves" % (NS, t)})
        w("assets/%s/models/item/plant/%s_sapling.json" % (NS, t),
          {"parent": "item/generated",
           "textures": {"layer0": "%s:block/fruit_tree/%s_sapling" % (NS, t)}})

        # ---------- 掉落表 ----------
        w("data/%s/loot_tables/blocks/plant/%s_branch.json" % (NS, t), loot_sticks())
        w("data/%s/loot_tables/blocks/plant/%s_growing_branch.json" % (NS, t), loot_sticks())
        w("data/%s/loot_tables/blocks/plant/%s_leaves.json" % (NS, t), loot_leaves(t, fruit))
        w("data/%s/loot_tables/blocks/plant/%s_sapling.json" % (NS, t), loot_sapling(t))
        w("data/%s/loot_tables/blocks/plant/potted/%s_sapling.json" % (NS, t), loot_potted(t))

        # ---------- 气候范围 ----------
        w("data/%s/tfc/climate_ranges/plant/%s_tree.json" % (NS, t),
          {"min_hydration": cr[0], "max_hydration": 100, "hydration_wiggle_range": 0,
           "min_temperature": cr[1], "max_temperature": cr[2], "temperature_wiggle_range": 0})

        # ---------- 地物 ----------
        w("data/%s/worldgen/configured_feature/plant/%s.json" % (NS, t),
          {"type": "tfc:fruit_trees", "config": {"state": "%s:plant/%s_growing_branch" % (NS, t)}})
        w("data/%s/worldgen/placed_feature/plant/%s.json" % (NS, t),
          {"feature": "%s:plant/%s" % (NS, t), "placement": [
              {"type": "tfc:climate", "min_temperature": pf[0], "max_temperature": pf[1],
               "min_rainfall": pf[2], "max_rainfall": pf[3], "max_forest": "normal"},
              {"type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG"},
              {"type": "minecraft:in_square"},
              {"type": "minecraft:rarity_filter", "chance": 50}]})

    print("已生成 %d 个文件" % len(created))


if __name__ == "__main__":
    main()
