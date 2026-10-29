"""Top up the glossary for the families that still leaked English."""
import io
import json
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
GLOSSARY = os.path.join(ROOT, "data", "lang-glossary.json")
g = json.load(io.open(GLOSSARY, encoding="utf-8-sig"))

# rock parts seen in the data but missing from the glossary
g["rock_parts"].update({
    "flagstone": "%s石板",
    "polished_column": "打磨%s石柱",
    "smoke_block": "%s烟熏块",
    "magma": "%s岩浆块",
    "loess": "%s黄土",
    "dripstone": "%s滴水石",
    "hardened": "硬化%s",
    "mossy_loose": "覆苔%s石子",
    "cobbled_bricks": "%s圆石砖",
    "mortar_and_cobble": "%s砂浆圆石",
    "stone_tiles": "%s石板瓦",
    "dirty_stone_tiles": "脏%s石板瓦",
    "dirtier_stone_tiles": "更脏%s石板瓦",
    "dirtiest_stone_tiles": "极脏%s石板瓦",
})

extra = {
    # plants / fungi that were fragmenting into English
    "Venus Flytrap": "捕蝇草",
    "Voodoo Lily": "巫毒百合",
    "Yarrow": "蓍草",
    "Turkey Tail": "火鸡尾菇",
    "Water Hyacinth": "水葫芦",
    "Water Plantain": "泽泻",
    "Weeping Milk Cap": "流泪奶盖菇",
    "Wheatgrass": "小麦草",
    "Whitestem Pondweed": "白茎眼子菜",
    "Wood Blewit": "紫丁香蘑",
    "Woolly Gomphus": "绵毛钉菇",
    "Pond Grass": "池草",
    "Jack in the Pulpit": "天南星",
    "Polished": "打磨",
    "Polished Column": "打磨石柱",
    "Loose": "松散",
    "Flint": "燧石",
    "Spider Cave": "蜘蛛洞穴",
    "Webbed": "覆网",
    "Dripstone": "滴水石",
    "Loose Rock": "松散岩石",
    "Mineral Sheet": "矿物板",
    "Animal": "动物",
    "Crystal": "水晶",
    "Food": "食物",
    "Device": "装置",
    "Devices": "装置",
    "Bundle": "捆",
    "Head": "头",
    "Leaves Head": "树叶顶部",
    "Old": "旧",
    "Old Barrels": "旧大桶",
    "Sand Layer": "沙层",
    "Sandy": "砂质",
    "Sandier": "含砂",
    "Sandiest": "富砂",
    "Tiles": "瓦",
    "Rock": "岩石",
    "Rocky": "多岩",
    "Loam": "壤土",
    "Silt": "粉砂土",
    "Sandy Loam": "砂壤土",
    "Silty Loam": "粉砂壤土",
    "Fluvisol": "冲积土",
    "Mollisol": "软土",
    "Oxisol": "氧化土",
    "Podzol": "灰化土",
}
for k, v in extra.items():
    g["words"].setdefault(k, v)

io.open(GLOSSARY, "w", encoding="utf-8", newline="\n").write(
    json.dumps(g, indent=2, ensure_ascii=False) + "\n")
print("glossary words:", len(g["words"]), " rock templates:", len(g["rock_parts"]))
