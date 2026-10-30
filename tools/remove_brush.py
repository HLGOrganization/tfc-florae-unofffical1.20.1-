"""Remove the brush item and the whole brushing feature.

There are no brushing recipes at all (data/tfcflorae/recipes/brushing/ does not exist),
so the brush cannot do anything in game.  Removing:

  java      BrushItem, BrushingRecipe
            TFCFItems.BRUSHES, TFCFRecipeSerializers.BRUSHING, TFCFRecipeTypes.BRUSHING,
            TFCFSounds.BRUSHING
  assets    textures/item/tools/brush.png, models/item/tools/brush.json
            sounds/item/brushing/*.ogg and their sounds.json entry
  lang      item.tfcflorae.tools.brush and the tfcflorae.brushing.* strings
"""
import io
import json
import os
import re
import shutil

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
J = os.path.join(ROOT, "src", "main", "java", "tfcflorae")
RES = os.path.join(ROOT, "src", "main", "resources")
A = os.path.join(RES, "assets", "tfcflorae")


def drop_static_field(src, field, kind="RegistryObject"):
    m = re.search(r'[ \t]*public static final [^;=]*\b' + field + r'\b[^=]*=\s*[^;]+;', src)
    if not m:
        return src, False
    start = m.start()
    nl = src.find("\n", m.end())
    return src[:start] + src[nl + 1:], True


# ---- java -----------------------------------------------------------------
for rel in ("common/items/BrushItem.java", "common/recipes/BrushingRecipe.java"):
    p = os.path.join(J, rel)
    if os.path.isfile(p):
        os.remove(p)
        print("deleted", rel)

for rel, field in (
    ("common/items/TFCFItems.java", "BRUSHES"),
    ("common/recipes/TFCFRecipeSerializers.java", "BRUSHING"),
    ("common/recipes/TFCFRecipeTypes.java", "BRUSHING"),
    ("client/TFCFSounds.java", "BRUSHING"),
):
    p = os.path.join(J, rel)
    s = io.open(p, encoding="utf-8").read()
    s, ok = drop_static_field(s, field)
    # clean imports
    for name in ("BrushItem", "BrushingRecipe"):
        s = re.sub(r'^import [\w\.]*\.' + name + r';\n', '', s, flags=re.M)
    io.open(p, "w", encoding="utf-8", newline="\n").write(s)
    print("  removed field", field, "from", rel, "ok" if ok else "NOT FOUND")

# ---- assets ---------------------------------------------------------------
for rel in ("textures/item/tools/brush.png", "models/item/tools/brush.json"):
    p = os.path.join(A, rel)
    if os.path.isfile(p):
        os.remove(p)
        print("deleted asset", rel)

snd = os.path.join(A, "sounds", "item", "brushing")
if os.path.isdir(snd):
    shutil.rmtree(snd)
    print("deleted sounds/item/brushing/")

# sounds.json entry
sp = os.path.join(A, "sounds.json")
if os.path.isfile(sp):
    d = json.load(io.open(sp, encoding="utf-8-sig"))
    if "item.brushing.brushing" in d:
        del d["item.brushing.brushing"]
        io.open(sp, "w", encoding="utf-8", newline="\n").write(json.dumps(d, indent=2) + "\n")
        print("removed sounds.json entry")

# ---- lang -----------------------------------------------------------------
DROP_KEYS = [
    "item.tfcflorae.tools.brush",
    "tfcflorae.brushing",
    "tfcflorae.brushing.bad_fluid",
    "tfcflorae.brushing.cannot_place",
    "tfcflorae.brushing.no_recipe",
]
for name in ("en_us.json", "zh_cn.json"):
    p = os.path.join(A, "lang", name)
    d = json.load(io.open(p, encoding="utf-8-sig"))
    before = len(d)
    for k in DROP_KEYS:
        d.pop(k, None)
    if len(d) != before:
        io.open(p, "w", encoding="utf-8", newline="\n").write(
            json.dumps(d, indent=2, ensure_ascii=False) + "\n")
        print("%s: removed %d keys" % (name, before - len(d)))

# ---- report remaining references -----------------------------------------
left = []
for dirpath, _d, files in os.walk(J):
    for fn in files:
        if not fn.endswith(".java"):
            continue
        p = os.path.join(dirpath, fn)
        t = io.open(p, encoding="utf-8").read()
        if re.search(r'\b(BrushItem|BrushingRecipe|BRUSHES)\b', t):
            left.append(os.path.relpath(p, J))
print("remaining java references:", left if left else "none")
