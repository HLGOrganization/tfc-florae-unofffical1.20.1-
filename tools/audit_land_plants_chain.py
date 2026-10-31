"""Strict audit: for every placed_feature referenced by tfc:feature/land_plants,
verify the configured_feature it points at actually exists.

chain: biome page -> #tfc:in_biome/surface_decoration/<biome> -> #tfc:feature/land_plants
       -> tfc:plant/<name> (placed_feature) -> feature field (configured_feature)
       -> that configured_feature's own "type"

Any missing link makes the whole tag fail to load, and TFC logs one line for it only when
the JSON itself cannot be parsed - a missing reference inside a tag is silent.
"""
import json
import os
import zipfile

MODS = r"D:\线框的服务器\versions\TFC_Wild_Fire\mods"
JARS = [
    ("tfc", os.path.join(MODS, "[群峦传说] TerraFirmaCraft-Forge-1.20.1-3.2.20.jar")),
    ("tfe", os.path.join(MODS, "Wildfire_TerraFirmaEarth-1.20.1-forge-2.3.0.jar")),
    ("tfcflorae", os.path.join(MODS, "TFCFlorae-1.20.1-2.2.0-mc1.20.1.jar")),
]

placed = {}       # ns:path -> dict
configured = {}   # ns:path -> dict
for label, jp in JARS:
    if not os.path.isfile(jp):
        continue
    z = zipfile.ZipFile(jp)
    for n in z.namelist():
        if "worldgen/placed_feature/" in n and n.endswith(".json"):
            key = n.split("worldgen/placed_feature/")[1][:-5]
            ns = n.split("/")[1]
            try:
                placed["%s:%s" % (ns, key)] = json.loads(z.read(n).decode("utf-8-sig"))
            except Exception:
                pass
        elif "worldgen/configured_feature/" in n and n.endswith(".json"):
            key = n.split("worldgen/configured_feature/")[1][:-5]
            ns = n.split("/")[1]
            try:
                configured["%s:%s" % (ns, key)] = json.loads(z.read(n).decode("utf-8-sig"))
            except Exception:
                pass

print("placed_feature known: %d | configured_feature known: %d" % (len(placed), len(configured)))

# land_plants members
tfe = zipfile.ZipFile(JARS[1][1])
lp = json.loads(tfe.read("data/tfc/tags/worldgen/placed_feature/feature/land_plants.json").decode())
members = [v for v in lp.get("values", []) if isinstance(v, str) and not v.startswith("#")]
print("land_plants direct members:", len(members))

missing_placed = []
missing_cfg = []
bad_type = []
for m in members:
    pf = placed.get(m)
    if pf is None:
        missing_placed.append(m)
        continue
    feat = pf.get("feature")
    if not feat:
        bad_type.append((m, "no feature field"))
        continue
    if feat not in configured:
        missing_cfg.append((m, feat))
        continue
    cf = configured[feat]
    t = cf.get("type", "")
    if not t:
        bad_type.append((m, "configured has no type"))
    elif t.startswith("tfc:") or t.startswith("tfe:") or t.startswith("minecraft:"):
        # known custom types; nothing further to resolve from data alone
        pass

print()
print("placed_feature missing      :", len(missing_placed))
for x in missing_placed[:10]:
    print("    -", x)
print("configured_feature missing  :", len(missing_cfg))
for x in missing_cfg[:10]:
    print("    - %s -> %s" % x)
print("configured without type     :", len(bad_type))
for x in bad_type[:10]:
    print("    -", x)

# what type distribution do these configured features have?
from collections import Counter
c = Counter()
for m in members:
    pf = placed.get(m)
    if not pf:
        continue
    cf = configured.get(pf.get("feature"))
    if cf:
        c[cf.get("type")] += 1
print()
print("configured types used by land_plants:")
for k, v in c.most_common():
    print("    %-40s %d" % (k, v))
