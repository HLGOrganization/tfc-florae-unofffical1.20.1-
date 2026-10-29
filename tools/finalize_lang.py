"""Final pass: drop foreign-namespace keys, top up the glossary, rebuild zh_cn."""
import io
import json
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
LANG = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae", "lang")
GLOSSARY = os.path.join(ROOT, "data", "lang-glossary.json")

# 1) remove keys that belong to TFC (biome.tfc.*) from both files - they would
#    override TFC's own translations
for name in ("en_us.json", "zh_cn.json"):
    p = os.path.join(LANG, name)
    d = json.load(io.open(p, encoding="utf-8-sig"))
    before = len(d)
    d = {k: v for k, v in d.items() if not k.startswith("biome.tfc.")}
    if len(d) != before:
        io.open(p, "w", encoding="utf-8", newline="\n").write(
            json.dumps(d, indent=2, ensure_ascii=False) + "\n")
        print("%s: removed %d foreign keys" % (name, before - len(d)))

# 2) glossary top-up
g = json.load(io.open(GLOSSARY, encoding="utf-8-sig"))
extra = {
    "Chiseled": "雕刻", "Cobble": "圆石", "Silty": "粉砂质", "Deposit": "矿床",
    "Cracked": "裂纹", "Glazed": "上釉", "Potted": "盆栽", "Roasted": "烤",
    "Blade": "刃", "Charred": "炭化", "Silk": "丝", "Acacia": "金合欢", "Moth": "蛾",
    "The": "", "Shore": "海岸", "Hardened": "硬化", "Bowl": "碗", "Jug": "罐",
    "Javelin": "标枪", "Pickaxe": "镐", "Propick": "探矿镐", "Spindle": "纺锤",
    "Soil": "土壤", "Knapping": "打制", "Alpine": "高山", "River": "河流",
    "Mountains": "山地", "Plateau": "高原", "Pulpit": "猪笼草", "Lavender": "薰衣草",
    "Lions": "狮", "Mane": "鬃", "Lobster": "龙虾", "Claws": "钳", "Narbon": "纳博讷",
    "Parasol": "伞菌", "Periwinkle": "长春花", "Pieris": "马醉木", "Pitahaya": "火龙果",
    "Porcini": "牛肝菌", "Junegrass": "六月禾", "Quaqua": "海星花", "Reishi": "灵芝",
    "Royal": "皇", "Jasmine": "茉莉", "Saltwort": "盐角草", "Sawgrass": "锯齿草",
    "Anemone": "银莲花", "Seagrass": "海草", "Seaweed": "海藻", "Shaggy": "毛状",
    "Bracket": "层菌", "Shiitake": "香菇", "Sulphur": "硫磺", "Shelf": "架",
    "Thistle": "蓟", "Titan": "泰坦", "Arum": "天南星", "Leaf": "叶", "Litter": "枯枝层",
    "Low": "低", "Liana": "藤", "Maidenhair": "铁线蕨", "Mallow": "锦葵", "Marram": "滨草",
}
for k, v in extra.items():
    g["words"].setdefault(k, v)
io.open(GLOSSARY, "w", encoding="utf-8", newline="\n").write(
    json.dumps(g, indent=2, ensure_ascii=False) + "\n")
print("glossary words:", len(g["words"]))
