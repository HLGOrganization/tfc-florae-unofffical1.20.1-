package tfcflorae.world.feature;
import net.minecraft.world.level.material.MapColor;

import com.mojang.serialization.Codec;
import net.minecraft.util.RandomSource;
import net.dries007.tfc.common.blocks.GroundcoverBlock;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.util.EnvironmentHelpers;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.world.TFCChunkGenerator;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
import net.dries007.tfc.world.surface.SoilSurfaceState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Plane;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import tfcflorae.Config;
import tfcflorae.common.blocks.TFCFBlocks;
import tfcflorae.common.blocks.rock.MossGrowingBoulderBlock;
import tfcflorae.common.blocks.rock.MossSpreadingBoulderBlock;
import tfcflorae.common.blocks.soil.Colors;
import tfcflorae.common.blocks.soil.SandLayerBlock;
import tfcflorae.util.TFCFHelpers;

public class SandLayerFeature extends Feature<NoneFeatureConfiguration> {
   public SandLayerFeature(Codec<NoneFeatureConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
      if (!(Boolean)Config.COMMON.enableSandLayers.get()) {
         return false;
      } else {
         boolean placedAny = false;
         WorldGenLevel level = context.level();
         BlockPos pos = context.origin();
         RandomSource random = context.random();
         MutableBlockPos mutablePos = new MutableBlockPos();

         for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
               int x = pos.getX() + i;
               int z = pos.getZ() + j;
               int y = level.getHeight(Types.OCEAN_FLOOR, x, z);
               mutablePos.set(x, y, z);
               if (context.chunkGenerator() instanceof TFCChunkGenerator chunkGen && y >= 62 && !(level.getBlockState(mutablePos).getBlock() instanceof SandLayerBlock)) {
                  Colors sandColor = TFCFHelpers.getSandColorTFCF(level, mutablePos, (Boolean)Config.COMMON.toggleCheapSandColourCalculations.get());
                  BlockState sandLayer = ((Block)TFCFBlocks.SAND_LAYERS.get(sandColor).get()).defaultBlockState();

                  for (Direction facing : Plane.HORIZONTAL) {
                     if (level.getBlockState(mutablePos.below()).isSolid()
                        && (
                           level.isStateAtPosition(mutablePos.above(), net.minecraft.world.level.block.state.BlockState::isAir)
                              || level.getBlockState(mutablePos.above()).liquid()
                              || DripstoneUtils.isEmptyOrWater(level.getBlockState(mutablePos.above()))
                        )
                        && this.canReplace(level, mutablePos, random, sandLayer)) {
                        boolean isBelowSeaLevel = y < 63;
                        boolean shouldWaterLog = isBelowSeaLevel || level.getBlockState(mutablePos).liquid();
                        boolean canPlaceInLiquidIfBelowSeaLevel = isBelowSeaLevel ? true : !level.getBlockState(mutablePos).liquid();
                        boolean hasSturdyFaceNearby = isBelowSeaLevel
                           ? true
                           : level.getBlockState(mutablePos.relative(facing).below()).isFaceSturdy(level, mutablePos.relative(facing).below(), Direction.UP);
                        if (this.hasNearbySandOrIsDryEnough(level, mutablePos, random, false) && hasSturdyFaceNearby && canPlaceInLiquidIfBelowSeaLevel) {
                           int sandLayerHeight = sandLayerHeight(chunkGen, mutablePos, random, false);
                           if (sandLayerHeight > 0) {
                              sandLayer = FluidHelpers.fillWithFluid(sandLayer, level.getBlockState(mutablePos).getFluidState().getType());
                              SandLayerBlock.placeSandPileStatic(
                                 level, sandLayer, mutablePos, level.getBlockState(mutablePos), sandLayerHeight, false, shouldWaterLog
                              );
                              placedAny = true;
                           }
                        }
                     }
                  }
               }
            }
         }

