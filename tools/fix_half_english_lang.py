"""Re-translate only the entries whose existing Chinese still contains English.

Keeping the original translation style means leaving the 12168 hand-written entries
alone - but a few hundred of them are half English ("earthenware_clay粉砂壤土"),
so those individual values are regenerated from the glossary.
"""
import io
import json
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
sys.path.insert(0, os.path.join(ROOT, "tools"))
import rebuild_zh_cn as R  # noqa: E402

LANG = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae", "lang")
latin = re.compile(r"[A-Za-z]")

en = json.load(io.open(os.path.join(LANG, "en_us.json"), encoding="utf-8-sig"))
zh = json.load(io.open(os.path.join(LANG, "zh_cn.json"), encoding="utf-8-sig"))

fixed = 0
left = []
for k, v in list(zh.items()):
    if not isinstance(v, str) or not latin.search(v):
        continue
    got = R.translate(k)
    if not got:
        text = str(en.get(k, ""))
        for pre in R.STRIP_PREFIX:
            if text.startswith(pre):
                text = text[len(pre):]
                break
        cand = R.fallback(text) if text else None
        got = cand if cand and not latin.search(cand) else None
    if got and not latin.search(got):
        zh[k] = got
        fixed += 1
    else:
        left.append((k, v))

io.open(os.path.join(LANG, "zh_cn.json"), "w", encoding="utf-8", newline="\n").write(
    json.dumps(zh, indent=2, ensure_ascii=False) + "\n")
print("re-translated:", fixed, " still latin:", len(left))
for k, v in left[:12]:
    print("   %-56s %s" % (k.replace("tfcflorae.", "")[:56], v[:40]))
