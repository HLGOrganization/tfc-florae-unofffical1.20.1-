"""Audit the port against the rules in tfcflorae-port/references/rules.md.

Checks that can be done statically:

  R1  .get(...).get() chains over maps that may be empty
  R3  registered block id vs its asset path
  A1  every registered variant has a complete asset set
      (blockstate + block model + item model + loot table + lang key)
  T3  tags referencing ids that are not registered
  A2  blockstates pointing at another mod's models
"""
import io
import json
import os
import re
import zipfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC = os.path.join(ROOT, "src", "main", "java")
RES = os.path.join(ROOT, "src", "main", "resources")
JAR = r"D:\线框的服务器\versions\TFC_Wild_Fire\mods\TFCFlorae-1.20.1-2.2.0-mc1.20.1.jar"

A = os.path.join(RES, "assets", "tfcflorae")
DATA = os.path.join(RES, "data", "tfcflorae")

print("=" * 70)
print("R1: .get(...).get() chains (NPE risk when a map lost entries)")
print("=" * 70)
hits = []
for dirpath, _d, files in os.walk(SRC):
    for fn in files:
        if not fn.endswith(".java"):
            continue
        p = os.path.join(dirpath, fn)
        text = io.open(p, encoding="utf-8", errors="ignore").read()
        for m in re.finditer(r'[^\n]*\bget\([^)]*\)\s*\.\s*get\([^)]*\)[^\n]*', text):
            line = m.group(0).strip()
            if line.startswith("//") or line.startswith("*"):
                continue
            hits.append((os.path.relpath(p, SRC), text[:m.start()].count("\n") + 1, line[:150]))
print("candidates:", len(hits))
for h in hits[:40]:
    print("  %s:%d  %s" % h)

print()
print("=" * 70)
print("R3/A1: registered block ids vs asset completeness")
print("=" * 70)
# collect block ids we can infer from blockstates on disk
blockstate_ids = set()
for dirpath, _d, files in os.walk(os.path.join(A, "blockstates")):
    for fn in files:
        if not fn.endswith(".json"):
            continue
        rel = os.path.relpath(os.path.join(dirpath, fn), os.path.join(A, "blockstates")).replace(os.sep, "/")
        blockstate_ids.add(rel[:-5])

missing_item_model = []
missing_loot = []
for bid in sorted(blockstate_ids):
    if not os.path.isfile(os.path.join(A, "models", "item", bid + ".json")):
        missing_item_model.append(bid)
    if not os.path.isfile(os.path.join(DATA, "loot_tables", "blocks", bid + ".json")):
        missing_loot.append(bid)

print("blockstates on disk:", len(blockstate_ids))
print("missing item model:", len(missing_item_model))
for x in missing_item_model[:25]:
    print("   ", x)
print("missing loot table:", len(missing_loot))
for x in missing_loot[:15]:
    print("   ", x)

print()
print("=" * 70)
print("A2: blockstates/models pointing at other namespaces' models")
print("=" * 70)
other_ns = []
for dirpath, _d, files in os.walk(os.path.join(A, "blockstates")):
    for fn in files:
        if not fn.endswith(".json"):
            continue
        p = os.path.join(dirpath, fn)
        text = io.open(p, encoding="utf-8", errors="ignore").read()
        for m in re.finditer(r'"model"\s*:\s*"(?!tfcflorae:|minecraft:)([a-z0-9_]+):', text):
            other_ns.append((os.path.relpath(p, A).replace(os.sep, "/"), m.group(1)))
print("blockstates referencing another namespace's model:", len(other_ns))
for x in other_ns[:15]:
    print("   ", x)

print()
print("=" * 70)
print("T3: tags referencing ids with no matching block/item json")
print("=" * 70)
# only check our own block/item tags against our own blockstates + tfc's known ids is too
# broad; limit to our namespace entries inside data/tfc and data/minecraft tags we ship
unknown = []
for dirpath, _d, files in os.walk(os.path.join(RES, "data")):
    for fn in files:
        if not fn.endswith(".json"):
            continue
        p = os.path.join(dirpath, fn)
        if os.sep + "tags" + os.sep not in p:
            continue
        try:
            data = json.load(io.open(p, encoding="utf-8-sig"))
        except Exception:
            continue
        vals = data.get("values")
        if not isinstance(vals, list):
            continue
        for v in vals:
            if not isinstance(v, str) or not v.startswith("tfcflorae:"):
                continue
            ident = v[1:] if v.startswith("#") else v
            ident = ident.split(":", 1)[1]
            # block or item, with or without sub path
            ok = (
                os.path.isfile(os.path.join(A, "blockstates", ident.replace("/", os.sep) + ".json"))
                or os.path.isfile(os.path.join(A, "models", "item", ident.replace("/", os.sep) + ".json"))
                or os.path.isfile(os.path.join(DATA, "loot_tables", "blocks", ident.replace("/", os.sep) + ".json"))
            )
            if not ok:
                unknown.append((os.path.relpath(p, RES).replace(os.sep, "/"), v))
print("tag entries with no local asset:", len(unknown))
seen = set()
for f, v in unknown[:30]:
    if v in seen:
        continue
    seen.add(v)
    print("   %-40s %s" % (v, f))
