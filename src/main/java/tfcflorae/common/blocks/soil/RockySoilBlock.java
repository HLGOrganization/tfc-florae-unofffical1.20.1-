package tfcflorae.common.blocks.soil;

import java.util.function.Supplier;
import net.dries007.tfc.common.blocks.soil.SoilBlockType;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.registry.RegistrySoilVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;
import tfcflorae.common.items.WalkingCaneItem;

public class RockySoilBlock extends Block {
   @Nullable
   private final Supplier<? extends Block> transformsTo;
   @Nullable
   private final Supplier<? extends Block> transformsFrom;
   @Nullable
   private final Supplier<? extends Block> soilVariantDrop;
   @Nullable
   private final Supplier<? extends Block> requiredItem;
   private final Boolean transformsToCobble;

   public RockySoilBlock(
      Properties properties,
      @Nullable Supplier<? extends Block> requiredItem,
      @Nullable Supplier<? extends Block> transformsTo,
      @Nullable Supplier<? extends Block> transformsFrom,
      @Nullable Supplier<? extends Block> soilVariantDrop,
      Boolean transformsToCobble
   ) {
      super(properties);
      this.transformsTo = transformsTo;
      this.transformsFrom = transformsFrom;
      this.soilVariantDrop = soilVariantDrop;
      this.requiredItem = requiredItem;
      this.transformsToCobble = transformsToCobble;
   }

   RockySoilBlock(Properties properties, @Nullable SoilBlockType dirtType, @Nullable RegistrySoilVariant variant, Boolean transformsToCobble) {
      this(properties, null, variant.getBlock(dirtType), variant.getBlock(dirtType), variant.getBlock(dirtType), transformsToCobble);
   }

   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      ItemStack stack = player.getItemInHand(hand);
      if (!Helpers.isItem(stack, this.requiredItem.get().asItem()) || this.transformsTo == null) {
         return InteractionResult.PASS;
      } else if (this.transformsToCobble && this.soilVariantDrop != null) {
         stack.shrink(1);
         BlockState block = this.transformsTo.get().defaultBlockState();
         level.setBlockAndUpdate(pos, block);
         ItemHandlerHelper.giveItemToPlayer(player, new ItemStack((ItemLike)this.soilVariantDrop.get()));
         Helpers.playSound(level, pos, SoundType.STONE.getPlaceSound());
         return InteractionResult.SUCCESS;
      } else {
         stack.shrink(1);
         BlockState block = this.transformsTo.get().defaultBlockState();
         level.setBlockAndUpdate(pos, block);
         Helpers.playSound(level, pos, SoundType.BASALT.getPlaceSound());
         return InteractionResult.SUCCESS;
      }
   }

   @Nullable
   public BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction action, boolean simulate) {
      if ((
            context.getItemInHand().canPerformAction(ToolActions.SHOVEL_FLATTEN)
               || context.getItemInHand().canPerformAction(ToolActions.HOE_TILL)
               || context.getItemInHand().getItem() instanceof WalkingCaneItem
         )
         && this.transformsFrom != null) {
         if (context.getPlayer() != null) {
            ItemHandlerHelper.giveItemToPlayer(context.getPlayer(), new ItemStack(this.requiredItem.get().asItem()));
         } else {
            context.getLevel().addFreshEntity(new ItemStack((ItemLike)this.soilVariantDrop.get()).getEntityRepresentation());
         }

         Helpers.playSound(context.getLevel(), context.getClickedPos(), SoundType.GRAVEL.getBreakSound());
         return this.transformsFrom.get().defaultBlockState();
      } else {
         return null;
      }
   }
}