         return placedAny;
      }
   }

   public static int sandLayerHeight(TFCChunkGenerator chunkGen, BlockPos inputPos, RandomSource random, boolean isShort) {
      BlockPos pos = new BlockPos(inputPos.getX(), inputPos.getY() - 1, inputPos.getZ());
      int y1 = pos.getY();
      ChunkPos chunkPos = new ChunkPos(pos);
      double actualHeight = chunkGen.createHeightFillerForChunk(chunkPos).sampleHeight(pos.getX(), pos.getZ());
      double actualHeightToInt = (double)Mth.floor(actualHeight);
      if (actualHeightToInt < (double)y1 && y1 > 62) {
         return 0;
      } else {
         int sandLayerHeight = 0;
         if (isShort) {
            double adjustedSandLayerHeight = (actualHeight - actualHeightToInt) * 8.0 + random.nextGaussian() * 0.2F;
            sandLayerHeight = Mth.clamp(Mth.floor(Math.pow(0.75, -adjustedSandLayerHeight * 1.125) - 2.5), 0, 7);
         } else {
            double adjustedSandLayerHeight = (actualHeight - actualHeightToInt) * 8.0 + random.nextGaussian() * 0.2F;
            sandLayerHeight = Mth.clamp(Mth.floor(Math.pow(0.75, -adjustedSandLayerHeight * 1.1) - 1.0), 0, 7);
         }

         return y1 > 62 ? sandLayerHeight - 1 : sandLayerHeight;
      }
   }

   public boolean hasNearbySandOrIsDryEnough(WorldGenLevel level, BlockPos pos, RandomSource random, boolean isPicky) {
      boolean isSandy = false;
      if (isPicky) {
         Direction[] var6 = TFCFHelpers.DIRECTIONS_HORIZONTAL;
         int var7 = var6.length;
         byte var8 = 0;
         if (var8 < var7) {
            Direction direction = var6[var8];
            isSandy = Helpers.isBlock(level.getBlockState(pos.relative(direction)).getBlock(), BlockTags.SAND);
            return isSandy || isSandy && this.properRainfall(level, pos, random);
         }
      } else {
         Direction[] var12 = TFCFHelpers.DIRECTIONS;
         int var13 = var12.length;
         byte var14 = 0;
         if (var14 < var13) {
            Direction direction = var12[var14];
            isSandy = Helpers.isBlock(level.getBlockState(pos.relative(direction)).getBlock(), BlockTags.SAND);
            return isSandy || isSandy && this.properRainfall(level, pos, random);
         }
      }

      return false;
   }

   public boolean canReplace(WorldGenLevel level, BlockPos pos, RandomSource random, BlockState sandLayer) {
      BlockState state = level.getBlockState(pos);
      return SandLayerBlock.canPlaceSandPileStatic(level, pos, state, sandLayer)
         || FluidHelpers.isAirOrEmptyFluid(state)
         || EnvironmentHelpers.isWorldgenReplaceable(level, pos)
         || state.getBlock() instanceof GroundcoverBlock
         || state.getBlock() instanceof MossGrowingBoulderBlock
         || state.getBlock() instanceof MossSpreadingBoulderBlock
         || state.getBlock().defaultMapColor() == MapColor.PLANT
         || state.getBlock().defaultMapColor() == MapColor.PLANT;
   }

   public boolean isSmallerThanNew(WorldGenLevel level, BlockPos pos, int height) {
      BlockState state = level.getBlockState(pos);
      return state.getBlock() instanceof SandLayerBlock && (Integer)state.getValue(SandLayerBlock.LAYERS) >= height;
   }

   public boolean properRainfall(WorldGenLevel level, BlockPos pos, RandomSource random) {
      ChunkDataProvider provider = ChunkDataProvider.get(level);
      ChunkData data = provider.get(level, pos);
      float rainfall = data.getRainfall(pos);
      float noise = (float)(SoilSurfaceState.PATCH_NOISE.noise((float)pos.getX(), (float)pos.getZ()));
      float noiseGauss = noise + 2.0F * (float)random.nextGaussian();
      float noiseRainfall = rainfall + 10.0F * (float)random.nextGaussian();
      return noiseRainfall <= 80.0F ? noiseRainfall <= 50.0F || noiseGauss > 0.2F : false;
   }

   public int maxHeightSmallerThanPrevious(WorldGenLevel level, BlockPos pos, Direction facing, int distance, int inputHeight, int maxHeight, int minHeight) {
      BlockState state = level.getBlockState(pos.relative(facing));
      if (distance > 3) {
         return 1;
      } else if (distance > 0 && state.getBlock() instanceof SandLayerBlock && (Integer)state.getValue(SandLayerBlock.LAYERS) >= inputHeight) {
         int heightDifference = (Integer)state.getValue(SandLayerBlock.LAYERS) - inputHeight;
         return Mth.clamp(inputHeight - (heightDifference + 1), minHeight, maxHeight);
      } else {
         return inputHeight;
      }
   }

   public BlockPos getNearestSandBlock(WorldGenLevel level, BlockPos sourcePos, int radius) {
      BlockPos closestPos = null;

      for (int x = sourcePos.getX() - radius; x < sourcePos.getX() + radius; x++) {
         for (int z = sourcePos.getZ() - radius; z < sourcePos.getZ() + radius; z++) {
            BlockPos checkPos = BlockPos.containing(x, sourcePos.getY(), z);
            if (level.getBlockState(checkPos).getBlock() instanceof SandBlock
               && !(level.getBlockState(checkPos).getBlock() instanceof SandLayerBlock)
               && (
                  closestPos == null
                     || TFCFHelpers.distanceToSqr(
                           sourcePos,
                           sourcePos.getX() - checkPos.getX(),
                           sourcePos.getY() - checkPos.getY(),
                           sourcePos.getZ() - checkPos.getZ()
                        )
                        < TFCFHelpers.distanceToSqr(
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
}
