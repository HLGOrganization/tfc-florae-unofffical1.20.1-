"""Rebuild zh_cn.json: translate every entry that is still plain English.

Reference is TFC's own Chinese wording (material first, then part: "橡木木板",
"天然花岗岩", "花岗岩砖"), so the rules below follow the same word order.

Family rules (by key path, not by the English text):
  wood.<part>.<wood>            -> <wood><part>            e.g. 橡木木板
  rock.<part>[.<sub>].<rock>    -> per-part template       e.g. 天然花岗岩 / 花岗岩台阶
  ore.<ore>[.<rock>]            -> <rock><ore>矿石 / <ore>矿石
  sand.<type>.<color>[.<rock>]  -> <color><rock><type>沙
  everything else               -> word-by-word from the glossary, unknown words
                                   keep their English form and get reported
"""
from __future__ import annotations

import io
import json
import os
import re
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
LANG = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae", "lang")
GLOSSARY = os.path.join(ROOT, "data", "lang-glossary.json")

g = json.load(io.open(GLOSSARY, encoding="utf-8-sig"))
WOODS = g["woods"]
ROCKS = g["rocks"]
COLORS = g["colors"]
ROCK_PARTS = g["rock_parts"]
WOOD_PARTS = g["wood_parts"]
ORES = g["ore_names"]
WORDS = g["words"]

unknown = {}


def word(w):
    """translate a single English word/phrase, or keep it and report"""
    if w in WORDS:
        return WORDS[w]
    low = w.lower()
    if low in COLORS:
        return COLORS[low]
    # try multi-word keys of WORDS (e.g. "Sandy Loam")
    return None


def fallback(text):
    out = []
    i = 0
    parts = text.split()
    while i < len(parts):
        hit = None
        for take in (3, 2, 1):
            if i + take <= len(parts):
                cand = " ".join(parts[i:i + take])
                tr = word(cand)
                if tr:
                    hit = (tr, take)
                    break
        if hit:
            out.append(hit[0])
            i += hit[1]
        else:
            unknown[parts[i]] = unknown.get(parts[i], 0) + 1
            out.append(parts[i])
            i += 1
    return "".join(out)


def rock_part(part, rock_zh):
    """part key like 'bricks' or 'raw.slab' -> Chinese"""
    segs = part.split(".")
    base = segs[0]
    deco = segs[1] if len(segs) > 1 else None
    tmpl = ROCK_PARTS.get(base)
    if tmpl is None:
        return None
    name = tmpl % rock_zh if "%s" in tmpl else tmpl
    if deco:
        d = {"slab": "台阶", "stairs": "楼梯", "wall": "墙"}.get(deco)
        if d:
            return name + d
        return None
    return name


def translate(key, en_value):
    p = key.split(".")
    if len(p) < 4:
        return None
    fam = p[2]
    rest = p[3:]

    if fam == "wood" and len(rest) >= 2:
        wood = rest[-1]
        part = ".".join(rest[:-1])
        if wood in WOODS and part in WOOD_PARTS:
            return WOODS[wood] + WOOD_PARTS[part]

    if fam == "rock" and len(rest) >= 2:
        rock = rest[-1]
        part = ".".join(rest[:-1])
        if rock in ROCKS:
            got = rock_part(part, ROCKS[rock])
            if got:
                return got

    if fam == "ore" and rest:
        if len(rest) >= 2 and rest[-1] in ROCKS:
            rock, ore = rest[-1], rest[-2]
            if ore in ORES:
                return ROCKS[rock] + ORES[ore] + "矿石"
        if rest[-1] in ORES:
            return ORES[rest[-1]] + "矿石"

    if fam == "sand" and len(rest) >= 2:
        rock = rest[-1] if rest[-1] in ROCKS else None
        color = rest[-2] if rock and len(rest) >= 3 else (rest[-1] if rest[-1] in COLORS else None)
        kind = None
        if rock:
            kind = ".".join(rest[:-2])
        else:
            kind = ".".join(rest[:-1])
        kind_zh = {
            "rocky": "多岩", "sandy_tiles": "砂瓦", "sandy": "砂质",
            "sandier": "含砂", "sandiest": "富砂", "dense_grass": "稠密草",
            "sparse_grass": "稀疏草", "dry_grass": "干燥草", "grass": "草",
            "sand_layer": "沙层", "weathered_terracotta": "风化陶瓦",
        }.get(kind, kind)
        bits = []
        if color:
            bits.append(COLORS[color])
        if rock:
            bits.append(ROCKS[rock])
        bits.append(kind_zh)
        bits.append("沙")
        return "".join(bits)

    return None


def main():
    en = json.load(io.open(os.path.join(LANG, "en_us.json"), encoding="utf-8-sig"))
    zh = json.load(io.open(os.path.join(LANG, "zh_cn.json"), encoding="utf-8-sig"))

    translated = 0
    fallbacks = 0
    still = []
    out = dict(zh)
    for k, en_v in en.items():
        # full rebuild: every entry is regenerated so terminology is consistent
        # (the old file mixed styles, e.g. common_oak was "夏栎木" elsewhere)"橡木")
        got = translate(k, en_v)
        if got:
            out[k] = got
            translated += 1
            continue
        got = fallback(str(en_v))
        if got and got != en_v:
            out[k] = got
            translated += 1
            fallbacks += 1
        else:
            still.append(k)

    io.open(os.path.join(LANG, "zh_cn.json"), "w", encoding="utf-8", newline="\n").write(
        json.dumps(out, indent=2, ensure_ascii=False, sort_keys=False) + "\n")
    print("translated:", translated, "(family rules: %d, word fallback: %d)" % (translated - fallbacks, fallbacks))
    print("still english:", len(still))
    if unknown:
        top = sorted(unknown.items(), key=lambda kv: -kv[1])[:40]
        print("unknown words:", len(unknown))
        print("   " + ", ".join("%s(%d)" % (w, n) for w, n in top))
    for k in still[:10]:
        print("   LEFT", k, "=", en[k])


if __name__ == "__main__":
    main()
