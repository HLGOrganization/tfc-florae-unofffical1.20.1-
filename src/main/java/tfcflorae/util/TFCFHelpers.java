package tfcflorae.util;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.util.RandomSource;
import java.util.function.Consumer;
import net.dries007.tfc.common.blockentities.FarmlandBlockEntity.NutrientType;
import net.dries007.tfc.common.blocks.Gem;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.plant.fruit.FruitBlocks.Tree;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.common.blocks.rock.Rock.BlockType;
import net.dries007.tfc.common.blocks.soil.SandBlockType;
import net.dries007.tfc.common.blocks.soil.SoilBlockType.Variant;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.climate.Climate;
import net.dries007.tfc.util.registry.RegistryRock;
import net.dries007.tfc.util.registry.RegistrySoilVariant;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
import net.dries007.tfc.world.chunkdata.LerpFloatLayer;
import net.dries007.tfc.world.noise.Noise2D;
import net.dries007.tfc.world.noise.OpenSimplex2D;
import net.dries007.tfc.world.settings.RockSettings;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.server.ServerLifecycleHooks;
import tfcflorae.common.TFCFTags;
import tfcflorae.common.blocks.TFCFBlocks;
import tfcflorae.common.blocks.rock.TFCFRock;
import tfcflorae.common.blocks.soil.Colors;
import tfcflorae.common.blocks.soil.TFCFSoil;

