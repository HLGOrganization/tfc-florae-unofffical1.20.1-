"""Batch 1: reuse TFC's implementations for sluice, farmland and tool rack.

These three were ported as custom classes that are only partial copies of TFC's
(TFCFSluiceBlock is 13 lines vs TFC's 204, TFCFFarmlandBlock 29 vs 233, TFCFToolRackBlock
29 vs 186) so they are missing entityInside, getFluidOutputPos, getHydration, canSurvive,
getShape and more.  AFC ships none of these classes - it uses TFC's blocks directly.

Changes:
  1. woodMapper: the SLUICE and TOOL_RACK branches are removed, so both fall through to
     BlockType.create(wood) (TFC's SluiceBlock / ToolRackBlock)
  2. TFCFSoil.FARMLAND uses TFC's FarmlandBlock instead of the custom one
  3. TFCFRotationBlockEntities extends TFCBlockEntities.SLUICE / TOOL_RACK / FARMLAND
     with our blocks so the block entities exist
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


# ---- 1) woodMapper: drop SLUICE / TOOL_RACK ------------------------------
s = io.open(BLOCKS, encoding="utf-8").read()
removed = 0
for marker in ("type == BlockType.SLUICE) {", "type == BlockType.TOOL_RACK) {"):
    while True:
        s, ok = drop_else_if(s, marker)
        if not ok:
            break
        removed += 1
io.open(BLOCKS, "w", encoding="utf-8", newline="\n").write(s)
print("woodMapper: removed", removed, "branches (sluice / tool_rack)")

# ---- 2) farmland uses TFC's block --------------------------------------
so = io.open(SOIL, encoding="utf-8").read()
old = """   FARMLAND(
      (self, variant) -> new TFCFFarmlandBlock(
            ExtendedProperties.of(
                  Properties.of().mapColor(MapColor.DIRT).strength(0.6F).sound(SoundType.GRAVEL).isViewBlocking(TFCBlocks::always).isSuffocating(TFCBlocks::always)
               )
               .blockEntity(TFCFBlockEntities.FARMLAND),
            (Supplier<? extends Block>)TFCFBlocks.TFCFSOIL.get(self.transform()).get(variant)
         )
   ),"""
new = """   FARMLAND(
      (self, variant) -> new net.dries007.tfc.common.blocks.soil.FarmlandBlock(
            ExtendedProperties.of(
                  Properties.of().mapColor(MapColor.DIRT).strength(0.6F).sound(SoundType.GRAVEL).isViewBlocking(TFCBlocks::always).isSuffocating(TFCBlocks::always)
               )
               .blockEntity(net.dries007.tfc.common.blockentities.TFCBlockEntities.FARMLAND),
            (Supplier<? extends Block>)TFCFBlocks.TFCFSOIL.get(self.transform()).get(variant)
         )
   ),"""
assert old in so, "farmland anchor"
io.open(SOIL, "w", encoding="utf-8", newline="\n").write(so.replace(old, new, 1))
print("TFCFSoil.FARMLAND now uses TFC's FarmlandBlock")

# ---- 3) extend TFC's block entity types --------------------------------
r = io.open(ROT, encoding="utf-8").read()
anchor = """        modifyBlockEntityType("trapped_chest", TFCBlockEntities.TRAPPED_CHEST.get(), BlockType.TRAPPED_CHEST);"""
assert anchor in r, "chest anchor"
add = anchor + """
        // sluice / tool rack / farmland: TFC's classes, so TFC's block entities, renderers
        // and update logic all apply; only the block list needs our blocks
        modifyBlockEntityType("sluice", TFCBlockEntities.SLUICE.get(), BlockType.SLUICE);
        modifyBlockEntityType("tool_rack", TFCBlockEntities.TOOL_RACK.get(), BlockType.TOOL_RACK);
        extendSoilBlockEntity();"""
r = r.replace(anchor, add, 1)

# helper for the farmland blocks (they live in TFCFBlocks.TFCFSOIL, not WOODS)
helper_anchor = "   private static void modifyBlockEntityType(String label, BlockEntityType<?> type, BlockType blockType)"
assert helper_anchor in r, "helper anchor"
helper = '''   /** Farmland blocks are registered per soil variant in TFCFBlocks.TFCFSOIL. */
   private static void extendSoilBlockEntity()
   {
      try
      {
         final Set<Block> extra = new HashSet<>();
         for (Map<?, ?> variants : TFCFBlocks.TFCFSOIL.values())
         {
            for (Object value : variants.values())
            {
               if (value instanceof RegistryObject<?> reg && reg.get() instanceof Block block)
               {
                  extra.add(block);
               }
            }
         }
         addBlocks("farmland", net.dries007.tfc.common.blockentities.TFCBlockEntities.FARMLAND.get(), extra);
      }
      catch (Throwable t)
      {
         LOGGER.error("TFCFlorae: could not extend the TFC farmland block entity type", t);
      }
   }

''' + helper_anchor
r = r.replace(helper_anchor, helper, 1)

# generic addBlocks(Set) used by the soil helper
add_anchor = "   @SuppressWarnings(\"unchecked\")\n   private static void modifyBlockEntityType"
if add_anchor not in r:
    add_anchor = "   private static void modifyBlockEntityType(String label, BlockEntityType<?> type, BlockType blockType)"
helper2_anchor = add_anchor
helper2 = '''   private static void addBlocks(String label, BlockEntityType<?> type, Set<Block> extra)
   {
      try
      {
         final Set<Block> blocks = new HashSet<>(((BlockEntityTypeAccessor) type).accessor$getValidBlocks());
         final int before = blocks.size();
         blocks.addAll(extra);
         if (blocks.size() == before)
         {
            return;
         }
         ((BlockEntityTypeAccessor) type).accessor$setValidBlocks(blocks);
         LOGGER.info("TFCFlorae: added {} blocks to TFC {} block entity type", blocks.size() - before, label);
      }
      catch (Throwable t)
      {
         LOGGER.error("TFCFlorae: could not extend the TFC {} block entity type", label, t);
      }
   }

''' + helper2_anchor
r = r.replace(helper2_anchor, helper2, 1)
io.open(ROT, "w", encoding="utf-8", newline="\n").write(r)
print("TFCFRotationBlockEntities: sluice / tool_rack / farmland added")
