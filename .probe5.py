import zipfile, io, json
from PIL import Image

TFC = r"D:\学习资料\附属模组工作区\[群峦传说] TerraFirmaCraft-Forge-1.20.1-3.2.20.jar"
OUR = r"D:\线框的服务器\versions\TFC_Wild_Fire\mods\TFCFlorae-1.20.1-2.2.0-mc1.20.1.jar"

print("=== 贴图 alpha 覆盖率（判断疏密）===")
zt = zipfile.ZipFile(TFC)
zo = zipfile.ZipFile(OUR)


def stat(z, path, label):
    if path not in z.namelist():
        print(f"   {label:34s} 缺失 {path}")
        return
    img = Image.open(io.BytesIO(z.read(path))).convert("RGBA")
    px = list(img.getdata())
    opaque = sum(1 for p in px if p[3] > 16)
    full = sum(1 for p in px if p[3] > 250)
    lum = [sum(p[:3]) / 3 for p in px if p[3] > 16]
    print(f"   {label:34s} {img.size}  不透明 {opaque:4d}/{len(px)} = {opaque/len(px)*100:5.1f}%"
          f"  全实心 {full/len(px)*100:5.1f}%  亮度 {sum(lum)/len(lum):5.1f}")


stat(zt, "assets/tfc/textures/block/grass_top.png", "tfc grass_top (普通)")
stat(zo, "assets/tfcflorae/textures/block/dense_grass_top.png", "tfcflorae dense_grass_top")
stat(zo, "assets/tfcflorae/textures/block/sparse_grass_top.png", "tfcflorae sparse_grass_top")
print()
stat(zt, "assets/tfc/textures/block/grass_side.png", "tfc grass_side (普通)")
stat(zo, "assets/tfcflorae/textures/block/dense_grass_side.png", "tfcflorae dense_grass_side")
stat(zo, "assets/tfcflorae/textures/block/sparse_grass_side.png", "tfcflorae sparse_grass_side")

zt.close()
zo.close()
