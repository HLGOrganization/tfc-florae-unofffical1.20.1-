package tfcflorae.world.feature.tree;
import tfcflorae.common.TFCFBlockStateProperties;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.util.RandomSource;
import net.dries007.tfc.common.TFCTags.Blocks;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.wood.ILeavesBlock;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.util.EnvironmentHelpers;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.world.biome.BiomeExtension;
import net.dries007.tfc.world.biome.TFCBiomes;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
import net.dries007.tfc.world.chunkdata.ForestType;
import net.dries007.tfc.world.feature.tree.ForestConfig;
import net.dries007.tfc.world.feature.tree.ForestConfig.Type;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Plane;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import org.jetbrains.annotations.Nullable;

public class SubsurfaceForestFeature extends Feature<ForestConfig> {
   public SubsurfaceForestFeature(Codec<ForestConfig> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<ForestConfig> context) {
      WorldGenLevel level = context.level();
      BlockPos pos = context.origin();
      RandomSource rand = context.random();
      ForestConfig config = (ForestConfig)context.config();
      ChunkDataProvider provider = ChunkDataProvider.get(context.chunkGenerator());
      ChunkData data = provider.get(level, pos);
      MutableBlockPos mutablePos = new MutableBlockPos();
      ForestType forestType = data.getForestType();
      Type typeConfig = (Type)config.typeMap().get(forestType);
      Biome biome = (Biome)level.getBiome(pos).value();
      BiomeExtension variants = TFCBiomes.getExtensionOrThrow(context.level(), biome);
      float density = data.getForestDensity();
      int terrainHeight = BlockPos.getY((long)level.getHeight(Types.OCEAN_FLOOR_WG, pos.getX(), pos.getZ()));
      if (rand.nextFloat() > typeConfig.perChunkChance() && pos.getY() > terrainHeight - 20) {
         return false;
      } else {
         int treeCount = typeConfig.treeCount().sample(rand);
         int groundCount = typeConfig.groundcoverCount().sample(rand);
         int bushCount = typeConfig.sampleBushCount(rand, typeConfig.bushCount(), treeCount, density);
         boolean placedTrees = false;
         boolean placedBushes = false;
         treeCount = (int)((float)treeCount * (0.6F + 0.9F * density));

         for (int i = 0; i < treeCount; i++) {
            placedTrees |= this.placeTree(level, context.chunkGenerator(), rand, pos, config, data, mutablePos, typeConfig);
         }

         for (int j = 0; j < bushCount; j++) {
            placedBushes |= this.placeBush(level, rand, pos, config, data, mutablePos);
         }

         if (placedTrees) {
            this.placeGroundcover(level, rand, pos, config, data, mutablePos, groundCount);
            this.placeFallenTree(level, rand, pos, config, data, mutablePos);
         }

         return placedTrees || placedBushes;
      }
   }

   private boolean placeTree(
      WorldGenLevel level,
      ChunkGenerator generator,
      RandomSource random,
      BlockPos chunkBlockPos,
      ForestConfig config,
      ChunkData data,
      MutableBlockPos mutablePos,
      Type typeConfig
   ) {
      int chunkX = chunkBlockPos.getX();
      int chunkZ = chunkBlockPos.getZ();
      mutablePos.set(chunkX + random.nextInt(16), mutablePos.getY(), chunkZ + random.nextInt(16));
      net.dries007.tfc.world.feature.tree.ForestConfig.Entry entry = this.getTree(data, random, config, mutablePos);
      if (entry == null) {
         return false;
      } else {
         int oldChance = entry.oldGrowthChance();
         int deadChance = entry.deadChance();
         ConfiguredFeature<?, ?> feature;
         if (typeConfig.allowOldGrowth() && oldChance > 0 && random.nextInt(oldChance) == 0) {
            feature = entry.getOldGrowthFeature();
         } else if (deadChance > 0 && random.nextInt(deadChance) == 0) {
            feature = entry.getDeadFeature();
         } else {
            int spoilerChance = entry.spoilerOldGrowthChance();
            if (typeConfig.hasSpoilers() && spoilerChance > 0 && random.nextInt(spoilerChance) == 0) {
               feature = entry.getOldGrowthFeature();
            } else {
               feature = entry.getFeature();
            }
         }

         return feature.place(level, generator, random, mutablePos);
      }
   }

