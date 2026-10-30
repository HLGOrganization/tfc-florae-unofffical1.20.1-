"""Port the compact-dirt and clay cavern features from 1.18.2.

The five feature files live in the 1.18 caverns system, which TFC 1.20.1 does not have.
They are still plain vanilla feature types (vegetation_patch / simple_random_selector), so
they can be registered on their own and hooked into TFC 1.20's
tfc:in_biome/underground_decoration group.

Block ids were audited against TFCFSoil: rocky_compact_dirt and rockiest_compact_dirt were
merged into rockier_compact_dirt in this port, so those are rewritten.  Everything else
the files reference is registered (some of them currently lack blockstate assets, which is
a separate asset problem).

Files written:
  data/tfcflorae/worldgen/placed_feature/caverns/jungle/floor.json
  data/tfcflorae/worldgen/placed_feature/caverns/jungle/floor_soil.json
  data/tfcflorae/worldgen/placed_feature/caverns/webbed/floor.json
  data/tfcflorae/worldgen/placed_feature/caverns/thermal/dripleaf_clay.json
  data/tfcflorae/worldgen/placed_feature/caverns/thermal/lichen_clay.json
  data/tfc/worldgen/configured_feature/caverns/thermal/dripleaf.json  (referenced)
  data/tfc/tags/worldgen/placed_feature/in_biome/underground_decoration.json  (overlay)
  data/tfcflorae/tags/blocks/replaceable_not_air.json
"""
import io
import json
import os

SRC = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                   "..", "tfc_florae", "jar", "data")
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATA = os.path.join(ROOT, "src", "main", "resources", "data")

FILES = [
    "tfc/worldgen/placed_feature/caverns/jungle/floor.json",
    "tfc/worldgen/placed_feature/caverns/jungle/floor_soil.json",
    "tfc/worldgen/placed_feature/caverns/webbed/floor.json",
    "tfc/worldgen/placed_feature/caverns/thermal/dripleaf_clay.json",
    "tfc/worldgen/placed_feature/caverns/thermal/lichen_clay.json",
    "tfc/worldgen/configured_feature/caverns/thermal/dripleaf.json",
]

# rocky/rockiest were merged into rockier in this port
REWRITES = [
    ("tfcflorae:rocky_compact_dirt", "tfcflorae:rockier_compact_dirt"),
    ("tfcflorae:rockiest_compact_dirt", "tfcflorae:rockier_compact_dirt"),
]

written = []
for rel in FILES:
    src = os.path.join(SRC, rel)
    if not os.path.isfile(src):
        print("!! missing source:", rel)
        continue
    text = io.open(src, encoding="utf-8-sig").read()
    for old, new in REWRITES:
        text = text.replace(old, new)
    # move to our namespace
    out_rel = rel.replace("tfc/worldgen/", "tfcflorae/worldgen/", 1)
    out = os.path.join(DATA, out_rel.replace("/", os.sep))
    os.makedirs(os.path.dirname(out), exist_ok=True)
    io.open(out, "w", encoding="utf-8", newline="\n").write(text)
    written.append(out_rel)
    print("wrote", out_rel, "(%d bytes)" % len(text))

# feature references inside those files must point at the copied configured feature
for rel in written:
    out = os.path.join(DATA, rel.replace("/", os.sep))
    t = io.open(out, encoding="utf-8").read()
    t2 = t.replace('"tfc:caverns/thermal/dripleaf"', '"tfcflorae:caverns/thermal/dripleaf"')
    if t2 != t:
        io.open(out, "w", encoding="utf-8", newline="\n").write(t2)
        print("  repointed configured feature in", rel)

# ---- overlay TFC's underground decoration group -------------------------
tag_src = os.path.join(SRC, "tfc", "tags", "worldgen", "placed_feature", "in_biome", "underground_decoration.json")
tag = json.load(io.open(tag_src, encoding="utf-8-sig"))
existing = list(tag.get("values", []))
added = [
    "tfcflorae:caverns/jungle/floor",
    "tfcflorae:caverns/jungle/floor_soil",
    "tfcflorae:caverns/webbed/floor",
    "tfcflorae:caverns/thermal/dripleaf_clay",
    "tfcflorae:caverns/thermal/lichen_clay",
]
for a in added:
    if a not in existing:
        existing.append(a)
tag["values"] = existing
out = os.path.join(DATA, "tfc", "tags", "worldgen", "placed_feature", "in_biome", "underground_decoration.json")
os.makedirs(os.path.dirname(out), exist_ok=True)
io.open(out, "w", encoding="utf-8", newline="\n").write(json.dumps(tag, indent=2) + "\n")
print("overlaid underground_decoration with", len(added), "features; total", len(existing))

# ---- replaceable tag used by the floor features -------------------------
rep_src = os.path.join(SRC, "tfcflorae", "tags", "blocks", "replaceable_not_air.json")
if os.path.isfile(rep_src):
    rep = json.load(io.open(rep_src, encoding="utf-8-sig"))
    out = os.path.join(DATA, "tfcflorae", "tags", "blocks", "replaceable_not_air.json")
    os.makedirs(os.path.dirname(out), exist_ok=True)
    io.open(out, "w", encoding="utf-8", newline="\n").write(json.dumps(rep, indent=2) + "\n")
    print("copied replaceable_not_air tag:", len(rep.get("values", [])), "values")
else:
    print("!! replaceable_not_air tag missing in 1.18.2")

# ---- audit -------------------------------------------------------------
import re
bad = set()
for rel in written:
    out = os.path.join(DATA, rel.replace("/", os.sep))
    t = io.open(out, encoding="utf-8").read()
    for m in re.findall(r'"Name":\s*"([^"]+)"', t):
        if m.startswith("tfcflorae:") and ("rocky_compact" in m or "rockiest_compact" in m):
            bad.add(m)
    for m in re.findall(r'"#?(tfcflorae:[a-z0-9_/]+)"', t):
        if "rocky_compact" in m or "rockiest_compact" in m:
            bad.add(m)
print("leftover merged ids:", bad if bad else "none")
