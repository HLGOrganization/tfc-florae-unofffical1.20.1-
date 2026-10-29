"""Remove anthracite (无烟煤) completely: ore blocks, powder item, recipes, assets.

Anthracite is this mod's only ungraded ore (TFCFOre.ANTHRACITE) and one of the two
powders (TFCFPowder.ANTHRACITE).  Removing it means:
  - both ORES mapper predicates stop accepting it (the tables become empty)
  - POWDERS no longer registers powder/anthracite
  - recipes: quern/anthracite and the black dye recipe are deleted, the 12 collapse
    recipes drop ore/anthracite/<rock> from their ingredient list
  - assets + lang keys for ore/anthracite and powder/anthracite
"""
import io
import json
import os
import shutil

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
JAVA = os.path.join(ROOT, "src", "main", "java", "tfcflorae")
RES = os.path.join(ROOT, "src", "main", "resources")
DATA = os.path.join(RES, "data")
ASSETS = os.path.join(RES, "assets", "tfcflorae")
RECIPES = os.path.join(DATA, "tfcflorae", "recipes")

# ---- 1) java: stop registering the ore blocks --------------------------------
p = os.path.join(JAVA, "common", "blocks", "TFCFBlocks.java")
s = io.open(p, encoding="utf-8").read()
n = s.count("ore -> !ore.isGraded(),")
s = s.replace("ore -> !ore.isGraded(),",
              "ore -> !ore.isGraded() && ore != TFCFOre.ANTHRACITE,")
io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("TFCFBlocks: patched %d ore predicates" % n)

# ---- 2) java: drop the powder ----------------------------------------------
p = os.path.join(JAVA, "common", "items", "TFCFItems.java")
s = io.open(p, encoding="utf-8").read()
old = '''   public static final Map<TFCFPowder, RegistryObject<Item>> POWDERS = Helpers.mapOfKeys(
      TFCFPowder.class, powder -> register("powder/" + powder.name(), TFCCreativeTabs.MISC)
   );'''
new = '''   public static final Map<TFCFPowder, RegistryObject<Item>> POWDERS = Helpers.mapOfKeys(
      TFCFPowder.class, powder -> powder != TFCFPowder.ANTHRACITE,
      powder -> register("powder/" + powder.name(), TFCCreativeTabs.MISC)
   );'''
if old in s:
    s = s.replace(old, new, 1)
    io.open(p, "w", encoding="utf-8", newline="\n").write(s)
    print("TFCFItems: POWDERS now skips ANTHRACITE")
else:
    print("!! TFCFItems POWDERS block not matched (check manually)")

# ---- 3) recipes --------------------------------------------------------------
removed = []
for rel in ("quern/anthracite.json", "crafting/dyes/black_dye_anthracite.json"):
    fp = os.path.join(RECIPES, *rel.split("/"))
    if os.path.isfile(fp):
        os.remove(fp)
        removed.append(rel)

patched = []
col_dir = os.path.join(RECIPES, "collapse")
if os.path.isdir(col_dir):
    for fn in sorted(os.listdir(col_dir)):
        if not fn.endswith(".json"):
            continue
        fp = os.path.join(col_dir, fn)
        d = json.load(io.open(fp, encoding="utf-8-sig"))
        ing = d.get("ingredient")
        if isinstance(ing, list):
            keep = [v for v in ing if "ore/anthracite" not in json.dumps(v)]
            if len(keep) != len(ing):
                d["ingredient"] = keep
                io.open(fp, "w", encoding="utf-8", newline="\n").write(
                    json.dumps(d, indent=2, ensure_ascii=False) + "\n")
                patched.append(fn)
print("recipes deleted:", removed)
print("collapse recipes patched:", len(patched))

# ---- 4) assets --------------------------------------------------------------
dropped = 0
for sub in ("blockstates/ore/anthracite", "models/block/ore/anthracite", "models/item/ore/anthracite"):
    d = os.path.join(ASSETS, *sub.split("/"))
    if os.path.isdir(d):
        dropped += len(os.listdir(d))
        shutil.rmtree(d)

for rel in ("models/item/powder/anthracite.json",):
    fp = os.path.join(ASSETS, *rel.split("/"))
    if os.path.isfile(fp):
        os.remove(fp)
        dropped += 1

# textures: anything whose name mentions anthracite
for dirpath, _d, files in os.walk(os.path.join(ASSETS, "textures")):
    for fn in files:
        if "anthracite" in fn:
            os.remove(os.path.join(dirpath, fn))
            dropped += 1
print("asset files dropped:", dropped)

# ---- 5) lang ----------------------------------------------------------------
for lang in ("en_us.json", "zh_cn.json"):
    lp = os.path.join(ASSETS, "lang", lang)
    if not os.path.isfile(lp):
        continue
    d = json.load(io.open(lp, encoding="utf-8-sig"))
    before = len(d)
    for k in list(d):
        if "anthracite" in k.lower() or "anthracite" in str(d[k]).lower():
            del d[k]
    if len(d) != before:
        io.open(lp, "w", encoding="utf-8", newline="\n").write(json.dumps(d, indent=2, ensure_ascii=False) + "\n")
        print("%s: %d lang keys removed" % (lang, before - len(d)))
