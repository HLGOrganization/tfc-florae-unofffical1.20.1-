package tfcflorae.common.blocks.devices;

import java.util.List;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.devices.BarrelBlock;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.Tooltips;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.ItemHandlerHelper;
import tfcflorae.common.blockentities.TFCFBarrelBlockEntity;
import tfcflorae.common.blockentities.TFCFBlockEntities;

public class TFCFBarrelBlock extends BarrelBlock {
   public static void toggleSeal(Level level, BlockPos pos, BlockState state) {
      level.getBlockEntity(pos, TFCFBlockEntities.BARREL.get()).ifPresent(barrel -> {
         boolean previousSealed = (Boolean)state.getValue(SEALED);
         level.setBlockAndUpdate(pos, (BlockState)state.setValue(SEALED, !previousSealed));
         if (previousSealed) {
            barrel.onUnseal();
         } else {
            barrel.onSeal();
         }
      });
   }

   public TFCFBarrelBlock(ExtendedProperties properties) {
      super(properties);
   }

   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      TFCFBarrelBlockEntity barrel = (TFCFBarrelBlockEntity)level.getBlockEntity(pos, TFCFBlockEntities.BARREL.get()).orElse(null);
      if (barrel == null) {
         return InteractionResult.PASS;
      } else {
         ItemStack stack = player.getItemInHand(hand);
         if (stack.isEmpty() && player.isShiftKeyDown()) {
            if ((Boolean)state.getValue(RACK) && level.getBlockState(pos.above()).isAir() && hit.getLocation().y - (double)pos.getY() > 0.875) {
               ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(net.minecraft.world.item.Items.BARREL));
               level.setBlockAndUpdate(pos, (BlockState)state.setValue(RACK, false));
            } else {
               toggleSeal(level, pos, state);
            }

            level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 0.85F);
            return InteractionResult.SUCCESS;
         } else if (Helpers.isItem(stack, net.minecraft.world.item.Items.BARREL) && state.getValue(FACING) != Direction.UP) {
            if (!player.isCreative()) {
               stack.shrink(1);
            }

            level.setBlockAndUpdate(pos, (BlockState)state.setValue(RACK, true));
            return InteractionResult.sidedSuccess(level.isClientSide);
         } else if (FluidHelpers.transferBetweenBlockEntityAndItem(stack, barrel, player, hand)) {
            return InteractionResult.SUCCESS;
         } else {
            if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
               Helpers.openScreen(serverPlayer, barrel, barrel.getBlockPos());
            }

            return InteractionResult.SUCCESS;
         }
      }
   }

   protected void addExtraInfo(List<Component> tooltip, CompoundTag inventoryTag) {
      FluidTank tank = new FluidTank((Integer)TFCConfig.SERVER.barrelCapacity.get());
      tank.readFromNBT(inventoryTag.getCompound("tank"));
      if (!tank.isEmpty()) {
         tooltip.add(Tooltips.fluidUnitsOf(tank.getFluid()));
      }
   }
}
