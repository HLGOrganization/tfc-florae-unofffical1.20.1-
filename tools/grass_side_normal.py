# -*- coding: utf-8 -*-
"""让草方块侧面显示正常的侧面纹理（而不是顶部纹理）。

TFC 系草方块 blockstate 是 multipart：
  - bottom / top / snowy_top：基础几何（无 when）
  - 方向分支（when.north/east/south/west = true）：阶梯裸露的那一面，
    原版用 .../top 或 .../snowy_top 叠加 → 侧面被画成草顶。
本工具把这些**方向分支**的模型换成 .../side 与 .../snowy_side，
分支本身保留（它们提供侧面几何，删掉会变透明）。

输出写入 paxi 资源包 Wild_Fire.zip，按原路径覆盖，不需要重编模组 jar。
"""
import json
import os
import shutil
import sys
import zipfile

TFC = r"D:\学习资料\附属模组工作区\[群峦传说] TerraFirmaCraft-Forge-1.20.1-3.2.20.jar"
TFE = r"D:\线框的服务器\versions\TFC_Wild_Fire\mods\Wildfire_TerraFirmaEarth-1.20.1-forge-2.3.0.jar"
OURS = r"D:\学习资料\附属模组工作区\tfc_florae_port\build\libs\TFCFlorae-1.20.1-2.2.0-mc1.20.1.jar"
PACK = r"D:\线框的服务器\versions\TFC_Wild_Fire\config\paxi\resourcepacks\Wild_Fire.zip"
APPLY = "--apply" in sys.argv

DIRS = ("north", "east", "south", "west")


def is_grass_bs(path):
    return "/blockstates/" in path and path.endswith(".json") and "grass" in path


def fix(j):
    """把方向分支的模型从 top/snowy_top 换成 side/snowy_side；返回改动条数。"""
    changed = 0
    for part in j.get("multipart", []):
        when = part.get("when", {})
        if not any(d in when for d in DIRS):
            continue
        apply = part.get("apply")
        items = apply if isinstance(apply, list) else [apply]
        for a in items:
            m = a.get("model", "")
            if m.endswith("/snowy_top"):
                a["model"] = m[: -len("/snowy_top")] + "/snowy_side"
                changed += 1
            elif m.endswith("/top"):
                a["model"] = m[: -len("/top")] + "/side"
                changed += 1
    return changed


overrides = {}
for label, jar in (("tfc", TFC), ("tfe", TFE), ("tfcflorae", OURS)):
    if not os.path.exists(jar):
        print("跳过: " + jar)
        continue
    z = zipfile.ZipFile(jar)
    total = 0
    for e in z.namelist():
        if not e.startswith("assets/") or not is_grass_bs(e):
            continue
        try:
            j = json.loads(z.read(e).decode("utf-8"))
        except Exception:
            continue
        n = fix(j)
        if n:
            overrides[e] = (label, j, n)
            total += 1
    z.close()
    print("%-10s 需改 %d 个" % (label, total))

print()
print("合计覆盖 %d 个 blockstate" % len(overrides))
for e in sorted(overrides)[:6]:
    print("   %-62s 换 %d 条（%s）" % (e, overrides[e][2], overrides[e][0]))

if not os.path.exists(PACK):
    print("!! 资源包不存在")
    sys.exit(1)

if not APPLY:
    print()
    print("（预演，加 --apply 才写）")
    sys.exit(0)

tmp = PACK + ".tmp"
with zipfile.ZipFile(PACK) as zin, zipfile.ZipFile(tmp, "w", zipfile.ZIP_DEFLATED) as zout:
    existing = set()
    for info in zin.infolist():
        if info.is_dir():
            continue
        existing.add(info.filename)
        data = zin.read(info.filename)
        if info.filename in overrides:
            data = (json.dumps(overrides[info.filename][1], ensure_ascii=False,
                               separators=(",", ":")) + "\n").encode("utf-8")
        zout.writestr(info.filename, data)
    for path, (label, j, n) in overrides.items():
        if path in existing:
            continue
        zout.writestr(path, (json.dumps(j, ensure_ascii=False,
                                        separators=(",", ":")) + "\n").encode("utf-8"))

shutil.move(tmp, PACK)
with zipfile.ZipFile(PACK) as z:
    print()
    print("写入完成: %d 条目, %.1f MB, 校验 %s" % (
        len(z.infolist()), os.path.getsize(PACK) / 1048576.0,
        "通过" if z.testzip() is None else "损坏"))
