"""Re-map the first 12 overlays by their actual file names.

The first batch was saved with the wrong soil names: I assumed an order of
loam/sandy_loam/silt/silty_loam per variant, but the user's attachments are named
silty_loam, loam, sandy_loam, silt.  Every one of the three variants therefore
loaded the neighbouring soil's overlay, which is why a placed block "turned into a
different soil".

This rewrites the 12 overlays under their correct names.
"""
import os
from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae")
ATT = r"C:\Users\hsy66\.dsh\attachments\v1\objects"

# (variant, soil) -> attachment, taken from the file names in the message
ROUNDS = [
    ("rockier_compact_dirt", [
        ("silty_loam", "66/663a2be3eedabe62f4ea39c1b65af6646c425ad7a1bd953275f403f9c2ace245"),
        ("loam",       "62/62120706aa7198d6c1fbe69cfdda05122b3d37eb546cc1bd227f33ea01333366"),
        ("sandy_loam", "b6/b69d4318c0e16ddc0014af2063a2a63bf4cd3c53a28071c155a1442e9bb300b4"),
        ("silt",       "80/80098e4f1847977b3e6210cbb4bd4aa55e62dc75b73d0dc0c880eeda2744c9ed"),
    ]),
    ("pebble_compact_dirt", [
        ("silty_loam", "98/98f9e1a438d4ea123cb6c57aa9fdf6bbb291376651d2c82949cf21aa14359956"),
        ("loam",       "46/4638813d614b79cd456dce87100790f44264db9c642bdfc539a08ac665074009"),
        ("sandy_loam", "8f/8f84d0179f95c436ff1c826f91555e57f941b7f47153903f9a5ec2293a4135f1"),
        ("silt",       "d4/d426ab7e11c8bcd1e251eb600ccd7f00204f66d9b221d0838126fb24d4492ce6"),
    ]),
    ("rocky_compact_dirt", [
        ("loam",       "89/8945b6e72fce9768ecce75587ed997859527f50d00a10589775a6ce9bd0da559"),
        ("sandy_loam", "7d/7df43a9922a28ba0901f895923f35047069d4b603c0b4f3e06dd1576cf2d2391"),
        ("silt",       "0e/0e1dff82d0c5fd3a351c2f36250302f441393480af6aa5d6826661e6b0ad2806"),
        ("silty_loam", "a5/a544e59dfaa84a5f4d1bf97ca573e558c403a65e2123a6a995daf43d858e895f"),
    ]),
]

for variant, items in ROUNDS:
    out_dir = os.path.join(ASSETS, "textures", "block", "compact_dirt", variant)
    os.makedirs(out_dir, exist_ok=True)
    for soil, rel in items:
        im = Image.open(os.path.join(ATT, *rel.split("/"))).convert("RGBA")
        if im.size != (16, 16):
            im = im.resize((16, 16), Image.NEAREST)
        im.save(os.path.join(out_dir, soil + ".png"))
    print("%-22s -> %s" % (variant, ", ".join(s for s, _ in items)))

# quick colour sanity check: the four loam overlays should look alike
import statistics
print("\nloam overlays (mean rgb):")
for variant, _ in ROUNDS:
    p = os.path.join(ASSETS, "textures", "block", "compact_dirt", variant, "loam.png")
    im = Image.open(p).convert("RGBA")
    px = [(r, g, b) for r, g, b, a in im.getdata() if a > 128]
    if px:
        print("   %-22s %s" % (variant, tuple(round(statistics.mean(c[i] for c in px)) for i in range(3))))
