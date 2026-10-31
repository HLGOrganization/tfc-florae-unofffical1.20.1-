import zipfile, json, io
from PIL import Image

OUR = r"D:\线框的服务器\versions\TFC_Wild_Fire\mods\TFCFlorae-1.20.1-2.2.0-mc1.20.1.jar"
z = zipfile.ZipFile(OUR)


def model_textures(mp):
    if mp not in z.namelist():
        return None, None
    m = json.loads(z.read(mp))
    return m.get("parent"), m.get("textures")


def resolve_tex(tex, depth=0):
    """顺着 parent 链找贴图"""
    if tex and tex.startswith("tfcflorae:"):
        p = "assets/tfcflorae/textures/" + tex.split(":")[1] + ".png"
        if p in z.namelist():
            return p
    return None


print("=== 三种沙草方块 top 模型的绿像素占比 ===")
for fam in ("grass", "dense_grass", "sparse_grass"):
    mp = f"assets/tfcflorae/models/block/sand/{fam}/black/top.json"
    parent, tex = model_textures(mp)
    print(f"\n  sand/{fam}/black/top")
    print(f"    parent   = {parent}")
    print(f"    textures = {tex}")
    found = False
    if tex:
        for k, v in tex.items():
            tp = resolve_tex(v)
            if tp:
                found = True
                img = Image.open(io.BytesIO(z.read(tp))).convert("RGBA")
                px = [p for p in img.getdata() if p[3] > 16]
                green = sum(1 for r, g, b, a in px if g > r + 10 and g > b + 10)
                print(f"    {k:10s} {tp}")
                print(f"               {img.size} 不透明 {len(px)} 绿 {green} = {green/len(px)*100:.1f}%")
    if not found:
        print("    （没解析到贴图，看模型原文）")
        print("   ", z.read(mp).decode()[:500])

# 兜底：直接扫所有名字里带 grass 的贴图
print("\n=== 所有含 grass 的贴图 ===")
gs = sorted(n for n in z.namelist() if "grass" in n and n.endswith(".png") and "/block/" in n)
for g in gs:
    img = Image.open(io.BytesIO(z.read(g))).convert("RGBA")
    px = [p for p in img.getdata() if p[3] > 16]
    green = sum(1 for r, gg, b, a in px if gg > r + 10 and gg > b + 10)
    ratio = green / len(px) * 100 if px else 0
    print(f"   {ratio:5.1f}% 绿   {g}")
z.close()
