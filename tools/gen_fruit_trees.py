# -*- coding: utf-8 -*-
"""为 TFCFlorae 生成 TFC 3.2 风格「果树」(fruit tree) 的整套资源。

参考实现：Firmalife 1.20.1 的可可树 (FLFruitBlocks.Tree)。
方块由 TFC 的 FruitTree*Block 直接实例化，这里只生成资源与数据。
"""
import json
import os
import shutil
import zipfile
import glob

RES = "src/main/resources"
TFC_JAR = sorted(glob.glob(r"D:\线框的服务器\versions\TFC_Wild_Fire\mods\*TerraFirmaCraft-Forge*.jar"))[-1]
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


def dry_target():
    """从 TFC 的 cherry 一对贴图推出「枯萎态」的色相与亮度系数。

    以前这里是「枯萎均值 / 健康均值」的逐通道比值，再乘到灰度图上。那个做法对
    本身是绿色的贴图没问题（TFC 樱桃就是绿的），但我们的树叶贴图大多是灰度，
    灰度乘一个「红略高、绿大减、蓝略减」的比值必然得到品红/紫——山楂、花楸那批
    枯萎态实测是 (110,66,93)、(187,112,158)，一眼就是粉色。

    正确做法：只保留原图的亮度，然后统一乘上褐色色相。色相和系数都从 TFC 的
    cherry 那对贴图反推，保证结果与 TFC 的枯萎态同色系、同亮度。
    """
    import io
    import numpy as np
    from PIL import Image
    z = zipfile.ZipFile(TFC_JAR)
    live = Image.open(io.BytesIO(z.read("assets/tfc/textures/block/fruit_tree/cherry_leaves.png")))
    dry = Image.open(io.BytesIO(z.read("assets/tfc/textures/block/fruit_tree/cherry_dry_leaves.png")))
    d = _avg_rgb(dry)
    l = _avg_rgb(live)
    live_lum = float(l @ np.array([0.299, 0.587, 0.114]))
    hue = d / d.max()                       # 归一化色相，最大通道 = 1
    scale = float(d.max()) / live_lum       # 亮度系数
    print("TFC cherry 枯萎态: 均值=%s 亮度=%.1f -> 色相=(%.3f,%.3f,%.3f) 亮度系数=%.3f"
          % (tuple(d.round(0).astype(int)), live_lum, hue[0], hue[1], hue[2], scale))
    return hue, scale


def make_dry(leaves_src, dst, target, size):
    """把健康树叶贴图做成褐色枯萎态：保留每像素亮度与 alpha，只换色相。"""
    import numpy as np
    from PIL import Image
    hue, scale = target
    a = np.asarray(_fit(_load_block_tex(leaves_src), size), dtype=np.float64)
    rgb, alpha = a[..., :3], a[..., 3]
    gray = rgb @ np.array([0.299, 0.587, 0.114])
    out_rgb = np.clip(gray[..., None] * scale * hue, 0, 255)
    _save_block_tex(Image.fromarray(np.dstack([out_rgb, alpha]).astype(np.uint8), "RGBA"), dst)


