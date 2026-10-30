"""Rebuild the wooden bookshelves the AFC way: vanilla 1.20 ChiseledBookshelfBlock.

Our port registered 1.18-style bookshelves - an opaque ExtendedBlock at
wood/bookshelf/<wood> plus a custom ChiseledBookshelfBlock at
wood/chiseled_bookshelf/<wood> with its own block entity.  1.20.1 TFC has neither:
BlockType.BOOKSHELF is a plain block and AFC registers its shelves as
wood/planks/<wood>_bookshelf using vanilla's ChiseledBookshelfBlock (its model's parent
is literally minecraft:block/chiseled_bookshelf).

Changes:
  1. BlockType.BOOKSHELF registers wood/planks/<wood>_bookshelf with vanilla's
     ChiseledBookshelfBlock
  2. the chiseled_bookshelf family (mapper + custom block + block entity) is removed
  3. vanilla BlockEntityType.CHISELED_BOOKSHELF is extended with our blocks
  4. assets are regenerated in AFC's layout and the old 1.18 directories are dropped
"""
import io
import json
import os
import re
import shutil

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
J = os.path.join(ROOT, "src", "main", "java", "tfcflorae")
A = os.path.join(ROOT, "src", "main", "resources", "assets", "tfcflorae")
SRC118 = os.path.join(os.path.dirname(ROOT), "tfc_florae", "jar", "assets", "tfcflorae")

# --------------------------------------------------------------------------
# 1) java: BOOKSHELF branch -> vanilla ChiseledBookshelfBlock
# --------------------------------------------------------------------------
p = os.path.join(J, "common", "blocks", "TFCFBlocks.java")
s = io.open(p, encoding="utf-8").read()

old = """                  } else if (type == BlockType.BOOKSHELF) {
                     subMap.put(
                        type,
                        register(
                           ("wood/bookshelf/" + wood.getSerializedName()).toLowerCase(Locale.ROOT),
                           () -> new ExtendedBlock(
                                 ExtendedProperties.of(
                                       net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(wood.woodColor())
                                          .sound(SoundType.WOOD)
                                          .strength(2.0F, 3.0F)
                                    )
                                    .flammableLikePlanks()
                              ),
                           TFCCreativeTabs.WOOD
                        )
                     );
                  } else {"""
new = """                  } else if (type == BlockType.BOOKSHELF) {
                     // 1.20 layout: one chiseled bookshelf per wood, exactly like AFC
                     // (id wood/planks/<wood>_bookshelf, vanilla ChiseledBookshelfBlock)
                     subMap.put(
                        type,
                        register(
                           ("wood/planks/" + wood.getSerializedName() + "_bookshelf").toLowerCase(Locale.ROOT),
                           () -> new net.minecraft.world.level.block.ChiseledBookshelfBlock(
                                 net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()
                                    .mapColor(wood.woodColor())
                                    .sound(SoundType.WOOD)
                                    .strength(2.0F, 3.0F)
                                    .noOcclusion()
                              ),
                           TFCCreativeTabs.WOOD
                        )
                     );
                  } else {"""
assert old in s, "bookshelf branch anchor"
s = s.replace(old, new, 1)

# drop the chiseled bookshelf mapper entirely
m = re.search(r'[ \t]*private static Map<Wood, RegistryObject<Block>> chiseledBookshelfMapperTFC\(Class<Wood> enumClass\) \{', s)
if m:
    start = m.start()
    # brace-match the whole method
    brace = s.find("{", m.end())
    depth = 0
    i = brace
    while i < len(s):
        if s[i] == "{":
            depth += 1
        elif s[i] == "}":
            depth -= 1
            if depth == 0:
                break
        i += 1
    end = s.find("\n", i)
    s = s[:start] + s[end + 1:]
    print("removed chiseledBookshelfMapperTFC")

# any call sites of that mapper
s = re.sub(r'^[ \t]*[^\n]*chiseledBookshelfMapperTFC\([^\n]*\)[^\n]*\n', '', s, flags=re.M)
io.open(p, "w", encoding="utf-8", newline="\n").write(s)
print("BOOKSHELF branch -> vanilla ChiseledBookshelfBlock")

# --------------------------------------------------------------------------
# 2) java: extend vanilla CHISELED_BOOKSHELF block entity
# --------------------------------------------------------------------------
q = os.path.join(J, "common", "blockentities", "TFCFRotationBlockEntities.java")
r = io.open(q, encoding="utf-8").read()
if "extendChiseledBookshelves" not in r:
    anchor = "        extendLargeVessels();"
    assert anchor in r, "anchor"
    r = r.replace(anchor, anchor + "\n        extendChiseledBookshelves();", 1)
    ha = "   /** Fired clay large vessels use TFC's LargeVesselBlock, hence its block entity. */"
    assert ha in r, "helper anchor"
    helper = '''   /** Vanilla ChiseledBookshelfBlock keeps its block entity in a vanilla type. */
   private static void extendChiseledBookshelves()
   {
      try
      {
         final Set<Block> extra = new HashSet<>();
         for (Map<BlockType, RegistryObject<Block>> parts : TFCFBlocks.WOODS.values())
         {
            final RegistryObject<Block> block = parts.get(BlockType.BOOKSHELF);
            if (block != null && block.get() instanceof Block b)
            {
               extra.add(b);
            }
         }
         addBlocks("chiseled_bookshelf",
            net.minecraft.world.level.block.entity.BlockEntityType.CHISELED_BOOKSHELF, extra);
      }
      catch (Throwable t)
      {
         LOGGER.error("TFCFlorae: could not extend the vanilla chiseled bookshelf block entity type", t);
      }
   }

''' + ha
    r = r.replace(ha, helper, 1)
    io.open(q, "w", encoding="utf-8", newline="\n").write(r)
    print("vanilla CHISELED_BOOKSHELF extension added")

# --------------------------------------------------------------------------
# 3) assets: remove old 1.18 directories
# --------------------------------------------------------------------------
for rel in ("textures/block/wood/bookshelf", "textures/block/wood/chiseled_bookshelf",
            "models/block/wood/bookshelf", "models/block/wood/chiseled_bookshelf",
            "blockstates/wood/bookshelf", "blockstates/wood/chiseled_bookshelf",
            "models/item/wood/bookshelf", "models/item/wood/chiseled_bookshelf"):
    p2 = os.path.join(A, rel)
    if os.path.isdir(p2):
        shutil.rmtree(p2)
        print("removed", rel)
