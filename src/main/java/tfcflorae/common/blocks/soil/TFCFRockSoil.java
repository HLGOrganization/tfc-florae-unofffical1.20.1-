package tfcflorae.common.blocks.soil;

import java.util.Map;
import java.util.function.Supplier;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.rock.Rock.BlockType;
import net.dries007.tfc.common.blocks.soil.SoilBlockType;
import net.dries007.tfc.common.blocks.soil.SoilBlockType.Variant;
import net.dries007.tfc.util.registry.RegistryRock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;
import tfcflorae.common.blocks.TFCFBlocks;
import tfcflorae.common.blocks.rock.TFCFRock;
import tfcflorae.common.items.TFCFItems;
import tfcflorae.util.TriFunction;
import tfcflorae.util.registry.TFCFRegistryRock;

public enum TFCFRockSoil {
   ROCKIER_COMPACT_DIRT(
      (self, variant, rock) -> new RockySoilBlock(
            Properties.of().mapColor(MapColor.DIRT).strength(0.9F).sound(SoundType.TUFF).requiresCorrectToolForDrops(),
            (Supplier<? extends Block>)((Map)TFCBlocks.ROCK_BLOCKS.get(rock)).get(BlockType.LOOSE),
            (Supplier<? extends Block>)TFCFBlocks.TFCFROCKSOIL.get(self.transform()).get(variant).get(rock),
            null,
            null,
            false
         ),
      (self, variant, rock) -> new RockySoilBlock(
            Properties.of().mapColor(MapColor.DIRT).strength(0.9F).sound(SoundType.TUFF).requiresCorrectToolForDrops(),
            (Supplier<? extends Block>)((Map)TFCBlocks.ROCK_BLOCKS.get(rock)).get(BlockType.LOOSE),
            (Supplier<? extends Block>)TFCFBlocks.TFCROCKSOIL.get(self.transform()).get(variant).get(rock),
            null,
            null,
            false
         ),
      (self, variant, rock) -> new RockySoilBlock(
            Properties.of().mapColor(MapColor.DIRT).strength(0.9F).sound(SoundType.TUFF).requiresCorrectToolForDrops(),
            (Supplier<? extends Block>)TFCFBlocks.TFCF_ROCK_BLOCKS.get(rock).get(BlockType.LOOSE),
            (Supplier<? extends Block>)TFCFBlocks.TFCFROCKSOIL2.get(self.transform()).get(variant).get(rock),
            null,
            null,
            false
         ),
      (self, variant, rock) -> new RockySoilBlock(
            Properties.of().mapColor(MapColor.DIRT).strength(0.9F).sound(SoundType.TUFF).requiresCorrectToolForDrops(),
            (Supplier<? extends Block>)TFCFBlocks.TFCF_ROCK_BLOCKS.get(rock).get(BlockType.LOOSE),
            (Supplier<? extends Block>)TFCFBlocks.TFCROCKSOIL2.get(self.transform()).get(variant).get(rock),
            null,
            null,
            false
         )
   ),
   DIRTIEST_STONE_TILES(
      (self, variant, rock) -> new TFCFRockySoilBlock(
            Properties.of().mapColor(MapColor.DIRT).strength(1.25F, 8.0F).sound(SoundType.TUFF).requiresCorrectToolForDrops(),
            (Supplier<?>)TFCFItems.SOIL_PILE_TFCF.get(variant),
            (Supplier<? extends Block>)TFCFBlocks.TFCFSOIL.get(TFCFSoil.DIRT).get(variant),
            null,
            (Supplier<? extends Block>)TFCFBlocks.ROCK_BLOCKS.get(rock).get(TFCFRock.TFCFBlockType.STONE_TILES),
            true
         ),
      (self, variant, rock) -> new TFCFRockySoilBlock(
            Properties.of().mapColor(MapColor.DIRT).strength(1.25F, 8.0F).sound(SoundType.TUFF).requiresCorrectToolForDrops(),
            (Supplier<?>)TFCFItems.SOIL_PILE_TFC.get(variant),
            (Supplier<? extends Block>)((Map)TFCBlocks.SOIL.get(SoilBlockType.DIRT)).get(variant),
            null,
            (Supplier<? extends Block>)TFCFBlocks.ROCK_BLOCKS.get(rock).get(TFCFRock.TFCFBlockType.STONE_TILES),
            true
         ),
      (self, variant, rock) -> new TFCFRockySoilBlock(
            Properties.of().mapColor(MapColor.DIRT).strength(1.25F, 8.0F).sound(SoundType.TUFF).requiresCorrectToolForDrops(),
            (Supplier<?>)TFCFItems.SOIL_PILE_TFCF.get(variant),
            (Supplier<? extends Block>)TFCFBlocks.TFCFSOIL.get(TFCFSoil.DIRT).get(variant),
            null,
            (Supplier<? extends Block>)TFCFBlocks.TFCF_ROCKTYPE_BLOCKS.get(rock).get(TFCFRock.TFCFBlockType.STONE_TILES),
            true
         ),
      (self, variant, rock) -> new TFCFRockySoilBlock(
            Properties.of().mapColor(MapColor.DIRT).strength(1.25F, 8.0F).sound(SoundType.TUFF).requiresCorrectToolForDrops(),
            (Supplier<?>)TFCFItems.SOIL_PILE_TFC.get(variant),
            (Supplier<? extends Block>)((Map)TFCBlocks.SOIL.get(SoilBlockType.DIRT)).get(variant),
            null,
            (Supplier<? extends Block>)TFCFBlocks.TFCF_ROCKTYPE_BLOCKS.get(rock).get(TFCFRock.TFCFBlockType.STONE_TILES),
            true
         )
   ),
   DIRTIER_STONE_TILES(
      (self, variant, rock) -> new TFCFRockySoilBlock(
            Properties.of().mapColor(MapColor.DIRT).strength(1.35F, 9.0F).sound(SoundType.TUFF).requiresCorrectToolForDrops(),
            (Supplier<?>)TFCFItems.SOIL_PILE_TFCF.get(variant),
            (Supplier<? extends Block>)TFCFBlocks.TFCFROCKSOIL.get(self.transform()).get(variant).get(rock),
            null,
            null,
            false
         ),
      (self, variant, rock) -> new TFCFRockySoilBlock(
            Properties.of().mapColor(MapColor.DIRT).strength(1.35F, 9.0F).sound(SoundType.TUFF).requiresCorrectToolForDrops(),
            (Supplier<?>)TFCFItems.SOIL_PILE_TFC.get(variant),
            (Supplier<? extends Block>)TFCFBlocks.TFCROCKSOIL.get(self.transform()).get(variant).get(rock),
            null,
            null,
            false
         ),
      (self, variant, rock) -> new TFCFRockySoilBlock(
            Properties.of().mapColor(MapColor.DIRT).strength(1.35F, 9.0F).sound(SoundType.TUFF).requiresCorrectToolForDrops(),
            (Supplier<?>)TFCFItems.SOIL_PILE_TFCF.get(variant),
            (Supplier<? extends Block>)TFCFBlocks.TFCFROCKSOIL2.get(self.transform()).get(variant).get(rock),
            null,
            null,
            false
         ),
      (self, variant, rock) -> new TFCFRockySoilBlock(
            Properties.of().mapColor(MapColor.DIRT).strength(1.35F, 9.0F).sound(SoundType.TUFF).requiresCorrectToolForDrops(),
            (Supplier<?>)TFCFItems.SOIL_PILE_TFC.get(variant),
            (Supplier<? extends Block>)TFCFBlocks.TFCROCKSOIL2.get(self.transform()).get(variant).get(rock),
            null,
            null,
            false
         )
   ),
   DIRTY_STONE_TILES(
      (self, variant, rock) -> new TFCFRockySoilBlock(
            Properties.of().mapColor(MapColor.DIRT).strength(1.5F, 10.0F).sound(SoundType.TUFF).requiresCorrectToolForDrops(),
            (Supplier<?>)TFCFItems.SOIL_PILE_TFCF.get(variant),
            (Supplier<? extends Block>)TFCFBlocks.TFCFROCKSOIL.get(self.transform()).get(variant).get(rock),
            null,
            null,
            false
         ),
      (self, variant, rock) -> new TFCFRockySoilBlock(
            Properties.of().mapColor(MapColor.DIRT).strength(1.5F, 10.0F).sound(SoundType.TUFF).requiresCorrectToolForDrops(),
            (Supplier<?>)TFCFItems.SOIL_PILE_TFC.get(variant),
            (Supplier<? extends Block>)TFCFBlocks.TFCROCKSOIL.get(self.transform()).get(variant).get(rock),
            null,
            null,
            false
         ),
      (self, variant, rock) -> new TFCFRockySoilBlock(
            Properties.of().mapColor(MapColor.DIRT).strength(1.5F, 10.0F).sound(SoundType.TUFF).requiresCorrectToolForDrops(),
            (Supplier<?>)TFCFItems.SOIL_PILE_TFCF.get(variant),
            (Supplier<? extends Block>)TFCFBlocks.TFCFROCKSOIL2.get(self.transform()).get(variant).get(rock),
            null,
            null,
            false
         ),
      (self, variant, rock) -> new TFCFRockySoilBlock(
            Properties.of().mapColor(MapColor.DIRT).strength(1.5F, 10.0F).sound(SoundType.TUFF).requiresCorrectToolForDrops(),
            (Supplier<?>)TFCFItems.SOIL_PILE_TFC.get(variant),
            (Supplier<? extends Block>)TFCFBlocks.TFCROCKSOIL2.get(self.transform()).get(variant).get(rock),
            null,
            null,
            false
         )
   );