def foliage_tint():
    """取 TFC 叶色调色板 `colormap/foliage.png` 的温带中心色，转成乘算系数。

    TFC 的果树树叶是**每个状态一张已经上好色的成图**，客户端全程不染色：
    `flowerColor` 只被 `FallingLeafParticle` 用（粒子颜色），
    TFC 的 `ClientEventHandler` 里没有任何果树树叶的 BlockColor。
    所以这里把绿色直接烘进贴图，运行时就不需要染色了。
    """
    import io
    import numpy as np
    from PIL import Image
    z = zipfile.ZipFile(TFC_JAR)
    im = Image.open(io.BytesIO(z.read("assets/tfc/textures/colormap/foliage.png"))).convert("RGB")
    a = np.asarray(im)
    h, w, _ = a.shape
    green = a[h // 2, w // 2].astype(np.float64)
    lum = float(green @ np.array([0.299, 0.587, 0.114]))
    tint = green / lum
    print("TFC 叶色调色板中心色 = %s (亮度 %.1f) -> 乘算系数 (%.3f, %.3f, %.3f)"
          % (tuple(green.astype(int)), lum, tint[0], tint[1], tint[2]))
    return tint


def _load_block_tex(rel):
    from PIL import Image
    return Image.open(os.path.join(RES, "assets", NS, "textures", "block", rel)).convert("RGBA")


def _fit(img, size):
    """把贴图对齐到目标尺寸（最近邻，保持像素画风）。

    joshua 的花/果叠加层是 32x32 而树叶是 16x16；旧方案分两层画时各用各的分辨率，
    合成成单张后必须统一。取较大者，保住叠加层的细节。
    """
    from PIL import Image
    return img if img.size == size else img.resize(size, Image.NEAREST)


def _save_block_tex(img, dst):
    d = os.path.join(RES, "assets", NS, "textures", "block", "fruit_tree", dst)
    os.makedirs(os.path.dirname(d), exist_ok=True)
    img.save(d)
    created.append("textures/" + dst)


def bake_leaves(leaves_src, dst, tint, size):
    """把绿色烘进树叶贴图：保留每像素明暗与 alpha，色相统一到 TFC 的温带绿。"""
    import numpy as np
    from PIL import Image
    a = np.asarray(_fit(_load_block_tex(leaves_src), size), dtype=np.float64)
    rgb, alpha = a[..., :3], a[..., 3]
    out_rgb = np.clip(rgb * tint, 0, 255)
    _save_block_tex(Image.fromarray(np.dstack([out_rgb, alpha]).astype(np.uint8), "RGBA"), dst)


def bake_overlay(leaves_src, overlay_src, dst, tint, size):
    """把「染好绿的健康树叶」与「花/果叠加层」合成一张成图（标准 over 混合）。

    旧方案用 leaves_overlay 父模型分两层画：底层的 #all 被 BlockColor 染绿、
    上层的 #overlay 保持本色。现在按 TFC 的布局合成成单张，视觉等价，
    但不再依赖任何运行时染色。
    """
    import numpy as np
    from PIL import Image
    base = np.asarray(_fit(_load_block_tex(leaves_src), size), dtype=np.float64)
    ov = np.asarray(_fit(_load_block_tex(overlay_src), size), dtype=np.float64)
    base[..., :3] = np.clip(base[..., :3] * tint, 0, 255)
    ba = base[..., 3:4] / 255.0
    oa = ov[..., 3:4] / 255.0
    out_a = oa + ba * (1.0 - oa)
    safe = np.where(out_a > 0, out_a, 1.0)
    out_rgb = (ov[..., :3] * oa + base[..., :3] * ba * (1.0 - oa)) / safe
    out = np.dstack([np.clip(out_rgb, 0, 255), np.clip(out_a * 255.0, 0, 255)]).astype(np.uint8)
    _save_block_tex(Image.fromarray(out, "RGBA"), dst)



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
    """生长中的枝干：只掉 1-4 根木棍。"""
    return {"type": "minecraft:block", "pools": [{
        "name": "loot_pool", "rolls": 1,
        "entries": [{"type": "minecraft:item", "name": "minecraft:stick", "functions": [
            {"function": "minecraft:set_count", "count": {"min": 1, "max": 4, "type": "minecraft:uniform"}}]}],
        "conditions": [{"condition": "minecraft:survives_explosion"}]}]}


def loot_branch(t):
    """长成的枝干：两池。

    第 1 池是果树唯一的存活获取途径——当这根枝干「往上还有」且「往侧面还有」
    （即 up=true 且四向之一=true，说明是一个分叉点），用斧头砍掉会掉一棵果树苗。
    第 2 池才是 1-4 根木棍。两者都照抄 TFC 的 cherry_branch。
    """
    b = "%s:plant/%s_branch" % (NS, t)
    forks = [{"condition": "minecraft:block_state_property", "block": b,
              "properties": {"up": "true", d: "true"}}
             for d in ("west", "east", "north", "south")]
    return {"type": "minecraft:block", "pools": [
        {"name": "loot_pool", "rolls": 1, "entries": [
            {"type": "minecraft:item", "name": "%s:plant/%s_sapling" % (NS, t), "conditions": [
                {"condition": "minecraft:all_of", "terms": [
                    {"condition": "minecraft:any_of", "terms": forks},
                    {"condition": "minecraft:match_tool", "predicate": {"tag": "tfc:axes"}}]}]}],
         "conditions": [{"condition": "minecraft:survives_explosion"}]},
        {"name": "loot_pool", "rolls": 1, "entries": [
            {"type": "minecraft:item", "name": "minecraft:stick", "functions": [
                {"function": "minecraft:set_count",
                 "count": {"min": 1, "max": 4, "type": "minecraft:uniform"}}]}],
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

    target = dry_target()
    tint = foliage_tint()

    for t, (fruit, cr, pf) in TREES.items():
        # ---------- 贴图 ----------
        copy_tex("wood/log/%s.png" % t, "%s_branch.png" % t)
        copy_tex("wood/sapling/%s.png" % t, "%s_sapling.png" % t)
        # 四态统一分辨率：取树叶/花/果三者中最大的那个（只有 joshua 是 32x32）
        _sz = [_load_block_tex(p % t).size for p in
               ("wood/leaves/%s.png", "wood/flowers/%s.png", "wood/fruits/%s.png")]
        size = (max(s[0] for s in _sz), max(s[1] for s in _sz))
        # 健康 / 开花 / 结果三态都是「烘好色的成图」，与 TFC 的布局一致
        bake_leaves("wood/leaves/%s.png" % t, "%s_leaves.png" % t, tint, size)
        bake_overlay("wood/leaves/%s.png" % t, "wood/flowers/%s.png" % t,
                     "%s_flowering_leaves.png" % t, tint, size)
        bake_overlay("wood/leaves/%s.png" % t, "wood/fruits/%s.png" % t,
                     "%s_fruiting_leaves.png" % t, tint, size)
        make_dry("wood/leaves/%s.png" % t, "%s_dry_leaves.png" % t, target, size)

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
        # 四种状态一律「一张成图 + block/leaves 父模型」，与 TFC 的
        # models/block/plant/<tree>_<state>_leaves.json 完全同构，不再用 leaves_overlay。
        for suffix in ("leaves", "flowering_leaves", "fruiting_leaves", "dry_leaves"):
            w("assets/%s/models/block/plant/%s_%s.json" % (NS, t, suffix),
              {"parent": "block/leaves",
               "textures": {"all": "%s:block/fruit_tree/%s_%s" % (NS, t, suffix)}})
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
        w("data/%s/loot_tables/blocks/plant/%s_branch.json" % (NS, t), loot_branch(t))
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
