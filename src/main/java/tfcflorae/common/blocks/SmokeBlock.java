package tfcflorae.common.blocks;

import net.minecraft.util.RandomSource;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.fluids.FluidProperty;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.Fluid;
import tfcflorae.common.TFCFTags;

public class SmokeBlock extends Block {
   public static final FluidProperty FLUID = TFCBlockStateProperties.WATER_AND_LAVA;
   private static final int SMOKE_DISTANCE = 5;
   private final boolean spawnParticles;

   public SmokeBlock(Properties properties, boolean spawnParticles) {
      super(properties);
      this.spawnParticles = spawnParticles;
      this.registerDefaultState(this.defaultBlockState());
   }

   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      if (this.spawnParticles) {
         for (int i = 0; i < random.nextInt(1) + 1; i++) {
            BlockState belowState = level.getBlockState(pos.below());
            Fluid fluidAbove = level.getFluidState(pos.above()).getType();
            if (!Helpers.isFluid(fluidAbove, FluidTags.WATER) && !Helpers.isFluid(fluidAbove, FluidTags.LAVA)) {
               level.addParticle(
                  ParticleTypes.LAVA,
                  (double)pos.getX() + 0.5,
                  (double)pos.getY() + 0.5,
                  (double)pos.getZ() + 0.5,
                  (double)(random.nextFloat() / 2.0F),
                  5.0E-5,
                  (double)(random.nextFloat() / 2.0F)
               );
               level.addParticle(
                  ParticleTypes.SMOKE,
                  (double)pos.getX() + 0.5 + random.nextDouble() / 4.0 * (double)(random.nextBoolean() ? 1 : -1),
                  (double)pos.getY() + 0.4,
                  (double)pos.getZ() + 0.5 + random.nextDouble() / 4.0 * (double)(random.nextBoolean() ? 1 : -1),
                  0.0,
                  0.005,
                  0.0
               );
            } else if (Helpers.isBlock(belowState, TFCFTags.Blocks.MAGMA_BLOCKS)) {
               if (random.nextInt(10) == 0) {
                  level.playLocalSound(
                     (double)pos.getX() + 0.5,
                     (double)pos.getY() + 0.5,
                     (double)pos.getZ() + 0.5,
                     SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_AMBIENT,
                     SoundSource.BLOCKS,
                     0.5F + random.nextFloat(),
                     random.nextFloat() * 0.7F + 0.6F,
                     false
                  );
               }

               level.addAlwaysVisibleParticle(
                  ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,
                  true,
                  (double)pos.getX() + 0.5 + random.nextDouble() / 3.0 * (double)(random.nextBoolean() ? 1 : -1),
                  (double)pos.getY() + random.nextDouble() + random.nextDouble(),
                  (double)pos.getZ() + 0.5 + random.nextDouble() / 3.0 * (double)(random.nextBoolean() ? 1 : -1),
                  0.0,
                  0.07,
                  0.0
               );
            } else {
               if (random.nextInt(10) == 0) {
                  level.playLocalSound(
                     (double)pos.getX() + 0.5,
                     (double)pos.getY() + 0.5,
                     (double)pos.getZ() + 0.5,
                     SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_AMBIENT,
                     SoundSource.BLOCKS,
                     0.5F + random.nextFloat(),
                     random.nextFloat() * 0.7F + 0.6F,
                     false
                  );
               }

               level.addAlwaysVisibleParticle(
                  ParticleTypes.CAMPFIRE_COSY_SMOKE,
                  true,
                  (double)pos.getX() + 0.5 + random.nextDouble() / 3.0 * (double)(random.nextBoolean() ? 1 : -1),
                  (double)pos.getY() + random.nextDouble() + random.nextDouble(),
                  (double)pos.getZ() + 0.5 + random.nextDouble() / 3.0 * (double)(random.nextBoolean() ? 1 : -1),
                  0.0,
                  0.07,
                  0.0
               );
            }
         }
      }
   }
}
