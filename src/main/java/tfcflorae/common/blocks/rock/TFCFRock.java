package tfcflorae.common.blocks.rock;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import net.dries007.tfc.common.blocks.DecorationBlockRegistryObject;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.SandstoneBlockType;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.rock.MossGrowingBlock;
import net.dries007.tfc.common.blocks.rock.MossSpreadingBlock;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.common.blocks.rock.RockCategory;
import net.dries007.tfc.common.blocks.rock.RockDisplayCategory;
import net.dries007.tfc.common.blocks.rock.Rock.BlockType;
import net.dries007.tfc.common.blocks.soil.SandBlockType;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.registry.RegistryRock;
import net.dries007.tfc.world.settings.RockSettings;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;
import tfcflorae.common.blocks.TFCFBlocks;
import tfcflorae.common.blocks.soil.SandLayerBlock;
import tfcflorae.util.registry.TFCFRegistryRock;

public enum TFCFRock implements TFCFRegistryRock {
   RED_GRANITE(RockCategory.IGNEOUS_INTRUSIVE, MapColor.TERRACOTTA_RED, "RED"),
   PORPHYRY(RockCategory.IGNEOUS_EXTRUSIVE, MapColor.TERRACOTTA_RED, "RED"),
   BONINITE(RockCategory.IGNEOUS_EXTRUSIVE, MapColor.TERRACOTTA_GRAY, "GRAY"),
   CARBONATITE(RockCategory.IGNEOUS_EXTRUSIVE, MapColor.TERRACOTTA_YELLOW, "YELLOW"),
   LATERITE(RockCategory.SEDIMENTARY, MapColor.TERRACOTTA_ORANGE, "ORANGE"),
   MUDSTONE(RockCategory.SEDIMENTARY, MapColor.TERRACOTTA_LIGHT_GRAY, "LIGHT_GRAY"),
   SANDSTONE(RockCategory.SEDIMENTARY, MapColor.TERRACOTTA_YELLOW, "YELLOW"),
   ARKOSE(RockCategory.SEDIMENTARY, MapColor.TERRACOTTA_BROWN, "BROWN"),
   JASPILLITE(RockCategory.SEDIMENTARY, MapColor.TERRACOTTA_RED, "RED"),
   WACKESTONE(RockCategory.SEDIMENTARY, MapColor.TERRACOTTA_BLACK, "BLACK"),
   CATACLASITE(RockCategory.METAMORPHIC, MapColor.TERRACOTTA_GRAY, "GRAY"),
   SOAPSTONE(RockCategory.METAMORPHIC, MapColor.TERRACOTTA_CYAN, "CYAN"),
   TUFF(RockCategory.IGNEOUS_EXTRUSIVE, MapColor.TERRACOTTA_GRAY, "GRAY");

   public static final TFCFRock[] VALUES = values();
   public final String serializedName = this.name().toLowerCase(Locale.ROOT);
   public final RockCategory category;
   public final MapColor color;
   public final String sandType;

   private TFCFRock(RockCategory category, MapColor color, String sandType) {
      this.category = category;
      this.color = color;
      this.sandType = sandType.toUpperCase(Locale.ROOT);
   }

   public Supplier<? extends Block> getSandType() {
      switch (this.sandType) {
         case "BLACK":
            return (Supplier)TFCBlocks.SAND.get(SandBlockType.BLACK);
         case "BROWN":
            return (Supplier)TFCBlocks.SAND.get(SandBlockType.BROWN);
         case "GREEN":
            return (Supplier)TFCBlocks.SAND.get(SandBlockType.GREEN);
         case "PINK":
            return (Supplier)TFCBlocks.SAND.get(SandBlockType.PINK);
         case "RED":
            return (Supplier)TFCBlocks.SAND.get(SandBlockType.RED);
         case "WHITE":
            return (Supplier)TFCBlocks.SAND.get(SandBlockType.WHITE);
         case "YELLOW":
            return (Supplier)TFCBlocks.SAND.get(SandBlockType.YELLOW);
         default:
            return (Supplier)TFCBlocks.SAND.get(SandBlockType.YELLOW);
      }
   }

