"""Insert the missing `tuff` translation keys into zh_cn.json and en_us.json.

The 19 tuff entries reported as B-class (registered, model present, no lang) are
filled in by following the exact naming pattern used by the other rocks
(e.g. `block.tfcflorae.rock.magma.boninite` = 玻安岩岩浆块 / "Rock Magma Boninite").
Keys are inserted in alphabetical order, the rest of the file is left byte for
byte untouched, and the BOM (if any) is preserved.

Safe to re-run: keys that already exist are reported and skipped.
"""
from __future__ import annotations

import argparse
import json
import os
import re

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
LANG = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae", "lang")

ROCK_ZH = "凝灰岩"
ROCK_EN = "Tuff"

# type -> (zh pattern, en pattern); `{r}` is the rock name placeholder
ROCK_TYPES = {
    "anvil": ("{r}石砧", "Rock Anvil {R}"),
    "aqueduct": ("{r}引水桥", "Rock Aqueduct {R}"),
    "bricks": ("{r}石砖", "Rock Bricks {R}"),
    "button": ("{r}按钮", "Rock Button {R}"),
    "chiseled": ("錾制{r}", "Rock Chiseled {R}"),
    "cobble": ("{r}圆石", "Rock Cobble {R}"),
    "cracked_bricks": ("{r}裂纹砖", "Rock Cracked Bricks {R}"),
    "gravel": ("{r}沙砾", "Rock Gravel {R}"),
    "hardened": ("硬化{r}", "Rock Hardened {R}"),
    "loose": ("{r}石子", "Rock Loose {R}"),
    "magma": ("{r}岩浆块", "Rock Magma {R}"),
    "mossy_bricks": ("{r}苔石砖", "Rock Mossy Bricks {R}"),
    "mossy_cobble": ("{r}苔石", "Rock Mossy Cobble {R}"),
    "mossy_loose": ("长满苔藓的{r}", "Mossy Loose {R} Rock"),
    "pressure_plate": ("{r}压力板", "Rock Pressure Plate {R}"),
    "raw": ("天然{r}", "Rock Raw {R}"),
    "smooth": ("平滑{r}", "Rock Smooth {R}"),
    "spike": ("{r}石锥", "Rock Spike {R}"),
}

KEY_RE = re.compile(r'^\s*"((?:[^"\\]|\\.)*)"\s*:')


def build_entries():
    zh, en = {}, {}
    for typ, (zpat, epat) in ROCK_TYPES.items():
        z = zpat.format(r=ROCK_ZH)
        e = epat.format(R=ROCK_EN)
        zh["block.tfcflorae.rock.%s.tuff" % typ] = z
        zh["item.tfcflorae.rock.%s.tuff" % typ] = z
        en["block.tfcflorae.rock.%s.tuff" % typ] = e
        en["item.tfcflorae.rock.%s.tuff" % typ] = e
    # the loose rock brick item, following `item.tfcflorae.brick.boninite`
    zh["item.tfcflorae.brick.tuff"] = ROCK_ZH + "砖"
    en["item.tfcflorae.brick.tuff"] = ROCK_EN + " Brick"
    return zh, en


def insert(path, entries):
    raw = open(path, "rb").read()
    has_bom = raw.startswith(b"\xef\xbb\xbf")
    text = raw.decode("utf-8-sig")
    lines = text.split("\n")

    positions = {}
    for idx, line in enumerate(lines):
        m = KEY_RE.match(line)
        if m:
            positions[m.group(1)] = idx

    ordered_keys = sorted(positions)
    inserts = []  # (line index, "  \"k\": \"v\",")
    skipped = []
    for key in sorted(entries):
        if key in positions:
            skipped.append(key)
            continue
        # first existing key that sorts after the new one
        lo, hi = 0, len(ordered_keys)
        while lo < hi:
            mid = (lo + hi) // 2
            if ordered_keys[mid] < key:
                lo = mid + 1
            else:
                hi = mid
        if lo == len(ordered_keys):
            raise SystemExit("refusing to append at EOF: %s" % key)
        line = '  %s: %s,' % (json.dumps(key, ensure_ascii=False), json.dumps(entries[key], ensure_ascii=False))
        inserts.append((positions[ordered_keys[lo]], line))

    if not inserts:
        print("  %s: nothing to do (%d already present)" % (os.path.basename(path), len(skipped)))
        return 0

    # insert bottom-up so earlier indices stay valid
    for idx, line in sorted(inserts, reverse=True):
        lines.insert(idx, line)

    out = "\n".join(lines)
    data = out.encode("utf-8")
    if has_bom:
        data = b"\xef\xbb\xbf" + data
    open(path, "wb").write(data)
    print("  %s: inserted %d keys%s" % (
        os.path.basename(path), len(inserts),
        (" (skipped %d existing)" % len(skipped)) if skipped else ""))
    return len(inserts)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--check", action="store_true", help="only report what would be inserted")
    args = ap.parse_args()

    zh, en = build_entries()
    print("entries: zh=%d en=%d" % (len(zh), len(en)))
    if args.check:
        for key in sorted(zh):
            print("  +", key, "=>", zh[key], "/", en[key])
        return

    insert(os.path.join(LANG, "zh_cn.json"), zh)
    insert(os.path.join(LANG, "en_us.json"), en)

    # re-read and validate
    for name in ("zh_cn.json", "en_us.json"):
        p = os.path.join(LANG, name)
        data = json.loads(open(p, encoding="utf-8-sig").read())
        missing = [k for k in zh if k not in data]
        print("  %s: %d keys total, still missing %d" % (name, len(data), len(missing)))


if __name__ == "__main__":
    main()
