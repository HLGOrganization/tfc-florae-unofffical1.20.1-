"""Inventory every tag file shipped by the mod, with a short purpose note."""
from __future__ import annotations

import collections
import json
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATA = os.path.join(ROOT, "src", "main", "resources", "data")

# purpose lookup: longest matching key wins
PURPOSE = {
    # ---- mechanics (TFC reads these) ----
    "can_landslide": "方块失去支撑时是否滑坡（TFC 滑坡机制）",
    "supports_landslide": "触发滑坡的支撑面",
    "can_collapse": "方块是否会坍塌（TFC 坍塌机制）",
    "can_start_collapse": "可作为坍塌起点（支撑失效时引发坍塌）",
    "can_trigger_collapse": "受上方坍塌影响会被带塌",
    "breaks_when_isolated": "失去邻接岩石时直接破碎（原石）",
    "loose_rocks": "地上的碎石/石子（可捡起）",
    "replaceable_plants": "植物可被替换（放置时顶掉）",
    "single_block_replaceable": "单格可替换方块",
    "replaceable": "可被替换方块",
    "can_be_snow_piled": "可积雪",
    "can_be_ice_piled": "可积冰",
    "toughness_1": "坍塌/挖掘耐久等级 1",
    "toughness_2": "坍塌/挖掘耐久等级 2",
    "toughness_3": "坍塌/挖掘耐久等级 3",
    "mineable_with_hammer": "可用锤开采",
    "mineable_with_knife": "可用刀开采",
    "mineable_with_scythe": "可用镰刀开采",
    "mineable_with_sharp_tool": "可用利器开采",
    "mineable_with_blunt_tool": "可用钝器开采",
    # ---- rock families ----
    "rock/raw": "原石类（坍塌起点、石砧/石板原料判定）",
    "rock/hardened": "硬化岩石",
    "rock/smooth": "平滑岩石",
    "rock/gravel": "岩石砂砾",
    "rock/bricks": "岩石砖类（含苔石砖/裂纹砖/錾制砖）",
    "rock/mossy_bricks": "苔石砖",
    "rock/cracked_bricks": "裂纹砖",
    "rock/chiseled_bricks": "錾制砖",
    "rock/aqueducts": "岩石引水桥",
    "rock/ores": "岩石中的矿石方块",
    "rock_anvils": "石砧",
    "ore_deposits": "地表矿床（淘洗/勘探）",
    # ---- geology (mod-defined) ----
    "rock/sedimentary": "沉积岩类（岩质 tag，模组自定义）",
    "rock/igneous_extrusive": "喷出火成岩类（岩质 tag，模组自定义）",
    "rock/igneous_intrusive": "侵入火成岩类（岩质 tag，模组自定义）",
    "rock/metamorphic": "变质岩类（岩质 tag，模组自定义）",
    # ---- soil / sand ----
    "dirt": "泥土类",
    "grass": "草方块类",
    "clay": "黏土块",
    "kaolin_clay": "高岭土",
    "mud": "泥巴",
    "farmland": "耕地",
    "paths": "土径",
    "rooted_dirt": "带根泥土",
    "sand": "沙子类（通用）",
    "sandstone": "砂岩类",
    "gravel": "砂砾类（通用）",
    "cobblestone/normal": "圆石类（通用）",
    "stone": "石头类（通用）",
    "stone_bricks": "石砖类（通用）",
    "smooth_stone": "平滑石类（通用）",
    # ---- wood ----
    "logs": "原木类（可烧木炭、工具/支撑配方）",
    "logs_that_log": "可被原木剥皮/合成判定",
    "planks": "木板类",
    "leaves": "树叶类",
    "saplings": "树苗类",
    "fallen_leaves": "落叶层",
    "stripped_logs": "去皮原木",
    "chests/wooden": "木箱",
    "barrels": "木桶（可发酵/储液）",
    "bookshelves": "书架",
    "workbenches": "工作台",
    "looms": "织布机",
    "sluices": "洗矿槽",
    "lecterns": "讲台",
    "jar_shelves": "罐架",
    "scribing_tables": "抄写台",
    "sewing_tables": "缝纫台",
    "tool_racks": "工具架",
    "water_wheels": "水车",
    "twigs": "树枝",
    "fences": "栅栏",
    "fence_gates": "栅栏门",
    "doors": "门",
    "trapdoors": "活板门",
    "buttons": "按钮",
    "pressure_plates": "压力板",
    "slabs": "台阶",
    "stairs": "楼梯",
    "signs": "告示牌",
    "flower_pots": "花盆/盆栽",
    # ---- plants / food ----
    "plants": "可种植植物",
    "knapping_any": "本模组所有可敲制物品合集（追加进 tfc:any_knapping）",
    "any_knapping": "所有可敲制物品（TFC 汇总 tag）",
    "rock_knapping": "可敲制的碎石",
    "earthenware_clay_knapping": "陶土敲制输入",
    "stoneware_clay_knapping": "石胎瓷土敲制输入",
    "foods": "食物总类",
    "fruits": "水果（营养/配方）",
    "vegetables": "蔬菜",
    "meats": "肉类",
    "cooked_meats": "熟肉",
    "usable_in_salad": "可做沙拉",
    "usable_in_sandwich": "可做三明治",
    "usable_in_soup": "可做汤",
    # ---- misc ----
    "silkmoth_nests": "蚕蛾巢",
    "silkmoth_target_blocks": "蚕蛾可附着方块",
    "bog_iron_soil": "沼铁土壤",
    "geode_air": "晶洞空腔",
    "thermal_disk_replaceable": "热泉圆盘可替换",
    "column_generates_on": "石柱生成基底",
    "creeping_stone_plantable_on": "爬石植物可种植",
    "creeping_plantable_on": "爬藤可种植",
    "bush_plantable_on": "灌木可种植",
    "clay_grass": "黏土草",
    "muddy_roots": "泥泞树根",
    "mossy_packed_mud": "苔藓压实泥",
    "podzol": "灰化土",
    "dirty_stone_tiles": "脏石砖",
    "layered_sandstone": "层状砂岩",
    "magma_block": "岩浆块",
    "smoke_block": "发烟石",
    "terracotta": "陶瓦",
    "brushes": "刷子",
    "bypass_slowdown": "跳过减速",
    "crafting_tables": "工作台（旧名）",
    # ---- TFC tags this mod appends to ----
    "all_fluids": "所有流体",
    "bricks": "砖块类",
    "can_carve": "可被凿空/雕刻（TFC 机制）",
    "axles": "传动轴",
    "animal_products": "动物产品",
    "charcoal_cover_whitelist": "可被木炭堆覆盖",
    "clay_indicators": "指示附近有黏土",
    "converts_to_humus": "会转化为腐殖土",
    "corals": "珊瑚",
    "crops": "作物",
    "explosion_proof": "抗爆",
    "fired_large_vessels": "烧制的大陶罐",
    "fruit_tree_branch": "果树树枝",
    "fruit_tree_leaves": "果树树叶",
    "fruit_tree_sapling": "果树树苗",
    "glass_basin_blocks": "玻璃盆方块",
    "glass_pouring_table": "玻璃浇注台",
    "halophyte": "盐生植物",
    "halophyte_plantable_on": "盐生植物可种植",
    "kelp_branch": "海带分枝",
    "kelp_flower": "海带花",
    "kelp_tree": "海带树",
    "krummholz": "矮曲林",
    "lamps": "灯",
    "large_vessels": "大陶罐",
    "metal_plated_blocks": "金属包覆方块",
    "minecart_holdable": "矿车可装载",
    "monster_spawns_on": "怪物可生成于其上",
    "not_solid_supporting": "非实体支撑面",
    "pet_sits_on": "宠物可坐",
    "powderkeg_breaking_blocks": "火药桶可破坏",
    "prospectable": "可勘探",
    "rabbit_raidable": "兔子可啃食",
    "fox_raidable": "狐狸可偷取",
    "scraping_surface": "可刮削的表面",
    "seasonal_leaves": "季节性树叶",
    "snow": "雪",
    "snow_layer_survives_on": "雪层可存活于其上",
    "spreading_bush": "蔓延灌木",
    "spreading_fruit_grows_on": "蔓延果实的生长基",
    "support_beams": "支撑梁",
    "thatch_bed_thatch": "茅草床",
    "thorny_bushes": "荆棘灌木",
    "tide_pool_blocks": "潮池方块",
    "tree_grows_on": "树木可生长于其上",
    "wall_corals": "墙珊瑚",
    "wild_crop_grows_on": "野生作物生长基",
    "wild_crops": "野生作物",
    "wild_fruits": "野生水果",
    "blast_furnace_insulation": "高炉隔热层",
    "bloomery_insulation": "锻铁炉隔热层",
    "forge_insulation": "锻造炉隔热层",
    "forge_invisible_whitelist": "锻造炉隐身白名单",
    "candle_cakes": "可插蜡烛的蛋糕",
    "candles": "蜡烛",
    "anvils": "铁砧",
    "bells": "钟",
    "supported_items": "可被支撑的物品",
    "mineable/pickaxe": "镐可开采",
    "needs_stone_tool": "需要石级工具",
    "needs_wood_tool": "需要木级工具",
    "needs_iron_tool": "需要铁级工具",
    "needs_diamond_tool": "需要钻石级工具",
    "needs_netherite_tool": "需要下界合金级工具",
    "needs_copper_tool": "需要铜级工具",
    "needs_steel_tool": "需要钢级工具",
    "needs_black_steel_tool": "需要黑钢级工具",
    "needs_colored_steel_tool": "需要彩色钢级工具",
    "needs_wrought_iron_tool": "需要锻铁级工具",
    # ---- TFC-style rock/stone families defined by this mod ----
    "ceramic_bricks": "陶制砖",
    "chiseled": "錾制石",
    "cobble": "圆石类",
    "cracked_bricks": "裂纹砖",
    "hardened": "硬化石",
    "log_walls": "原木墙",
    "mossy_bricks": "苔石砖",
    "mossy_cobble": "苔圆石",
    "ores": "矿石总类",
    "smooth": "平滑石",
    "raw": "原石",
    "flagstone": "板石",
    "stone_tiles": "石板瓦",
    "cobbled_bricks": "圆石砖",
    "mortar_and_cobble": "砂浆圆石",
    "polished_cobbled_bricks": "磨制圆石砖",
    "mossy_flagstone_bricks": "苔板石砖",
    "cracked_flagstone_bricks": "裂板石砖",
    "column": "石柱",
    # ---- assorted TFC / Forge families ----
    "buckets": "桶",
    "ceramic": "陶器",
    "clutches": "离合器",
    "gear_boxes": "齿轮箱",
    "lumber": "木材（建筑用）",
    "pans": "淘金盘",
    "pots": "陶罐",
    "anvil": "铁砧",
    "aqueduct": "引水桥",
    "button": "按钮",
    "cobblestone": "圆石",
    "geyser": "间歇泉",
    "gravel_layer": "砂砾层",
    "loose": "碎石",
    "magma": "岩浆块",
    "polished_column": "磨制石柱",
    "spike": "石锥",
    "double_ingots": "双层锭",
    "double_sheets": "双层板",
    "sheets": "金属板",
    "rods": "金属杆",
    "ingots": "金属锭",
    "raw_materials": "原材料",
    "ores_in_ground": "地下矿石",
    "tools": "工具",
    "axes": "斧",
    "pickaxes": "镐",
    "shovels": "铲",
    "hoes": "锄",
    "swords": "剑",
    "knives": "刀",
    "hammers": "锤",
    "chisels": "凿子",
    "saws": "锯",
    "maces": "锤矛",
    "javelins": "标枪",
    "bows": "弓",
    "arrows": "箭",
    # ---- vanilla / common ----
    "base_stone_overworld": "主世界基岩/石层",
    "logs_that_burn": "可燃原木（可烧木炭）",
    "mineable/axe": "斧可快速破坏",
    "mineable/hoe": "锄可快速破坏",
    "mineable/shovel": "铲可快速破坏",
    "mineable/sword": "剑可快速破坏",
    "mineable_axe": "斧可破坏（旧版命名）",
    "stone_buttons": "石按钮",
    "stone_pressure_plates": "石压力板",
    "valid_spawn": "有效生成点",
    "wall_signs": "墙上告示牌",
    "walls": "墙",
    "wooden_buttons": "木按钮",
    "wooden_doors": "木门",
    "wooden_fences": "木栅栏",
    "wooden_pressure_plates": "木压力板",
    "wooden_slabs": "木台阶",
    "wooden_stairs": "木楼梯",
    "wooden_trapdoors": "木活板门",
    "wooden_signs": "木告示牌",
    "pressure_plate": "压力板",
    "configured_feature": "世界生成：配置特征分组",
    "forest_trees": "森林树木",
    # ---- tfcflorae's own legacy groups ----
    "bamboo": "竹子（竹竿/竹苗/竹块）",
    "brown_shroom": "棕色蘑菇",
    "red_shroom": "红色蘑菇",
    "creates_upward_bubbles": "产生上升气泡",
    "groundcover_rocks": "地面碎石（可捡起）",
    "kelp": "海带",
    "loam_soil": "壤土类土壤（泥土/草/耕地等）",
    "sandy_loam_soil": "砂质壤土类土壤",
    "silt_soil": "粉砂土类土壤",
    "air_and_plants": "空气与植物（可替换）",
    "mangrove_logs_can_grow_through": "红树原木可穿过",
    "mangrove_roots_can_grow_through": "红树根可穿过",
    "mycelium_indicators": "指示菌丝存在",
    "replaceable_not_air": "非空气的可替换方块",
    "replaceable_air_or_plant": "空气或植物可替换",
    "sapling_can_replace": "树苗可替换",
    "boats": "船",
    "chest_minecarts": "箱子矿车",
    "fungi_on_fallen_logs": "倒木上的真菌",
    "geode_air": "晶洞空腔",
    "column_generates_on": "石柱生成基底",
    "creeping_stone_plantable_on": "爬石植物可种植",
    "bog_iron_soil": "沼铁土壤",
    "thermal_disk_replaceable": "热泉圆盘可替换",
    "silkmoth_nests": "蚕蛾巢",
    "silkmoth_target_blocks": "蚕蛾可附着方块",
    "bush_plantable_on": "灌木可种植",
    "muddy_roots": "泥泞树根",
    "mossy_packed_mud": "苔藓压实泥",
    "podzol": "灰化土",
    "roots": "树根",
    "empty": "空 tag（占位）",
    "silkmoth_temptation_items": "吸引蚕蛾的物品",
    "walking_canes": "手杖",
    "terracotta": "陶瓦",
    "layered_sandstone": "层状砂岩",
    "magma_block": "岩浆块",
    "smoke_block": "发烟石",
    "clay_grass": "黏土草",
    "dirty_stone_tiles": "脏石砖",
    "dirty_stone_tile_slabs": "脏石砖台阶",
    "dirty_stone_tile_stairs": "脏石砖楼梯",
    "dirty_stone_tile_walls": "脏石砖墙",
    "rocky_compact_dirt": "多石紧实泥土",
    "rocky_compact_dirt_slabs": "多石紧实泥土台阶",
    "rocky_compact_dirt_walls": "多石紧实泥土墙",
    "earthenware_clay": "陶土",
    "stoneware_clay": "石胎瓷土",
    "kaolinite_clay": "高岭土",
}


