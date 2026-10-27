package tfcflorae.world.feature.tree.mangrove;

import com.google.common.collect.Lists;
import com.mojang.datafixers.Products.P3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.RandomSource;
import java.util.function.BiConsumer;
import net.dries007.tfc.common.TFCTags.Blocks;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Plane;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import tfcflorae.world.feature.tree.RootedTreeConfig;

public class MangroveRootPlacer {
   public static final Codec<MangroveRootPlacer> CODEC = RecordCodecBuilder.create(
      instance -> codec(instance)
            .and(MangroveRootPlacement.CODEC.fieldOf("mangrove_root_placement").forGetter(placer -> placer.mangroveRootPlacement))
            .apply(instance, MangroveRootPlacer::new)
   );
   protected final IntProvider trunkOffsetY;
   protected final BlockStateProvider rootProvider;
   protected final Optional<AboveRootPlacement> aboveRootPlacement;
   private final MangroveRootPlacement mangroveRootPlacement;

   protected static <P extends MangroveRootPlacer> P3<Mu<P>, IntProvider, BlockStateProvider, Optional<AboveRootPlacement>> codec(Instance<P> instance) {
      return instance.group(
         IntProvider.CODEC.fieldOf("trunk_offset_y").forGetter(placer -> placer.trunkOffsetY),
         BlockStateProvider.CODEC.fieldOf("root_provider").forGetter(placer -> placer.rootProvider),
         AboveRootPlacement.CODEC.optionalFieldOf("above_root_placement").forGetter(placer -> placer.aboveRootPlacement)
      );
   }

   public MangroveRootPlacer(
      IntProvider trunkOffsetY, BlockStateProvider rootProvider, Optional<AboveRootPlacement> aboveRootPlacement, MangroveRootPlacement mangroveRootPlacement
   ) {
      this.trunkOffsetY = trunkOffsetY;
      this.rootProvider = rootProvider;
      this.aboveRootPlacement = aboveRootPlacement;
      this.mangroveRootPlacement = mangroveRootPlacement;
   }

   public boolean generate(
      LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> replacer, RandomSource random, BlockPos pos, BlockPos origin, RootedTreeConfig config
   ) {
      ArrayList<BlockPos> positions = Lists.newArrayList();
      MutableBlockPos mutable = pos.mutable();

      while (mutable.getY() < origin.getY()) {
         if (!this.canGrowThrough(level, mutable)) {
            return false;
         }

         mutable.move(Direction.UP);
      }

      positions.add(origin.below());

      for (Direction direction : Plane.HORIZONTAL) {
         BlockPos position = origin.relative(direction);
         ArrayList<BlockPos> offshootPositions = Lists.newArrayList();
         if (!this.canGrow(level, random, position, direction, origin, offshootPositions, 0)) {
            return false;
         }

         positions.addAll(offshootPositions);
         positions.add(origin.relative(direction));
      }

      for (BlockPos position : positions) {
         this.placeRoots(level, replacer, random, position, config);
      }

      return true;
   }

   protected boolean canGrowThrough(LevelSimulatedReader level, BlockPos pos) {
      return TreeFeature.validTreePos(level, pos)
         || level.isStateAtPosition(pos, FluidHelpers::isAirOrEmptyFluid)
         || level.isStateAtPosition(pos, state -> state.is(Blocks.SINGLE_BLOCK_REPLACEABLE))
         || level.isStateAtPosition(pos, state -> state.is(this.mangroveRootPlacement.canGrowThrough()))
         || level.isStateAtPosition(pos, state -> state.is((Block)TFCBlocks.SALT_WATER.get()))
         || level.isStateAtPosition(pos, state -> state.is((Block)TFCBlocks.RIVER_WATER.get()))
         || level.isStateAtPosition(pos, state -> state.is((Block)TFCBlocks.SPRING_WATER.get()));
   }

   private boolean canGrow(
      LevelSimulatedReader level, RandomSource random, BlockPos pos, Direction direction, BlockPos origin, List<BlockPos> offshootPositions, int rootLength
   ) {
      int length = this.mangroveRootPlacement.maxRootLength();
      if (rootLength != length && offshootPositions.size() <= length) {
         for (BlockPos position : this.getOffshootPositions(pos, direction, random, origin)) {
            if (this.canGrowThrough(level, position)) {
               offshootPositions.add(position);
               if (!this.canGrow(level, random, position, direction, origin, offshootPositions, rootLength + 1)) {
                  return false;
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   protected List<BlockPos> getOffshootPositions(BlockPos pos, Direction direction, RandomSource random, BlockPos origin) {
      BlockPos below = pos.below();
      BlockPos offset = pos.relative(direction);
      int distance = pos.distManhattan(origin);
      int rootWidth = this.mangroveRootPlacement.maxRootWidth();
      float chance = this.mangroveRootPlacement.randomSkewChance();
      if (distance > rootWidth - 3 && distance <= rootWidth) {
         return random.nextFloat() < chance ? List.of(below, offset.below()) : List.of(below);
      } else if (distance > rootWidth) {
         return List.of(below);
      } else if (random.nextFloat() < chance) {
         return List.of(below);
      } else {
         return random.nextBoolean() ? List.of(offset) : List.of(below);
      }
   }

   protected void placeRoots(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> replacer, RandomSource random, BlockPos pos, RootedTreeConfig config) {
      if (level.isStateAtPosition(pos, state -> state.is(this.mangroveRootPlacement.muddyRootsIn()))) {
         BlockState state = this.mangroveRootPlacement.muddyRootsProvider().getState(random, pos);
         replacer.accept(pos, this.applyWaterlogging(level, pos, state));
      } else if (this.canGrowThrough(level, pos)) {
         replacer.accept(pos, this.applyWaterlogging(level, pos, this.rootProvider.getState(random, pos)));
         if (this.aboveRootPlacement.isPresent()) {
            AboveRootPlacement placement = this.aboveRootPlacement.get();
            BlockPos above = pos.above();
            if (random.nextFloat() < placement.aboveRootPlacementChance() && level.isStateAtPosition(above, net.minecraft.world.level.block.state.BlockState::isAir)) {
               replacer.accept(above, this.applyWaterlogging(level, above, placement.aboveRootProvider().getState(random, above)));
            }
         }
      }
   }

   public BlockState applyWaterlogging(LevelSimulatedReader level, BlockPos pos, BlockState state) {
      if (level.isStateAtPosition(pos, MangroveRootPlacer::isFreshWater)) {
         return FluidHelpers.fillWithFluid(state, net.minecraft.world.level.block.Blocks.WATER.defaultBlockState().getFluidState().getType());
      } else {
         return level.isStateAtPosition(pos, MangroveRootPlacer::isSaltWater)
            ? FluidHelpers.fillWithFluid(state, ((LiquidBlock)TFCBlocks.SALT_WATER.get()).defaultBlockState().getFluidState().getType())
            : state;
      }
   }

   public BlockPos trunkOffset(BlockPos pos, RandomSource random) {
      return pos.above(this.trunkOffsetY.sample(random));
   }

   public static boolean isFreshWater(BlockState state) {
      return state.is(net.minecraft.world.level.block.Blocks.WATER);
   }

   public static boolean isSaltWater(BlockState state) {
      return state.is((Block)TFCBlocks.SALT_WATER.get());
   }
}
