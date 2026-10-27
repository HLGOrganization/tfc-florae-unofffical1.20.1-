package tfcflorae.world.feature.tree.mangrove;
import net.minecraft.core.registries.Registries;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.util.RandomSource;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction.Plane;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer.FoliageAttachment;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import tfcflorae.world.feature.TFCFFeatures;

public class UpwardBranchingTrunk extends TrunkPlacer {
   public static final Codec<UpwardBranchingTrunk> CODEC = RecordCodecBuilder.create(
      instance -> trunkPlacerParts(instance)
            .and(
               instance.group(
                  IntProvider.POSITIVE_CODEC.fieldOf("extra_branch_steps").forGetter(placer -> placer.extraBranchSteps),
                  Codec.floatRange(0.0F, 1.0F).fieldOf("place_branch_per_log_probability").forGetter(placer -> placer.placeBranchPerLogProbability),
                  IntProvider.NON_NEGATIVE_CODEC.fieldOf("extra_branch_length").forGetter(placer -> placer.extraBranchLength),
                  RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("can_grow_through").forGetter(placer -> placer.canGrowThrough)
               )
            )
            .apply(instance, UpwardBranchingTrunk::new)
   );
   public final HolderSet<Block> canGrowThrough;
   private final IntProvider extraBranchSteps;
   private final float placeBranchPerLogProbability;
   private final IntProvider extraBranchLength;

   public UpwardBranchingTrunk(
      int baseHeight,
      int firstRandomHeight,
      int secondRandomHeight,
      IntProvider extraBranchSteps,
      float placeBranchPerLogProbability,
      IntProvider extraBranchLength,
      HolderSet<Block> canGrowThrough
   ) {
      super(baseHeight, firstRandomHeight, secondRandomHeight);
      this.extraBranchSteps = extraBranchSteps;
      this.placeBranchPerLogProbability = placeBranchPerLogProbability;
      this.extraBranchLength = extraBranchLength;
      this.canGrowThrough = canGrowThrough;
   }

   public List<FoliageAttachment> placeTrunk(
      LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> replacer, RandomSource random, int height, BlockPos startPos, TreeConfiguration config
   ) {
      List<FoliageAttachment> attachments = Lists.newArrayList();
      MutableBlockPos mutable = new MutableBlockPos();

      for (int i = 0; i < height; i++) {
         int yOffset = startPos.getY() + i;
         if (placeLog(level, replacer, random, mutable.set(startPos.getX(), yOffset, startPos.getZ()), config)
            && i < height - 1
            && random.nextFloat() < this.placeBranchPerLogProbability) {
            Direction direction = Plane.HORIZONTAL.getRandomDirection(random);
            int offset = this.extraBranchLength.sample(random);
            int length = Math.max(0, offset - this.extraBranchLength.sample(random) - 1);
            int steps = this.extraBranchSteps.sample(random);
            this.placeBranch(level, replacer, random, height, config, attachments, mutable, yOffset, direction, length, steps);
         }

         if (i == height - 1) {
            attachments.add(new FoliageAttachment(mutable.set(startPos.getX(), yOffset + 1, startPos.getZ()), 0, false));
         }
      }

      return attachments;
   }

   private void placeBranch(
      LevelSimulatedReader level,
      BiConsumer<BlockPos, BlockState> replacer,
      RandomSource random,
      int height,
      TreeConfiguration config,
      List<FoliageAttachment> attachments,
      MutableBlockPos mutable,
      int yOffset,
      Direction direction,
      int length,
      int steps
   ) {
      int y = yOffset + length;
      int x = mutable.getX();
      int z = mutable.getZ();
      int l = length;

      while (l < height && steps > 0) {
         if (l >= 1) {
            int offset = yOffset + l;
            x += direction.getStepX();
            z += direction.getStepZ();
            y = offset;
            if (placeLog(level, replacer, random, mutable.set(x, offset, z), config)) {
               y = offset + 1;
            }

            attachments.add(new FoliageAttachment(mutable.immutable(), 0, false));
         }

         l++;
         steps--;
      }

      if (y - yOffset > 1) {
         BlockPos pos = BlockPos.containing(x, y, z);
         attachments.add(new FoliageAttachment(pos, 0, false));
         attachments.add(new FoliageAttachment(pos.below(2), 0, false));
      }
   }

   protected TrunkPlacerType<?> type() {
      return TFCFFeatures.UPWARDS_BRANCHING_TRUNK.get();
   }
}