   public Supplier<? extends Block> getSandstone() {
      switch (this.sandType) {
         case "BLACK":
            return (Supplier)((Map)TFCBlocks.SANDSTONE.get(SandBlockType.BLACK)).get(SandstoneBlockType.RAW);
         case "BLUE":
            return (Supplier)((Map)TFCBlocks.SANDSTONE.get(SandBlockType.WHITE)).get(SandstoneBlockType.RAW);
         case "BROWN":
            return (Supplier)((Map)TFCBlocks.SANDSTONE.get(SandBlockType.BROWN)).get(SandstoneBlockType.RAW);
         case "CYAN":
            return (Supplier)((Map)TFCBlocks.SANDSTONE.get(SandBlockType.GREEN)).get(SandstoneBlockType.RAW);
         case "GRAY":
            return (Supplier)((Map)TFCBlocks.SANDSTONE.get(SandBlockType.BLACK)).get(SandstoneBlockType.RAW);
         case "GREEN":
            return (Supplier)((Map)TFCBlocks.SANDSTONE.get(SandBlockType.GREEN)).get(SandstoneBlockType.RAW);
         case "LIGHT_BLUE":
            return (Supplier)((Map)TFCBlocks.SANDSTONE.get(SandBlockType.WHITE)).get(SandstoneBlockType.RAW);
         case "LIGHT_GRAY":
            return (Supplier)((Map)TFCBlocks.SANDSTONE.get(SandBlockType.WHITE)).get(SandstoneBlockType.RAW);
         case "LIGHT_GREEN":
            return (Supplier)((Map)TFCBlocks.SANDSTONE.get(SandBlockType.GREEN)).get(SandstoneBlockType.RAW);
         case "MAGENTA":
            return (Supplier)((Map)TFCBlocks.SANDSTONE.get(SandBlockType.PINK)).get(SandstoneBlockType.RAW);
         case "ORANGE":
            return (Supplier)((Map)TFCBlocks.SANDSTONE.get(SandBlockType.RED)).get(SandstoneBlockType.RAW);
         case "PINK":
            return (Supplier)((Map)TFCBlocks.SANDSTONE.get(SandBlockType.PINK)).get(SandstoneBlockType.RAW);
         case "PURPLE":
            return (Supplier)((Map)TFCBlocks.SANDSTONE.get(SandBlockType.PINK)).get(SandstoneBlockType.RAW);
         case "RED":
            return (Supplier)((Map)TFCBlocks.SANDSTONE.get(SandBlockType.RED)).get(SandstoneBlockType.RAW);
         case "WHITE":
            return (Supplier)((Map)TFCBlocks.SANDSTONE.get(SandBlockType.WHITE)).get(SandstoneBlockType.RAW);
         case "YELLOW":
            return (Supplier)((Map)TFCBlocks.SANDSTONE.get(SandBlockType.YELLOW)).get(SandstoneBlockType.RAW);
         default:
            return (Supplier)((Map)TFCBlocks.SANDSTONE.get(SandBlockType.YELLOW)).get(SandstoneBlockType.RAW);
      }
   }

   public boolean isSandTFC(String sandType) {
      switch (sandType) {
         case "BLUE":
         case "CYAN":
         case "GRAY":
         case "LIGHT_BLUE":
         case "LIGHT_GRAY":
         case "LIGHT_GREEN":
         case "MAGENTA":
         case "ORANGE":
         case "PURPLE":
            return false;
         case "BLACK":
         case "BROWN":
         case "GREEN":
         case "PINK":
         case "RED":
         case "WHITE":
         case "YELLOW":
            return true;
         default:
            return true;
      }
   }

   public RockCategory category() {
      return this.category;
   }

   public MapColor color() {
      return this.color;
   }

   public Supplier<? extends Block> getBlock(Rock.BlockType type) {
      return (Supplier)((Map)TFCFBlocks.TFCF_ROCK_BLOCKS.get(this)).get(type);
   }

   public Supplier<? extends Block> getBlockTFC(RegistryRock rock, TFCFRock.TFCFBlockType type) {
      return (Supplier)((Map)TFCFBlocks.ROCK_BLOCKS.get(rock)).get(type);
   }

   public Supplier<? extends Block> getBlockTFCF(TFCFRock.TFCFBlockType type) {
      return (Supplier)((Map)TFCFBlocks.TFCF_ROCKTYPE_BLOCKS.get(this)).get(type);
   }

   public Supplier<? extends Block> getAnvil() {
      return (Supplier)TFCFBlocks.ROCK_ANVILS.get(this);
   }

