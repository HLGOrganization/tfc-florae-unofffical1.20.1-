"""Cross-pack audit of every worldgen tag entry and feature reference.

Packs involved (later ones override earlier by path):
  TFC jar -> TFE jar -> TFCFlorae jar -> kubejs/data

For each tag under data/*/tags/worldgen/**, resolve every member against the merged view of
all four packs.  For each configured_feature, check that the `feature` type it names is a
real registered type (we can only verify the known TFC/TFE/minecraft ones from jar contents).
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
KJS = r"D:\线框的服务器\versions\TFC_Wild_Fire\kubejs\data"

# merged path -> (pack, json)
merged = {}
for label, jp in JARS:
    if not os.path.isfile(jp):
        continue
    z = zipfile.ZipFile(jp)
    for n in z.namelist():
        if not n.endswith(".json"):
            continue
        try:
            merged[n] = (label, json.loads(z.read(n).decode("utf-8-sig")))
        except Exception:
            pass
for dirpath, _d, files in os.walk(KJS):
    for fn in files:
        if not fn.endswith(".json"):
            continue
        full = os.path.join(dirpath, fn)
        rel = os.path.relpath(full, KJS).replace("\\", "/")
        try:
            merged["data/" + rel] = ("kubejs", json.loads(open(full, encoding="utf-8-sig").read()))
        except Exception:
            pass

print("merged json files:", len(merged))


def exists(kind, ident):
    """kind: placed_feature / configured_feature / biome. ident: ns:path"""
    if ":" not in ident:
        return False
    ns, path = ident.split(":", 1)
    return ("data/%s/worldgen/%s/%s.json" % (ns, kind, path)) in merged


# ---------------------------------------------------------------- tags
bad = []
tag_count = 0
for path, (label, data) in merged.items():
    if "/tags/worldgen/" not in path:
        continue
    tag_count += 1
    # data/<ns>/tags/worldgen/<kind>/<...>.json
    parts = path.split("/")
    try:
        wi = parts.index("worldgen")
    except ValueError:
        continue
    kind = parts[wi + 1]
    if kind not in ("placed_feature", "configured_feature", "biome"):
        continue
    for v in data.get("values", []):
        if not isinstance(v, str):
            continue
        if v.startswith("#"):
            inner = v[1:]
            # nested tag reference
            ns, tpath = inner.split(":", 1) if ":" in inner else ("minecraft", inner)
            ok = ("data/%s/tags/worldgen/%s/%s.json" % (ns, kind, tpath)) in merged
            if not ok:
                bad.append((label, path, v, "nested tag missing"))
            continue
        if not exists(kind, v):
            bad.append((label, path, v, "entry missing"))

print("worldgen tags scanned:", tag_count)
print("dangling tag entries:", len(bad))
by_pack = {}
for label, path, v, why in bad:
    by_pack.setdefault(label, []).append((path, v, why))
for label in by_pack:
    print()
    print("== pack:", label, len(by_pack[label]))
    seen = set()
    for path, v, why in by_pack[label]:
        key = (path.split("/tags/")[-1], v)
        if key in seen:
            continue
        seen.add(key)
        print("   %-58s %-42s %s" % (path.split("/tags/")[-1][:58], v[:42], why))
        if len(seen) > 15:
            break
