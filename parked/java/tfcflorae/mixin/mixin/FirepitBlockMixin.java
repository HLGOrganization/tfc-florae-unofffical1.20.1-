package tfcflorae.mixin;

import net.dries007.tfc.common.TFCTags.Items;
import net.dries007.tfc.common.blockentities.AbstractFirepitBlockEntity;
import net.dries007.tfc.common.blockentities.TFCBlockEntities;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.devices.FirepitBlock;
import net.dries007.tfc.common.items.TFCItems;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.advancements.TFCAdvancements;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tfcflorae.common.TFCFTags;

@Mixin({FirepitBlock.class})
public abstract class FirepitBlockMixin {
   @Shadow
   public static final BooleanProperty LIT = BlockStateProperties.LIT;
   @Shadow
   public static final IntegerProperty SMOKE_LEVEL = TFCBlockStateProperties.SMOKE_LEVEL;

   public FirepitBlockMixin() {
   }

   @Overwrite(
      remap = false
   )
   public BlockState getStateToDraw(
      Level level, Player player, BlockState lookState, Direction direction, BlockPos pos, double x, double y, double z, ItemStack item
   ) {
      if (Helpers.isItem(item, TFCFTags.Items.POTS)) {
         return (BlockState)((Block)TFCBlocks.POT.get()).defaultBlockState().setValue(LIT, (Boolean)lookState.getValue(LIT));
      } else {
         return Helpers.isItem(item, (Item)TFCItems.WROUGHT_IRON_GRILL.get())
            ? (BlockState)((Block)TFCBlocks.GRILL.get()).defaultBlockState().setValue(LIT, (Boolean)lookState.getValue(LIT))
            : null;
      }
   }

   @Overwrite(
      remap = true
   )
   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
      AbstractFirepitBlockEntity<?> firepit = (AbstractFirepitBlockEntity<?>)level.getBlockEntity(pos, (BlockEntityType)TFCBlockEntities.FIREPIT.get()).orElse(null);
      if (firepit != null) {
         ItemStack stack = player.getItemInHand(hand);
         if (!Helpers.isItem(stack.getItem(), TFCFTags.Items.POTS) && stack.getItem() != TFCItems.WROUGHT_IRON_GRILL.get()) {
            if (Helpers.isItem(stack.getItem(), Items.EXTINGUISHER) && (Boolean)state.getValue(LIT)) {
               firepit.extinguish(state);
               return InteractionResult.SUCCESS;
            } else {
               if (player instanceof ServerPlayer serverPlayer) {
                  Helpers.openScreen(serverPlayer, firepit, pos);
               }

               return InteractionResult.SUCCESS;
            }
         } else {
            if (!level.isClientSide) {
               Block newBlock = Helpers.isItem(stack.getItem(), TFCFTags.Items.POTS) ? (Block)TFCBlocks.POT.get() : (Block)TFCBlocks.GRILL.get();
               AbstractFirepitBlockEntity.convertTo(level, pos, state, firepit, newBlock);
               if (player instanceof ServerPlayer serverPlayer) {
                  TFCAdvancements.FIREPIT_CREATED.trigger(serverPlayer, newBlock.defaultBlockState());
               }

               if (!player.isCreative()) {
                  stack.shrink(1);
               }
            }

            return InteractionResult.SUCCESS;
         }
      } else {
         return InteractionResult.PASS;
      }
   }
}
