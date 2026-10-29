"""Batch 2: reuse TFC's implementations for barrel, loom and drying bricks.

Same story as batch 1 - the custom classes are partial copies:

    TFCFBarrelBlock         92 lines vs TFC BarrelBlock's 296   (no attack, getShape, ...)
    TFCFBarrelBlockEntity  171 lines vs 708
    TFCFLoomBlock           33 lines vs TFC TFCLoomBlock's 147  (no getShape, fluid)
    TFCFLoomBlockEntity     20 lines vs 277
    TFCFDryingBricksBlock   93 lines vs 207                     (no canBeReplaced, ...)

Changes:
  1. woodMapper: drop the BARREL and LOOM branches so they fall through to
     BlockType.create(wood)
  2. TFCFSoil.DRYING_BRICKS uses TFC's DryingBricksBlock
  3. TFCFRotationBlockEntities extends TFCBlockEntities.BARREL and LOOM
"""
import io
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
BLOCKS = os.path.join(ROOT, "src", "main", "java", "tfcflorae", "common", "blocks", "TFCFBlocks.java")
SOIL = os.path.join(ROOT, "src", "main", "java", "tfcflorae", "common", "blocks", "soil", "TFCFSoil.java")
ROT = os.path.join(ROOT, "src", "main", "java", "tfcflorae", "common", "blockentities",
                   "TFCFRotationBlockEntities.java")


def drop_else_if(src, marker):
    idx = src.find(marker)
    if idx == -1:
        return src, False
    start = src.rfind("else if", 0, idx)
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
removed = 0
for marker in ("type == BlockType.BARREL) {", "type == BlockType.LOOM) {"):
    while True:
        s, ok = drop_else_if(s, marker)
        if not ok:
            break
        removed += 1
io.open(BLOCKS, "w", encoding="utf-8", newline="\n").write(s)
print("woodMapper: removed", removed, "branches (barrel / loom)")

# drying bricks -> TFC's class
so = io.open(SOIL, encoding="utf-8").read()
i = so.find("new TFCFDryingBricksBlock(")
assert i != -1, "drying bricks anchor"
# print the current registration for the log, then swap the class name
j = so.find("),", i)
print("   current:", " ".join(so[i:j + 1].split())[:200])
so = so.replace("new TFCFDryingBricksBlock(", "new net.dries007.tfc.common.blocks.devices.DryingBricksBlock(", 1)
io.open(SOIL, "w", encoding="utf-8", newline="\n").write(so)
print("TFCFSoil.DRYING_BRICKS now uses TFC's DryingBricksBlock")

# extend barrel + loom block entities
r = io.open(ROT, encoding="utf-8").read()
anchor = '        modifyBlockEntityType("tool_rack", TFCBlockEntities.TOOL_RACK.get(), BlockType.TOOL_RACK);'
assert anchor in r, "tool_rack anchor"
add = anchor + """
        // barrel and loom: TFC's classes, so TFC's GUI, data and renderers apply
        modifyBlockEntityType("barrel", TFCBlockEntities.BARREL.get(), BlockType.BARREL);
        modifyBlockEntityType("loom", TFCBlockEntities.LOOM.get(), BlockType.LOOM);"""
r = r.replace(anchor, add, 1)
io.open(ROT, "w", encoding="utf-8", newline="\n").write(r)
print("TFCFRotationBlockEntities: barrel + loom added")
