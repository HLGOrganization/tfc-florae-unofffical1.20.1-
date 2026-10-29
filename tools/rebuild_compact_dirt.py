"""Switch rockier/pebble/rocky compact dirt to "rock base + soil overlay".

Before: every (variant x soil x rock) had its own baked texture - 4 variants x 4
soils x 34 rocks = 442 textures.
After: 3 overlay sets (12 png, one per variant+soil, supplied by the user, ~50%
alpha) layered on top of the existing 34 rock textures.  Same look, ~430 fewer
textures.

Layout produced:
  textures/block/compact_dirt/<variant>/<soil>.png      (the 12 overlays)
  models/block/template/soil_over_rock.json             (two-layer cube)
  models/block/<variant>/<soil>/<rock>.json             (parent + 2 textures)
The blockstate files keep pointing at models/block/<variant>/<soil>/<rock>, so
they do not need to change.
"""
import io
import json
import os
from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae")
ATT = r"C:\Users\hsy66\.dsh\attachments\v1\objects"

# (variant, soil) -> attachment path, in the order the user posted them:
# 3 rounds of silty_loam, loam, sandy_loam, silt
ORDER = [
    ("loam",        "66/663a2be3eedabe62f4ea39c1b65af6646c425ad7a1bd953275f403f9c2ace245"),
    ("sandy_loam",  "62/62120706aa7198d6c1fbe69cfdda05122b3d37eb546cc1bd227f33ea01333366"),
    ("silt",        "b6/b69d4318c0e16ddc0014af2063a2a63bf4cd3c53a28071c155a1442e9bb300b4"),
    ("silty_loam",  "80/80098e4f1847977b3e6210cbb4bd4aa55e62dc75b73d0dc0c880eeda2744c9ed"),
    ("loam",        "98/98f9e1a438d4ea123cb6c57aa9fdf6bbb291376651d2c82949cf21aa14359956"),
    ("sandy_loam",  "46/4638813d614b79cd456dce87100790f44264db9c642bdfc539a08ac665074009"),
    ("silt",        "8f/8f84d0179f95c436ff1c826f91555e57f941b7f47153903f9a5ec2293a4135f1"),
    ("silty_loam",  "d4/d426ab7e11c8bcd1e251eb600ccd7f00204f66d9b221d0838126fb24d4492ce6"),
    ("loam",        "89/8945b6e72fce9768ecce75587ed997859527f50d00a10589775a6ce9bd0da559"),
    ("sandy_loam",  "7d/7df43a9922a28ba0901f895923f35047069d4b603c0b4f3e06dd1576cf2d2391"),
    ("silt",        "0e/0e1dff82d0c5fd3a351c2f36250302f441393480af6aa5d6826661e6b0ad2806"),
    ("silty_loam",  "a5/a544e59dfaa84a5f4d1bf97ca573e558c403a65e2123a6a995daf43d858e895f"),
]
# group the 12 into three variants: rounds of four
VARIANTS = ["rockier_compact_dirt", "pebble_compact_dirt", "rocky_compact_dirt"]
SOILS = ["loam", "sandy_loam", "silt", "silty_loam"]

# rocks that currently have baked textures = the ones to rebuild
baked_root = os.path.join(ASSETS, "textures", "block", "rockier_compact_dirt", "loam")
rocks = sorted(f[:-4] for f in os.listdir(baked_root) if f.endswith(".png"))
print("rocks:", len(rocks), rocks)

OURS = {f[:-4] for f in os.listdir(os.path.join(ASSETS, "textures", "block", "rock", "raw"))
        if f.endswith(".png")}


def rock_texture(rock):
    if rock in OURS:
        return "tfcflorae:block/rock/raw/" + rock
    return "tfc:block/rock/raw/" + rock


# ---- 1) save the 12 overlays ------------------------------------------------
saved = 0
for idx, (soil, rel) in enumerate(ORDER):
    variant = VARIANTS[idx // 4]
    src = os.path.join(ATT, *rel.split("/"))
    im = Image.open(src).convert("RGBA")
    if im.size != (16, 16):
        im = im.resize((16, 16), Image.NEAREST)
    out_dir = os.path.join(ASSETS, "textures", "block", "compact_dirt", variant)
    os.makedirs(out_dir, exist_ok=True)
    out = os.path.join(out_dir, soil + ".png")
    im.save(out)
    saved += 1
print("overlays saved:", saved)

# ---- 2) template model ------------------------------------------------------
faces = ["down", "up", "north", "south", "west", "east"]
tmpl = {
    "parent": "block/block",
    "textures": {"particle": "#rock", "rock": "#rock", "soil": "#soil"},
    "elements": [
        {"from": [0, 0, 0], "to": [16, 16, 16],
         "faces": {f: {"texture": "#rock"} for f in faces}},
        {"from": [0, 0, 0], "to": [16, 16, 16],
         "faces": {f: {"texture": "#soil"} for f in faces}},
    ],
}
tmpl_dir = os.path.join(ASSETS, "models", "block", "template")
os.makedirs(tmpl_dir, exist_ok=True)
io.open(os.path.join(tmpl_dir, "soil_over_rock.json"), "w", encoding="utf-8", newline="\n").write(
    json.dumps(tmpl, indent=2) + "\n")

# ---- 3) one model per variant/soil/rock ------------------------------------
models = 0
for variant in VARIANTS:
    for soil in SOILS:
        d = os.path.join(ASSETS, "models", "block", variant, soil)
        os.makedirs(d, exist_ok=True)
        for rock in rocks:
            m = {
                "parent": "tfcflorae:block/template/soil_over_rock",
                "textures": {
                    "rock": rock_texture(rock),
                    "soil": "tfcflorae:block/compact_dirt/%s/%s" % (variant, soil),
                },
            }
            io.open(os.path.join(d, rock + ".json"), "w", encoding="utf-8", newline="\n").write(
                json.dumps(m, indent=2) + "\n")
            models += 1
print("models written:", models)