   public static final TFCFRockSoil[] VALUES = values();
   private final TriFunction<TFCFRockSoil, TFCFSoil.TFCFVariant, RegistryRock, Block> TFCFFactory;
   private final TriFunction<TFCFRockSoil, Variant, RegistryRock, Block> TFCFactory;
   private final TriFunction<TFCFRockSoil, TFCFSoil.TFCFVariant, TFCFRegistryRock, Block> TFCFFactory2;
   private final TriFunction<TFCFRockSoil, Variant, TFCFRegistryRock, Block> TFCFactory2;

   public static TFCFRockSoil valueOf(int i) {
      return i >= 0 && i < VALUES.length ? VALUES[i] : ROCKIER_COMPACT_DIRT;
   }

   private TFCFRockSoil(
      TriFunction<TFCFRockSoil, TFCFSoil.TFCFVariant, RegistryRock, Block> TFCFFactory,
      TriFunction<TFCFRockSoil, Variant, RegistryRock, Block> TFCFactory,
      TriFunction<TFCFRockSoil, TFCFSoil.TFCFVariant, TFCFRegistryRock, Block> TFCFFactory2,
      TriFunction<TFCFRockSoil, Variant, TFCFRegistryRock, Block> TFCFactory2
   ) {
      this.TFCFFactory = TFCFFactory;
      this.TFCFactory = TFCFactory;
      this.TFCFFactory2 = TFCFFactory2;
      this.TFCFactory2 = TFCFactory2;
   }

