package tfcflorae.common.blockentities;
import net.minecraft.core.registries.Registries;

import java.util.function.Supplier;
import java.util.stream.Stream;
import net.dries007.tfc.common.blockentities.FarmlandBlockEntity;
import net.dries007.tfc.common.blockentities.LoomBlockEntity;
import net.dries007.tfc.common.blockentities.SluiceBlockEntity;
import net.dries007.tfc.common.blocks.wood.Wood.BlockType;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.registry.RegistrationHelpers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import tfcflorae.common.blocks.TFCFBlocks;
import tfcflorae.common.blocks.soil.TFCFSoil;

public class TFCFBlockEntities {
   public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, "tfcflorae");
   public static final RegistryObject<BlockEntityType<SandPileBlockEntity>> SAND_PILE = register(
      "sand_pile", SandPileBlockEntity::new, blockStreams(TFCFBlocks.SAND_LAYERS)
   );
   /**
    * 果树树叶的方块实体类型。
    * <p>
    * 必须是我们自己的注册项，不能借 TFC 的 {@code tfc:berry_bush}：{@code BlockEntityType} 的
    * {@code validBlocks} 是固定的，而 {@code LevelChunk.BoundTickingBlockEntity.tick()} 会先判
    * {@code type.isValid(state)}，不通过就直接跳过 ticker —— 借来的类型永远不认我们的树叶，
    * {@code serverTick} 不执行，{@code /addday} 对果树失效。详见 {@link TFCFBerryBushBlockEntity}。
    */
   public static final RegistryObject<BlockEntityType<TFCFBerryBushBlockEntity>> BERRY_BUSH = register(
      "tfcf_berry_bush", TFCFBerryBushBlockEntity::new, blockStreams(TFCFBlocks.FRUIT_TREE_LEAVES.values())
   );
   public static final RegistryObject<BlockEntityType<TFCFTickCounterBlockEntity>> TICK_COUNTER = register(
      "tfcf_tick_counter",
      TFCFTickCounterBlockEntity::new,
      blockStreams(
            TFCFBlocks.WOODS.values().stream().map(map -> (RegistryObject)map.get(BlockType.SAPLING)),
            TFCFBlocks.TFCFSOIL.get(TFCFSoil.DRYING_BRICKS).values(),
            TFCFBlocks.BAMBOO_SAPLINGS.values().stream()
         )
   );
   public static final RegistryObject<BlockEntityType<ChiseledBookshelfBlockEntity>> CHISELED_BOOKSHELF = register(
      "chiseled_bookshelf", ChiseledBookshelfBlockEntity::new, blockStreams(TFCFBlocks.CHISELED_BOOKSHELF_TFCF.values().stream())

   );
   public static final RegistryObject<BlockEntityType<WebbedChestBlockEntity>> WEBBED_CHEST = register(
      "webbed_chest", WebbedChestBlockEntity::new, blockStreams(TFCFBlocks.WEBBED_CHEST)

   );
   public static final RegistryObject<BlockEntityType<SilkmothNestBlockEntity>> SILKMOTH_NEST = register(
      "silkmoth_nest", SilkmothNestBlockEntity::new, blockStreams(TFCFBlocks.SILKMOTH_NEST)

   );
   public static final RegistryObject<BlockEntityType<FarmlandBlockEntity>> FARMLAND = register(
      "tfcf_farmland", TFCFFarmlandBlockEntity::new, TFCFBlocks.TFCFSOIL.get(TFCFSoil.FARMLAND).values().stream()
   );
   public static final RegistryObject<BlockEntityType<SluiceBlockEntity>> SLUICE = register(
      "tfcf_sluice", TFCFSluiceBlockEntity::new, TFCFBlocks.WOODS.values().stream().map(map -> map.get(BlockType.SLUICE))
   );
   public static final RegistryObject<BlockEntityType<TFCFSignBlockEntity>> SIGN = register(
      "tfcf_sign", TFCFSignBlockEntity::new, TFCFBlocks.WOODS.values().stream().flatMap(map -> blockStreams(BlockType.SIGN, BlockType.WALL_SIGN).map(map::get))
   );
   public static final RegistryObject<BlockEntityType<LecternBlockEntity>> LECTERN = register(
      "tfcf_lectern", TFCFLecternBlockEntity::new, TFCFBlocks.WOODS.values().stream().map(map -> map.get(BlockType.LECTERN))
   );
   public static final RegistryObject<BlockEntityType<TFCFToolRackBlockEntity>> TOOL_RACK = register(
      "tfcf_tool_rack", TFCFToolRackBlockEntity::new, TFCFBlocks.WOODS.values().stream().map(map -> map.get(BlockType.TOOL_RACK))
   );
   public static final RegistryObject<BlockEntityType<MineralSheetBlockEntity>> MINERAL_SHEET = register(
      "mineral_sheet", MineralSheetBlockEntity::new, TFCFBlocks.MINERAL_SHEET
   );

   public TFCFBlockEntities() {
   }

   private static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> register(
      String name, BlockEntityType.BlockEntitySupplier<T> factory, Supplier<? extends Block> block
   ) {
      return RegistrationHelpers.register(BLOCK_ENTITIES, name, factory, block);
   }

   private static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> register(
      String name, BlockEntityType.BlockEntitySupplier<T> factory, Stream<? extends Supplier<? extends Block>> blocks
   ) {
      return RegistrationHelpers.register(BLOCK_ENTITIES, name, factory, blocks);
   }

   /**
    * Flattens an arbitrary mix of {@link Stream}s, {@link java.util.Collection}s and single
    * {@link Supplier}s of blocks into one stream, for {@link BlockEntityType} registration.
    * (1.18.2 relied on TFC's {@code Helpers.flatten}, which TFC 3.x removed.)
    */
    private static Stream<? extends Supplier<? extends Block>> blockStreams(Object... parts) {
      final java.util.List<Supplier<? extends Block>> out = new java.util.ArrayList<>();
      for (Object part : parts) {
         flattenInto(part, out);
      }
      return out.stream();
   }

   private static void flattenInto(Object part, java.util.List<Supplier<? extends Block>> out) {
      if (part instanceof Stream<?> stream) {
         stream.forEach(child -> flattenInto(child, out));
      } else if (part instanceof java.util.Collection<?> collection) {
         collection.forEach(child -> flattenInto(child, out));
      } else if (part instanceof Supplier<?> supplier) {
         out.add((Supplier<? extends Block>) supplier);
      }
   }
}
