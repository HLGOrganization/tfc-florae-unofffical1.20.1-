package tfcflorae.world.feature.tree.mangrove;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalInt;
import net.minecraft.util.RandomSource;
import java.util.Set;
import java.util.function.BiConsumer;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.util.EnvironmentHelpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer.FoliageAttachment;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import tfcflorae.world.feature.tree.RootedTreeConfig;

public class RootedTreeFeature extends Feature<RootedTreeConfig> {
   public RootedTreeFeature(Codec<RootedTreeConfig> codec) {
      super(codec);
   }

   public boolean generate(
      WorldGenLevel level,
      RandomSource random,
      BlockPos pos,
      BiConsumer<BlockPos, BlockState> rootPlacerReplacer,
      BiConsumer<BlockPos, BlockState> trunkPlacerReplacer,
      BiConsumer<BlockPos, BlockState> foliagePlacerReplacer,
      RootedTreeConfig config
   ) {
      int treeHeight = config.trunkPlacer.getTreeHeight(random);
      int foliageHeight = config.foliagePlacer.foliageHeight(random, treeHeight, config);
      int trunkLength = treeHeight - foliageHeight;
      int foliageRadius = config.foliagePlacer.foliageRadius(random, trunkLength);
      BlockPos origin = config.rootPlacer.trunkOffset(pos, random);
      int minHeight = Math.min(pos.getY(), origin.getY());
      int maxHeight = Math.max(pos.getY(), origin.getY()) + treeHeight + 1;
      if (minHeight >= level.getMinBuildHeight() + 1 && maxHeight <= level.getMaxBuildHeight()) {
         OptionalInt clippedHeight = config.minimumSize.minClippedHeight();
         int topPosition = this.getTopPosition(level, treeHeight, origin, config);
         if (topPosition >= treeHeight || clippedHeight.isPresent() && topPosition >= clippedHeight.getAsInt()) {
            if (!config.rootPlacer.generate(level, rootPlacerReplacer, random, pos, origin, config)) {
               return false;
            } else {
               List<FoliageAttachment> foliage = config.trunkPlacer.placeTrunk(level, trunkPlacerReplacer, random, topPosition, origin, config);
               FoliagePlacer.FoliageSetter foliageSetter = new ReplacerFoliageSetter(foliagePlacerReplacer);
               foliage.forEach(node -> config.foliagePlacer.createFoliage(level, foliageSetter, random, config, topPosition, node, foliageHeight, foliageRadius));
               return true;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private int getTopPosition(LevelSimulatedReader level, int height, BlockPos pos, RootedTreeConfig config) {
      MutableBlockPos mutable = new MutableBlockPos();

      for (int y = 0; y <= height + 1; y++) {
         int size = config.minimumSize.getSizeAtHeight(height, y);

         for (int x = -size; x <= size; x++) {
            for (int z = -size; z <= size; z++) {
               boolean var10000;
               label71: {
                  mutable.setWithOffset(pos, x, y, z);
                  label50:
                  if (!TreeFeature.validTreePos(level, pos)
                     && !level.isStateAtPosition(pos, state -> isEmptyOrWater(state))
                     && !level.isStateAtPosition(pos, state -> state.is(BlockTags.LOGS))) {
                     if (config.trunkPlacer instanceof UpwardBranchingTrunk trunk
                        && (level.isStateAtPosition(pos, state -> state.is(trunk.canGrowThrough)) || level.isStateAtPosition(pos, state -> isEmptyOrWater(state)))) {
                        break label50;
                     }

                     var10000 = false;
                     break label71;
                  }

                  var10000 = true;
               }

               boolean isValid = var10000;
               if (!isValid || !config.ignoreVines && level.isStateAtPosition(mutable, RootedTreeFeature::isVine)) {
                  return y - 2;
               }
            }
         }
      }

      return height;
   }

   private static boolean isVine(BlockState state) {
      return state.is(Blocks.VINE) || state.is(Blocks.CAVE_VINES) || state.is(Blocks.CAVE_VINES_PLANT);
   }

   /**
    * 1.20.1 的 {@link FoliagePlacer.FoliageSetter} 不再是函数式接口（set / isSet 两个方法），
    * 因此用一个包装器把旧的 BiConsumer 适配过去，方法体逻辑零改动。
    */
   private static final class ReplacerFoliageSetter implements FoliagePlacer.FoliageSetter {
      private final BiConsumer<BlockPos, BlockState> replacer;
      private final Set<BlockPos> set;

      private ReplacerFoliageSetter(BiConsumer<BlockPos, BlockState> replacer) {
         this.replacer = replacer;
         this.set = new HashSet<>();
      }

      @Override
      public void set(BlockPos pos, BlockState state) {
         this.set.add(pos.immutable());
         this.replacer.accept(pos, state);
      }

      @Override
      public boolean isSet(BlockPos pos) {
         return this.set.contains(pos);
      }
   }

   public static boolean isEmptyOrWater(BlockState state) {
      return EnvironmentHelpers.isWorldgenReplaceable(state)
         || state.is(Blocks.WATER)
         || state.is((Block)TFCBlocks.SALT_WATER.get())
         || EnvironmentHelpers.isWater(state)
         || FluidHelpers.isAirOrEmptyFluid(state);
   }

   public final boolean place(FeaturePlaceContext<RootedTreeConfig> context) {
      WorldGenLevel level = context.level();
      RandomSource random = context.random();
      BlockPos pos = context.origin();
      RootedTreeConfig config = (RootedTreeConfig)context.config();
      HashSet<BlockPos> rootPos = Sets.newHashSet();
      HashSet<BlockPos> trunkPos = Sets.newHashSet();
      HashSet<BlockPos> foliagePos = Sets.newHashSet();
      HashSet<BlockPos> decoratorPos = Sets.newHashSet();
      BiConsumer<BlockPos, BlockState> rootReplacer = (position, state) -> {
         rootPos.add(position.immutable());
         level.setBlock(position, state, 19);
      };
      BiConsumer<BlockPos, BlockState> trunkReplacer = (position, state) -> {
         trunkPos.add(position.immutable());
         level.setBlock(position, state, 19);
      };
      BiConsumer<BlockPos, BlockState> foliageReplacer = (position, state) -> {
         foliagePos.add(position.immutable());
         level.setBlock(position, state, 19);
      };
      BiConsumer<BlockPos, BlockState> decoratorReplacer = (position, state) -> {
         decoratorPos.add(position.immutable());
         level.setBlock(position, state, 19);
      };
      boolean generate = this.generate(level, random, pos, rootReplacer, trunkReplacer, foliageReplacer, config);
      if (generate && (!trunkPos.isEmpty() || !foliagePos.isEmpty())) {
         if (!config.decorators.isEmpty()) {
            ArrayList<BlockPos> rootPositions = Lists.newArrayList(rootPos);
            ArrayList<BlockPos> trunkPositions = Lists.newArrayList(trunkPos);
            ArrayList<BlockPos> foliagePositions = Lists.newArrayList(foliagePos);
            trunkPositions.sort(Comparator.comparingInt(Vec3i::getY));
            foliagePositions.sort(Comparator.comparingInt(Vec3i::getY));
            rootPositions.sort(Comparator.comparingInt(Vec3i::getY));
            TreeDecorator.Context decoratorContext = new TreeDecorator.Context(
               level,
               decoratorReplacer,
               random,
               Sets.newHashSet(trunkPositions),
               Sets.newHashSet(foliagePositions),
               Sets.newHashSet(rootPositions)
            );
            config.decorators.forEach(treeDecorator -> treeDecorator.place(decoratorContext));
         }

         return BoundingBox.encapsulatingPositions(Iterables.concat(trunkPos, foliagePos, decoratorPos)).map(box -> {
            DiscreteVoxelShape shape = placeLogsAndLeaves(level, box, trunkPos, decoratorPos);
            StructureTemplate.updateShapeAtEdge(level, 3, shape, box.minX(), box.minY(), box.minZ());
            return true;
         }).orElse(false);
      } else {
         return false;
      }
   }

   private static DiscreteVoxelShape placeLogsAndLeaves(LevelAccessor level, BoundingBox box, Set<BlockPos> trunkPositions, Set<BlockPos> decoratorPositions) {
      ArrayList<Set<BlockPos>> positions = Lists.newArrayList();
      BitSetDiscreteVoxelShape shape = new BitSetDiscreteVoxelShape(box.getXSpan(), box.getYSpan(), box.getZSpan());

      for (int tries = 0; tries < 6; tries++) {
         positions.add(Sets.newHashSet());
      }

      MutableBlockPos mutable = new MutableBlockPos();

      for (BlockPos pos : Lists.newArrayList(decoratorPositions)) {
         if (box.isInside(pos)) {
            shape.fill(pos.getX() - box.minX(), pos.getY() - box.minY(), pos.getZ() - box.minZ());
         }
      }

      for (BlockPos posx : Lists.newArrayList(trunkPositions)) {
         if (box.isInside(posx)) {
            shape.fill(posx.getX() - box.minX(), posx.getY() - box.minY(), posx.getZ() - box.minZ());
         }

         for (Direction direction : Direction.values()) {
            mutable.setWithOffset(posx, direction);
            BlockState blockState;
            if (!trunkPositions.contains(mutable) && (blockState = level.getBlockState(mutable)).hasProperty(BlockStateProperties.DISTANCE)) {
               positions.get(0).add(mutable.immutable());
               level.setBlock(mutable, (BlockState)blockState.setValue(BlockStateProperties.DISTANCE, 1), 3);
               if (box.isInside(mutable)) {
                  shape.fill(mutable.getX() - box.minX(), mutable.getY() - box.minY(), mutable.getZ() - box.minZ());
               }
            }
         }
      }

      for (int tries = 1; tries < 6; tries++) {
         Set<BlockPos> trunkPos = positions.get(tries - 1);
         Set<BlockPos> foliagePos = positions.get(tries);

         for (BlockPos posx : trunkPos) {
            if (box.isInside(posx)) {
               shape.fill(posx.getX() - box.minX(), posx.getY() - box.minY(), posx.getZ() - box.minZ());
            }

            for (Direction directionx : Direction.values()) {
               mutable.setWithOffset(posx, directionx);
               BlockState state = level.getBlockState(mutable);
               if (!trunkPos.contains(mutable)
                  && !foliagePos.contains(mutable)
                  && state.hasProperty(BlockStateProperties.DISTANCE)
                  && (Integer)state.getValue(BlockStateProperties.DISTANCE) > tries + 1) {
                  BlockState foliage = (BlockState)state.setValue(BlockStateProperties.DISTANCE, tries + 1);
                  level.setBlock(mutable, foliage, 3);
                  if (box.isInside(mutable)) {
                     shape.fill(mutable.getX() - box.minX(), mutable.getY() - box.minY(), mutable.getZ() - box.minZ());
                  }

                  foliagePos.add(mutable.immutable());
               }
            }
         }
      }

      return shape;
   }
}
