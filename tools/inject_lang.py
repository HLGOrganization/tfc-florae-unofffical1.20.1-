"""Inject the lang files (and any other assets) from src into the deployed jar.

sync_jar_assets.py only *removes* stale entries; content changes to existing files
still need to be written, otherwise a rebuilt zh_cn.json never reaches the game
without a full gradle build.
"""
import io
import json
import os
import shutil
import zipfile

JAR = r"D:\线框的服务器\versions\TFC_Wild_Fire\mods\TFCFlorae-1.20.1-2.2.0-mc1.20.1.jar"
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC = os.path.join(ROOT, "src", "main", "resources")

# paths (relative to resources) to push into the jar
TARGETS = [
    "assets/tfcflorae/lang/en_us.json",
    "assets/tfcflorae/lang/zh_cn.json",
]

tmp = JAR + ".langsync"
written = 0
with zipfile.ZipFile(JAR) as zin, zipfile.ZipFile(tmp, "w", zipfile.ZIP_DEFLATED) as zout:
    names = set(zin.namelist())
    for item in zin.infolist():
        if item.filename in TARGETS:
            continue
        zout.writestr(item, zin.read(item.filename))
    for rel in TARGETS:
        p = os.path.join(SRC, *rel.split("/"))
        if os.path.isfile(p):
            zout.write(p, rel)
            written += 1
            print("wrote", rel, os.path.getsize(p), "bytes")
shutil.move(tmp, JAR)
print("jar now", os.path.getsize(JAR), "bytes")
