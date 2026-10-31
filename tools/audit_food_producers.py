"""Audit every TFCFFood entry: is there a mechanism that actually produces it?

Evidence classes collected per food item (id = tfcflorae:food/<name>):

  WOOD   the item is the `productItem` of a TFCFWood tree species (fruiting leaves/log)
  PLANT  the item is the fruit supplier of a TFCFPlant entry (berry bush / plant)
  BLOCK  the item is referenced from TFCFBlocks (fruiting cactus / fruiting plant block)
  RECIPE some recipe under data/*/recipes outputs it
  LOOT   some loot table drops it
  LANG   a zh_cn / en_us name exists
  MODEL  an item model exists

Anything with none of WOOD / PLANT / BLOCK / RECIPE / LOOT has no known producer and is a
candidate orphan.  The script prints the table plus a per-item evidence trail so the
mechanism can be judged by hand (a berry bush that is never world-generated still counts as
"produced" only if it is obtainable another way - that second level is checked separately).
"""
import json
import os
import re

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
JAVA = os.path.join(ROOT, "src", "main", "java")
RES = os.path.join(ROOT, "src", "main", "resources")


def read(path):
    with open(path, encoding="utf-8", errors="ignore") as fh:
        return fh.read()


def food_constants():
    src = read(os.path.join(JAVA, "tfcflorae", "common", "items", "TFCFFood.java"))
    body = src[src.index("public enum TFCFFood {"):src.index(";", src.index("public enum TFCFFood {"))]
    names = re.findall(r"^\s{3}([A-Z][A-Z0-9_]*)\s*\(", body, re.M)
    return names


def main():
    foods = food_constants()
    print("TFCFFood 条目数:", len(foods))
    print()

    wood = read(os.path.join(JAVA, "tfcflorae", "common", "blocks", "wood", "TFCFWood.java"))
    plant = read(os.path.join(JAVA, "tfcflorae", "common", "blocks", "plant", "TFCFPlant.java"))
    blocks = read(os.path.join(JAVA, "tfcflorae", "common", "blocks", "TFCFBlocks.java"))

    # recipes / loot tables: scan all json once, index by any tfcflorae:food/... mention
    recipes = {}
    loots = {}
    for base, store in ((os.path.join(RES, "data"), recipes),):
        for dp, _, fs in os.walk(base):
            for f in fs:
                if not f.endswith(".json"):
                    continue
                p = os.path.join(dp, f)
                rel = os.path.relpath(p, RES).replace(os.sep, "/")
                try:
                    text = read(p)
                except Exception:
                    continue
                for m in re.finditer(r"tfcflorae:food/([a-z0-9_]+)", text):
                    store.setdefault(m.group(1), []).append(rel)

    zh = json.loads(read(os.path.join(RES, "assets", "tfcflorae", "lang", "zh_cn.json")))
    en = json.loads(read(os.path.join(RES, "assets", "tfcflorae", "lang", "en_us.json")))
    models = set()
    for dp, _, fs in os.walk(os.path.join(RES, "assets", "tfcflorae", "models", "item")):
        for f in fs:
            models.add(f[:-5])

    rows = []
    for name in foods:
        low = name.lower()
        wood_hit = "TFCFFood." + name in wood
        plant_hit = "TFCFFood." + name in plant
        block_hit = "TFCFFood." + name in blocks
        rec = recipes.get(low, [])
        # loot tables live under data/*/loot_tables, already covered by the same scan
        loot = [r for r in rec if "/loot_tables/" in r or "loot" in r]
        rec_only = [r for r in rec if r not in loot]
        key = "item.tfcflorae.food.%s" % low
        has_lang = key in zh and key in en
        has_model = low in models
        rows.append(dict(name=name, low=low, wood=wood_hit, plant=plant_hit, block=block_hit,
                         rec=rec_only, loot=loot, lang=has_lang, model=has_model))

    hdr = "%-30s %-5s %-5s %-5s %-6s %-6s %-5s %-5s" % ("ITEM", "WOOD", "PLANT", "BLOCK", "RECIPE", "LOOT", "LANG", "MODEL")
    print(hdr)
    print("-" * len(hdr))
    for r in rows:
        print("%-30s %-5s %-5s %-5s %-6d %-6d %-5s %-5s"
              % (r["name"], "Y" if r["wood"] else ".", "Y" if r["plant"] else ".",
                 "Y" if r["block"] else ".", len(r["rec"]), len(r["loot"]),
                 "Y" if r["lang"] else "MISS", "Y" if r["model"] else "MISS"))

    print()
    orphan = [r for r in rows if not (r["wood"] or r["plant"] or r["block"] or r["rec"] or r["loot"])]
    print("=== 没有任何产出途径的候选孤儿 (%d) ===" % len(orphan))
    for r in orphan:
        print("   ", r["name"])
    print()
    print("=== 有产出的条目，证据明细 ===")
    for r in rows:
        if r in orphan:
            continue
        bits = []
        if r["wood"]:
            bits.append("tree")
        if r["plant"]:
            bits.append("plant")
        if r["block"]:
            bits.append("block")
        if r["rec"]:
            bits.append("recipes=%d" % len(r["rec"]))
        if r["loot"]:
            bits.append("loot=%d" % len(r["loot"]))
        print("   %-30s %s" % (r["name"], ", ".join(bits)))
        for x in (r["rec"] + r["loot"])[:4]:
            print("        ", x)


if __name__ == "__main__":
    main()
