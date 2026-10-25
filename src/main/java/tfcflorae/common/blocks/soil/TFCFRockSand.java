package tfcflorae.common.blocks.soil;

import java.util.Map;
import java.util.function.Supplier;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.rock.Rock.BlockType;
import net.dries007.tfc.util.registry.RegistryRock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import tfcflorae.common.blocks.TFCFBlocks;
import tfcflorae.common.blocks.rock.TFCFRock;
import tfcflorae.util.TriFunction;
import tfcflorae.util.registry.TFCFRegistryRock;

public enum TFCFRockSand {
   ROCKY(
      (self, sandColor, rock) -> new RockySoilBlock(
            Properties.of().mapColor(sandColor.getMaterialColor()).strength(0.85F).sound(SoundType.GRAVEL).requiresCorrectToolForDrops(),
            (Supplier<? extends Block>)((Map)TFCBlocks.ROCK_BLOCKS.get(rock)).get(BlockType.LOOSE),
            (Supplier<? extends Block>)TFCFBlocks.ROCKY_SAND_TFC.get(self.transform()).get(sandColor).get(rock),
            null,
            null,
            false
         ),
      (self, sandColor, rock) -> new RockySoilBlock(
            Properties.of().mapColor(sandColor.getMaterialColor()).strength(0.85F).sound(SoundType.GRAVEL).requiresCorrectToolForDrops(),
            (Supplier<? extends Block>)TFCFBlocks.TFCF_ROCK_BLOCKS.get(rock).get(BlockType.LOOSE),
            (Supplier<? extends Block>)TFCFBlocks.ROCKY_SAND_TFCF.get(self.transform()).get(sandColor).get(rock),
            null,
            null,
            false
         )
   ),
   SANDIEST_TILES(
      (self, sandColor, rock) -> new TFCFRockySoilBlock(
            Properties.of().mapColor(sandColor.getMaterialColor()).strength(1.25F, 8.0F).sound(SoundType.TUFF).requiresCorrectToolForDrops(),
            (Supplier<?>)TFCFBlocks.SAND_LAYERS.get(sandColor),
            (Supplier<? extends Block>)TFCBlocks.SAND.get(sandColor.toSandTFC(true)),
            null,
            (Supplier<? extends Block>)TFCFBlocks.ROCK_BLOCKS.get(rock).get(TFCFRock.TFCFBlockType.STONE_TILES),
            true
         ),
      (self, sandColor, rock) -> new TFCFRockySoilBlock(
            Properties.of().mapColor(sandColor.getMaterialColor()).strength(1.25F, 8.0F).sound(SoundType.TUFF).requiresCorrectToolForDrops(),
            (Supplier<?>)TFCFBlocks.SAND_LAYERS.get(sandColor),
            (Supplier<? extends Block>)TFCBlocks.SAND.get(sandColor.toSandTFC(true)),
            null,
            (Supplier<? extends Block>)TFCFBlocks.TFCF_ROCKTYPE_BLOCKS.get(rock).get(TFCFRock.TFCFBlockType.STONE_TILES),
            true
         )
   ),
   SANDIER_TILES(
      (self, sandColor, rock) -> new TFCFRockySoilBlock(
            Properties.of().mapColor(sandColor.getMaterialColor()).strength(1.35F, 9.0F).sound(SoundType.TUFF).requiresCorrectToolForDrops(),
            (Supplier<?>)TFCFBlocks.SAND_LAYERS.get(sandColor),
            (Supplier<? extends Block>)TFCFBlocks.ROCKY_SAND_TFC.get(self.transform()).get(sandColor).get(rock),
            null,
            null,
            false
         ),
      (self, sandColor, rock) -> new TFCFRockySoilBlock(
            Properties.of().mapColor(sandColor.getMaterialColor()).strength(1.35F, 9.0F).sound(SoundType.TUFF).requiresCorrectToolForDrops(),
            (Supplier<?>)TFCFBlocks.SAND_LAYERS.get(sandColor),
            (Supplier<? extends Block>)TFCFBlocks.ROCKY_SAND_TFCF.get(self.transform()).get(sandColor).get(rock),
            null,
            null,
            false
         )
   ),
   SANDY_TILES(
      (self, sandColor, rock) -> new TFCFRockySoilBlock(
            Properties.of().mapColor(sandColor.getMaterialColor()).strength(1.5F, 10.0F).sound(SoundType.TUFF).requiresCorrectToolForDrops(),
            (Supplier<?>)TFCFBlocks.SAND_LAYERS.get(sandColor),
            (Supplier<? extends Block>)TFCFBlocks.ROCKY_SAND_TFC.get(self.transform()).get(sandColor).get(rock),
            null,
            null,
            false
         ),
      (self, sandColor, rock) -> new TFCFRockySoilBlock(
            Properties.of().mapColor(sandColor.getMaterialColor()).strength(1.5F, 10.0F).sound(SoundType.TUFF).requiresCorrectToolForDrops(),
            (Supplier<?>)TFCFBlocks.SAND_LAYERS.get(sandColor),
            (Supplier<? extends Block>)TFCFBlocks.ROCKY_SAND_TFCF.get(self.transform()).get(sandColor).get(rock),
            null,
            null,
            false
         )
   );

   public static final TFCFRockSand[] VALUES = values();
   private final TriFunction<TFCFRockSand, Colors, RegistryRock, Block> TFCFactory;
   private final TriFunction<TFCFRockSand, Colors, TFCFRegistryRock, Block> TFCFFactory;

   public static TFCFRockSand valueOf(int i) {
      return i >= 0 && i < VALUES.length ? VALUES[i] : ROCKY;
   }

   private TFCFRockSand(
      TriFunction<TFCFRockSand, Colors, RegistryRock, Block> TFCFactory, TriFunction<TFCFRockSand, Colors, TFCFRegistryRock, Block> TFCFFactory
   ) {
      this.TFCFactory = TFCFactory;
      this.TFCFFactory = TFCFFactory;
   }

   private TFCFRockSand(TriFunction<TFCFRockSand, Colors, RegistryRock, Block> TFCFactory) {
      this.TFCFactory = null;
      this.TFCFFactory = null;
   }

   public Block TFCCreate(Colors sandColor, RegistryRock rock) {
      return this.TFCFactory.apply(this, sandColor, rock);
   }

   public Block TFCFCreate(Colors sandColor, TFCFRegistryRock rock) {
      return this.TFCFFactory.apply(this, sandColor, rock);
   }

   public TriFunction<TFCFRockSand, Colors, RegistryRock, Block> getTFCFactory() {
      return this.TFCFactory;
   }

   public TriFunction<TFCFRockSand, Colors, TFCFRegistryRock, Block> getTFCFFactory() {
      return this.TFCFFactory;
   }

   public TFCFRockSand transform() {
      switch (this) {
         case SANDY_TILES:
            return SANDIER_TILES;
         case SANDIER_TILES:
            return SANDIEST_TILES;
         case SANDIEST_TILES:
            return ROCKY;
         case ROCKY:
            return ROCKY;
         default:
            return this;
      }
   }
}
