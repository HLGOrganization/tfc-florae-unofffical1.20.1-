"""Finish the TFE soil variants: item models, translations, and drop the pebble-mixing use().

1. item models - the block models were generated but not the item forms, so the blocks had
   no inventory icon at all (that is the "missing texture").
2. translations - 4 variants x 32 rocks x 2 languages, following the existing naming
   ("<rock>混石<soil>"), rock names taken from the existing loam keys.
3. RockySoilBlock.use() - holding a loose rock and right-clicking the block shrank the
   stack and replaced the block.  That pebble-mixing mechanic is supposed to be gone, so
   use() now passes through.
"""
import io
import json
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
A = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae")
JAVA = os.path.join(ROOT, "src", "main", "java", "tfcflorae")

VARIANTS = ["fluvisol", "mollisol", "oxisol", "podzol"]
ROCKS = ["andesite", "arkose", "basalt", "boninite", "carbonatite", "cataclasite",
         "chalk", "chert", "claystone", "conglomerate", "dacite", "diorite", "dolomite",
         "gabbro", "gneiss", "granite", "jaspillite", "laterite", "limestone", "marble",
         "mudstone", "phyllite", "porphyry", "quartzite", "red_granite", "rhyolite",
         "sandstone", "schist", "shale", "slate", "soapstone", "wackestone"]

# ---- 1) item models -------------------------------------------------------
made = 0
for v in VARIANTS:
    for r in ROCKS:
        out = os.path.join(A, "models", "item", "rockier_compact_dirt", v, r + ".json")
        os.makedirs(os.path.dirname(out), exist_ok=True)
        model = {
            "__comment__": "This file was automatically created by mcresources",
            "parent": "tfcflorae:block/rockier_compact_dirt/%s/%s" % (v, r),
        }
        io.open(out, "w", encoding="utf-8", newline="\n").write(json.dumps(model, indent=2) + "\n")
        made += 1
print("item models written:", made)

# ---- 2) translations ------------------------------------------------------
SOIL_ZH = {"fluvisol": "冲积土", "mollisol": "软土", "oxisol": "氧化土", "podzol": "灰化土"}
SOIL_EN = {"fluvisol": "Fluvisol", "mollisol": "Mollisol", "oxisol": "Oxisol", "podzol": "Podzol"}

for lang, soil_map in (("en_us.json", SOIL_EN), ("zh_cn.json", SOIL_ZH)):
    p = os.path.join(A, "lang", lang)
    d = json.load(io.open(p, encoding="utf-8-sig"))
    # learn the rock naming from the existing loam keys
    rock_zh = {}
    rock_en = {}
    for r in ROCKS:
        rock_zh[r] = d.get("block.tfcflorae.rockier_compact_dirt.loam." + r, r)
        rock_en[r] = r.replace("_", " ").title()
    added = 0
    for v in VARIANTS:
        for r in ROCKS:
            key = "block.tfcflorae.rockier_compact_dirt.%s.%s" % (v, r)
            if key in d:
                continue
            if lang == "zh_cn.json":
                base = rock_zh[r].replace("混石壤土", "").replace("混石", "")
                d[key] = base + "混石" + soil_map[v]
            else:
                # existing english style: "<Rock> Rocky Soil" -> reuse rock word
                d[key] = "%s-Bearing %s" % (rock_en[r], soil_map[v])
            added += 1
    # also the block item name (single key shared by all rocks is not used, but keep tidy)
    io.open(p, "w", encoding="utf-8", newline="\n").write(
        json.dumps(d, indent=2, ensure_ascii=False) + "\n")
    print(lang, "added", added, "keys")

# ---- 3) remove the loose-rock use() --------------------------------------
p = os.path.join(JAVA, "common", "blocks", "soil", "RockySoilBlock.java")
s = io.open(p, encoding="utf-8").read()
start = s.find("public InteractionResult use(BlockState state")
assert start != -1, "use() not found"
# brace-match the method body
brace = s.find("{", start)
depth = 0
i = brace
while i < len(s):
    if s[i] == "{":
        depth += 1
    elif s[i] == "}":
        depth -= 1
        if depth == 0:
            break
    i += 1
new_body = """public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
   {
      // 石子与多石土的互转机制已废弃：手持石子点击不再消耗石子、也不改变方块
      return InteractionResult.PASS;
   }"""
s = s[:start] + new_body + s[i + 1:]
io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("RockySoilBlock.use() neutralised")
