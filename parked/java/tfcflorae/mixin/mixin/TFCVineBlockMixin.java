package tfcflorae.mixin;

import java.util.Collections;
import java.util.List;
import net.minecraft.util.RandomSource;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.plant.TFCVineBlock;
import net.dries007.tfc.util.climate.Climate;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext.Builder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tfcflorae.Config;
import tfcflorae.common.blocks.ICooldown;
import tfcflorae.util.TFCFHelpers;

@Mixin({TFCVineBlock.class})
public abstract class TFCVineBlockMixin extends VineBlock implements ICooldown {
   @Unique
   private boolean isDead;
   @Unique
   private long cooldown = Long.MIN_VALUE;

   public TFCVineBlockMixin(ExtendedProperties properties) {
      super(properties.properties());
      this.isDead = false;
   }

   @Inject(
      method = {"<init>"},
      at = {@At("RETURN")}
   )
   private void inject$init(CallbackInfo ci) {
      this.cooldown = Long.MIN_VALUE;
      this.isDead = false;
   }

   @Inject(
      method = {"randomTick"},
      at = {@At("TAIL")},
      cancellable = true
   )
   private void inject$randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
      double tempThreshold = (Double)Config.COMMON.foliageDecayThreshold.get();
      if (!this.isDead) {
         if ((double)Climate.getTemperature(level, pos) < tempThreshold && (double)TFCFHelpers.getAverageDailyTemperature(level, pos) < tempThreshold) {
            this.isDead = true;
            level.blockUpdated(pos, state.getBlock());
            return;
         }

         if (Climate.getTemperature(level, pos) > 0.0F) {
            super.randomTick(state, level, pos, random);
         }
      } else if (this.isDead
         && this.getCooldown(level, this.cooldown)
         && (double)Climate.getTemperature(level, pos) >= tempThreshold
         && (double)TFCFHelpers.getAverageDailyTemperature(level, pos) >= tempThreshold) {
         this.setCooldown(level, 72000L, this.cooldown);
         this.isDead = false;
         level.blockUpdated(pos, state.getBlock());
         return;
      }
   }

   public RenderShape getRenderShape(BlockState state) {
      return this.isDead ? RenderShape.INVISIBLE : super.getRenderShape(state);
   }

   protected boolean isAir(BlockState state) {
      return this.isDead ? true : super.isAir(state);
   }

   public List<ItemStack> getDrops(BlockState state, Builder builder) {
      return this.isDead ? Collections.emptyList() : super.getDrops(state, builder);
   }

   public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
      return this.isDead ? true : super.canBeReplaced(state, context);
   }

   public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
      return this.isDead ? 1.0F : super.getShadeBrightness(state, level, pos);
   }
}
