package tfcflorae.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.util.RandomSource;
import net.dries007.tfc.common.blocks.soil.ConnectedGrassBlock;
import net.dries007.tfc.common.blocks.soil.DirtBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BambooLeaves;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;

public class TFCBambooFeature extends Feature<ProbabilityFeatureConfiguration> {
   private static final BlockState BAMBOO_TRUNK = (BlockState)((BlockState)((BlockState)Blocks.BAMBOO.defaultBlockState().setValue(BambooStalkBlock.AGE, 1))
         .setValue(BambooStalkBlock.LEAVES, BambooLeaves.NONE))
      .setValue(BambooStalkBlock.STAGE, 0);
   private static final BlockState BAMBOO_FINAL_LARGE = (BlockState)((BlockState)BAMBOO_TRUNK.setValue(BambooStalkBlock.LEAVES, BambooLeaves.LARGE))
      .setValue(BambooStalkBlock.STAGE, 1);
   private static final BlockState BAMBOO_TOP_LARGE = (BlockState)BAMBOO_TRUNK.setValue(BambooStalkBlock.LEAVES, BambooLeaves.LARGE);
   private static final BlockState BAMBOO_TOP_SMALL = (BlockState)BAMBOO_TRUNK.setValue(BambooStalkBlock.LEAVES, BambooLeaves.SMALL);

   public TFCBambooFeature(Codec<ProbabilityFeatureConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<ProbabilityFeatureConfiguration> context) {
      int placed = 0;
      BlockPos blockpos = context.origin();
      WorldGenLevel level = context.level();
      RandomSource random = context.random();
      ProbabilityFeatureConfiguration config = (ProbabilityFeatureConfiguration)context.config();
      MutableBlockPos cursor = blockpos.mutable();
      MutableBlockPos cursor2 = blockpos.mutable();
      if (level.isStateAtPosition(cursor, net.minecraft.world.level.block.state.BlockState::isAir)) {
         if (Blocks.BAMBOO.defaultBlockState().canSurvive(level, cursor)) {
            int trunkSize = random.nextInt(12) + 5;
            if (random.nextFloat() < config.probability) {
               int radius = random.nextInt(4) + 1;

               for (int x = blockpos.getX() - radius; x <= blockpos.getX() + radius; x++) {
                  for (int z = blockpos.getZ() - radius; z <= blockpos.getZ() + radius; z++) {
                     int dx = x - blockpos.getX();
                     int dz = z - blockpos.getZ();
                     if (dx * dx + dz * dz <= radius * radius) {
                        cursor2.set(x, level.getHeight(Types.WORLD_SURFACE, x, z) - 1, z);
                        Block under = level.getBlockState(cursor2).getBlock();
                        if (under instanceof ConnectedGrassBlock) {
                           ConnectedGrassBlock grass = (ConnectedGrassBlock)under;
                           Block var19 = grass.getDirt().getBlock();
                           if (var19 instanceof DirtBlock) {
                              DirtBlock dirt = (DirtBlock)var19;
                              if (dirt.getRooted() != null) {
                                 level.setBlock(cursor2, dirt.getRooted(), 2);
                                 continue;
                              }
                           }
                        }

                        if (under instanceof DirtBlock) {
                           DirtBlock dirt = (DirtBlock)under;
                           if (dirt.getRooted() != null) {
                              level.setBlock(cursor2, dirt.getRooted(), 2);
                           }
                        }
                     }
                  }
               }
            }

            for (int j = 0; j < trunkSize && level.isStateAtPosition(cursor, net.minecraft.world.level.block.state.BlockState::isAir); j++) {
               level.setBlock(cursor, BAMBOO_TRUNK, 2);
               cursor.move(Direction.UP, 1);
            }

            if (cursor.getY() - blockpos.getY() >= 3) {
               level.setBlock(cursor, BAMBOO_FINAL_LARGE, 2);
               level.setBlock(cursor.move(Direction.DOWN, 1), BAMBOO_TOP_LARGE, 2);
               level.setBlock(cursor.move(Direction.DOWN, 1), BAMBOO_TOP_SMALL, 2);
            }
         }

         placed++;
      }

      return placed > 0;
   }
}
