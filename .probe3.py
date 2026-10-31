import zipfile, json, io, os
from PIL import Image

OUR = r"D:\线框的服务器\versions\TFC_Wild_Fire\mods\TFCFlorae-1.20.1-2.2.0-mc1.20.1.jar"
TFE = r"D:\线框的服务器\versions\TFC_Wild_Fire\mods\Wildfire_TerraFirmaEarth-1.20.1-forge-2.3.0.jar"

z = zipfile.ZipFile(OUR)
print("=== blockstate 原文 ===")
for fam in ("grass", "dense_grass", "sparse_grass"):
    print(f"\n  sand/{fam}/black.json:")
    print("   ", z.read(f"assets/tfcflorae/blockstates/sand/{fam}/black.json").decode()[:400])

print("\n=== 所有 sand 相关贴图 ===")
tex = [n for n in z.namelist() if n.startswith("assets/tfcflorae/textures/block/sand/") and n.endswith(".png")]
print("  总数:", len(tex))
for t in sorted(tex)[:40]:
    print("   ", t)
z.close()

print("\n\n=== TFE 里的群系 id（data/tfc/worldgen/biome）===")
z = zipfile.ZipFile(TFE)
ids = sorted(n[len("data/tfc/worldgen/biome/"):-5] for n in z.namelist()
             if n.startswith("data/tfc/worldgen/biome/") and n.endswith(".json"))
print("  总数:", len(ids))
kw = ("badland", "mesa", "dune", "desert", "canyon", "red_", "arid", "sand", "clay")
hits = [i for i in ids if any(k in i for k in kw)]
print("  像恶地的:")
for i in hits:
    print("   tfc:" + i)
print()
print("  全表:")
for i in range(0, len(ids), 6):
    print("   " + ", ".join(ids[i:i+6]))
z.close()
