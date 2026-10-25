package tfcflorae.common.blocks.rock;
import net.minecraft.world.level.material.MapColor;

import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import net.dries007.tfc.common.blocks.rock.MossGrowingBlock;
import net.dries007.tfc.common.blocks.rock.MossSpreadingBlock;
import net.dries007.tfc.common.blocks.rock.RockCategory;
import net.dries007.tfc.common.blocks.rock.Rock.BlockType;
import net.dries007.tfc.common.blocks.soil.SandBlockType;
import net.dries007.tfc.util.registry.RegistryRock;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import org.jetbrains.annotations.Nullable;
import tfcflorae.common.blocks.TFCFBlocks;

public enum DripstoneRock implements RegistryRock {
   DRIPSTONE(RockCategory.SEDIMENTARY, SandBlockType.YELLOW),
   GYPSUM(RockCategory.SEDIMENTARY, SandBlockType.WHITE);

   public static final DripstoneRock[] VALUES = values();
   private final String serializedName = this.name().toLowerCase(Locale.ROOT);
   private final RockCategory category;
   private final SandBlockType sandType;

   private DripstoneRock(RockCategory category, SandBlockType sandType) {
      this.category = category;
      this.sandType = sandType;
   }

   public SandBlockType getSandType() {
      return this.sandType;
   }

   public RockCategory category() {
      return this.category;
   }

   public Supplier<? extends Block> getBlock(BlockType type) {
      return (Supplier<? extends Block>)TFCFBlocks.DRIPSTONE_BLOCKS.get(this).get(type);
   }

   public Supplier<? extends Block> getBlock(DripstoneRock.TFCFBlockType type) {
      return (Supplier<? extends Block>)TFCFBlocks.DRIPSTONE_BLOCKS.get(this).get(type);
   }

   public Supplier<? extends Block> getAnvil() {
      return null;
   }

   public Supplier<? extends SlabBlock> getSlab(BlockType type) {
      return null;
   }

   public Supplier<? extends StairBlock> getStair(BlockType type) {
      return null;
   }

   public Supplier<? extends WallBlock> getWall(BlockType type) {
      return null;
   }

   public String getSerializedName() {
      return this.serializedName;
   }

   public static enum TFCFBlockType implements StringRepresentable {
      STONE_TILES(
         (rock, self) -> new Block(
               Properties.of().mapColor(MapColor.STONE).sound(SoundType.DEEPSLATE).strength(rock.category().hardness(6.5F), 10.0F).requiresCorrectToolForDrops()
            ),
         true
      ),
      COBBLED_BRICKS(
         (rock, self) -> new MossGrowingRotatedPillarBlock(
               Properties.of().mapColor(MapColor.STONE).sound(SoundType.DEEPSLATE_BRICKS).strength(rock.category().hardness(9.0F), 10.0F).requiresCorrectToolForDrops(),
               (Supplier<? extends Block>)TFCFBlocks.ROCK_BLOCKS.get(rock).get(self.mossy())
            ),
         true
      ),
      FLAGSTONE_BRICKS(
         (rock, self) -> new MossGrowingBlock(
               Properties.of().mapColor(MapColor.STONE).sound(SoundType.DEEPSLATE_TILES).strength(rock.category().hardness(9.0F), 10.0F).requiresCorrectToolForDrops(),
               (Supplier)TFCFBlocks.ROCK_BLOCKS.get(rock).get(self.mossy())
            ),
         true
      ),
      MOSSY_COBBLED_BRICKS(
         (rock, self) -> new MossSpreadingRotatedPillarBlock(
               Properties.of().mapColor(MapColor.STONE).sound(SoundType.DEEPSLATE_BRICKS).strength(rock.category().hardness(9.0F), 10.0F).requiresCorrectToolForDrops()
            ),
         true
      ),
      MOSSY_FLAGSTONE_BRICKS(
         (rock, self) -> new MossSpreadingBlock(
               Properties.of().mapColor(MapColor.STONE).sound(SoundType.DEEPSLATE_TILES).strength(rock.category().hardness(9.0F), 10.0F).requiresCorrectToolForDrops()
            ),
         true
      ),
      CRACKED_FLAGSTONE_BRICKS(
         (rock, self) -> new Block(
               Properties.of().mapColor(MapColor.STONE).sound(SoundType.DEEPSLATE_TILES).strength(rock.category().hardness(9.0F), 10.0F).requiresCorrectToolForDrops()
            ),
         true
      );

      public static final DripstoneRock.TFCFBlockType[] VALUES = values();
      private final boolean variants;
      private final BiFunction<RegistryRock, DripstoneRock.TFCFBlockType, Block> blockFactory;
      private final String serializedName;

      public static DripstoneRock.TFCFBlockType valueOf(int i) {
         return i >= 0 && i < VALUES.length ? VALUES[i] : STONE_TILES;
      }

      private TFCFBlockType(BiFunction<RegistryRock, DripstoneRock.TFCFBlockType, Block> blockFactory, boolean variants) {
         this.blockFactory = blockFactory;
         this.variants = variants;
         this.serializedName = this.name().toLowerCase(Locale.ROOT);
      }

      public boolean hasVariants() {
         return this.variants;
      }

      public Block create(RegistryRock rock) {
         return this.blockFactory.apply(rock, this);
      }

      public String getSerializedName() {
         return this.serializedName;
      }

      @Nullable
      public DripstoneRock.TFCFBlockType mossy() {
         return switch (this) {
            case COBBLED_BRICKS, MOSSY_COBBLED_BRICKS -> MOSSY_COBBLED_BRICKS;
            case FLAGSTONE_BRICKS, MOSSY_FLAGSTONE_BRICKS -> MOSSY_FLAGSTONE_BRICKS;
            default -> null;
         };
      }
   }

   @Override
   public net.dries007.tfc.common.blocks.rock.RockDisplayCategory displayCategory() {
      return net.dries007.tfc.common.blocks.rock.RockDisplayCategory.SEDIMENTARY;
   }
}
