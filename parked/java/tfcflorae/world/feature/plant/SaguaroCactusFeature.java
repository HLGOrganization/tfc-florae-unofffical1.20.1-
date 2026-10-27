package tfcflorae.world.feature.plant;

import com.mojang.serialization.Codec;
import net.minecraft.util.RandomSource;
import net.dries007.tfc.util.EnvironmentHelpers;
import net.dries007.tfc.world.feature.BlockConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import tfcflorae.common.blocks.TFCFBlocks;
import tfcflorae.common.blocks.plant.SaguaroCactusBlock;
import tfcflorae.common.blocks.plant.TFCFPlant;

public class SaguaroCactusFeature extends Feature<BlockConfig<SaguaroCactusBlock>> {
   public static final Codec<BlockConfig<SaguaroCactusBlock>> CODEC = BlockConfig.codec(
      b -> b instanceof SaguaroCactusBlock t ? t : null, "Must be a " + SaguaroCactusBlock.class.getSimpleName()
   );

   public SaguaroCactusFeature(Codec<BlockConfig<SaguaroCactusBlock>> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<BlockConfig<SaguaroCactusBlock>> context) {
      WorldGenLevel level = context.level();
      BlockPos pos = context.origin();
      Random random = context.random();
      BlockState state = level.getBlockState(pos);
      return EnvironmentHelpers.isWorldgenReplaceable(state) && !state.getMaterial().isLiquid()
         ? ((SaguaroCactusBlock)((BlockConfig)context.config()).block())
            .generateCactus((Block)TFCFBlocks.FRUITING_PLANTS.get(TFCFPlant.SAGUARO_CACTUS).get(), level, random.nextBoolean(), pos, random, false)
         : false;
   }
}
