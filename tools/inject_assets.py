"""Push src assets into the deployed jar: overwrite changed files, drop stale ones.

Gradle's incremental processResources neither updates deleted files nor notices
every change, so for resource-only work this replaces the whole assets/tfcflorae
section from src.  Seconds instead of a 3 minute build.
"""
import os
import shutil
import zipfile

JAR = r"D:\线框的服务器\versions\TFC_Wild_Fire\mods\TFCFlorae-1.20.1-2.2.0-mc1.20.1.jar"
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC = os.path.join(ROOT, "src", "main", "resources")

files = {}
for dirpath, _d, fs in os.walk(os.path.join(SRC, "assets", "tfcflorae")):
    for fn in fs:
        full = os.path.join(dirpath, fn)
        rel = os.path.relpath(full, SRC).replace(os.sep, "/")
        files[rel] = full

print("source asset files:", len(files))

tmp = JAR + ".assetinject"
kept = dropped = 0
with zipfile.ZipFile(JAR) as zin, zipfile.ZipFile(tmp, "w", zipfile.ZIP_DEFLATED) as zout:
    for item in zin.infolist():
        name = item.filename
        if name.startswith("assets/tfcflorae/"):
            dropped += 1
            continue  # replaced below
        zout.writestr(item, zin.read(name))
        kept += 1
    for rel, full in sorted(files.items()):
        zout.write(full, rel)
shutil.move(tmp, JAR)
print("kept:", kept, "replaced:", dropped, "with", len(files))
print("jar size:", os.path.getsize(JAR))