   public Supplier<? extends SlabBlock> getSlab(Rock.BlockType type) {
      return ((DecorationBlockRegistryObject)((Map)TFCFBlocks.TFCF_ROCKTYPE_DECORATIONS.get(this)).get(type)).slab();
   }

   public Supplier<? extends StairBlock> getStair(Rock.BlockType type) {
      return ((DecorationBlockRegistryObject)((Map)TFCFBlocks.TFCF_ROCKTYPE_DECORATIONS.get(this)).get(type)).stair();
   }

   public Supplier<? extends WallBlock> getWall(Rock.BlockType type) {
      return ((DecorationBlockRegistryObject)((Map)TFCFBlocks.TFCF_ROCKTYPE_DECORATIONS.get(this)).get(type)).wall();
   }

   public Supplier<? extends SlabBlock> getSlabTFC(RegistryRock rock, TFCFRock.TFCFBlockType type) {
      return TFCFBlocks.TFC_ROCK_DECORATIONS.get(rock) != null && ((Map)TFCFBlocks.TFC_ROCK_DECORATIONS.get(rock)).get(type) != null ? ((DecorationBlockRegistryObject)((Map)TFCFBlocks.TFC_ROCK_DECORATIONS.get(rock)).get(type)).slab() : ((DecorationBlockRegistryObject)((Map)TFCFBlocks.TFC_ROCK_DECORATIONS.get(Rock.GRANITE)).get(TFCFRock.TFCFBlockType.COBBLED_BRICKS)).slab();
   }

   public Supplier<? extends StairBlock> getStairTFC(RegistryRock rock, TFCFRock.TFCFBlockType type) {
      return TFCFBlocks.TFC_ROCK_DECORATIONS.get(rock) != null && ((Map)TFCFBlocks.TFC_ROCK_DECORATIONS.get(rock)).get(type) != null ? ((DecorationBlockRegistryObject)((Map)TFCFBlocks.TFC_ROCK_DECORATIONS.get(rock)).get(type)).stair() : ((DecorationBlockRegistryObject)((Map)TFCFBlocks.TFC_ROCK_DECORATIONS.get(Rock.GRANITE)).get(TFCFRock.TFCFBlockType.COBBLED_BRICKS)).stair();
   }

   public Supplier<? extends WallBlock> getWallTFC(RegistryRock rock, TFCFRock.TFCFBlockType type) {
      return TFCFBlocks.TFC_ROCK_DECORATIONS.get(rock) != null && ((Map)TFCFBlocks.TFC_ROCK_DECORATIONS.get(rock)).get(type) != null ? ((DecorationBlockRegistryObject)((Map)TFCFBlocks.TFC_ROCK_DECORATIONS.get(rock)).get(type)).wall() : ((DecorationBlockRegistryObject)((Map)TFCFBlocks.TFC_ROCK_DECORATIONS.get(Rock.GRANITE)).get(TFCFRock.TFCFBlockType.COBBLED_BRICKS)).wall();
   }

   public Supplier<? extends SlabBlock> getSlabTFCF(TFCFRock.TFCFBlockType type) {
      return ((DecorationBlockRegistryObject)((Map)TFCFBlocks.TFCF_ROCK_DECORATIONS.get(this)).get(type)).slab();
   }

   public Supplier<? extends StairBlock> getStairTFCF(TFCFRock.TFCFBlockType type) {
      return ((DecorationBlockRegistryObject)((Map)TFCFBlocks.TFCF_ROCK_DECORATIONS.get(this)).get(type)).stair();
   }

   public Supplier<? extends WallBlock> getWallTFCF(TFCFRock.TFCFBlockType type) {
      return ((DecorationBlockRegistryObject)((Map)TFCFBlocks.TFCF_ROCK_DECORATIONS.get(this)).get(type)).wall();
   }

   public String getSerializedName() {
      return this.serializedName;
   }