def purpose_for(tag, kind=None):
    if kind == "worldgen":
        if tag.startswith("biome"):
            return "世界生成：生物群系分类"
        if tag.startswith("placed_feature") or tag.startswith("configured_feature"):
            return "世界生成：地物放置/配置特征分组"
        return "世界生成分组"
    best = None
    for key in PURPOSE:
        if tag == key or tag.endswith("/" + key) or tag.startswith(key + "/") or key in tag.split("/"):
            if best is None or len(key) > len(best):
                best = key
    if best:
        return PURPOSE[best]
    if tag.endswith("_logs") or tag.endswith("_log"):
        return "某木材的原木类（原木/去皮原木/木块）"
    if tag.startswith("rock_") or tag.startswith("rock/"):
        return "岩石类（旧版下划线命名的分组）"
    if tag.startswith("worldgen/"):
        return "世界生成分组（生物群系/特征/放置特征）"
    if tag.startswith("wood/"):
        return "木材部件分类"
    return "模组自定义分组 tag"


def main():
    rows = []
    for dirpath, _d, files in os.walk(DATA):
        for fn in files:
            if not fn.endswith(".json"):
                continue
            full = os.path.join(dirpath, fn)
            rel = os.path.relpath(full, DATA).replace(os.sep, "/")
            if "/tags/" not in rel:
                continue
            ns, rest = rel.split("/tags/", 1)
            kind, path = rest.split("/", 1)
            path = path[:-5]
            try:
                data = json.load(open(full, encoding="utf-8-sig"))
            except Exception:
                continue
            values = data.get("values", []) if isinstance(data, dict) else []
            ours = sum(1 for v in values if isinstance(v, str) and v.startswith("tfcflorae:"))
            if not values:
                origin = "空"
            elif ours == len(values):
                origin = "自有"
            elif ours:
                origin = "混合"
            else:
                origin = "纯外部"
            rows.append({
                "ns": ns,
                "kind": kind,
                "path": path,
                "id": "%s:%s" % (ns, path),
                "count": len(values),
                "origin": origin,
                "purpose": purpose_for(path, kind),
                "replace": data.get("replace") if isinstance(data, dict) else None,
            })

    rows.sort(key=lambda r: (r["ns"], r["kind"], r["path"]))
    print("total tag files:", len(rows))
    by = collections.Counter((r["ns"], r["kind"]) for r in rows)
    for k, v in sorted(by.items()):
        print("  %-10s %-12s %d" % (k[0], k[1], v))

    out = os.path.join(ROOT, "reports", "tag-catalog.md")
    lines = ["# 本模组的 tag 清单（自动生成）", ""]
    lines.append("共 **%d** 个 tag 文件。分三类：" % len(rows))
    lines.append("")
    lines.append("- `tfcflorae:` 自有 tag —— 模组自己的分组，供配方/其它模组引用")
    lines.append("- `tfc:` 追加 tag —— 以 `replace:false` 把本模组方块/物品并入 TFC 的既有 tag（TFC 机制据此生效）")
    lines.append("- `forge:` / `minecraft:` 追加 tag —— 并入通用 tag（配方、工具、方块类别）")
    lines.append("")

    for ns in ("tfc", "forge", "minecraft", "tfcflorae"):
        sub = [r for r in rows if r["ns"] == ns]
        if not sub:
            continue
        lines.append("## %s 命名空间（%d 个）" % (ns, len(sub)))
        lines.append("")
        for kind in sorted({r["kind"] for r in sub}):
            part = [r for r in sub if r["kind"] == kind]
            lines.append("### %s / %s（%d 个）" % (ns, kind, len(part)))
            lines.append("")
            lines.append("| tag | 条目数 | 来源 | 用途 |")
            lines.append("|---|---:|---|---|")
            for r in part:
                lines.append("| `%s` | %d | %s | %s |" % (r["id"], r["count"], r["origin"], r["purpose"]))
            lines.append("")
    open(out, "w", encoding="utf-8", newline="\n").write("\n".join(lines))
    print("wrote", out, len(lines), "lines")


if __name__ == "__main__":
    main()
