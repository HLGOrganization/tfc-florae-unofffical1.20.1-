# -*- coding: utf-8 -*-
"""给果树方块补中英语言键。

命名沿用 TFC 自己的果树惯例（block.tfc.plant.cherry_branch = 樱桃树枝）：
  <基名>树枝 / <基名>树叶 / <基名>树苗 / <基名>树苗花盆
注意：过渡期内和原有的 wood/leaves、wood/sapling 显示名相同，
等旧果树那一套删掉之后就自然唯一了。
"""
import json
import os

RES = "src/main/resources/assets/tfcflorae/lang"

# 树种 -> (中文基名, 英文基名)
TREES = {
    "hawthorn":      ("山楂",     "Hawthorn"),
    "jabuticabeira": ("嘉宝果树",  "Jabuticabeira"),
    "joshua":        ("短叶丝兰",  "Joshua"),
    "maclura":       ("橙桑",     "Maclura"),
    "medlar":        ("欧楂",     "Medlar"),
    "mulberry":      ("桑",       "Mulberry"),
    "pear":          ("梨",       "Pear"),
    "persimmon":     ("柿",       "Persimmon"),
    "quince":        ("榲桲",     "Quince"),
    "rowan":         ("花楸",     "Rowan"),
    "sloe":          ("黑刺李",   "Sloe"),
    "sorb":          ("山梨",     "Sorb"),
    "syzygium":      ("蒲桃",     "Syzygium"),
    "yew":           ("紫杉",     "Yew"),
}


def entries(base):
    """返回 (key后缀, 中文, 英文) 列表。"""
    return [
        ("_branch",         base[0] + "树枝",     base[1] + " Branch"),
        ("_growing_branch", base[0] + "树枝",     base[1] + " Branch"),
        ("_leaves",         base[0] + "树叶",     base[1] + " Leaves"),
        ("_sapling",        base[0] + "树苗",     base[1] + " Sapling"),
        ("_potted_sapling", base[0] + "树苗花盆", "Potted " + base[1] + " Sapling"),
    ]


def insert_sorted(d, key, value):
    """在字典里按字母序插入，尽量不打乱既有顺序。"""
    if key in d:
        d[key] = value
        return False
    items = list(d.items())
    idx = len(items)
    for i, (k, _) in enumerate(items):
        if k > key:
            idx = i
            break
    items.insert(idx, (key, value))
    d.clear()
    d.update(items)
    return True


def main():
    for lang, idx in (("zh_cn", 1), ("en_us", 2)):
        path = os.path.join(RES, lang + ".json")
        with open(path, encoding="utf-8") as f:
            d = json.load(f)
        before = len(d)
        added = 0
        for t, base in TREES.items():
            for suffix, zh, en in entries(base):
                text = zh if lang == "zh_cn" else en
                if suffix == "_potted_sapling":
                    key = "block.tfcflorae.plant.potted.%s_sapling" % t
                else:
                    key = "block.tfcflorae.plant.%s%s" % (t, suffix)
                if insert_sorted(d, key, text):
                    added += 1
        with open(path, "w", encoding="utf-8", newline="\n") as f:
            json.dump(d, f, indent=2, ensure_ascii=False)
            f.write("\n")
        print("  %-6s %d -> %d (+%d)" % (lang, before, len(d), added))


if __name__ == "__main__":
    main()
