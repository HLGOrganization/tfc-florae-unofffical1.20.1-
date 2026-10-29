"""Work out how to rebuild zh_cn: suffix-match TFC nouns, collect the prefixes.

Our untranslated values look like "<OurPrefix> <TFC noun>", e.g.
"Dry Grass Bog Iron" or "Grass Loam".  TFC translates the noun but never that
prefix, so matching the longest TFC English value that is a suffix of ours gives
the noun's Chinese, and leaves a small, repetitive set of prefixes to translate by
hand.
"""
import io
import json
import os
import zipfile
from collections import Counter

TFC = r"D:\学习资料\附属模组工作区\[群峦传说] TerraFirmaCraft-Forge-1.20.1-3.2.20.jar"
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
LANG = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae", "lang")

with zipfile.ZipFile(TFC) as z:
    tfc_en = json.loads(z.read("assets/tfc/lang/en_us.json"))
    tfc_zh = json.loads(z.read("assets/tfc/lang/zh_cn.json"))

val_map = {}
for k, en in tfc_en.items():
    zh = tfc_zh.get(k)
    if zh and isinstance(en, str) and isinstance(zh, str) and zh != en:
        val_map.setdefault(en, zh)

ours_en = json.load(io.open(os.path.join(LANG, "en_us.json"), encoding="utf-8-sig"))
ours_zh = json.load(io.open(os.path.join(LANG, "zh_cn.json"), encoding="utf-8-sig"))
untranslated = {k: v for k, v in ours_zh.items() if ours_en.get(k) == v}

suffix_keys = sorted(val_map, key=len, reverse=True)
prefixes = Counter()
matched = {}
unmatched = []
for k, v in untranslated.items():
    hit = None
    for s in suffix_keys:
        if v == s or v.endswith(" " + s):
            hit = s
            break
    if hit is None:
        unmatched.append((k, v))
    else:
        prefix = v[: len(v) - len(hit)].strip()
        prefixes[prefix] += 1
        matched[k] = (prefix, hit, val_map[hit])

print("untranslated:", len(untranslated))
print("suffix-matched:", len(matched), " unmatched:", len(unmatched))
print("\ndistinct prefixes:", len(prefixes))
for p, n in prefixes.most_common(40):
    print("   %-38s %4d   e.g. %s" % (p if p else "(none)", n, val_map[next(s for s in suffix_keys if s in [vv[1] for kk, vv in matched.items() if vv[0] == p][:1])] if p in [vv[0] for vv in matched.values()] else ""))
print("\nunmatched sample:")
for k, v in unmatched[:15]:
    print("   %-58s %s" % (k, v))