   private TFCFRockSoil(
      TriFunction<TFCFRockSoil, TFCFSoil.TFCFVariant, RegistryRock, Block> TFCFFactory,
      TriFunction<TFCFRockSoil, TFCFSoil.TFCFVariant, TFCFRegistryRock, Block> TFCFFactory2
   ) {
      this.TFCFFactory = TFCFFactory;
      this.TFCFactory = null;
      this.TFCFFactory2 = TFCFFactory2;
      this.TFCFactory2 = null;
   }

   private TFCFRockSoil(TriFunction<TFCFRockSoil, TFCFSoil.TFCFVariant, RegistryRock, Block> TFCFFactory) {
      this.TFCFFactory = TFCFFactory;
      this.TFCFactory = null;
      this.TFCFFactory2 = null;
      this.TFCFactory2 = null;
   }

   public Block TFCFCreate(TFCFSoil.TFCFVariant variant, RegistryRock rock) {
      return this.TFCFFactory.apply(this, variant, rock);
   }

   public Block TFCCreate(Variant variant, RegistryRock rock) {
      return this.TFCFactory.apply(this, variant, rock);
   }

   public Block TFCFCreate2(TFCFSoil.TFCFVariant variant, TFCFRegistryRock rock) {
      return this.TFCFFactory2.apply(this, variant, rock);
   }

   public Block TFCCreate2(Variant variant, TFCFRegistryRock rock) {
      return this.TFCFactory2.apply(this, variant, rock);
   }

   public TriFunction<TFCFRockSoil, Variant, RegistryRock, Block> getTFCFactory() {
      return this.TFCFactory;
   }

   public TriFunction<TFCFRockSoil, TFCFSoil.TFCFVariant, RegistryRock, Block> getTFCFFactory() {
      return this.TFCFFactory;
   }

   public TriFunction<TFCFRockSoil, Variant, TFCFRegistryRock, Block> getTFCFactory2() {
      return this.TFCFactory2;
   }

   public TriFunction<TFCFRockSoil, TFCFSoil.TFCFVariant, TFCFRegistryRock, Block> getTFCFFactory2() {
      return this.TFCFFactory2;
   }

   public TFCFRockSoil transform() {
      switch (this) {
         case DIRTY_STONE_TILES:
            return DIRTIER_STONE_TILES;
         case DIRTIER_STONE_TILES:
            return DIRTIEST_STONE_TILES;
         case DIRTIEST_STONE_TILES:
            return ROCKIER_COMPACT_DIRT;
         default:
            return this;
      }
}
}
