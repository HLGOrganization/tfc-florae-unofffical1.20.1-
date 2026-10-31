import zipfile, json, glob, os

TFE = r"D:\线框的服务器\versions\TFC_Wild_Fire\mods\Wildfire_TerraFirmaEarth-1.20.1-forge-2.3.0.jar"
OUR = r"D:\线框的服务器\versions\TFC_Wild_Fire\mods\TFCFlorae-1.20.1-2.2.0-mc1.20.1.jar"
TFC = r"D:\学习资料\附属模组工作区\[群峦传说] TerraFirmaCraft-Forge-1.20.1-3.2.20.jar"

print("=== TFE 里像恶地 / 沙丘 的群系 ===")
z = zipfile.ZipFile(TFE)
ids = sorted(n[len("data/tfe/worldgen/biome/"):-5] for n in z.namelist()
             if n.startswith("data/tfe/worldgen/biome/") and n.endswith(".json"))
print("   TFE 群系总数:", len(ids))
kw = ("badland", "mesa", "dune", "desert", "red_", "canyon", "arid", "sand")
hits = [i for i in ids if any(k in i for k in kw)]
for i in hits:
    print("   tfe:" + i)
print()
print("=== TFE 群系全表（前 40）===")
print("   " + ", ".join(ids[:40]))
z.close()

print()
print("=== TFC 里像恶地的群系 ===")
z = zipfile.ZipFile(TFC)
tids = sorted(n[len("data/tfc/worldgen/biome/"):-5] for n in z.namelist()
              if n.startswith("data/tfc/worldgen/biome/") and n.endswith(".json"))
print("   TFC 群系总数:", len(tids))
for i in tids:
    if any(k in i for k in kw):
        print("   tfc:" + i)
print("   TFC 全表:", ", ".join(tids))
z.close()

print()
print("=== 三种沙子草方块的中文名（确认疏密顺序）===")
z = zipfile.ZipFile(OUR)
zh = json.loads(z.read("assets/tfcflorae/lang/zh_cn.json"))
for fam in ("grass", "dense_grass", "sparse_grass"):
    for c in ("black", "brown", "green", "pink", "red", "white", "yellow"):
        k = f"block.tfcflorae.sand.{fam}.{c}"
        if k in zh:
            print(f"   {fam:14s} {c:7s} -> {zh[k]}")
    print()
print("=== blockstates 是否齐全 ===")
for fam in ("grass", "dense_grass", "sparse_grass"):
    n = len([x for x in z.namelist()
             if x.startswith(f"assets/tfcflorae/blockstates/sand/{fam}/") and x.endswith(".json")])
    print(f"   sand/{fam}/<color> : {n} 个")
z.close()