   private boolean placeBush(WorldGenLevel level, RandomSource random, BlockPos chunkBlockPos, ForestConfig config, ChunkData data, MutableBlockPos mutablePos) {
      int chunkX = chunkBlockPos.getX();
      int chunkZ = chunkBlockPos.getZ();
      mutablePos.set(chunkX + random.nextInt(16), mutablePos.getY(), chunkZ + random.nextInt(16));
      net.dries007.tfc.world.feature.tree.ForestConfig.Entry entry = this.getTree(data, random, config, mutablePos);
      if (entry != null && EnvironmentHelpers.canPlaceBushOn(level, mutablePos)) {
         entry.bushLog().ifPresent(log -> entry.bushLeaves().ifPresent(leaves -> {
               this.placeBushPart(level, mutablePos, log, leaves, 1.0F, random, true);

               for (int i = 0; i < 5; i++) {
                  if (random.nextInt(4) == 0) {
                     mutablePos.move(Plane.HORIZONTAL.getRandomDirection(random));
                     this.placeBushPart(level, mutablePos, leaves, leaves, 0.7F, random, false);
                     if (random.nextInt(6) == 0) {
                        mutablePos.move(Direction.UP);
                        this.placeBushPart(level, mutablePos, leaves, leaves, 0.6F, random, false);
                        break;
                     }
                  }
               }
            }));
         return true;
      } else {
         return false;
      }
   }

   private void placeBushPart(
      WorldGenLevel level, MutableBlockPos mutablePos, BlockState log, BlockState leaves, float decay, RandomSource rand, boolean needsEmptyCenter
   ) {
      if (EnvironmentHelpers.isWorldgenReplaceable(level, mutablePos)) {
         this.setBlock(level, mutablePos, log);
      } else if (needsEmptyCenter) {
         return;
      }

      for (Direction facing : Helpers.DIRECTIONS) {
         if (facing != Direction.DOWN) {
            BlockPos offsetPos = mutablePos.offset(facing.getStepX(), facing.getStepY(), facing.getStepZ());
            if (EnvironmentHelpers.isWorldgenReplaceable(level, offsetPos) && rand.nextFloat() < decay) {
               this.setBlock(level, offsetPos, leaves);
            }
         }
      }
   }

   private void placeGroundcover(
      WorldGenLevel level, RandomSource random, BlockPos chunkBlockPos, ForestConfig config, ChunkData data, MutableBlockPos mutablePos, int tries
   ) {
      int chunkX = chunkBlockPos.getX();
      int chunkZ = chunkBlockPos.getZ();
      mutablePos.set(chunkX + random.nextInt(16), mutablePos.getY(), chunkZ + random.nextInt(16));
      net.dries007.tfc.world.feature.tree.ForestConfig.Entry entry = this.getTree(data, random, config, mutablePos);
      if (entry != null) {
         entry.groundcover()
            .ifPresent(
               groundcover -> {
                  for (int j = 0; j < tries; j++) {
                     BlockState placementState = (BlockState)groundcover.get(random);
                     mutablePos.set(chunkX + random.nextInt(16), mutablePos.getY(), chunkZ + random.nextInt(16));
                     placementState = FluidHelpers.fillWithFluid(placementState, level.getFluidState(mutablePos).getType());
                     if (placementState != null
                        && EnvironmentHelpers.isWorldgenReplaceable(level.getBlockState(mutablePos))
                        && EnvironmentHelpers.isOnSturdyFace(level, mutablePos)) {
                        this.setBlock(level, mutablePos, placementState);
                     }
                  }
               }
            );
      }
   }

