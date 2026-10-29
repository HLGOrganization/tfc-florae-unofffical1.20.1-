"""Mine TFC's zh_cn into material + part glossaries.

TFC translates block.tfc.wood.log.oak as "橡木原木" (material + part) and
block.tfc.rock.raw.granite as "花岗岩原岩".  By subtracting the known part word we
get the material's Chinese name, and by subtracting the material we get the part's.

Prints:
  * TFC wood names        (en -> zh)
  * TFC rock names        (en -> zh)
  * TFC part names        (key segment -> zh)  from wood and rock families
"""
import io
import json
import zipfile
from collections import Counter, defaultdict

TFC = r"D:\学习资料\附属模组工作区\[群峦传说] TerraFirmaCraft-Forge-1.20.1-3.2.20.jar"
with zipfile.ZipFile(TFC) as z:
    en = json.loads(z.read("assets/tfc/lang/en_us.json"))
    zh = json.loads(z.read("assets/tfc/lang/zh_cn.json"))

def get(k):
    kk = "block.tfc." + k
    return en.get(kk), zh.get(kk)

# ---- wood names: block.tfc.wood.log.<wood> == "<material>原木"
woods = {}
for k, v in zh.items():
    if k.startswith("block.tfc.wood.log.") and isinstance(v, str) and v.endswith("原木"):
        woods[k.split(".")[-1]] = v[:-2]
print("TFC woods:", len(woods))
print("   ", list(woods.items())[:8])

# ---- rock names: block.tfc.rock.raw.<rock> == "<material>原岩"
rocks = {}
for k, v in zh.items():
    if k.startswith("block.tfc.rock.raw.") and isinstance(v, str) and v.endswith("原岩"):
        rocks[k.split(".")[-1]] = v[:-2]
print("TFC rocks:", len(rocks))
print("   ", list(rocks.items())[:8])

# ---- part names from wood family
parts = Counter()
for k, v in zh.items():
    if k.startswith("block.tfc.wood.") and isinstance(v, str):
        seg = k[len("block.tfc.wood."):].split(".")
        if len(seg) < 2:
            continue
        part, wood = ".".join(seg[:-1]), seg[-1]
        mat = woods.get(wood)
        if mat and v.startswith(mat):
            rest = v[len(mat):]
            if rest:
                parts[(part, rest)] += 1
print("\nwood part -> zh (top 25):")
for (part, rest), n in parts.most_common(25):
    print("   %-28s %-10s %d" % (part, rest, n))

rparts = Counter()
for k, v in zh.items():
    if k.startswith("block.tfc.rock.") and isinstance(v, str):
        seg = k[len("block.tfc.rock."):].split(".")
        if len(seg) < 2:
            continue
        part, rock = ".".join(seg[:-1]), seg[-1]
        mat = rocks.get(rock)
        if mat and v.startswith(mat):
            rest = v[len(mat):]
            if rest:
                rparts[(part, rest)] += 1
print("\nrock part -> zh (top 25):")
for (part, rest), n in rparts.most_common(25):
    print("   %-28s %-10s %d" % (part, rest, n))

io.open("reports/tfc-glossary.json", "w", encoding="utf-8", newline="\n").write(
    json.dumps({"woods": woods, "rocks": rocks,
                "wood_parts": {p: r for (p, r), n in parts.most_common()},
                "rock_parts": {p: r for (p, r), n in rparts.most_common()}},
               indent=1, ensure_ascii=False) + "\n")
print("\nwrote reports/tfc-glossary.json")
