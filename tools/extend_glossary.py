"""Extend the glossary with Title Case material names and a few missing words."""
import io
import json
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
GLOSSARY = os.path.join(ROOT, "data", "lang-glossary.json")
g = json.load(io.open(GLOSSARY, encoding="utf-8-sig"))
words = g["words"]


def title(key):
    return " ".join(p.capitalize() for p in key.split("_"))


# materials may appear capitalised inside English values
for src, bucket in ((g["woods"], "woods"), (g["rocks"], "rocks"), (g["ore_names"], "ores")):
    for k, v in src.items():
        words.setdefault(title(k), v)
        words.setdefault(k.capitalize(), v)

# extra connectives / qualifiers seen in the report
extra = {
    "Ore": "矿石",
    "Normal": "普通",
    "Poor": "贫瘠",
    "Rich": "富集",
    "Native": "原生",
    "Rock": "岩石",
    "Gravel": "砂砾",
    "Bundle": "捆",
    "Blossom": "花",
    "Cherry": "樱",
    "Elm": "榆",
    "Cypress": "柏",
    "Joshua": "约书亚",
    "Mangrove": "红树",
    "Jacaranda": "蓝花楹",
    "Ipe": "风铃木",
    "Bamboo": "竹",
    "Granite": "花岗岩",
    "Layered": "层叠",
    "Old": "旧",
    "Tile": "瓦",
    "Tiles": "瓦",
    "Sandy": "砂质",
    "Rocky": "多岩",
    "Sandier": "含砂",
    "Sandiest": "富砂",
    "Sand Layer": "沙层",
    "Weathered": "风化",
    "Terracotta": "陶瓦",
    "Coarse": "粗糙",
    "Path": "小径",
    "Dirt Path": "土径",
    "Grass Path": "草径",
    "Dense Grass": "稠密草",
    "Sparse Grass": "稀疏草",
    "Dry Grass": "干燥草",
    "Clay Grass": "黏土草",
    "Mud Brick": "泥砖",
    "Smooth Mud Bricks": "平滑泥砖",
    "Hanging": "悬挂",
    "Vine": "藤蔓",
    "Vines": "藤蔓",
    "Creeper": "匍匐植物",
    "Creeping": "匍匐",
    "Sea": "海",
    "Bush": "灌木",
    "Berry": "浆果",
    "Cactus": "仙人掌",
    "Fern": "蕨",
    "Lichen": "地衣",
    "Moss": "苔藓",
    "Mushroom": "蘑菇",
    "Flower": "花",
    "Fruit": "果实",
    "Seed": "种子",
    "Pile": "堆",
    "Stone Tiles": "石板瓦",
    "Dirty Stone Tiles": "脏石板瓦",
    "Dirtier Stone Tiles": "更脏石板瓦",
    "Dirtiest Stone Tiles": "极脏石板瓦",
    "Flagstone": "石板",
    "Cobbled Bricks": "圆石砖",
    "Mortar And Cobble": "砂浆圆石",
    "Sandy Tiles": "砂瓦",
    "Rockier Compact Dirt": "多岩紧实泥土",
    "Compact Dirt": "紧实泥土",
    "Mycelium Dirt": "菌丝泥土",
    "Rooted Dirt": "缠根泥土",
    "Fire Clay": "耐火黏土",
    "Kaolinite": "高岭石",
    "Large Vessel": "大容器",
    "Jar": "罐",
    "Pot": "锅",
    "Mold": "模具",
    "Ingot": "锭",
    "Double Ingot": "双层锭",
    "Sheet": "板",
    "Double Sheet": "双层板",
    "Tuyere": "风口",
    "Pick": "镐头",
    "Axe": "斧头",
    "Shovel": "铲头",
    "Hoe": "锄头",
    "Hammer": "锤头",
    "Chisel": "凿子",
    "Saw": "锯",
    "Knife": "刀",
    "Scythe": "镰刀",
    "Mace": "钉头锤",
    "Sword": "剑",
    "Spike": "石锥",
    "Anvil": "铁砧",
    "Aqueduct": "引水桥",
    "Magma": "岩浆块",
    "Column": "石柱",
    "Smoke Block": "烟熏块",
    "Pressure Plate": "压力板",
    "Tool Rack": "工具架",
    "Barrel Rack": "桶架",
    "Fence Gate": "栅栏门",
    "Log Fence": "原木栅栏",
    "Wall Sign": "墙上的告示牌",
    "Vertical Support": "竖向支撑梁",
    "Horizontal Support": "横向支撑梁",
    "Scribing Table": "书写桌",
    "Jar Shelf": "罐架",
    "Gear Box": "齿轮箱",
    "Water Wheel": "水车",
    "Bladed Axle": "带刃传动杆",
    "Encased Axle": "封闭传动杆",
    "Trapped Chest": "陷阱箱",
    "Chest Minecart": "运输矿车",
    "Fallen Leaves": "落叶",
}
for k, v in extra.items():
    words.setdefault(k, v)
    if not k.startswith(k.capitalize()):
        words.setdefault(k.capitalize(), v)

g["words"] = words
io.open(GLOSSARY, "w", encoding="utf-8", newline="\n").write(
    json.dumps(g, indent=2, ensure_ascii=False) + "\n")
print("glossary words:", len(words))
