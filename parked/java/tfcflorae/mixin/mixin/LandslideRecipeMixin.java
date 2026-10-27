package tfcflorae.mixin;

import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.soil.FarmlandBlock;
import net.dries007.tfc.common.blocks.wood.FallenLeavesBlock;
import net.dries007.tfc.common.entities.TFCFallingBlockEntity;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.common.recipes.LandslideRecipe;
import net.dries007.tfc.config.TFCConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import tfcflorae.common.blocks.soil.SandLayerBlock;

@Mixin({LandslideRecipe.class})
public class LandslideRecipeMixin {
   public LandslideRecipeMixin() {
   }

   @Overwrite(
      remap = false
   )
   public static boolean tryLandslide(Level level, BlockPos pos, BlockState state) {
      if (!level.isClientSide() && (Boolean)TFCConfig.SERVER.enableBlockLandslides.get()) {
         BlockPos fallPos = LandslideRecipe.getLandslidePos(level, pos, state);
         if (fallPos != null) {
            LandslideRecipe recipe = LandslideRecipe.getRecipe(state);
            if (recipe != null) {
               BlockState fallingState = recipe.getBlockCraftingResult(state);
               if (level.getBlockState(pos).getBlock() instanceof SandLayerBlock originSand) {
                  if (level.getBlockState(fallPos).getBlock() instanceof SandLayerBlock targetSand) {
                     if ((Integer)level.getBlockState(fallPos).getValue(SandLayerBlock.LAYERS) >= 7) {
                        level.setBlock(fallPos, targetSand.transformsInto(), 3);
                     } else {
                        level.setBlock(
                           fallPos,
                           (BlockState)targetSand.defaultBlockState()
                              .setValue(SandLayerBlock.LAYERS, (Integer)level.getBlockState(fallPos).getValue(SandLayerBlock.LAYERS) + 1),
                           3
                        );
                     }

                     level.removeBlock(pos, false);
                  } else if (level.getBlockState(fallPos).getMaterial().isReplaceable()) {
                     level.setBlock(fallPos, (BlockState)fallingState.setValue(SandLayerBlock.LAYERS, 1), 3);
                     level.removeBlock(pos, false);
                  }
               }

               if (level.getBlockState(pos).getBlock() instanceof FallenLeavesBlock originLeaves) {
                  if (level.getBlockState(fallPos).getBlock() instanceof FarmlandBlock || level.getBlockState(pos.below()).getBlock() instanceof FarmlandBlock) {
                     return false;
                  }

                  if (level.getBlockState(fallPos).getBlock() instanceof FallenLeavesBlock targetLeaves) {
                     if ((Integer)level.getBlockState(fallPos).getValue(BlockStateProperties.LAYERS) == 8) {
                        level.setBlock(fallPos.above(), (BlockState)targetLeaves.defaultBlockState().setValue(BlockStateProperties.LAYERS, 1), 3);
                     } else {
                        level.setBlock(
                           fallPos,
                           (BlockState)targetLeaves.defaultBlockState()
                              .setValue(BlockStateProperties.LAYERS, (Integer)level.getBlockState(fallPos).getValue(BlockStateProperties.LAYERS) + 1),
                           3
                        );
                     }

                     level.removeBlock(pos, false);
                  } else if (level.getBlockState(fallPos).getMaterial().isReplaceable()) {
                     level.setBlock(fallPos, (BlockState)fallingState.setValue(BlockStateProperties.LAYERS, 1), 3);
                     level.removeBlock(pos, false);
                  }
               } else if (!(level.getBlockState(pos).getBlock() instanceof SandLayerBlock) && !(level.getBlockState(pos).getBlock() instanceof FallenLeavesBlock)) {
                  if (!fallPos.equals(pos)) {
                     level.removeBlock(pos, false);
                     if (!FluidHelpers.isAirOrEmptyFluid(level.getBlockState(fallPos))) {
                        level.destroyBlock(fallPos, true);
                     }
                  }

                  if ((Boolean)TFCConfig.SERVER.farmlandMakesTheBestRaceTracks.get()) {
                     level.setBlockAndUpdate(fallPos, fallingState);
                  } else {
                     if (state.hasProperty(TFCBlockStateProperties.NATURAL) && fallingState.hasProperty(TFCBlockStateProperties.NATURAL)) {
                        fallingState.setValue(TFCBlockStateProperties.NATURAL, (Boolean)state.getValue(TFCBlockStateProperties.NATURAL));
                     }

                     level.setBlock(fallPos, fallingState, 18);
                  }
               }

               level.playSound(null, pos, (SoundEvent)TFCSounds.DIRT_SLIDE_SHORT.get(), SoundSource.BLOCKS, 0.4F, 1.0F);
               level.addFreshEntity(
                  new TFCFallingBlockEntity(
                     level, (double)fallPos.getX() + 0.5, (double)fallPos.getY(), (double)fallPos.getZ() + 0.5, fallingState, 0.8F, 10
                  )
               );
            }

            return true;
         }
      }

      return false;
   }
}
