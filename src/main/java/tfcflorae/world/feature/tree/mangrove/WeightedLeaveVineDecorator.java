package tfcflorae.world.feature.tree.mangrove;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.util.RandomSource;
import java.util.function.BiConsumer;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.plant.Plant;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraftforge.registries.RegistryObject;
import tfcflorae.world.feature.TFCFFeatures;

public class WeightedLeaveVineDecorator extends TreeDecorator {
   public static final Codec<WeightedLeaveVineDecorator> CODEC = Codec.floatRange(0.0F, 1.0F)
      .fieldOf("probability")
      .xmap(WeightedLeaveVineDecorator::new, decorator -> decorator.probability)
      .codec();
   private final float probability;

   public WeightedLeaveVineDecorator(float probability) {
      this.probability = probability;
   }

   public void place(TreeDecorator.Context context) {
      final LevelSimulatedReader level = context.level();
      final BiConsumer<BlockPos, BlockState> replacer = context::setBlock;
      final RandomSource random = context.random();
      final List<BlockPos> trunkPositions = context.logs();
      final List<BlockPos> foliagePositions = context.leaves();
      foliagePositions.forEach(pos -> {
         if (random.nextFloat() < this.probability && level.isStateAtPosition(pos.west(), net.minecraft.world.level.block.state.BlockState::isAir)) {
            addHangingVine(level, pos.west(), VineBlock.EAST, replacer);
         }

         if (random.nextFloat() < this.probability && level.isStateAtPosition(pos.east(), net.minecraft.world.level.block.state.BlockState::isAir)) {
            addHangingVine(level, pos.east(), VineBlock.WEST, replacer);
         }

         if (random.nextFloat() < this.probability && level.isStateAtPosition(pos.north(), net.minecraft.world.level.block.state.BlockState::isAir)) {
            addHangingVine(level, pos.north(), VineBlock.SOUTH, replacer);
         }

         if (random.nextFloat() < this.probability && level.isStateAtPosition(pos.south(), net.minecraft.world.level.block.state.BlockState::isAir)) {
            addHangingVine(level, pos.south(), VineBlock.NORTH, replacer);
         }
      });
   }

   private static void addHangingVine(LevelSimulatedReader level, BlockPos pos, BooleanProperty property, BiConsumer<BlockPos, BlockState> replacer) {
      placeVine(replacer, pos, property);
      pos = pos.below();

      for (int i = 4; level.isStateAtPosition(pos, net.minecraft.world.level.block.state.BlockState::isAir) && i > 0; i--) {
         placeVine(replacer, pos, property);
         pos = pos.below();
      }
   }

   protected TreeDecoratorType<?> type() {
      return TFCFFeatures.WEIGHTED_LEAVE_VINE.get();
   }

   protected static void placeVine(BiConsumer<BlockPos, BlockState> blockSetter, BlockPos pos, BooleanProperty sideProperty) {
      blockSetter.accept(pos, (BlockState)((Block)((RegistryObject)TFCBlocks.PLANTS.get(Plant.JUNGLE_VINES)).get()).defaultBlockState().setValue(sideProperty, true));
   }
}
