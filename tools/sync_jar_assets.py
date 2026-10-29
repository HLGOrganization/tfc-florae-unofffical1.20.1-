"""Delete stale asset files from the deployed jar.

Gradle's incremental processResources does not remove files that were deleted from
src/main/resources, so the jar kept tfcflorae assets for content we already dropped
(fluid/ore textures, podzol models, wood spikes...).  This removes every
assets/tfcflorae/** entry whose source file no longer exists.
"""
import os
import shutil
import zipfile

JAR = r"D:\线框的服务器\versions\TFC_Wild_Fire\mods\TFCFlorae-1.20.1-2.2.0-mc1.20.1.jar"
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC = os.path.join(ROOT, "src", "main", "resources")

# every source file we ship
src = set()
for dirpath, _d, files in os.walk(SRC):
    for fn in files:
        rel = os.path.relpath(os.path.join(dirpath, fn), SRC).replace(os.sep, "/")
        src.add(rel)

tmp = JAR + ".syncing"
removed = []
kept = 0
with zipfile.ZipFile(JAR) as zin, zipfile.ZipFile(tmp, "w", zipfile.ZIP_DEFLATED) as zout:
    for item in zin.infolist():
        name = item.filename
        # only prune our own assets; data was injected separately and classes are built
        if name.startswith("assets/tfcflorae/") and name not in src:
            removed.append(name)
            continue
        zout.writestr(item, zin.read(name))
        kept += 1
shutil.move(tmp, JAR)

print("kept entries:", kept)
print("stale asset entries removed:", len(removed))
from collections import Counter
c = Counter("/".join(n.split("/")[:4]) for n in removed)
for k, v in c.most_common(10):
    print("   %-46s %d" % (k, v))
