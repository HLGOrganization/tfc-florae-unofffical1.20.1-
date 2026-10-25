package tfcflorae.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.util.RandomSource;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.BlockColumnConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.BlockColumnConfiguration.Layer;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import tfcflorae.common.blocks.spidercave.WebbedChestBlock;

public class ColumnFeature extends Feature<BlockColumnConfiguration> {
   public ColumnFeature(Codec<BlockColumnConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<BlockColumnConfiguration> context) {
      WorldGenLevel level = context.level();
      BlockColumnConfiguration config = (BlockColumnConfiguration)context.config();
      RandomSource random = context.random();
      int lSize = config.layers().size();
      int[] hSamples = new int[lSize];
      int accHeight = 0;

      for (int i = 0; i < lSize; i++) {
         hSamples[i] = ((Layer)config.layers().get(i)).height().sample(random);
         accHeight += hSamples[i];
      }

      if (accHeight == 0) {
         return false;
      } else {
         MutableBlockPos blockpos$mutableblockpos1 = context.origin().mutable();
         MutableBlockPos blockpos$mutableblockpos = blockpos$mutableblockpos1.mutable().move(config.direction());

         for (int i = 0; i < accHeight; i++) {
            if (!config.allowedPlacement().test(level, blockpos$mutableblockpos)) {
               truncate(hSamples, accHeight, i, config.prioritizeTip());
               break;
            }

            blockpos$mutableblockpos.move(config.direction());
         }

         for (int i = 0; i < lSize; i++) {
            int hSample = hSamples[i];
            if (hSample != 0) {
               Layer blockcolumnconfiguration$layer = (Layer)config.layers().get(i);

               for (int j = 0; j < hSample; j++) {
                  level.setBlock(blockpos$mutableblockpos1, blockcolumnconfiguration$layer.state().getState(random, blockpos$mutableblockpos1), 2);
                  if (level.getBlockState(blockpos$mutableblockpos1).getBlock() instanceof WebbedChestBlock) {
                     int chance = random.nextInt(3);
                     if (chance > 0) {
                        RandomizableContainerBlockEntity.setLootTable(level, random, blockpos$mutableblockpos1, BuiltInLootTables.SIMPLE_DUNGEON);
                     } else {
                        RandomizableContainerBlockEntity.setLootTable(level, random, blockpos$mutableblockpos1, BuiltInLootTables.NETHER_BRIDGE);
                     }
                  }

                  blockpos$mutableblockpos1.move(config.direction());
               }
            }
         }

         return false;
      }
   }

   private static void truncate(int[] hSamples, int accHeight, int index, boolean prioritizeTip) {
      int i = accHeight - index;
      int j = prioritizeTip ? 1 : -1;
      int k = prioritizeTip ? 0 : hSamples.length - 1;
      int l = prioritizeTip ? hSamples.length : -1;

      for (int i1 = k; i1 != l && i > 0; i1 += j) {
         int j1 = hSamples[i1];
         int k1 = Math.min(j1, i);
         i -= k1;
         hSamples[i1] -= k1;
      }
   }
}
