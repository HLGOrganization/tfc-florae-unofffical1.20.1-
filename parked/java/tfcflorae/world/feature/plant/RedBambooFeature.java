package tfcflorae.world.feature.plant;

import com.mojang.serialization.Codec;
import net.minecraft.util.RandomSource;
import net.dries007.tfc.common.blocks.soil.SoilBlockType.Variant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BambooBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BambooLeaves;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import tfcflorae.common.blocks.TFCFBlocks;
import tfcflorae.common.blocks.plant.TFCFPlant;
import tfcflorae.common.blocks.soil.TFCFSoil;

public class RedBambooFeature extends Feature<ProbabilityFeatureConfiguration> {
   private static final BlockState BAMBOO_TRUNK = (BlockState)((BlockState)((BlockState)((Block)TFCFBlocks.PLANTS.get(TFCFPlant.RED_BAMBOO).get())
            .defaultBlockState()
            .setValue(BambooBlock.AGE, 1))
         .setValue(BambooBlock.LEAVES, BambooLeaves.NONE))
      .setValue(BambooBlock.STAGE, 0);
   private static final BlockState BAMBOO_FINAL_LARGE = (BlockState)((BlockState)BAMBOO_TRUNK.setValue(BambooBlock.LEAVES, BambooLeaves.LARGE))
      .setValue(BambooBlock.STAGE, 1);
   private static final BlockState BAMBOO_TOP_LARGE = (BlockState)BAMBOO_TRUNK.setValue(BambooBlock.LEAVES, BambooLeaves.LARGE);
   private static final BlockState BAMBOO_TOP_SMALL = (BlockState)BAMBOO_TRUNK.setValue(BambooBlock.LEAVES, BambooLeaves.SMALL);

   public RedBambooFeature(Codec<ProbabilityFeatureConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<ProbabilityFeatureConfiguration> context) {
      int i = 0;
      BlockPos blockpos = context.origin();
      WorldGenLevel worldgenlevel = context.level();
      Random random = context.random();
      ProbabilityFeatureConfiguration probabilityfeatureconfiguration = (ProbabilityFeatureConfiguration)context.config();
      MutableBlockPos blockpos$mutableblockpos = blockpos.mutable();
      MutableBlockPos blockpos$mutableblockpos1 = blockpos.mutable();
      if (worldgenlevel.isEmptyBlock(blockpos$mutableblockpos)) {
         if (((Block)TFCFBlocks.PLANTS.get(TFCFPlant.RED_BAMBOO).get()).defaultBlockState().canSurvive(worldgenlevel, blockpos$mutableblockpos)) {
            int j = random.nextInt(12) + 5;
            if (random.nextFloat() < probabilityfeatureconfiguration.probability) {
               int k = random.nextInt(4) + 1;

               for (int l = blockpos.getX() - k; l <= blockpos.getX() + k; l++) {
                  for (int i1 = blockpos.getZ() - k; i1 <= blockpos.getZ() + k; i1++) {
                     int j1 = l - blockpos.getX();
                     int k1 = i1 - blockpos.getZ();
                     if (j1 * j1 + k1 * k1 <= k * k) {
                        blockpos$mutableblockpos1.set(l, worldgenlevel.getHeight(Types.WORLD_SURFACE, l, i1) - 1, i1);
                        if (isDirt(worldgenlevel.getBlockState(blockpos$mutableblockpos1))) {
                           worldgenlevel.setBlock(
                              blockpos$mutableblockpos1, ((Block)TFCFBlocks.TFCSOIL.get(TFCFSoil.PODZOL).get(Variant.LOAM).get()).defaultBlockState(), 2
                           );
                        }
                     }
                  }
               }
            }

            for (int l1 = 0; l1 < j && worldgenlevel.isEmptyBlock(blockpos$mutableblockpos); l1++) {
               worldgenlevel.setBlock(blockpos$mutableblockpos, BAMBOO_TRUNK, 2);
               blockpos$mutableblockpos.move(Direction.UP, 1);
            }

            if (blockpos$mutableblockpos.getY() - blockpos.getY() >= 3) {
               worldgenlevel.setBlock(blockpos$mutableblockpos, BAMBOO_FINAL_LARGE, 2);
               worldgenlevel.setBlock(blockpos$mutableblockpos.move(Direction.DOWN, 1), BAMBOO_TOP_LARGE, 2);
               worldgenlevel.setBlock(blockpos$mutableblockpos.move(Direction.DOWN, 1), BAMBOO_TOP_SMALL, 2);
            }
         }

         i++;
      }

      return i > 0;
   }
}
