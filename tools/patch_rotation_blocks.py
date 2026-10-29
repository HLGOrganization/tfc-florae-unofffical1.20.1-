"""Give the rotation machines their own block entities instead of extending TFC's.

The reflection attempt did not take effect at runtime, so build it properly: register
our own BlockEntityTypes (reusing TFC's block entity classes) and construct the seven
rotation blocks with those types, the same way TFC constructs its own.
"""
import io
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
BE = os.path.join(ROOT, "src", "main", "java", "tfcflorae", "common", "blockentities", "TFCFBlockEntities.java")
BLOCKS = os.path.join(ROOT, "src", "main", "java", "tfcflorae", "common", "blocks", "TFCFBlocks.java")

# ---------- 1) block entity registrations ----------------------------------
s = io.open(BE, encoding="utf-8").read()
anchor = 'public static final RegistryObject<BlockEntityType<LoomBlockEntity>> LOOM = register('
assert anchor in s, "LOOM anchor"
extra_be = '''// TFC's own rotation block entity types only list its 20 woods; a block entity is
   // only created for a block in that list, so machines built from this mod's woods
   // had none and rendered as nothing.  These mirror TFC's types for our blocks.
   public static final RegistryObject<BlockEntityType<net.dries007.tfc.common.blockentities.rotation.WaterWheelBlockEntity>> WATER_WHEEL = register(
      "tfcf_water_wheel", net.dries007.tfc.common.blockentities.rotation.WaterWheelBlockEntity::new,
      TFCFBlocks.WOODS.values().stream().map(map -> map.get(BlockType.WATER_WHEEL))
   );
   public static final RegistryObject<BlockEntityType<net.dries007.tfc.common.blockentities.rotation.AxleBlockEntity>> AXLE = register(
      "tfcf_axle", net.dries007.tfc.common.blockentities.rotation.AxleBlockEntity::new,
      TFCFBlocks.WOODS.values().stream().map(map -> map.get(BlockType.AXLE))
   );
   public static final RegistryObject<BlockEntityType<net.dries007.tfc.common.blockentities.rotation.BladedAxleBlockEntity>> BLADED_AXLE = register(
      "tfcf_bladed_axle", net.dries007.tfc.common.blockentities.rotation.BladedAxleBlockEntity::new,
      TFCFBlocks.WOODS.values().stream().map(map -> map.get(BlockType.BLADED_AXLE))
   );
   public static final RegistryObject<BlockEntityType<net.dries007.tfc.common.blockentities.rotation.EncasedAxleBlockEntity>> ENCASED_AXLE = register(
      "tfcf_encased_axle", net.dries007.tfc.common.blockentities.rotation.EncasedAxleBlockEntity::new,
      TFCFBlocks.WOODS.values().stream().map(map -> map.get(BlockType.ENCASED_AXLE))
   );
   public static final RegistryObject<BlockEntityType<net.dries007.tfc.common.blockentities.rotation.ClutchBlockEntity>> CLUTCH = register(
      "tfcf_clutch", net.dries007.tfc.common.blockentities.rotation.ClutchBlockEntity::new,
      TFCFBlocks.WOODS.values().stream().map(map -> map.get(BlockType.CLUTCH))
   );
   public static final RegistryObject<BlockEntityType<net.dries007.tfc.common.blockentities.rotation.GearBoxBlockEntity>> GEAR_BOX = register(
      "tfcf_gear_box", net.dries007.tfc.common.blockentities.rotation.GearBoxBlockEntity::new,
      TFCFBlocks.WOODS.values().stream().map(map -> map.get(BlockType.GEAR_BOX))
   );
   public static final RegistryObject<BlockEntityType<net.dries007.tfc.common.blockentities.rotation.WindmillBlockEntity>> WINDMILL = register(
      "tfcf_windmill", net.dries007.tfc.common.blockentities.rotation.WindmillBlockEntity::new,
      TFCFBlocks.WOODS.values().stream().map(map -> map.get(BlockType.WINDMILL))
   );
   ''' + anchor
s = s.replace(anchor, extra_be, 1)
io.open(BE, "w", encoding="utf-8", newline="\n").write(s)
print("block entities added")

# ---------- 2) construct rotation blocks with our block entities ------------
b = io.open(BLOCKS, encoding="utf-8").read()
loop = "            for (BlockType type : BlockType.values()) {\n               if (wood == TFCFWood.BAMBOO) {"
assert loop in b, "wood mapper loop anchor"
hook = '''            for (BlockType type : BlockType.values()) {
               // rotation machines need a block entity that lists our blocks; TFC's own
               // types only know its 20 woods, so build these blocks ourselves
               if (isRotationType(type)) {
                  subMap.put(type, register(
                     ("wood/" + type.name() + "/" + wood.getSerializedName()).toLowerCase(Locale.ROOT),
                     () -> createRotationBlock(type, wood),
                     TFCCreativeTabs.WOOD
                  ));
                  continue;
               }
               if (wood == TFCFWood.BAMBOO) {'''
