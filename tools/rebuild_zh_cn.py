"""Rebuild zh_cn.json with proper Chinese word order.

Reference is TFC's own wording: material first, then part ("橡木木板", "天然花岗岩",
"花岗岩砖"), decoration last ("花岗岩砖台阶").  Rules per family:

  wood.<part>.<wood>              <wood><part>
  rock.<part>[.<deco>].<rock>     <rock><template>[<deco>]
  ore.<ore>[.<rock>]              <rock><ore>矿石
  sand.<kind>.<colour>[.<rock>]   <colour><rock><kind>沙
  plant / others                  drop the English category prefix ("Plant ", "Wood ",
                                  "Ore "...) and translate the rest; multi-word names
                                  are matched as a whole first so "Jack in the Pulpit"
                                  and "Venus Flytrap" do not fragment.
"""
from __future__ import annotations

import io
import json
import os

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

STRIP_PREFIX = ("Plant ", "Wood ", "Ore ", "Rock ", "Ceramic ", "Fluid ", "Mineral ")

SAND_KINDS = {
    "rocky": "多岩",
    "rockier": "更含岩",
    "sandier": "含砂",
    "sandiest": "富砂",
    "sandy": "砂质",
    "tiles": "瓦",
    "sandy_tiles": "砂瓦",
    "sandier_tiles": "含砂瓦",
    "sandiest_tiles": "富砂瓦",
    "rocky_tiles": "多岩瓦",
    "dense_grass": "稠密草",
    "sparse_grass": "稀疏草",
    "dry_grass": "干燥草",
    "grass": "草",
    "sand_layer": "沙层",
    "weathered_terracotta": "风化陶瓦",
    "terracotta": "陶瓦",
}

unknown = {}


_LOWER = {k.lower(): v for k, v in WORDS.items()}


def lookup(text):
    if not text:
        return None
    if text in WORDS:
        return WORDS[text]
    low = text.lower().strip()
    if low in _LOWER:
        return _LOWER[low]
    if low in COLORS:
        return COLORS[low]
    return None


def fallback(text):
    whole = lookup(text)
    if whole:
        return whole
    parts = text.split()
    out = []
    i = 0
    while i < len(parts):
        hit = None
        for take in (4, 3, 2, 1):
            if i + take <= len(parts):
                cand = " ".join(parts[i:i + take])
                tr = lookup(cand)
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


def rock_name_for(part_key, rock_zh):
    segs = part_key.split(".")
    deco = None
    if segs and segs[-1] in ("slab", "stairs", "wall", "button", "pressure_plate"):
        deco = segs[-1]
        segs = segs[:-1]
    base = ".".join(segs)
    tmpl = ROCK_PARTS.get(base)
    if tmpl is None:
        return None
    name = tmpl % rock_zh if "%s" in tmpl else tmpl
    if deco:
        name += {"slab": "台阶", "stairs": "楼梯", "wall": "墙",
                 "button": "按钮", "pressure_plate": "压力板"}[deco]
    return name


def translate(key):
    p = key.split(".")
    if len(p) < 4:
        return None
    fam = p[2]
    rest = p[3:]

    if fam == "wood" and len(rest) >= 2:
        w, part = rest[-1], ".".join(rest[:-1])
        if w in WOODS and part in WOOD_PARTS:
            tmpl = WOOD_PARTS[part]
            return tmpl % WOODS[w] if "%s" in tmpl else WOODS[w] + tmpl

    if fam == "rock" and len(rest) >= 2:
        r, part = rest[-1], ".".join(rest[:-1])
        if r in ROCKS:
            return rock_name_for(part, ROCKS[r])

    if fam == "ore" and rest:
        if len(rest) >= 2 and rest[-1] in ROCKS and rest[-2] in ORES:
            return ROCKS[rest[-1]] + ORES[rest[-2]] + "矿石"
        if rest[-1] in ORES:
            return ORES[rest[-1]] + "矿石"

    if fam == "sand" and len(rest) >= 2:
        rock = rest[-1] if rest[-1] in ROCKS else None
        idx = len(rest) - 1 - (1 if rock else 0)
        color = rest[idx] if idx >= 0 and rest[idx] in COLORS else None
        if color:
            kind = ".".join(rest[:idx])
            bits = [COLORS[color]]
            if rock:
                bits.append(ROCKS[rock])
            if kind in SAND_KINDS:
                bits.append(SAND_KINDS[kind])
            elif kind:
                bits.append(fallback(kind.replace("_", " ").title()))
            bits.append("沙")
            return "".join(bits)

    return None


def main():
    en = json.load(io.open(os.path.join(LANG, "en_us.json"), encoding="utf-8-sig"))
    existing = json.load(io.open(os.path.join(LANG, "zh_cn.json"), encoding="utf-8-sig"))

    out = {}
    by_rule = by_fallback = kept = 0
    still = []
    for k, en_v in en.items():
        # incremental mode: keep any translation that is not simply the English text
        prev = existing.get(k)
        if prev is not None and prev != en_v:
            out[k] = prev
            kept += 1
            continue
        got = translate(k)
        if got:
            out[k] = got
            by_rule += 1
            continue
        text = str(en_v)
        whole = lookup(text)
        if whole:
            out[k] = whole
            by_fallback += 1
            continue
        for pre in STRIP_PREFIX:
            if text.startswith(pre):
                text = text[len(pre):]
                break
        got = fallback(text)
        if got and got != text:
            out[k] = got
            by_fallback += 1
        else:
            out[k] = got or text
            still.append(k)

    io.open(os.path.join(LANG, "zh_cn.json"), "w", encoding="utf-8", newline="\n").write(
        json.dumps(out, indent=2, ensure_ascii=False) + "\n")
    print("kept existing:", kept, " newly filled:", by_rule + by_fallback,
          "(rules:", by_rule, " fallback:", by_fallback, ")")
    print("english left:", len(still))
    if unknown:
        top = sorted(unknown.items(), key=lambda kv: -kv[1])[:30]
        print("unknown words:", len(unknown))
        print("   " + ", ".join("%s(%d)" % (w, n) for w, n in top))
    for k in still[:8]:
        print("   LEFT", k.replace("tfcflorae.", ""), "=", en[k])


if __name__ == "__main__":
    main()
