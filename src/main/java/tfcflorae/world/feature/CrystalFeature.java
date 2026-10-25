package tfcflorae.world.feature;
import net.minecraft.world.level.material.MapColor;

import com.mojang.serialization.Codec;
import net.minecraft.util.RandomSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;

public class CrystalFeature extends Feature<BlockStateConfiguration> {
   public CrystalFeature(Codec<BlockStateConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<BlockStateConfiguration> context) {
      WorldGenLevel worldgenlevel = context.level();
      BlockPos blockpos = context.origin();
      RandomSource random = context.random();
      BlockState state = ((BlockStateConfiguration)context.config()).state;
      if (!worldgenlevel.isStateAtPosition(blockpos, net.minecraft.world.level.block.state.BlockState::isAir)) {
         return false;
      } else {
         BlockState blockstate = worldgenlevel.getBlockState(blockpos.above());
         if (blockstate.getBlock().defaultBlockState().isSolid() && blockstate == state) {
            worldgenlevel.setBlock(blockpos, state, 2);

            for (int i = 0; i < 1500; i++) {
               BlockPos blockpos1 = blockpos.offset(random.nextInt(8) - random.nextInt(8), -random.nextInt(12), random.nextInt(8) - random.nextInt(8));
               if (worldgenlevel.isStateAtPosition(blockpos1, net.minecraft.world.level.block.state.BlockState::isAir)) {
                  int j = 0;

                  for (Direction direction : Direction.values()) {
                     if (worldgenlevel.getBlockState(blockpos1.relative(direction)) == state) {
                        j++;
                     }

                     if (j > 1) {
                        break;
                     }
                  }

                  if (j == 1) {
                     worldgenlevel.setBlock(blockpos1, state, 2);
                  }
               }
            }

            return true;
         } else {
            return false;
         }
      }
   }
}