   public static void registerDefaultRocks() {
      for(TFCFRock rock : values()) {
         // 凝灰岩没有矿石，也不需要 WorldGen 用的默认岩石设置
         if (rock == TUFF) {
            continue;
         }
         Map<Rock.BlockType, RegistryObject<Block>> blocks = (Map)TFCFBlocks.TFCF_ROCK_BLOCKS.get(rock);
         // 相关表被裁剪后可能整块不注册，get() 会返回 null；这里必须跳过，否则下面解引用直接 NPE
         if (blocks == null
            || blocks.get(BlockType.RAW) == null
            || blocks.get(BlockType.HARDENED) == null
            || blocks.get(BlockType.GRAVEL) == null
            || blocks.get(BlockType.COBBLE) == null) {
            continue;
         }
         ResourceLocation id = Helpers.identifier(rock.getSerializedName());
         RockCategory category = rock.category();
         RockSettings.register(id, new RockSettings((Block)((RegistryObject)blocks.get(BlockType.RAW)).get(), (Block)((RegistryObject)blocks.get(BlockType.HARDENED)).get(), (Block)((RegistryObject)blocks.get(BlockType.GRAVEL)).get(), (Block)((RegistryObject)blocks.get(BlockType.COBBLE)).get(), (Block)rock.getSandType().get(), (Block)rock.getSandstone().get(), Optional.of((Block)((RegistryObject)blocks.get(BlockType.SPIKE)).get()), Optional.of((Block)((RegistryObject)blocks.get(BlockType.LOOSE)).get()), Optional.empty()));
      }

   }

   public static Enum<?> getEnum(RockSettings rockSettings) {
      return values()[0];
   }

   public RockDisplayCategory displayCategory() {
      RockDisplayCategory var10000;
      switch (this.name().toLowerCase(Locale.ROOT)) {
         case "rhyolite":
         case "basalt":
         case "andesite":
         case "dacite":
            var10000 = RockDisplayCategory.FELSIC_IGNEOUS_EXTRUSIVE;
            break;
         case "porphyry":
         case "boninite":
         case "cataclasite":
            var10000 = RockDisplayCategory.INTERMEDIATE_IGNEOUS_EXTRUSIVE;
            break;
         case "granite":
         case "red_granite":
            var10000 = RockDisplayCategory.FELSIC_IGNEOUS_INTRUSIVE;
            break;
         case "diorite":
         case "gabbro":
            var10000 = RockDisplayCategory.INTERMEDIATE_IGNEOUS_INTRUSIVE;
            break;
         case "quartzite":
         case "slate":
         case "phyllite":
         case "schist":
         case "gneiss":
         case "marble":
         case "soapstone":
            var10000 = RockDisplayCategory.METAMORPHIC;
            break;
         case "shale":
         case "claystone":
         case "limestone":
         case "conglomerate":
         case "dolomite":
         case "chert":
         case "chalk":
         case "arkose":
         case "mudstone":
         case "wackestone":
         case "carbonatite":
         case "laterite":
         case "sandstone":
         case "jaspillite":
         case "dripstone":
         case "gypsum":
            var10000 = RockDisplayCategory.SEDIMENTARY;
            break;
         default:
            var10000 = RockDisplayCategory.SEDIMENTARY;
      }

      return var10000;
   }

