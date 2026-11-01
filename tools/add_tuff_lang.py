# -*- coding: utf-8 -*-
"""给 tuff 的 8 种多石土 + 7 种多石沙补 lang 条目。

命名规则直接照现有同族条目推出：
  zh_cn:  "block.tfcflorae.rockier_compact_dirt.<soil>.granite" = "花岗岩混石壤土"
          -> 用 granite 那条的模板，把「花岗岩」换成「凝灰岩」
  sand:   "block.tfcflorae.sand.rocky.<color>.granite" = ?
          -> 同样取 granite 那条，替换岩石名
"""
import json
import os
import re

ROOT = r"D:\学习资料\附属模组工作区\tfc_florae_port"
LANG = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae", "lang")

SOILS = ["fluvisol", "loam", "mollisol", "oxisol", "podzol", "sandy_loam", "silt", "silty_loam"]
COLORS = ["black", "brown", "green", "pink", "red", "white", "yellow"]

# 岩石名在各语言里的写法
ROCK_NAME = {
    "zh_cn": "凝灰岩",
    "en_us": "Tuff",
}
REF_NAME = {
    "zh_cn": "花岗岩",
    "en_us": "Granite",
}


def process(lang):
    p = os.path.join(LANG, lang + ".json")
    data = json.load(open(p, encoding="utf-8"))
    added = []

    for soil in SOILS:
        ref_key = "block.tfcflorae.rockier_compact_dirt.%s.granite" % soil
        new_key = "block.tfcflorae.rockier_compact_dirt.%s.tuff" % soil
        if ref_key not in data:
            print("  跳过（无参照）%s" % ref_key)
            continue
        if new_key in data:
            continue
        data[new_key] = data[ref_key].replace(REF_NAME[lang], ROCK_NAME[lang])
        added.append(new_key)

    for color in COLORS:
        ref_key = "block.tfcflorae.sand.rocky.%s.granite" % color
        new_key = "block.tfcflorae.sand.rocky.%s.tuff" % color
        if ref_key not in data:
            print("  跳过（无参照）%s" % ref_key)
            continue
        if new_key in data:
            continue
        data[new_key] = data[ref_key].replace(REF_NAME[lang], ROCK_NAME[lang])
        added.append(new_key)

    # 保持与原文相同的「键有序、UTF-8 无 BOM、2 空格缩进」
    with open(p, "w", encoding="utf-8", newline="\n") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
        f.write("\n")

    print("%s: 新增 %d 条" % (lang, len(added)))
    for k in added[:3]:
        print("   %s = %s" % (k, data[k]))
    return len(added)


total = 0
for lang in ("zh_cn", "en_us"):
    total += process(lang)
print("合计 %d 条" % total)
