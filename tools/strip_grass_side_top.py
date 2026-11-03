# -*- coding: utf-8 -*-
"""把「草方块侧面用顶部纹理」的多方向分支从 blockstate 里剔除，写入 Wild_Fire.zip 资源包覆盖。

来源：TFC jar / TFE jar / 本模组 jar 里所有含 grass 且带 multipart 方向分支的 blockstate。
输出：D:\\线框的服务器\\versions\\TFC_Wild_Fire\\config\\paxi\\resourcepacks\\Wild_Fire.zip
      （按原路径覆盖，pack 优先级最高，不需要重编模组）
"""
import io
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


def strip(j):
    mp = j.get("multipart")
    if not mp:
        return j, 0
    kept, removed = [], 0
    for part in mp:
        when = part.get("when", {})
        if any(d in when for d in DIRS):
            removed += 1
        else:
            kept.append(part)
    if removed:
        j["multipart"] = kept
    return j, removed


overrides = {}
for label, jar in (("tfc", TFC), ("tfe", TFE), ("tfcflorae", OURS)):
    if not os.path.exists(jar):
        print("跳过: " + jar)
        continue
    z = zipfile.ZipFile(jar)
    n = 0
    for e in z.namelist():
        if not e.startswith("assets/") or not is_grass_bs(e):
            continue
        try:
            j = json.loads(z.read(e).decode("utf-8"))
        except Exception:
            continue
        nj, removed = strip(j)
        if removed:
            overrides[e] = (label, nj, removed)
            n += 1
    z.close()
    print("%-10s 需改 %d 个" % (label, n))

print()
print("合计覆盖 %d 个 blockstate" % len(overrides))
sample = sorted(overrides)[:10]
for e in sample:
    print("   %-62s 删 %d 条（%s）" % (e, overrides[e][2], overrides[e][0]))

# 检查目标包
if not os.path.exists(PACK):
    print("!! 资源包不存在: " + PACK)
    sys.exit(1)
zp = zipfile.ZipFile(PACK)
names = [x for x in zp.infolist()]
print()
print("目标包: %d 条目, %.1f MB" % (len(names), os.path.getsize(PACK) / 1048576.0))
zp.close()

if not APPLY:
    print()
    print("（预演，加 --apply 才写）")
    sys.exit(0)

tmp = PACK + ".tmp"
with zipfile.ZipFile(PACK) as zin, zipfile.ZipFile(tmp, "w", zipfile.ZIP_DEFLATED) as zout:
    for info in zin.infolist():
        if info.is_dir():
            continue
        data = zin.read(info.filename)
        if info.filename in overrides:
            _, nj, _ = overrides[info.filename]
            data = (json.dumps(nj, ensure_ascii=False, separators=(",", ":")) + "\n").encode("utf-8")
        zout.writestr(info.filename, data)
    # 目标包里没有的，追加
    existing = set(i.filename for i in zipfile.ZipFile(PACK).infolist())
    for path, (label, nj, removed) in overrides.items():
        if path in existing:
            continue
        zout.writestr(path, (json.dumps(nj, ensure_ascii=False, separators=(",", ":")) + "\n").encode("utf-8"))

shutil.move(tmp, PACK)

with zipfile.ZipFile(PACK) as z:
    bad = z.testzip()
    print()
    print("写入完成: %d 条目, %.1f MB" % (len(z.infolist()), os.path.getsize(PACK) / 1048576.0))
    print("zip 校验: %s" % ("通过" if bad is None else ("损坏 " + str(bad))))
    print("pack.mcmeta 存在: %s" % ("pack.mcmeta" in z.namelist()))
