import io
import json
import os
from collections import Counter

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
LANG = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae", "lang")
en = json.load(io.open(os.path.join(LANG, "en_us.json"), encoding="utf-8-sig"))
zh = json.load(io.open(os.path.join(LANG, "zh_cn.json"), encoding="utf-8-sig"))
un = {k: v for k, v in zh.items() if en.get(k) == v}

fam = Counter()
for k in un:
    parts = k.split(".")
    if parts[0] == "block" and len(parts) > 2:
        fam["block." + parts[2]] += 1
    elif parts[0] == "item" and len(parts) > 2:
        fam["item." + parts[2]] += 1
    else:
        fam[parts[0]] += 1

print("untranslated:", len(un))
for f, n in fam.most_common(40):
    print("   %-40s %d" % (f, n))

# specific wood/rock shape
wood = [k for k in un if k.startswith("block.tfcflorae.wood.")]
rock = [k for k in un if k.startswith("block.tfcflorae.rock.")]
plant = [k for k in un if k.startswith("block.tfcflorae.plant.")]
itemwood = [k for k in un if k.startswith("item.tfcflorae.wood.")]
print("\nwood blocks:", len(wood), " rock blocks:", len(rock), " plant blocks:", len(plant), " wood items:", len(itemwood))
print("\nwood block samples:")
for k in wood[:6]:
    print("   %-58s %s" % (k, un[k]))
print("\nitem wood samples:")
for k in itemwood[:6]:
    print("   %-58s %s" % (k, un[k]))
