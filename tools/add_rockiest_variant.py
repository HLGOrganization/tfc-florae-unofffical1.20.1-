"""Add rockiest_compact_dirt as the fourth random variant.

The user supplied its four overlays; this
  1. saves them to textures/block/compact_dirt/rockiest_compact_dirt/<soil>.png
  2. writes its 136 layered models (4 soils x 34 rocks)
  3. adds that model to every rockier_compact_dirt blockstate's random variant list
     (so a placed block can look like rockier / pebble / rocky / rockiest)
"""
import io
import json
import os
from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae")
ATT = r"C:\Users\hsy66\.dsh\attachments\v1\objects"

OVERLAYS = {
    "loam":       "ef/efaac3a4dff66d80b0a519b1626736d8cfc4c3bf8030da11b7b5747aef46e814",
    "sandy_loam": "3a/3ace9f99ce29d9f20792a498bca84411448cd29aa46c49429a95cb4170b4eb02",
    "silt":       "0d/0d690aa1fdac33140739edde8a61d1fdc96c628465bb03c986d6d5960e4f8c71",
    "silty_loam": "dd/dd4cfbd03c107796de09fb41d5abd2bfc5184b63745e6de25f1029513c23d7dd",
}

VARIANT = "rockiest_compact_dirt"
SOILS = ["loam", "sandy_loam", "silt", "silty_loam"]
BOULDER = "rockier_compact_dirt"   # the blockstate folder / block id

# rocks: same set as the other variants
ref = os.path.join(ASSETS, "models", "block", "rockier_compact_dirt", "loam")
rocks = sorted(f[:-5] for f in os.listdir(ref) if f.endswith(".json"))
print("rocks:", len(rocks))

OURS = {f[:-4] for f in os.listdir(os.path.join(ASSETS, "textures", "block", "rock", "raw"))
        if f.endswith(".png")}
def rock_texture(rock):
    return ("tfcflorae:block/rock/raw/" if rock in OURS else "tfc:block/rock/raw/") + rock

# ---- 1) overlays ------------------------------------------------------------
for soil, rel in OVERLAYS.items():
    src = os.path.join(ATT, *rel.split("/"))
    im = Image.open(src).convert("RGBA")
    if im.size != (16, 16):
        im = im.resize((16, 16), Image.NEAREST)
    out_dir = os.path.join(ASSETS, "textures", "block", "compact_dirt", VARIANT)
    os.makedirs(out_dir, exist_ok=True)
    im.save(os.path.join(out_dir, soil + ".png"))
print("overlays saved:", len(OVERLAYS))

# ---- 2) models --------------------------------------------------------------
n = 0
for soil in SOILS:
    d = os.path.join(ASSETS, "models", "block", VARIANT, soil)
    os.makedirs(d, exist_ok=True)
    for rock in rocks:
        m = {
            "parent": "tfcflorae:block/template/soil_over_rock",
            "textures": {
                "rock": rock_texture(rock),
                "soil": "tfcflorae:block/compact_dirt/%s/%s" % (VARIANT, soil),
            },
        }
        io.open(os.path.join(d, rock + ".json"), "w", encoding="utf-8", newline="\n").write(
            json.dumps(m, indent=2) + "\n")
        n += 1
print("models written:", n)

# ---- 3) blockstates: add the fourth model ----------------------------------
patched = 0
base = os.path.join(ASSETS, "blockstates", BOULDER)
for soil in SOILS:
    sd = os.path.join(base, soil)
    if not os.path.isdir(sd):
        continue
    for fn in sorted(os.listdir(sd)):
        if not fn.endswith(".json"):
            continue
        fp = os.path.join(sd, fn)
        rock = fn[:-5]
        d = json.load(io.open(fp, encoding="utf-8-sig"))
        variants = d.get("variants", {})
        entry = variants.get("")
        if not isinstance(entry, list):
            continue
        target = "tfcflorae:block/%s/%s/%s" % (VARIANT, soil, rock)
        if any(isinstance(e, dict) and e.get("model") == target for e in entry):
            continue
        entry.append({"model": target})
        variants[""] = entry
        d["variants"] = variants
        io.open(fp, "w", encoding="utf-8", newline="\n").write(json.dumps(d, indent=2) + "\n")
        patched += 1
print("blockstates patched:", patched)
