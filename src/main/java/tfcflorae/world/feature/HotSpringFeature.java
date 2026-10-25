package tfcflorae.world.feature;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.RandomSource;
import java.util.Set;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.collections.IWeighted;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
import net.dries007.tfc.world.feature.HotSpringConfig;
import net.dries007.tfc.world.feature.FissureConfig.Decoration;
import net.dries007.tfc.world.noise.Metaballs2D;
import net.dries007.tfc.world.settings.RockSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Plane;
import net.minecraft.util.Mth;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class HotSpringFeature extends Feature<HotSpringConfig> {
   public HotSpringFeature(Codec<HotSpringConfig> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<HotSpringConfig> context) {
      WorldGenLevel level = context.level();
      BlockPos pos = context.origin();
      RandomSource random = context.random();
      HotSpringConfig config = (HotSpringConfig)context.config();
      Metaballs2D noise = Metaballs2D.simple(Helpers.fork(random), config.radius());
      MutableBlockPos mutablePos = new MutableBlockPos();
      ChunkDataProvider provider = ChunkDataProvider.get(context.chunkGenerator());
      ChunkData data = provider.get(context.level(), pos);
      RockSettings rock = data.getRockData().getRock(pos.getX(), 0, pos.getZ());
      Block rawBlock = rock.raw();
      BlockState rockState = rawBlock.defaultBlockState();
      BlockState gravelState = rock.gravel().defaultBlockState();
      Fluid fluid = config.fluidState().getFluidState().getType();
      boolean useFilledEmptyCheck = config.fluidState().isAir();
      Set<BlockPos> filledEmptyPositions = new HashSet<>();
      List<BlockPos> fissureStartPositions = new ArrayList<>();
      Optional<Map<Block, IWeighted<BlockState>>> replacers = config.replacesOnFluidContact();
      IWeighted<BlockState> magma = replacers.<IWeighted<BlockState>>map(
            map -> map.getOrDefault(rawBlock, (IWeighted<BlockState>)Helpers.getRandomValue(map, random))
         )
         .orElse(null);
      boolean touchedWater = false;

      for (int x = -config.radius(); x <= config.radius(); x++) {
         for (int z = -config.radius(); z <= config.radius(); z++) {
            int localX = pos.getX() + x;
            int localZ = pos.getZ() + z;
            int y = pos.getY();
            if ((config.allowUnderwater() || y > 63) && noise.inside((float)x, (float)z)) {
               mutablePos.set(localX, y + 1, localZ);
               BlockState stateAbove = level.getBlockState(mutablePos);
               if (!isEmptyBlock(config, stateAbove)) {
                  if (!stateAbove.canBeReplaced()) {
                     continue;
                  }

                  this.setBlock(level, mutablePos, stateAbove.getFluidState().createLegacyBlock());
                  mutablePos.move(0, 1, 0);
                  level.scheduleTick(mutablePos, level.getBlockState(mutablePos).getBlock(), 1);
               }

               boolean edge = false;

               for (Direction direction : Plane.HORIZONTAL) {
                  mutablePos.set(localX, y, localZ).move(direction);
                  BlockState stateAt = level.getBlockState(mutablePos);
                  if (stateAt.liquid()) {
                     touchedWater = true;
                  }

                  if (isEmptyBlock(config, stateAt) && (!useFilledEmptyCheck || !filledEmptyPositions.contains(mutablePos))) {
                     edge = true;
                     break;
                  }
               }

               float centerFactor = 1.0F - 0.7F * Mth.clamp((float)(2 * (x * x + z * z)) / (float)(config.radius() * config.radius()), 0.0F, 1.0F);
               int surfaceDepth = (int)((float)(8 + random.nextInt(3)) * centerFactor);
               if (edge) {
                  int startY = random.nextInt(12) == 0 ? -1 : 0;
                  if (startY == -1) {
                     mutablePos.set(localX, y, localZ);
                     this.placeWaterOrAir(level, mutablePos);
                  }
               } else {
                  mutablePos.set(localX, y, localZ);
                  BlockPos posAt = mutablePos.immutable();
                  fissureStartPositions.add(posAt);
                  BlockState toPlace = touchedWater && config.allowUnderwater() && magma != null ? (BlockState)magma.get(random) : config.fluidState();
                  this.setBlock(level, mutablePos, toPlace);
                  if (fluid != Fluids.EMPTY) {
                     level.scheduleTick(mutablePos, fluid, 0);
                  }

                  if (touchedWater) {
                     level.scheduleTick(mutablePos, toPlace.getBlock(), 20);
                  }

                  if (useFilledEmptyCheck) {
                     filledEmptyPositions.add(posAt);
                  }

                  mutablePos.set(localX, y - 1, localZ);
                  this.setFissureBaseBlock(config, level, mutablePos, gravelState);
               }

               for (int dy = edge ? 0 : -2; dy >= -surfaceDepth; dy--) {
                  mutablePos.set(localX, y + dy, localZ);
                  if (!this.setFissureBaseBlock(config, level, mutablePos, rockState)) {
                     break;
                  }
               }
            }
         }
      }

      if (fissureStartPositions.isEmpty()) {
         return false;
      } else {
         int fissureStarts = 1 + random.nextInt(1 + random.nextInt(Mth.clamp(fissureStartPositions.size(), 1, 7)));

         for (BlockPos start : Helpers.uniqueRandomSample(fissureStartPositions, fissureStarts, random)) {
            net.dries007.tfc.world.feature.FissureFeature.placeFissure(
               level, start, pos, mutablePos, random, config.fluidState(), rockState, 10, 22, 6, 16, 12, (Decoration)config.decoration().orElse(null)
            );
         }

         return true;
      }
   }

   private boolean setFissureBaseBlock(HotSpringConfig config, WorldGenLevel level, BlockPos pos, BlockState state) {
      BlockState stateAt = level.getBlockState(pos);
      if (isEmptyBlock(config, stateAt)) {
         return false;
      } else {
         level.setBlock(pos, state, 2);
         return true;
      }
   }

   private static boolean isEmptyBlock(HotSpringConfig config, BlockState state) {
      return config.allowUnderwater() ? FluidHelpers.isAirOrEmptyFluid(state) : state.isAir();
   }

   public void placeWaterOrAir(WorldGenLevel level, BlockPos pos) {
      for (Direction direction : Helpers.DIRECTIONS) {
         FluidState fluidState = level.getFluidState(pos.relative(direction));
         if (fluidState.getType() != Fluids.EMPTY) {
            this.setBlock(level, pos, fluidState.createLegacyBlock());
            this.markAboveForPostProcessing(level, pos);
            return;
         }
      }

      this.setBlock(level, pos, Blocks.AIR.defaultBlockState());
      this.markAboveForPostProcessing(level, pos);
   }
}
