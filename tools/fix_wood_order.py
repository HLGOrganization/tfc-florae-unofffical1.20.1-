"""Fix wood entries whose value still ends with the material ("命名台黄蓝花楹").

Wood names belong at the front ("黄蓝花楹命名台").  Exceptions kept as-is:
  * log / wood - their Chinese names are not a plain <material><part> ("原竹", "洋苏木")
  * stripped_log / stripped_wood - adjectives ("去皮桤木")
"""
import io
import json
import os
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
LANG = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae", "lang")
GLOSSARY = os.path.join(ROOT, "data", "lang-glossary.json")
APPLY = "--apply" in sys.argv

g = json.load(io.open(GLOSSARY, encoding="utf-8-sig"))
WOODS = g["woods"]
SKIP_PARTS = {"log", "wood", "stripped_log", "stripped_wood"}

zh = json.load(io.open(os.path.join(LANG, "zh_cn.json"), encoding="utf-8-sig"))

fixed = []
for k, v in zh.items():
    p = k.split(".")
    if len(p) < 5 or p[0] not in ("block", "item") or p[1] != "tfcflorae" or p[2] != "wood":
        continue
    mat = p[-1]
    part = ".".join(p[3:-1])
    if mat not in WOODS or part in SKIP_PARTS or not isinstance(v, str):
        continue
    m = WOODS[mat]
    if v.endswith(m) and not v.startswith(m) and len(v) > len(m):
        fixed.append((k, v, m + v[: -len(m)]))

print("wood entries to reorder:", len(fixed))
for k, old, new in fixed[:14]:
    print("   %-52s %s -> %s" % (k.replace("tfcflorae.", "")[:52], old, new))

if APPLY:
    for k, _o, n in fixed:
        zh[k] = n
    io.open(os.path.join(LANG, "zh_cn.json"), "w", encoding="utf-8", newline="\n").write(
        json.dumps(zh, indent=2, ensure_ascii=False) + "\n")
    print("rewritten:", len(fixed))
else:
    print("(dry run - pass --apply)")
