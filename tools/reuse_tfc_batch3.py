"""Batch 3: remove the anvil chain, the barrel GUI chain and the loom block entity.

After the earlier reworks these all duplicate TFC and have no live entry point:

  anvil    TFCFRockAnvilBlock was only imported (never instantiated), so the anvil block,
           its block entity, both menus and both screens are unreachable
  barrel   the barrel is a TFC block now, so TFCFBarrelScreen / Container / SealButton and
           TFCFBarrelBlockEntity are dead
  loom     the loom is a TFC block now, so TFCFLoomBlockEntity is dead

Deletes the classes, their entries in TFCFBlockEntities / TFCFContainerTypes, the screen
registrations in ClientEventHandler and the now-dangling imports.
"""
import io
import os
import re

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
JAVA = os.path.join(ROOT, "src", "main", "java", "tfcflorae")

DEAD_CLASSES = [
    # anvil
    "common/blocks/devices/TFCFAnvilBlock.java",
    "common/blocks/rock/TFCFRockAnvilBlock.java",
    "common/blockentities/TFCFAnvilBlockEntity.java",
    "client/screen/TFCFAnvilScreen.java",
    "client/screen/TFCFAnvilPlanScreen.java",
    "common/container/TFCFAnvilContainer.java",
    "common/container/TFCFAnvilPlanContainer.java",
    # barrel
    "client/screen/TFCFBarrelScreen.java",
    "client/screen/button/TFCFBarrelSealButton.java",
    "common/container/TFCFBarrelContainer.java",
    "common/blockentities/TFCFBarrelBlockEntity.java",
    # loom
    "common/blockentities/TFCFLoomBlockEntity.java",
]
DEAD_NAMES = {os.path.basename(p)[:-5] for p in DEAD_CLASSES}

BE_FIELDS = ["ANVIL", "BARREL", "LOOM"]
MENU_FIELDS = ["ANVIL", "ANVIL_PLAN", "BARREL"]


def drop_static_field(src, field):
    """remove `public static final ... FIELD = register(...);` by brace matching"""
    m = re.search(r'[ \t]*public static final [^;=]*?\b([A-Z_]+)\b[^=]*=\s*register\(', src)
    # search specifically for the requested field name
    m = re.search(r'[ \t]*public static final [^;=]*\b' + field + r'\b[^=]*=\s*(register|TFCFContainerTypes\.[\w]*register[\w]*)\(', src)
    if not m:
        return src, False
    start = m.start()
    paren = src.find("(", m.end())
    depth = 0
    i = paren
    while i < len(src):
        if src[i] == "(":
            depth += 1
        elif src[i] == ")":
            depth -= 1
            if depth == 0:
                break
        i += 1
    end = src.find(";", i)
    # eat the rest of the line
    nl = src.find("\n", end)
    return src[:start] + src[nl + 1:], True


# ---- 1) delete files ------------------------------------------------------
for rel in DEAD_CLASSES:
    p = os.path.join(JAVA, rel)
    if os.path.isfile(p):
        os.remove(p)
        print("deleted", rel)
    else:
        print("already gone", rel)

# ---- 2) block entity + menu registrations --------------------------------
be = os.path.join(JAVA, "common", "blockentities", "TFCFBlockEntities.java")
s = io.open(be, encoding="utf-8").read()
for field in BE_FIELDS:
    while True:
        s, ok = drop_static_field(s, field)
        if not ok:
            break
        print("  removed block entity field", field)
io.open(be, "w", encoding="utf-8", newline="\n").write(s)

ct = os.path.join(JAVA, "common", "container", "TFCFContainerTypes.java")
s = io.open(ct, encoding="utf-8").read()
for field in MENU_FIELDS:
    while True:
        s, ok = drop_static_field(s, field)
        if not ok:
            break
        print("  removed menu field", field)
io.open(ct, "w", encoding="utf-8", newline="\n").write(s)

# ---- 3) screen registrations ---------------------------------------------
ce = os.path.join(JAVA, "client", "ClientEventHandler.java")
s = io.open(ce, encoding="utf-8").read()
for cls in ("TFCFAnvilScreen", "TFCFAnvilPlanScreen", "TFCFBarrelScreen"):
    s, n = re.subn(r'^[ \t]*event\.register\([^\n]*' + cls + r'[^\n]*\n', '', s, flags=re.M)
    if n:
        print("  removed screen registration:", cls, n)
io.open(ce, "w", encoding="utf-8", newline="\n").write(s)

# ---- 4) dangling imports --------------------------------------------------
for dirpath, _d, files in os.walk(JAVA):
    for fn in files:
        if not fn.endswith(".java"):
            continue
        p = os.path.join(dirpath, fn)
        t = io.open(p, encoding="utf-8").read()
        o = t
        for name in DEAD_NAMES:
            t = re.sub(r'^import [\w\.]*\.' + re.escape(name) + r';\n', '', t, flags=re.M)
        if t != o:
            io.open(p, "w", encoding="utf-8", newline="\n").write(t)
            print("  cleaned imports in", os.path.relpath(p, JAVA))

print("\nremaining references to deleted classes:")
left = 0
for dirpath, _d, files in os.walk(JAVA):
    for fn in files:
        if not fn.endswith(".java"):
            continue
        p = os.path.join(dirpath, fn)
        txt = io.open(p, encoding="utf-8").read()
        for name in DEAD_NAMES:
            if re.search(r'\b' + re.escape(name) + r'\b', txt):
                print("   ", os.path.relpath(p, JAVA), "->", name)
                left += 1
print("   none" if left == 0 else "   %d hits" % left)