   public static enum TFCFBlockType implements StringRepresentable {
      STONE_TILES((rock, self) -> new Block(Properties.of().mapColor(rock.color()).sound(SoundType.DEEPSLATE).strength(rock.category().hardness(6.5F), 10.0F).requiresCorrectToolForDrops()), (rock, self) -> new Block(Properties.of().mapColor(rock.color()).sound(SoundType.DEEPSLATE).strength(rock.category().hardness(6.5F), 10.0F).requiresCorrectToolForDrops()), true),
      COBBLED_BRICKS((rock, self) -> new MossGrowingRotatedPillarBlock(Properties.of().mapColor(rock.color()).sound(SoundType.DEEPSLATE_BRICKS).strength(rock.category().hardness(9.0F), 10.0F).requiresCorrectToolForDrops(), (Supplier)((Map)TFCFBlocks.TFCF_ROCKTYPE_BLOCKS.get(rock)).get(self.mossy())), (rock, self) -> new MossGrowingRotatedPillarBlock(Properties.of().mapColor(rock.color()).sound(SoundType.DEEPSLATE_BRICKS).strength(rock.category().hardness(9.0F), 10.0F).requiresCorrectToolForDrops(), (Supplier)((Map)TFCFBlocks.ROCK_BLOCKS.get(rock)).get(self.mossy())), true),
      POLISHED_COBBLED_BRICKS((rock, self) -> new Block(Properties.of().mapColor(rock.color()).sound(SoundType.ANCIENT_DEBRIS).strength(rock.category().hardness(7.0F), 10.0F).requiresCorrectToolForDrops()), (rock, self) -> new Block(Properties.of().mapColor(rock.color()).sound(SoundType.ANCIENT_DEBRIS).strength(rock.category().hardness(7.0F), 10.0F).requiresCorrectToolForDrops()), true),
      FLAGSTONE_BRICKS((rock, self) -> new MossGrowingBlock(Properties.of().mapColor(rock.color()).sound(SoundType.DEEPSLATE_TILES).strength(rock.category().hardness(9.0F), 10.0F).requiresCorrectToolForDrops(), (Supplier)((Map)TFCFBlocks.TFCF_ROCKTYPE_BLOCKS.get(rock)).get(self.mossy())), (rock, self) -> new MossGrowingBlock(Properties.of().mapColor(rock.color()).sound(SoundType.DEEPSLATE_TILES).strength(rock.category().hardness(9.0F), 10.0F).requiresCorrectToolForDrops(), (Supplier)((Map)TFCFBlocks.ROCK_BLOCKS.get(rock)).get(self.mossy())), true),
      MOSSY_COBBLED_BRICKS((rock, self) -> new MossSpreadingRotatedPillarBlock(Properties.of().mapColor(rock.color()).sound(SoundType.DEEPSLATE_BRICKS).strength(rock.category().hardness(9.0F), 10.0F).requiresCorrectToolForDrops()), (rock, self) -> new MossSpreadingRotatedPillarBlock(Properties.of().mapColor(rock.color()).sound(SoundType.DEEPSLATE_BRICKS).strength(rock.category().hardness(9.0F), 10.0F).requiresCorrectToolForDrops()), true),
      MOSSY_FLAGSTONE_BRICKS((rock, self) -> new MossSpreadingBlock(Properties.of().mapColor(rock.color()).sound(SoundType.DEEPSLATE_TILES).strength(rock.category().hardness(9.0F), 10.0F).requiresCorrectToolForDrops()), (rock, self) -> new MossSpreadingBlock(Properties.of().mapColor(rock.color()).sound(SoundType.DEEPSLATE_TILES).strength(rock.category().hardness(9.0F), 10.0F).requiresCorrectToolForDrops()), true),
      CRACKED_FLAGSTONE_BRICKS((rock, self) -> new Block(Properties.of().mapColor(rock.color()).sound(SoundType.DEEPSLATE_TILES).strength(rock.category().hardness(9.0F), 10.0F).requiresCorrectToolForDrops()), (rock, self) -> new Block(Properties.of().mapColor(rock.color()).sound(SoundType.DEEPSLATE_TILES).strength(rock.category().hardness(9.0F), 10.0F).requiresCorrectToolForDrops()), true),
      MORTAR_AND_COBBLE((rock, self) -> new MossGrowingBlock(Properties.of().mapColor(rock.color()).sound(SoundType.STONE).strength(rock.category().hardness(5.5F), 10.0F).requiresCorrectToolForDrops(), (Supplier)((Map)TFCFBlocks.TFCF_ROCKTYPE_BLOCKS.get(rock)).get(self.mossy())), (rock, self) -> new MossGrowingBlock(Properties.of().mapColor(rock.color()).sound(SoundType.STONE).strength(rock.category().hardness(5.5F), 10.0F).requiresCorrectToolForDrops(), (Supplier)((Map)TFCFBlocks.ROCK_BLOCKS.get(rock)).get(self.mossy())), true),
      MOSSY_MORTAR_AND_COBBLE((rock, self) -> new MossSpreadingBlock(Properties.of().mapColor(rock.color()).sound(SoundType.STONE).strength(rock.category().hardness(5.5F), 10.0F).requiresCorrectToolForDrops()), (rock, self) -> new MossSpreadingBlock(Properties.of().mapColor(rock.color()).sound(SoundType.STONE).strength(rock.category().hardness(5.5F), 10.0F).requiresCorrectToolForDrops()), true),
      GRAVEL_LAYER((rock, self) -> new SandLayerBlock(rock.color().col, ExtendedProperties.of(rock.color()).strength(0.1F).requiresCorrectToolForDrops().sound(SoundType.GRAVEL), (Supplier)((Map)TFCFBlocks.TFCF_ROCK_BLOCKS.get(rock)).get(BlockType.GRAVEL)), (rock, self) -> new SandLayerBlock(rock.color().col, ExtendedProperties.of(rock.color()).strength(0.1F).requiresCorrectToolForDrops().sound(SoundType.GRAVEL), (Supplier)((Map)TFCBlocks.ROCK_BLOCKS.get(rock)).get(BlockType.GRAVEL)), false);

