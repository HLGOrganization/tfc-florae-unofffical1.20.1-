"""Hand-fix the few remaining non-item strings that no glossary rule can build."""
import io
import json
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
LANG = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae", "lang")

FIX = {
    "tfcflorae.brushing.bad_fluid": "该方块的刷制版本无法容纳这种流体",
    "tfcflorae.brushing.cannot_place": "该方块的刷制版本无法放置在这里",
    "tfcflorae.commands.locatebiome.radius_greater_than_1024": "警告：搜索半径过大，可能让世界卡顿较长时间",
    "tfcflorae.creative_tab.blocks": "群峦植物群系：方块",
    "tfcflorae.creative_tab.flora": "群峦植物群系：植物",
    "tfcflorae.creative_tab.items": "群峦植物群系：物品",
}

p = os.path.join(LANG, "zh_cn.json")
d = json.load(io.open(p, encoding="utf-8-sig"))
n = 0
for k, v in FIX.items():
    if k in d:
        d[k] = v
        n += 1
io.open(p, "w", encoding="utf-8", newline="\n").write(
    json.dumps(d, indent=2, ensure_ascii=False) + "\n")
print("fixed:", n, "of", len(FIX))

import re
latin = re.compile(r"[A-Za-z]")
bad = [(k, v) for k, v in d.items() if isinstance(v, str) and latin.search(v)]
print("values still containing latin:", len(bad))
for k, v in bad[:8]:
    print("   ", k.replace("tfcflorae.", ""), "->", v[:50])
