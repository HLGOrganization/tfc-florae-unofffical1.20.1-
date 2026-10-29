"""How much of our untranslated zh_cn can be filled from TFC's own Chinese files?

TFC ships en_us + zh_cn.  For every key whose English value matches one of ours,
we can reuse TFC's Chinese value verbatim (that is literally "reference the
official translation").  This measures the coverage of that trick.
"""
import io
import json
import os
import zipfile

TFC = r"D:\学习资料\附属模组工作区\[群峦传说] TerraFirmaCraft-Forge-1.20.1-3.2.20.jar"
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
LANG = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae", "lang")

with zipfile.ZipFile(TFC) as z:
    tfc_en = json.loads(z.read("assets/tfc/lang/en_us.json"))
    tfc_zh = json.loads(z.read("assets/tfc/lang/zh_cn.json"))

print("TFC en keys:", len(tfc_en), " zh keys:", len(tfc_zh))

# English value -> Chinese value (only where both exist and differ)
val_map = {}
for k, en in tfc_en.items():
    zh = tfc_zh.get(k)
    if zh and isinstance(en, str) and isinstance(zh, str) and zh != en:
        val_map.setdefault(en, set()).add(zh)

print("distinct en->zh value pairs:", len(val_map))

ours_en = json.load(io.open(os.path.join(LANG, "en_us.json"), encoding="utf-8-sig"))
ours_zh = json.load(io.open(os.path.join(LANG, "zh_cn.json"), encoding="utf-8-sig"))

untranslated = [k for k, v in ours_zh.items() if ours_en.get(k) == v]
print("our untranslated entries:", len(untranslated))

hit = [k for k in untranslated if ours_en[k] in val_map]
multi = [k for k in hit if len(val_map[ours_en[k]]) > 1]
print("directly covered by TFC value map:", len(hit), "(of which ambiguous:", len(multi), ")")
print("remaining:", len(untranslated) - len(hit))

for k in hit[:10]:
    print("   %-58s %-28s -> %s" % (k, ours_en[k][:28], list(val_map[ours_en[k]])[0]))
print("\nremaining sample:")
for k in [k for k in untranslated if k not in hit][:12]:
    print("   %-58s %s" % (k, ours_en[k][:50]))
