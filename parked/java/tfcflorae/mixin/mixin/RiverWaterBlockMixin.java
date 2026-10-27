package tfcflorae.mixin;

import net.minecraft.util.RandomSource;
import net.dries007.tfc.common.blocks.RiverWaterBlock;
import net.dries007.tfc.common.fluids.BucketPickupExtension;
import net.dries007.tfc.common.fluids.TFCFluids;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import org.spongepowered.asm.mixin.Mixin;
import tfcflorae.client.particle.TFCFParticles;

@Mixin({RiverWaterBlock.class})
public abstract class RiverWaterBlockMixin extends LiquidBlock implements BucketPickupExtension {
   public RiverWaterBlockMixin(Properties properties) {
      super(TFCFluids.RIVER_WATER, properties);
   }

   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      super.animateTick(state, level, pos, random);
      if (Minecraft.useFancyGraphics() && random.nextInt(34) == 0 && level.getBlockState(pos.above()).isAir()) {
         for (int i = 0; i < 3; i++) {
            level.addParticle(
               (ParticleOptions)TFCFParticles.WATER_FLOW.get(),
               (double)((float)pos.getX() + random.nextFloat()),
               (double)pos.getY() + 0.875,
               (double)((float)pos.getZ() + random.nextFloat()),
               0.0,
               0.0,
               0.0
            );
         }
      }
   }
}
