"""Rebuild the wooden chests the way AFC does it: pure TFC blocks.

AFC has no chest classes at all - its chests are plain TFC ChestBlock instances
(BlockType.CHEST via create(wood)) with TFC's block entity and TFC's renderer, and the
block list of TFCBlockEntities.CHEST is extended with the same accessor mixin used for
the rotation machines.  This port instead registered its own TFCFChestBlock and
TFCFBlockEntities.CHEST, and that custom path is why the chests cannot be opened.

This script:
  1. removes the CHEST and TRAPPED_CHEST branches from woodMapper, so both fall through to
     BlockType.create(wood) like every other wood block
  2. extends TFCFRotationBlockEntities to add our chest blocks to TFC's CHEST and
     TRAPPED_CHEST block entity types
  3. copies the chest textures into TFC's namespace (tfc:textures/entity/chest/<folder>/),
     which is what TFCChestBlockEntityRenderer resolves for a TFC ChestBlock
"""
import io
import os
import shutil

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
BLOCKS = os.path.join(ROOT, "src", "main", "java", "tfcflorae", "common", "blocks", "TFCFBlocks.java")
ROT = os.path.join(ROOT, "src", "main", "java", "tfcflorae", "common", "blockentities",
                   "TFCFRotationBlockEntities.java")
A = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae")
TFC_NS = os.path.join(ROOT, "src", "main", "resources", "assets", "tfc")


def drop_else_if(src, marker):
    """remove a `} else if (<marker>) { ... }` block by brace matching"""
    idx = src.find(marker)
    if idx == -1:
        return src, False
    start = src.rfind("else if", 0, idx)
    # include the preceding `} ` of the previous branch
    brace_open = src.find("{", idx)
    depth = 0
    i = brace_open
    while i < len(src):
        if src[i] == "{":
            depth += 1
        elif src[i] == "}":
            depth -= 1
            if depth == 0:
                break
        i += 1
    if i >= len(src):
        return src, False
    return src[:start] + src[i + 1:].lstrip(), True


s = io.open(BLOCKS, encoding="utf-8").read()
for marker in ("type == BlockType.CHEST) {", "type == BlockType.TRAPPED_CHEST) {"):
    # keep removing until none left (there are two mapper loops)
    while True:
        s, ok = drop_else_if(s, marker)
        if not ok:
            break
io.open(BLOCKS, "w", encoding="utf-8", newline="\n").write(s)
print("woodMapper: chest branches removed")

# 2) extend TFC's chest block entity types
r = io.open(ROT, encoding="utf-8").read()
anchor = "        modifyBlockEntityType(\"windmill\", TFCBlockEntities.WINDMILL.get(), BlockType.WINDMILL);"
assert anchor in r, "windmill anchor"
add = anchor + """
        // chests work exactly like the rotation machines: TFC's own ChestBlock plus TFC's
        // chest block entity; without this the block entity is never created and the chest
        // cannot be opened.
        modifyBlockEntityType("chest", TFCBlockEntities.CHEST.get(), BlockType.CHEST);
        modifyBlockEntityType("trapped_chest", TFCBlockEntities.TRAPPED_CHEST.get(), BlockType.TRAPPED_CHEST);"""
r = r.replace(anchor, add, 1)
io.open(ROT, "w", encoding="utf-8", newline="\n").write(r)
print("block entity types: chest + trapped_chest added")

# 3) chest textures into tfc's namespace
FOLDERS = ["normal", "normal_left", "normal_right", "trapped", "trapped_left", "trapped_right"]
woods = sorted(f[:-5] for f in os.listdir(os.path.join(A, "blockstates", "wood", "planks"))
               if f.endswith(".json"))
moved = 0
for folder in FOLDERS:
    src_dir = os.path.join(A, "textures", "entity", "chest", folder)
    out_dir = os.path.join(TFC_NS, "textures", "entity", "chest", folder)
    os.makedirs(out_dir, exist_ok=True)
    for w in woods:
        sfile = os.path.join(src_dir, w + ".png")
        if os.path.isfile(sfile):
            shutil.copy2(sfile, os.path.join(out_dir, w + ".png"))
            moved += 1
print("chest textures copied into tfc namespace:", moved)