b = b.replace(loop, hook, 1)

# helper methods, inserted before woodMapper
helper_anchor = "   private static Map<TFCFWood, Map<BlockType, RegistryObject<Block>>> woodMapper(Class<TFCFWood> enumClass) {"
assert helper_anchor in b, "woodMapper anchor"
helpers = '''   /** Types whose rendering depends on a block entity (invisible in world otherwise). */
   private static boolean isRotationType(BlockType type) {
      return type == BlockType.WATER_WHEEL || type == BlockType.AXLE || type == BlockType.BLADED_AXLE
         || type == BlockType.ENCASED_AXLE || type == BlockType.CLUTCH || type == BlockType.GEAR_BOX
         || type == BlockType.WINDMILL;
   }

   private static ExtendedProperties rotationProperties(TFCFWood wood) {
      return ExtendedProperties.of(wood.woodColor())
         .sound(SoundType.WOOD)
         .instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASS);
   }

   private static Block createRotationBlock(BlockType type, TFCFWood wood) {
      switch (type) {
         case WATER_WHEEL:
            return new net.dries007.tfc.common.blocks.rotation.WaterWheelBlock(
               rotationProperties(wood).strength(9F).noOcclusion()
                  .blockEntity(TFCFBlockEntities.WATER_WHEEL)
                  .ticks(net.dries007.tfc.common.blockentities.rotation.WaterWheelBlockEntity::serverTick,
                         net.dries007.tfc.common.blockentities.rotation.WaterWheelBlockEntity::clientTick),
               () -> (net.dries007.tfc.common.blocks.rotation.AxleBlock) WOODS.get(wood).get(BlockType.AXLE).get(),
               wood.getSerializedName());
         case AXLE:
            return new net.dries007.tfc.common.blocks.rotation.AxleBlock(
               rotationProperties(wood).noOcclusion().strength(2.5F).flammableLikeLogs()
                  .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
                  .blockEntity(TFCFBlockEntities.AXLE),
               () -> (net.dries007.tfc.common.blocks.rotation.WindmillBlock) WOODS.get(wood).get(BlockType.WINDMILL).get(),
               new net.minecraft.resources.ResourceLocation("tfcflorae", "block/wood/planks/" + wood.getSerializedName()));
         case BLADED_AXLE:
            return new net.dries007.tfc.common.blocks.rotation.BladedAxleBlock(
               rotationProperties(wood).noOcclusion().strength(2.5F).flammableLikeLogs()
                  .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
                  .blockEntity(TFCFBlockEntities.BLADED_AXLE),
               () -> (net.dries007.tfc.common.blocks.rotation.AxleBlock) WOODS.get(wood).get(BlockType.AXLE).get());
         case ENCASED_AXLE:
            return new net.dries007.tfc.common.blocks.rotation.EncasedAxleBlock(
               rotationProperties(wood).strength(2.5F).flammableLikeLogs()
                  .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
                  .blockEntity(TFCFBlockEntities.ENCASED_AXLE));
         case CLUTCH:
            return new net.dries007.tfc.common.blocks.rotation.ClutchBlock(
               rotationProperties(wood).strength(2.5F).flammableLikeLogs()
                  .pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)
                  .blockEntity(TFCFBlockEntities.CLUTCH),
               () -> (net.dries007.tfc.common.blocks.rotation.AxleBlock) WOODS.get(wood).get(BlockType.AXLE).get());
         case GEAR_BOX:
            return new net.dries007.tfc.common.blocks.rotation.GearBoxBlock(
               rotationProperties(wood).strength(2F).noOcclusion()
                  .blockEntity(TFCFBlockEntities.GEAR_BOX),
               () -> (net.dries007.tfc.common.blocks.rotation.AxleBlock) WOODS.get(wood).get(BlockType.AXLE).get());
         case WINDMILL:
            return new net.dries007.tfc.common.blocks.rotation.WindmillBlock(
               rotationProperties(wood).strength(9F).noOcclusion()
                  .blockEntity(TFCFBlockEntities.WINDMILL)
                  .ticks(net.dries007.tfc.common.blockentities.rotation.WindmillBlockEntity::serverTick,
                         net.dries007.tfc.common.blockentities.rotation.WindmillBlockEntity::clientTick),
               () -> (net.dries007.tfc.common.blocks.rotation.AxleBlock) WOODS.get(wood).get(BlockType.AXLE).get());
         default:
            return type.create(wood);
      }
   }

''' + helper_anchor
b = b.replace(helper_anchor, helpers, 1)
io.open(BLOCKS, "w", encoding="utf-8", newline="\n").write(b)
print("wood mapper patched")
