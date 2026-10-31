import zipfile, json, io, os
from PIL import Image

OUR = r"D:\线框的服务器\versions\TFC_Wild_Fire\mods\TFCFlorae-1.20.1-2.2.0-mc1.20.1.jar"
TFE = r"D:\线框的服务器\versions\TFC_Wild_Fire\mods\Wildfire_TerraFirmaEarth-1.20.1-forge-2.3.0.jar"

z = zipfile.ZipFile(OUR)

print("=== 三种沙草方块：模型 + 贴图 ===")
for fam in ("grass", "dense_grass", "sparse_grass"):
    bs = json.loads(z.read(f"assets/tfcflorae/blockstates/sand/{fam}/black.json"))
    model = bs.get("variants", {}).get("", {}).get("model")
    print(f"\n  sand/{fam}/black")
    print(f"    blockstate -> model: {model}")
    if model:
        mp = "assets/tfcflorae/models/" + model.split(":")[-1] + ".json"
        if mp in z.namelist():
            m = json.loads(z.read(mp))
            print(f"    model: parent={m.get('parent')} textures={m.get('textures')}")

print("\n=== 贴图绿像素占比（判断疏密）===")
for fam in ("grass", "dense_grass", "sparse_grass"):
    cands = [n for n in z.namelist()
             if n.startswith("assets/tfcflorae/textures/block/") and fam in n and n.endswith(".png")]
    cands = [c for c in cands if "black" in c][:3]
    print(f"\n  {fam}: {cands}")
    for c in cands:
        img = Image.open(io.BytesIO(z.read(c))).convert("RGBA")
        px = list(img.getdata())
        tot = 0
        green = 0
        for r, g, b, a in px:
            if a < 16:
                continue
            tot += 1
            if g > r + 12 and g > b + 12:
                green += 1
        print(f"     {os.path.basename(c):40s} {img.size}  不透明 {tot:4d}  绿 {green:4d} = {green/tot*100 if tot else 0:.1f}%")

z.close()

print("\n\n=== TFE jar 里 biome 相关路径 ===")
z = zipfile.ZipFile(TFE)
names = z.namelist()
biomeish = [n for n in names if "biome" in n.lower()]
print("  含 biome 的条目数:", len(biomeish))
for n in sorted(set(os.path.dirname(x) for x in biomeish))[:20]:
    print("   ", n)
print("\n  data 顶层目录:")
tops = sorted(set(n.split("/")[1] for n in names if n.startswith("data/") and len(n.split("/")) > 2))
print("   ", tops)
z.close()