      public static final TFCFRock.TFCFBlockType[] VALUES = values();
      private final boolean variants;
      private final BiFunction<RegistryRock, TFCFRock.TFCFBlockType, Block> TFCFactory;
      private final BiFunction<TFCFRegistryRock, TFCFRock.TFCFBlockType, Block> TFCFFactory;
      private final String serializedName;

      public static TFCFRock.TFCFBlockType valueOf(int i) {
         return i >= 0 && i < VALUES.length ? VALUES[i] : STONE_TILES;
      }

      private TFCFBlockType(BiFunction<TFCFRegistryRock, TFCFRock.TFCFBlockType, Block> TFCFFactory, BiFunction<RegistryRock, TFCFRock.TFCFBlockType, Block> TFCFactory, boolean variants) {
         this.TFCFFactory = TFCFFactory;
         this.TFCFactory = TFCFactory;
         this.variants = variants;
         this.serializedName = this.name().toLowerCase(Locale.ROOT);
      }

      private TFCFBlockType(BiFunction<TFCFRegistryRock, TFCFRock.TFCFBlockType, Block> TFCFFactory, boolean variants) {
         this.TFCFFactory = TFCFFactory;
         this.TFCFactory = null;
         this.variants = variants;
         this.serializedName = this.name().toLowerCase(Locale.ROOT);
      }

      public boolean hasVariants() {
         return this.variants;
      }

      public Block createTFC(RegistryRock rock) {
         return (Block)this.TFCFactory.apply(rock, this);
      }

      public Block createTFCF(TFCFRegistryRock rock) {
         return (Block)this.TFCFFactory.apply(rock, this);
      }

      public BiFunction<RegistryRock, TFCFRock.TFCFBlockType, Block> getTFCFactory() {
      // 本模组的多余岩石工艺品已废弃（资源已删除）：返回 null 后 rockTypeMapper / RockTypeDecoMapper 会跳过，不再注册任何方块。
      if (this == STONE_TILES || this == COBBLED_BRICKS || this == POLISHED_COBBLED_BRICKS || this == FLAGSTONE_BRICKS || this == MOSSY_COBBLED_BRICKS || this == MOSSY_FLAGSTONE_BRICKS || this == CRACKED_FLAGSTONE_BRICKS || this == MORTAR_AND_COBBLE || this == MOSSY_MORTAR_AND_COBBLE) {
         return null;
      }
         return this.TFCFactory;
      }

      public BiFunction<TFCFRegistryRock, TFCFRock.TFCFBlockType, Block> getTFCFFactory() {
      // 本模组的多余岩石工艺品已废弃（资源已删除）：返回 null 后 rockTypeMapper / RockTypeDecoMapper 会跳过，不再注册任何方块。
      if (this == STONE_TILES || this == COBBLED_BRICKS || this == POLISHED_COBBLED_BRICKS || this == FLAGSTONE_BRICKS || this == MOSSY_COBBLED_BRICKS || this == MOSSY_FLAGSTONE_BRICKS || this == CRACKED_FLAGSTONE_BRICKS || this == MORTAR_AND_COBBLE || this == MOSSY_MORTAR_AND_COBBLE) {
         return null;
      }
         return this.TFCFFactory;
      }

      public String getSerializedName() {
         return this.serializedName;
      }

      @Nullable
      public TFCFRock.@Nullable TFCFBlockType mossy() {
         TFCFRock.TFCFBlockType var10000;
         switch (this) {
            case COBBLED_BRICKS:
            case MOSSY_COBBLED_BRICKS:
               var10000 = MOSSY_COBBLED_BRICKS;
               break;
            case FLAGSTONE_BRICKS:
            case MOSSY_FLAGSTONE_BRICKS:
               var10000 = MOSSY_FLAGSTONE_BRICKS;
               break;
            case MORTAR_AND_COBBLE:
               var10000 = MOSSY_MORTAR_AND_COBBLE;
               break;
            default:
               var10000 = null;
         }

         return var10000;
      }
   }
}
