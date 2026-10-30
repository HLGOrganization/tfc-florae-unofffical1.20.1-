"""Sea urchin was the only groundcover type, so the whole groundcover feature goes.

  TFCFGroundcoverBlockType  (enum with a single SEA_URCHIN constant)
  TFCFGroundcoverBlock
  TFCFBlocks.GROUNDCOVER
  TFCFInteractionManager references
  ClientEventHandler render-layer entry
  textures + already-removed assets, loot table and tag entries
"""
import io
import json
import os
import re

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
J = os.path.join(ROOT, "src", "main", "java", "tfcflorae")
RES = os.path.join(ROOT, "src", "main", "resources")

# ---- 1) delete the two classes -------------------------------------------
for rel in ("common/blocks/TFCFGroundcoverBlockType.java",
            "common/blocks/TFCFGroundcoverBlock.java"):
    p = os.path.join(J, rel)
    if os.path.isfile(p):
        os.remove(p)
        print("deleted", rel)

# ---- 2) TFCFBlocks: drop the GROUNDCOVER field ---------------------------
p = os.path.join(J, "common", "blocks", "TFCFBlocks.java")
s = io.open(p, encoding="utf-8").read()
m = re.search(r'[ \t]*public static final Map<TFCFGroundcoverBlockType, RegistryObject<Block>> GROUNDCOVER = .*?\);\n', s, re.S)
if m:
    s = s[:m.start()] + s[m.end():]
    print("removed GROUNDCOVER field")
s = re.sub(r'^import [\w\.]*\.TFCFGroundcoverBlock(Type)?;\n', '', s, flags=re.M)
io.open(p, "w", encoding="utf-8", newline="\n").write(s)

# ---- 3) interaction manager ---------------------------------------------
p = os.path.join(J, "util", "TFCFInteractionManager.java")
if os.path.isfile(p):
    s = io.open(p, encoding="utf-8").read()
    before = s
    s = re.sub(r'^[ \t]*[^\n]*GROUNDCOVER[^\n]*\n', '', s, flags=re.M)
    s = re.sub(r'^import [\w\.]*\.TFCFGroundcoverBlock(Type)?;\n', '', s, flags=re.M)
    if s != before:
        io.open(p, "w", encoding="utf-8", newline="\n").write(s)
        print("cleaned TFCFInteractionManager")

# ---- 4) client render layer ---------------------------------------------
p = os.path.join(J, "client", "ClientEventHandler.java")
s = io.open(p, encoding="utf-8").read()
before = s
s = re.sub(r'^[ \t]*[^\n]*GROUNDCOVER[^\n]*\n', '', s, flags=re.M)
if s != before:
    io.open(p, "w", encoding="utf-8", newline="\n").write(s)
    print("cleaned ClientEventHandler")

# ---- 5) leftover textures ------------------------------------------------
for rel in ("assets/tfcflorae/textures/block/groundcover/sea_urchin_spikes.png",
            "assets/tfcflorae/textures/item/groundcover/sea_urchin.png"):
    f = os.path.join(RES, rel)
    if os.path.isfile(f):
        os.remove(f)
        print("deleted", rel)

# ---- 6) any remaining references ----------------------------------------
left = []
for dirpath, _d, files in os.walk(J):
    for fn in files:
        if not fn.endswith(".java"):
            continue
        fp = os.path.join(dirpath, fn)
        t = io.open(fp, encoding="utf-8").read()
        if re.search(r'Groundcover|GROUNDCOVER|SEA_URCHIN', t):
            left.append(os.path.relpath(fp, J))
print("java files still mentioning groundcover:", left if left else "none")
