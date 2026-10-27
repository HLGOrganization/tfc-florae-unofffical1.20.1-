package tfcflorae.world.feature.tree.mangrove;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.HashSet;
import java.util.List;
import net.minecraft.util.RandomSource;
import java.util.function.BiConsumer;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import tfcflorae.world.feature.TFCFFeatures;

public class AttachedToLeavesDecorator extends TreeDecorator {
   public static final Codec<AttachedToLeavesDecorator> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
               Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter(decorator -> decorator.probability),
               Codec.intRange(0, 16).fieldOf("exclusion_radius_xz").forGetter(decorator -> decorator.exclusionRadiusXZ),
               Codec.intRange(0, 16).fieldOf("exclusion_radius_y").forGetter(decorator -> decorator.exclusionRadiusY),
               BlockStateProvider.CODEC.fieldOf("block_provider").forGetter(decorator -> decorator.blockProvider),
               Codec.intRange(1, 16).fieldOf("required_empty_blocks").forGetter(decorator -> decorator.requiredEmptyBlocks),
               Direction.CODEC.listOf().fieldOf("directions").forGetter(decorator -> decorator.directions)
            )
            .apply(instance, AttachedToLeavesDecorator::new)
   );
   protected final float probability;
   protected final int exclusionRadiusXZ;
   protected final int exclusionRadiusY;
   protected final BlockStateProvider blockProvider;
   protected final int requiredEmptyBlocks;
   protected final List<Direction> directions;

   public AttachedToLeavesDecorator(
      float probability, int exclusionRadiusXZ, int exclusionRadiusY, BlockStateProvider blockProvider, int requiredEmptyBlocks, List<Direction> directions
   ) {
      this.probability = probability;
      this.exclusionRadiusXZ = exclusionRadiusXZ;
      this.exclusionRadiusY = exclusionRadiusY;
      this.blockProvider = blockProvider;
      this.requiredEmptyBlocks = requiredEmptyBlocks;
      this.directions = directions;
   }

   public void place(
      LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> replacer, RandomSource random, List<BlockPos> trunkPositions, List<BlockPos> foliagePositions
   ) {
      HashSet<BlockPos> positions = new HashSet<>();

      for (BlockPos pos : copyShuffled(new ObjectArrayList(foliagePositions), random)) {
         Direction direction = (Direction)Util.getRandom(this.directions, random);
         BlockPos offset = pos.relative(direction);
         if (!positions.contains(offset) && random.nextFloat() < this.probability && this.meetsRequiredEmptyBlocks(level, pos, direction)) {
            BlockPos minRadius = offset.offset(-this.exclusionRadiusXZ, -this.exclusionRadiusY, -this.exclusionRadiusXZ);
            BlockPos maxRadius = offset.offset(this.exclusionRadiusXZ, this.exclusionRadiusY, this.exclusionRadiusXZ);

            for (BlockPos position : BlockPos.betweenClosed(minRadius, maxRadius)) {
               positions.add(position.immutable());
            }

            replacer.accept(offset, this.blockProvider.getState(random, offset));
         }
      }
   }

   private boolean meetsRequiredEmptyBlocks(LevelSimulatedReader level, BlockPos pos, Direction direction) {
      for (int i = 1; i <= this.requiredEmptyBlocks; i++) {
         if (!level.isStateAtPosition(pos.relative(direction, i), net.minecraft.world.level.block.state.BlockState::isAir)) {
            return false;
         }
      }

      return true;
   }

   protected TreeDecoratorType<?> type() {
      return TFCFFeatures.ATTACHED_TO_LEAVES.get();
   }

   public static <T> void shuffle(List<T> entries, RandomSource random) {
      int size = entries.size();

      for (int i = size; i > 1; i--) {
         entries.set(i - 1, entries.set(random.nextInt(i), entries.get(i - 1)));
      }
   }

   public static <T> List<T> copyShuffled(T[] entries, RandomSource random) {
      ObjectArrayList<T> objects = new ObjectArrayList(entries);
      shuffle(objects, random);
      return objects;
   }

   public static <T> List<T> copyShuffled(ObjectArrayList<T> entries, RandomSource random) {
      ObjectArrayList<T> objects = new ObjectArrayList(entries);
      shuffle(objects, random);
      return objects;
   }
}
