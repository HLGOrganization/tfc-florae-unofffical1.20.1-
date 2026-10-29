import io
import json
import os
from collections import Counter

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
LANG = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae", "lang")
en = json.load(io.open(os.path.join(LANG, "en_us.json"), encoding="utf-8-sig"))
zh = json.load(io.open(os.path.join(LANG, "zh_cn.json"), encoding="utf-8-sig"))
un = [(k, v) for k, v in zh.items() if en.get(k) == v]

words = Counter()
for _k, v in un:
    for w in str(v).split():
        words[w] += 1

print("distinct words in untranslated values:", len(words))
print("top 120:")
line = []
for w, n in words.most_common(120):
    line.append("%s:%d" % (w, n))
print("  " + ", ".join(line))
