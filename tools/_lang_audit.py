"""Audit the rebuilt zh_cn for leftover English and awkward word order."""
import io
import json
import os
import re
from collections import Counter, defaultdict

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
LANG = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae", "lang")
en = json.load(io.open(os.path.join(LANG, "en_us.json"), encoding="utf-8-sig"))
zh = json.load(io.open(os.path.join(LANG, "zh_cn.json"), encoding="utf-8-sig"))

latin = re.compile(r"[A-Za-z]")
withlatin = [(k, zh[k]) for k in zh if isinstance(zh[k], str) and latin.search(zh[k])]
print("entries containing latin letters:", len(withlatin))
fam = Counter()
for k, v in withlatin:
    parts = k.split(".")
    fam[".".join(parts[:3])] += 1
for f, n in fam.most_common(12):
    print("   %-42s %d" % (f, n))

print("\nsamples:")
for k, v in withlatin[:20]:
    print("   %-56s %s" % (k.replace("tfcflorae.", "")[:56], v[:50]))

# how many are exactly the english value
same = [k for k in zh if zh[k] == en.get(k)]
print("\nstill identical to english:", len(same))

# spot-check ordering for a few families
print("\nfamily samples:")
for pref in ("block.tfcflorae.wood.", "item.tfcflorae.wood.", "block.tfcflorae.rock.",
             "item.tfcflorae.ore.", "block.tfcflorae.sand.", "block.tfcflorae.ceramic.",
             "item.tfcflorae.plant.", "block.tfcflorae.plant."):
    ks = [k for k in zh if k.startswith(pref)][:3]
    for k in ks:
        print("   %-58s %s" % (k.replace("tfcflorae.", "")[:58], zh[k][:44]))
