package tfcflorae.mixin;

import net.minecraft.util.RandomSource;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.wood.TFCSaplingBlock;
import net.dries007.tfc.util.climate.Climate;
import net.dries007.tfc.world.feature.tree.TFCTreeGrower;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({TFCSaplingBlock.class})
public abstract class TFCSaplingBlockMixin extends SaplingBlock {
   public TFCSaplingBlockMixin(TFCTreeGrower tree, ExtendedProperties properties) {
      super(tree, properties.properties());
   }

   @Inject(
      method = {"randomTick"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void inject$randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
      if (Climate.getTemperature(level, pos) < 0.0F) {
         ci.cancel();
      }
   }
}
