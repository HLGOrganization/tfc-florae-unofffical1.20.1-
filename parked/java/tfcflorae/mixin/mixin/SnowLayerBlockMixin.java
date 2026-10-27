package tfcflorae.mixin;

import net.dries007.tfc.config.TFCConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tfcflorae.util.TFCFHelpers;

@Mixin(
   value = {SnowLayerBlock.class},
   priority = 999999
)
public abstract class SnowLayerBlockMixin extends Block {
   @Unique
   private boolean canWalkThroughEffortlessly;

   public SnowLayerBlockMixin(Properties properties) {
      super(properties);
   }

   public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
      if (entity != null) {
         if (TFCFHelpers.canWalkThroughEffortlessly(entity)) {
            this.canWalkThroughEffortlessly = true;
         } else {
            this.canWalkThroughEffortlessly = false;
         }
      }
   }

   public float getSpeedFactor() {
      return TFCConfig.SERVER.enableSnowSlowEntities.get() ? (this.canWalkThroughEffortlessly ? 1.0F : 0.6F) : 1.0F;
   }

   @Inject(
      method = {"canSurvive"},
      at = {@At("TAIL")},
      cancellable = true
   )
   private void inject$canSurvive(BlockState state, LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
      if (this.isInvalidBlock(level, pos)) {
         cir.setReturnValue(false);
      }
   }

   @Inject(
      method = {"updateShape"},
      at = {@At("TAIL")},
      cancellable = true
   )
   private void updateShapeSurviveOnLeavesWithSingleLayer(
      BlockState stateIn,
      Direction facing,
      BlockState facingState,
      LevelAccessor level,
      BlockPos currentPos,
      BlockPos facingPos,
      CallbackInfoReturnable<BlockState> cir
   ) {
      if (this.isInvalidBlock(level, currentPos)) {
         cir.setReturnValue(Blocks.AIR.defaultBlockState());
      }
   }

   @Unique
   private boolean isInvalidBlock(LevelReader level, BlockPos pos) {
      BlockState stateBelow = level.getBlockState(pos.below());
      return stateBelow.getRenderShape() == RenderShape.INVISIBLE || stateBelow.isAir();
   }
}