   private void placeFallenTree(WorldGenLevel level, RandomSource random, BlockPos chunkBlockPos, ForestConfig config, ChunkData data, MutableBlockPos mutablePos) {
      int chunkX = chunkBlockPos.getX();
      int chunkZ = chunkBlockPos.getZ();
      mutablePos.set(chunkX + random.nextInt(16), mutablePos.getY(), chunkZ + random.nextInt(16));
      mutablePos.move(Direction.DOWN);
      BlockState downState = level.getBlockState(mutablePos);
      mutablePos.move(Direction.UP);
      if (Helpers.isBlock(downState, Blocks.BUSH_PLANTABLE_ON) || Helpers.isBlock(downState, Blocks.SEA_BUSH_PLANTABLE_ON)) {
         net.dries007.tfc.world.feature.tree.ForestConfig.Entry entry = this.getTree(data, random, config, mutablePos);
         if (entry != null) {
            int fallChance = entry.fallenChance();
            if (fallChance > 0 && level.getRandom().nextInt(fallChance) == 0) {
               BlockState log = (BlockState)entry.fallenLog().orElse(null);
               if (log != null) {
                  Direction axis = Plane.HORIZONTAL.getRandomDirection(random);
                  log = Helpers.setProperty(log, TFCFBlockStateProperties.NATURAL, false);
                  log = Helpers.setProperty(log, BlockStateProperties.AXIS, axis.getAxis());
                  int length = 4 + random.nextInt(10);
                  BlockPos start = mutablePos.immutable();
                  boolean[] moment = new boolean[length];
                  mutablePos.set(start);

                  int valid;
                  for (valid = 0; valid < length; valid++) {
                     BlockState replaceState = level.getBlockState(mutablePos);
                     if (!EnvironmentHelpers.isWorldgenReplaceable(replaceState) && !(replaceState.getBlock() instanceof ILeavesBlock)) {
                        break;
                     }

                     mutablePos.move(Direction.DOWN);
                     moment[valid] = level.getBlockState(mutablePos).isFaceSturdy(level, mutablePos, Direction.UP);
                     mutablePos.move(Direction.UP);
                     mutablePos.move(axis);
                  }

                  int left = 0;
                  int right = valid - 1;

                  while (left < moment.length && !moment[left]) {
                     left++;
                  }

                  while (right >= 0 && !moment[right]) {
                     right--;
                  }

                  if (left <= valid / 2 && right >= valid / 2 && valid >= 3) {
                     mutablePos.set(start);

                     for (int i = 0; i < length; i++) {
                        level.setBlock(mutablePos, log, 2);
                        mutablePos.move(axis);
                     }
                  }
               }
            }
         }
      }
   }

   @Nullable
   private net.dries007.tfc.world.feature.tree.ForestConfig.Entry getTree(ChunkData chunkData, RandomSource random, ForestConfig config, BlockPos pos) {
      List<net.dries007.tfc.world.feature.tree.ForestConfig.Entry> entries = new ArrayList<>(4);
      float rainfall = chunkData.getRainfall(pos);
      float averageTemperature = chunkData.getAverageTemp(pos);
      config.entries()
         .stream()
         .map(configuredFeature -> ((ConfiguredFeature)configuredFeature.value()).config())
         .map(cfg -> (net.dries007.tfc.world.feature.tree.ForestConfig.Entry)cfg)
         .forEach(entry -> {
            float lastRain = entry.getAverageRain();
            float lastTemp = entry.getAverageTemp();
            if (entry.isValid(averageTemperature, rainfall)) {
               if (entry.distanceFromMean(lastTemp, lastRain) < entry.distanceFromMean(averageTemperature, rainfall)) {
                  entries.add(entry);
               } else {
                  entries.add(0, entry);
               }
            }
         });
      if (entries.isEmpty()) {
         return null;
      } else {
         if (config.useWeirdness()) {
            float weirdness = chunkData.getForestWeirdness();
            Collections.rotate(entries, -((int)(weirdness * ((float)entries.size() - 1.0F))));

            for (int i = 1; i >= -1 && entries.size() > 1; i--) {
               if (random.nextFloat() > weirdness - 0.15F * (float)i + 0.1F) {
                  entries.remove(entries.size() - 1);
               }
            }
         }

         int index = 0;

         while (index < entries.size() - 1 && random.nextFloat() < 0.6F) {
            index++;
         }

         return entries.get(index);
      }
   }

   public static class Entry extends Feature<net.dries007.tfc.world.feature.tree.ForestConfig.Entry> {
      public Entry(Codec<net.dries007.tfc.world.feature.tree.ForestConfig.Entry> codec) {
         super(codec);
      }

      public boolean place(FeaturePlaceContext<net.dries007.tfc.world.feature.tree.ForestConfig.Entry> context) {
         throw new IllegalArgumentException("This is not a real feature and should never be placed!");
      }
   }
}
