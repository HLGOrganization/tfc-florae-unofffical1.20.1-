"""Fix reversed word order, judged by the template direction.

Chinese puts an adjective before the material ("錾制长石砂岩", "去皮桤木") but a noun
part after it ("赤花岗岩间歇泉", "长石砂岩圆石台阶").  The glossary templates already
encode which is which (%s at the end = adjective, %s at the start = noun part), so the
expected text is generated and compared:

  current == expected                      -> correct, leave it (hand edits too)
  current ends with the material name      -> reversed, replace with expected
  anything else                            -> leave alone

Usage: fix_word_order.py [--apply]
"""
import io
import json
import os
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
LANG = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae", "lang")
GLOSSARY = os.path.join(ROOT, "data", "lang-glossary.json")
APPLY = "--apply" in sys.argv

g = json.load(io.open(GLOSSARY, encoding="utf-8-sig"))
ROCKS, WOODS = g["rocks"], g["woods"]
ROCK_PARTS, WOOD_PARTS = g["rock_parts"], g["wood_parts"]
DECO = {"slab": "台阶", "stairs": "楼梯", "wall": "墙",
        "button": "按钮", "pressure_plate": "压力板"}

zh = json.load(io.open(os.path.join(LANG, "zh_cn.json"), encoding="utf-8-sig"))


def expected_for(fam, rest, material_map, parts):
    mat = rest[-1]
    if mat not in material_map:
        return None
    mat_zh = material_map[mat]
    part_key = ".".join(rest[:-1])
    segs = part_key.split(".")
    deco = None
    if segs and segs[-1] in DECO:
        deco = segs[-1]
        segs = segs[:-1]
    base = ".".join(segs)
    tmpl = parts.get(base)
    if tmpl is None:
        return None
    if "%s" in tmpl:
        name = tmpl % mat_zh
    elif fam == "wood":
        name = mat_zh + tmpl        # wood parts are bare nouns (木板, 原木 ...)
    else:
        name = tmpl
    if deco:
        name += DECO[deco]
    return mat_zh, name


def scan(fam, material_map, parts):
    """Only flag exact reversals: value == <part><material> while expected is
    <material><part>.  Loose suffix tests mis-fired on woods whose Chinese name is
    longer than the glossary entry (洋苏木 vs 苏木), so compare the whole string."""
    found = []
    for k, v in zh.items():
        p = k.split(".")
        if len(p) < 5 or p[0] not in ("block", "item") or p[1] != "tfcflorae" or p[2] != fam:
            continue
        res = expected_for(fam, p[3:], material_map, parts)
        if not res:
            continue
        mat_zh, expected = res
        mat = p[-1]
        part_key = ".".join(p[3:-1])
        if not isinstance(v, str) or v == expected:
            continue
        if not expected.startswith(mat_zh):
            continue                      # adjective form, material not first - fine
        part_zh = expected[len(mat_zh):]
        if material_map.get(mat) == "竹" and part_key in ("log", "stripped_log", "wood"):
            continue
        if v == part_zh + mat_zh:
            found.append((k, v, expected))
    return found


rock_bad = scan("rock", ROCKS, ROCK_PARTS)
wood_bad = scan("wood", WOODS, WOOD_PARTS)

print("reversed rock entries:", len(rock_bad))
for k, old, new in rock_bad[:10]:
    print("   %-56s %s -> %s" % (k.replace("tfcflorae.", "")[:56], old, new))
print("reversed wood entries:", len(wood_bad))
for k, old, new in wood_bad[:8]:
    print("   %-56s %s -> %s" % (k.replace("tfcflorae.", "")[:56], old, new))

if APPLY:
    for k, _old, new in rock_bad + wood_bad:
        zh[k] = new
    io.open(os.path.join(LANG, "zh_cn.json"), "w", encoding="utf-8", newline="\n").write(
        json.dumps(zh, indent=2, ensure_ascii=False) + "\n")
    print("\nrewritten:", len(rock_bad) + len(wood_bad))
else:
    print("\n(dry run - pass --apply)")
