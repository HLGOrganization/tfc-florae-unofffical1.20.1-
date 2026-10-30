"""Inventory every file / code site that mentions the woods and rocks being removed.

Exact-name matching only (skills rule: never substring-match, `rocky` vs `rockiest`).

Woods: yellow_jacaranda, white_mangrove, white_cherry_blossom, whitebeam,
       kauri, hazel, brazilwood, beech
Rocks: arkose, cataclasite, sandstone
"""
import collections
import io
import os
import re

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
WOODS = ["yellow_jacaranda", "white_mangrove", "white_cherry_blossom", "whitebeam",
         "kauri", "hazel", "brazilwood", "beech"]
ROCKS = ["arkose", "cataclasite", "sandstone"]
ALL = WOODS + ROCKS


def classify(path):
    rel = path.replace("\\", "/")
    if rel.endswith(".java"):
        return "java"
    if "/blockstates/" in rel:
        return "blockstate"
    if "/models/block/" in rel:
        return "block model"
    if "/models/item/" in rel:
        return "item model"
    if "/textures/" in rel:
        return "texture"
    if "/lang/" in rel:
        return "lang"
    if "/loot_tables/" in rel:
        return "loot"
    if "/recipes/" in rel:
        return "recipe"
    if "/tags/" in rel:
        return "tag"
    if "/worldgen/" in rel:
        return "worldgen"
    return "other"


def name_hits(text, name):
    """Exact-ish: name as a whole path/identifier segment."""
    return re.findall(r'(?<![\w-])' + re.escape(name) + r'(?![\w-])', text)


print("=" * 72)
print("1) file PATHS containing each name")
print("=" * 72)
by_name_files = collections.defaultdict(list)
for dirpath, _d, files in os.walk(ROOT):
    if any(seg in dirpath for seg in ("\\.git", "\\build", "\\_preview", "\\tools")):
        continue
    for fn in files:
        full = os.path.join(dirpath, fn)
        rel = os.path.relpath(full, ROOT)
        for n in ALL:
            if name_hits(rel, n):
                by_name_files[n].append(rel)
for n in ALL:
    fs = by_name_files[n]
    kinds = collections.Counter(classify(f) for f in fs)
    print("%-22s %5d files   %s" % (n, len(fs), dict(kinds)))

print()
print("=" * 72)
print("2) java code sites (enum constants and references)")
print("=" * 72)
JAVA = os.path.join(ROOT, "src", "main", "java")
java_by_name = collections.defaultdict(list)
for dirpath, _d, files in os.walk(JAVA):
    for fn in files:
        if not fn.endswith(".java"):
            continue
        full = os.path.join(dirpath, fn)
        rel = os.path.relpath(full, JAVA)
        text = io.open(full, encoding="utf-8", errors="ignore").read()
        for n in ALL:
            if name_hits(text, n):
                lines = [i + 1 for i, l in enumerate(text.splitlines())
                         if name_hits(l, n)]
                java_by_name[n].append((rel, lines[:6], len(lines)))
for n in ALL:
    hits = java_by_name[n]
    if not hits:
        print("%-22s (no java reference)" % n)
        continue
    print("%-22s %d files" % (n, len(hits)))
    for rel, lines, total in hits[:8]:
        print("      %-52s lines %s%s" % (rel, lines, "..." if total > 6 else ""))

print()
print("=" * 72)
print("3) lang keys")
print("=" * 72)
import json
for langfile in ("en_us.json", "zh_cn.json"):
    p = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae", "lang", langfile)
    if not os.path.isfile(p):
        continue
    d = json.load(io.open(p, encoding="utf-8-sig"))
    counts = {}
    for n in ALL:
        counts[n] = sum(1 for k in d if name_hits(k, n))
    print(langfile, {k: v for k, v in counts.items() if v})