public class TFCFHelpers {
   public static final RandomSource RANDOM = RandomSource.create();
   public static final Direction[] NOT_DOWN = new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.UP};
   public static final Direction[] NOT_UP = new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.DOWN};
   public static final Direction[] DIRECTIONS = Direction.values();
   public static final Direction[] DIRECTIONS_HORIZONTAL_FIRST = new Direction[]{
      Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH, Direction.UP, Direction.DOWN
   };
   public static final Direction[] DIRECTIONS_HORIZONTAL = Arrays.stream(DIRECTIONS)
      .filter(d -> d != Direction.DOWN && d != Direction.UP)
      .toArray(Direction[]::new);
   public static final Direction[] DIRECTIONS_VERTICAL = Arrays.stream(DIRECTIONS)
      .filter(d -> d == Direction.DOWN || d == Direction.UP)
      .toArray(Direction[]::new);

   public TFCFHelpers() {
   }

   public static RandomSource randomSeed(long seed) {
      RandomSource random = RandomSource.create(seed);
      random.setSeed(seed ^ random.nextLong());
      return random;
   }

   public static ServerLevel getLevel() {
      return ServerLifecycleHooks.getCurrentServer().overworld();
   }

   public static float getRainfall(long seed, int chunkX, int chunkZ) {
      if (getLevel() != null) {
         ChunkData data = ChunkDataProvider.get(getLevel()).get(getLevel().getChunk(chunkX, chunkZ));
         ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
         return data.getRainfall(chunkPos.getMiddleBlockX(), chunkPos.getMiddleBlockZ());
      } else {
         // TODO(worldgen): TFC 3.x removed ClimateSettings; fixed fallback value
         return 250.0F;
      }
   }

   public static float getTemperature(long seed, int chunkX, int chunkZ) {
      if (getLevel() != null) {
         ChunkData data = ChunkDataProvider.get(getLevel()).get(getLevel().getChunk(chunkX, chunkZ));
         ChunkPos chunkPos = new ChunkPos(chunkX, chunkZ);
         return data.getAverageTemp(chunkPos.getMiddleBlockX(), chunkPos.getMiddleBlockZ());
      } else {
         // TODO(worldgen): see getRainfall
         return 5.0F;
      }
   }

   public static float getForestDensity(long seed, int chunkX, int chunkZ) {
      if (getLevel() != null) {
         ChunkData data = ChunkDataProvider.get(getLevel()).get(getLevel().getChunk(chunkX, chunkZ));
         return data.getForestDensity();
      } else {
         return (float) new OpenSimplex2D(randomSeed(seed).nextInt())
            .octaves(4)
            .spread(0.0025F)
            .scaled(-0.2F, 1.2F)
            .clamped(0.0F, 1.0F)
            .noise(chunkX + 8, chunkZ + 8);
      }
   }

   public static ResourceLocation identifier(String id) {
      return new ResourceLocation("tfcflorae", id);
   }

   public static Direction getRandom(RandomSource random) {
      return (Direction)Util.getRandom(DIRECTIONS_HORIZONTAL, random);
   }

   public static Component blockEntityName(String name) {
      return Component.translatable("tfcflorae.block_entity." + name);
   }

   public static InteractionResult insertOne(Level level, ItemStack item, int slot, IItemHandler inv, Player player) {
      return !inv.isItemValid(slot, item) ? InteractionResult.PASS : completeInsertion(level, item, inv, player, slot);
   }

   public static InteractionResult takeOne(Level level, int slot, IItemHandler inv, Player player) {
      ItemStack stack = inv.extractItem(slot, 1, false);
      if (stack.isEmpty()) {
         return InteractionResult.PASS;
      } else {
         if (!level.isClientSide) {
            ItemHandlerHelper.giveItemToPlayer(player, stack);
         }

         return InteractionResult.sidedSuccess(level.isClientSide);
      }
   }

   public static InteractionResult insertOneAny(Level level, ItemStack item, int start, int end, IItemHandler inv, Player player) {
      for (int i = start; i <= end; i++) {
         if (inv.getStackInSlot(i).isEmpty()) {
            return completeInsertion(level, item, inv, player, i);
         }
      }

      return InteractionResult.PASS;
   }

   private static InteractionResult completeInsertion(Level level, ItemStack item, IItemHandler inv, Player player, int slot) {
      ItemStack stack = inv.insertItem(slot, item.split(1), false);
      if (stack.isEmpty()) {
         return InteractionResult.sidedSuccess(level.isClientSide);
      } else {
         if (!level.isClientSide) {
            ItemHandlerHelper.giveItemToPlayer(player, stack);
         }

         return InteractionResult.sidedSuccess(level.isClientSide);
      }
   }

   public static InteractionResult takeOneAny(Level level, int start, int end, IItemHandler inv, Player player) {
      for (int i = start; i <= end; i++) {
         ItemStack stack = inv.extractItem(i, 1, false);
         if (!stack.isEmpty()) {
            if (!level.isClientSide) {
               ItemHandlerHelper.giveItemToPlayer(player, stack);
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
         }
      }

      return InteractionResult.PASS;
   }

   public static Iterable<BlockPos> allPositionsCentered(BlockPos center, int radius, int height) {
      return BlockPos.betweenClosed(center.offset(-radius, -height, -radius), center.offset(radius, height, radius));
   }

   public static void openScreen(ServerPlayer player, MenuProvider containerSupplier) {
      NetworkHooks.openScreen(player, containerSupplier);
   }

   public static void openScreen(ServerPlayer player, MenuProvider containerSupplier, BlockPos pos) {
      NetworkHooks.openScreen(player, containerSupplier, pos);
   }

   public static void openScreen(ServerPlayer player, MenuProvider containerSupplier, Consumer<FriendlyByteBuf> extraDataWriter) {
      NetworkHooks.openScreen(player, containerSupplier, extraDataWriter);
   }

   public static int randomRange(int min, int max) {
      return RANDOM.nextInt(max - min) + min;
   }

   public static double randomRange(double min, double max) {
      return min + (max - min) * RANDOM.nextDouble();
   }

   public static float randomDirection(float value) {
      return RANDOM.nextBoolean() ? value : -value;
   }

   public static double randomDirection(double value) {
      return RANDOM.nextBoolean() ? value : -value;
   }

   public static int randomDirection(int value) {
      return RANDOM.nextBoolean() ? value : -value;
   }

   public static <T> T choose(T... values) {
      return values[RANDOM.nextInt(values.length)];
   }

   public static <T> T choose(List<T> values) {
      return values.get(RANDOM.nextInt(values.size()));
   }

   public static int randomBias(int min, int max) {
      int num = randomRange(min, max);
      int mid = max / 2 - min / 2;
      int halfMid = mid / 2;
      if (num > mid) {
         num -= RANDOM.nextInt(halfMid + 1);
      } else if (num < mid) {
         num += RANDOM.nextInt(halfMid + 1);
      }

      return num;
   }

   public static String toProperCase(String s) {
      return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
   }

   public static Component translatable(String key) {
      return Component.translatable(key);
   }

   public static RegistryRock rockType(ServerLevel level, BlockPos pos) {
      RockSettings surfaceRock = ChunkDataProvider.get(level).get(level, pos).getRockData().getRock(pos);
      if (surfaceRock != null) {
         for (Rock rockTFC : Rock.values()) {
            if (surfaceRock.raw() == ((RegistryObject)((Map)TFCBlocks.ROCK_BLOCKS.get(rockTFC)).get(BlockType.RAW)).get()) {
               return rockTFC;
            }

            for (TFCFRock rockTFCF : TFCFRock.values()) {
               if (surfaceRock.raw() == TFCFBlocks.TFCF_ROCK_BLOCKS.get(rockTFCF).get(BlockType.RAW).get()) {
                  return rockTFCF;
               }
            }
         }
      }

      return Rock.GRANITE;
   }

   public static RegistryRock rockType(WorldGenLevel level, BlockPos pos) {
      RockSettings surfaceRock = ChunkDataProvider.get(level).get(level, pos).getRockData().getRock(pos);
      if (surfaceRock != null) {
         for (Rock rockTFC : Rock.values()) {
            if (surfaceRock.raw() == ((RegistryObject)((Map)TFCBlocks.ROCK_BLOCKS.get(rockTFC)).get(BlockType.RAW)).get()) {
               return rockTFC;
            }

            for (TFCFRock rockTFCF : TFCFRock.values()) {
               if (surfaceRock.raw() == TFCFBlocks.TFCF_ROCK_BLOCKS.get(rockTFCF).get(BlockType.RAW).get()) {
                  return rockTFCF;
               }
            }
         }
      }

      return Rock.GRANITE;
   }

   public static RegistrySoilVariant getSoilVariant(WorldGenLevel level, BlockPos pos) {
      for (Variant soilVariant : Variant.values()) {
         if (level.getBlockState(pos).getBlock().getName().toString().toLowerCase(Locale.ROOT).contains(soilVariant.name().toLowerCase(Locale.ROOT))) {
            return soilVariant;
         }
      }

      for (TFCFSoil.TFCFVariant soilVariantx : TFCFSoil.TFCFVariant.values()) {
         if (level.getBlockState(pos).getBlock().getName().toString().toLowerCase(Locale.ROOT).contains(soilVariantx.name().toLowerCase(Locale.ROOT))) {
            return soilVariantx;
         }
      }

      return Variant.LOAM;
   }

   public static boolean isVanillaSoilVariant(WorldGenLevel level, BlockPos pos) {
      for (Variant soilVariant : Variant.values()) {
         if (getSoilVariant(level, pos).toString().toLowerCase(Locale.ROOT).contains(soilVariant.name().toLowerCase(Locale.ROOT))) {
            return true;
         }
      }

      return false;
   }

   public static Block getSandBlock(WorldGenLevel level, BlockPos pos, boolean cheap) {
      Colors color = getSandColorTFCF(level, pos, cheap);
      return color.hasSandNew() ? (Block)TFCFBlocks.SAND.get(color).get() : (Block)((RegistryObject)TFCBlocks.SAND.get(color.toSandTFC(true))).get();
   }

   public static SandBlockType getSandColor(WorldGenLevel level, BlockPos pos, boolean cheap) {
      boolean foundColour = false;
      if (!cheap) {
         for (Direction direction : DIRECTIONS_HORIZONTAL_FIRST) {
            SandBlockType colour = Colors.toSandTFC(Colors.fromMaterialColour(level.getBlockState(pos.relative(direction)).getMapColor(level, pos)), true);
            if (colour != null) {
               foundColour = true;
               return colour;
            }

            foundColour = false;
         }
      } else if (cheap) {
         SandBlockType colour = Colors.toSandTFC(Colors.fromMaterialColour(level.getBlockState(pos.below()).getMapColor(level, pos)), true);
         if (colour != null) {
            foundColour = true;
            return colour;
         }

         foundColour = false;
      } else if (!foundColour) {
         ChunkDataProvider provider = ChunkDataProvider.get(level);
         if (provider != null) {
            ChunkData data = provider.get(level, pos);
            Block sand = data.getRockData().getRock(pos).sand();

            for (Colors sandColors : Colors.values()) {
               if (sand != null
                  && (
                     sand == ((RegistryObject)TFCBlocks.SAND.get(Colors.toSandTFC(sandColors, true))).get()
                        || sand == TFCFBlocks.SAND.get(Colors.nonTFC(sandColors)).get()
                  )) {
                  return Colors.toSandTFC(sandColors, true);
               }
            }
         }
      }

      return SandBlockType.YELLOW;
   }

   public static Colors getSandColorTFCF(WorldGenLevel level, BlockPos pos, boolean cheap) {
      boolean foundColour = false;
      if (!cheap) {
         for (Direction direction : DIRECTIONS_HORIZONTAL_FIRST) {
            Colors colour = Colors.fromMaterialColour(level.getBlockState(pos.relative(direction)).getMapColor(level, pos));
            if (colour != null) {
               foundColour = true;
               return colour;
            }

            foundColour = false;
         }
      } else if (cheap) {
         Colors colour = Colors.fromMaterialColour(level.getBlockState(pos.below()).getMapColor(level, pos));
         if (colour != null) {
            foundColour = true;
            return colour;
         }

         foundColour = false;
      } else if (!foundColour) {
         ChunkDataProvider provider = ChunkDataProvider.get(level);
         if (provider != null) {
            ChunkData data = provider.get(level, pos);
            Block sand = data.getRockData().getRock(pos).sand();

            for (Colors sandColors : Colors.values()) {
               if (sand != null
                  && (
                     sand == ((RegistryObject)TFCBlocks.SAND.get(Colors.toSandTFC(sandColors, true))).get()
                        || sand == TFCFBlocks.SAND.get(Colors.nonTFC(sandColors)).get()
                  )) {
                  return sandColors;
               }
            }
         }
      }

      return Colors.ORANGE;
   }

   public static MapColor gemColor(Gem gem) {
      switch (gem) {
         case AMETHYST:
            return MapColor.COLOR_PURPLE;
         case DIAMOND:
            return MapColor.DIAMOND;
         case EMERALD:
            return MapColor.EMERALD;
         case LAPIS_LAZULI:
            return MapColor.LAPIS;
         case OPAL:
            return MapColor.COLOR_LIGHT_BLUE;
         case PYRITE:
            return MapColor.GOLD;
         case RUBY:
            return MapColor.COLOR_RED;
         case SAPPHIRE:
            return MapColor.COLOR_BLUE;
         case TOPAZ:
            return MapColor.COLOR_ORANGE;
         default:
            return MapColor.QUARTZ;
      }
   }

   public static int getFlowerColor(Tree tree) {
      switch (tree) {
         case CHERRY:
            return new Color(251, 135, 255).getRGB();
         case GREEN_APPLE:
            return new Color(252, 171, 255).getRGB();
         case LEMON:
            return new Color(215, 137, 217).getRGB();
         case OLIVE:
            return new Color(206, 198, 207).getRGB();
         case ORANGE:
            return new Color(251, 242, 252).getRGB();
         case PEACH:
            return new Color(230, 126, 188).getRGB();
         case PLUM:
            return new Color(165, 70, 189).getRGB();
         case RED_APPLE:
            return new Color(252, 171, 255).getRGB();
         default:
            return -1;
      }
   }

   public static Color getAverageColor(Level level, BlockState state) {
      TextureAtlasSprite sprite = ((BakedQuad)Minecraft.getInstance().getBlockRenderer().getBlockModel(state).getQuads(state, Direction.NORTH, level.getRandom()).get(0))
         .getSprite();
      int x0 = sprite.getX();
      int y0 = sprite.getY();
      int x1 = x0 + sprite.contents().width();
      int y1 = y0 + sprite.contents().height();
      long sumr = 0L;
      long sumg = 0L;
      long sumb = 0L;

      for (int xs = x0; xs < x1; xs++) {
         for (int ys = y0; ys < y1; ys++) {
            Color pixel = new Color(sprite.getPixelRGBA(0, xs, ys));
            sumr += (long)pixel.getRed();
            sumg += (long)pixel.getGreen();
            sumb += (long)pixel.getBlue();
         }
      }

      int num = sprite.contents().width() * sprite.contents().height();
      return new Color((float)(sumr / (long)num), (float)(sumg / (long)num), (float)(sumb / (long)num));
   }

   public static ResourceLocation animalTexture(String name) {
      return identifier("textures/entity/animal/" + name + ".png");
   }

   public static RandomSource randomSource(int posX, int posY, int posZ) {
      long i = Mth.getSeed(posX, posY, posZ);
      return new LegacyRandomSource(i);
   }

   public static BlockPos getNearestBlock(WorldGenLevel level, BlockPos sourcePos, int radius, Block targetBlock) {
      BlockPos closestPos = null;

      for (int x = sourcePos.getX() - radius; x < sourcePos.getX() + radius; x++) {
         for (int z = sourcePos.getZ() - radius; z < sourcePos.getZ() + radius; z++) {
            BlockPos checkPos = BlockPos.containing(x, sourcePos.getY(), z);
            if (level.getBlockState(checkPos).getBlock() == targetBlock
               && (
                  closestPos == null
                     || distanceToSqr(
                           sourcePos,
                           sourcePos.getX() - checkPos.getX(),
                           sourcePos.getY() - checkPos.getY(),
                           sourcePos.getZ() - checkPos.getZ()
                        )
                        < distanceToSqr(
                           sourcePos,
                           sourcePos.getX() - closestPos.getX(),
                           sourcePos.getY() - closestPos.getY(),
                           sourcePos.getZ() - closestPos.getZ()
                        )
               )) {
               closestPos = checkPos;
            }
         }
      }

      return closestPos;
   }

   public static int distanceToSqr(BlockPos sourcePos, int x, int y, int z) {
      int d0 = sourcePos.getX() - x;
      int d1 = sourcePos.getY() - y;
      int d2 = sourcePos.getZ() - z;
      return d0 * d0 + d1 * d1 + d2 * d2;
   }

   public static int distanceToNearestBlock(BlockPos sourcePos, BlockPos nearestBlockPos) {
      int d0 = sourcePos.getX() - nearestBlockPos.getX();
      int d1 = sourcePos.getY() - nearestBlockPos.getY();
      int d2 = sourcePos.getZ() - nearestBlockPos.getZ();
      return d0 * d0 + d1 * d1 + d2 * d2;
   }

   public static float catmullrom(float delta, float startPoint, float start, float end, float endPoint) {
      return 0.5F
         * (
            2.0F * start
               + (end - startPoint) * delta
               + (2.0F * startPoint - 5.0F * start + 4.0F * end - endPoint) * delta * delta
               + (3.0F * start - startPoint - 3.0F * end + endPoint) * delta * delta * delta
         );
   }

   public static void spawnParticleBelow(Level level, BlockPos pos, RandomSource random, ParticleOptions options) {
      double d0 = (double)pos.getX() + random.nextDouble();
      double d1 = (double)pos.getY() - 0.05;
      double d2 = (double)pos.getZ() + random.nextDouble();
      level.addParticle(options, d0, d1, d2, 0.0, 0.0, 0.0);
   }

   public static boolean canWalkThroughEffortlessly(Entity entity) {
      if (entity instanceof LivingEntity livingEntity) {
         EquipmentSlot[] var2 = EquipmentSlot.values();
         int var3 = var2.length;
         byte var4 = 0;
         if (var4 < var3) {
            EquipmentSlot slotType = var2[var4];
            return livingEntity.getItemBySlot(slotType).canWalkOnPowderedSnow(livingEntity)
               || Helpers.isItem(livingEntity.getItemBySlot(slotType).getItem(), TFCFTags.Items.BYPASS_SLOWDOWN);
         }
      }

      return false;
   }

   public static float getAverageDailyTemperature(Level level, BlockPos pos) {
      float sum = 0.0F;

      for (int hour = 0; hour < 48; hour++) {
         sum += Climate.getTemperature(level, pos, Calendars.get(level).getCalendarTicks() - (long)(hour * 1000), Calendars.get(level).getCalendarDaysInMonth());
      }

      return sum / 24.0F;
   }

   public static Direction chooseOtherDirection(Direction direction, RandomSource random) {
      int age = random.nextInt(3);
      switch (age) {
         case 0:
            return direction.getClockWise();
         case 1:
            return direction.getCounterClockWise();
         default:
            return direction.getOpposite();
      }
   }

   public static float getRockNutrient(RegistryRock rockType, NutrientType forType, RandomSource random) {
      float gaussModifier = Mth.abs((float)random.nextGaussian()) * 0.09F;
      if (forType == NutrientType.NITROGEN) {
         switch (rockType.category()) {
            case IGNEOUS_EXTRUSIVE:
               return 0.005F + gaussModifier;
            case IGNEOUS_INTRUSIVE:
               return 0.01F + gaussModifier;
            case METAMORPHIC:
               return 0.025F + gaussModifier;
            case SEDIMENTARY:
               return 0.04F + gaussModifier;
            default:
               return 0.01F + gaussModifier;
         }
      } else if (forType == NutrientType.PHOSPHOROUS) {
         switch (rockType.category()) {
            case IGNEOUS_EXTRUSIVE:
               return 0.13F + gaussModifier;
            case IGNEOUS_INTRUSIVE:
               return 0.09F + gaussModifier;
            case METAMORPHIC:
               return 0.05F + gaussModifier;
            case SEDIMENTARY:
               return 0.08F + gaussModifier;
            default:
               return 0.04F + gaussModifier;
         }
      } else if (forType == NutrientType.POTASSIUM) {
         switch (rockType.category()) {
            case IGNEOUS_EXTRUSIVE:
               return 0.2F + gaussModifier;
            case IGNEOUS_INTRUSIVE:
               return 0.125F + gaussModifier;
            case METAMORPHIC:
               return 0.265F + gaussModifier;
            case SEDIMENTARY:
               return 0.05F + gaussModifier;
            default:
               return 0.04F + gaussModifier;
         }
      } else {
         return 0.0F;
      }
   }
}
