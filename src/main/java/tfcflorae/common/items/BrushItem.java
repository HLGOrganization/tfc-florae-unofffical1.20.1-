package tfcflorae.common.items;

import com.mojang.datafixers.util.Either;
import java.util.function.Function;
import net.dries007.tfc.common.capabilities.player.PlayerDataCapability;
import net.dries007.tfc.common.recipes.CollapseRecipe;
import net.dries007.tfc.config.TFCConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.Vanishable;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.items.ItemHandlerHelper;
import tfcflorae.client.TFCFSounds;
import tfcflorae.common.recipes.BrushingRecipe;

public class BrushItem extends Item implements Vanishable {
   public static final int USE_TIME = 100;

   public BrushItem(Properties properties) {
      super(properties);
   }

   public InteractionResult useOn(UseOnContext context) {
      Player player = context.getPlayer();
      if (player != null) {
         Level level = context.getLevel();
         BlockPos pos = context.getClickedPos();
         BlockState state = level.getBlockState(pos);
         Either<BlockState, InteractionResult> result = BrushingRecipe.computeResult(
            player, state, new BlockHitResult(context.getClickLocation(), context.getClickedFace(), pos, context.isInside()), true
         );
         return (InteractionResult)result.map(resultState -> {
            player.playSound((SoundEvent)TFCFSounds.BRUSHING.get(), 1.0F, 1.0F);
            ItemStack held = player.getMainHandItem();
            if (!level.isClientSide) {
               if ((Boolean)TFCConfig.SERVER.enableChiselsStartCollapses.get() && CollapseRecipe.tryTriggerCollapse(level, pos)) {
                  return InteractionResult.SUCCESS;
               }

               player.getCapability(PlayerDataCapability.CAPABILITY).ifPresent(cap -> {
                  BrushingRecipe recipeUsed = BrushingRecipe.getRecipe(state, held);
                  if (recipeUsed != null) {
                     ItemStack extraDrop = recipeUsed.getExtraDrop(held);
                     if (!extraDrop.isEmpty()) {
                        ItemHandlerHelper.giveItemToPlayer(player, extraDrop);
                     }
                  }
               });
            }

            level.setBlockAndUpdate(pos, resultState);
            held.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND));
            player.getCooldowns().addCooldown(this, 10);
            return InteractionResult.sidedSuccess(level.isClientSide);
         }, Function.identity());
      } else {
         return InteractionResult.PASS;
      }
   }

   public int getUseDuration(ItemStack stack) {
      return 100;
   }

   public UseAnim getUseAnimation(ItemStack pStack) {
      return UseAnim.CROSSBOW;
   }

   public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int countLeft) {
      if (countLeft % 16 == 0 && !level.isClientSide) {
         level.playSound(null, entity, (SoundEvent)TFCFSounds.BRUSHING.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
      }
   }
}
