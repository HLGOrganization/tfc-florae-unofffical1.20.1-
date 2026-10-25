package tfcflorae.common.blocks.soil;

import java.util.function.Supplier;
import net.dries007.tfc.common.blocks.soil.SoilBlockType;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.registry.RegistrySoilVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
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

public class TFCFRockySoilBlock extends Block {
   @Nullable
   private final Supplier<? extends Block> transformsTo;
   @Nullable
   private final Supplier<? extends Block> transformsFrom;
   @Nullable
   private final Supplier<? extends Block> soilVariantDrop;
   @Nullable
   private final Supplier<?> requiredInput;
   private final Boolean transformsToCobble;

   public TFCFRockySoilBlock(
      Properties properties,
      @Nullable Supplier<?> requiredInput,
      @Nullable Supplier<? extends Block> transformsTo,
      @Nullable Supplier<? extends Block> transformsFrom,
      @Nullable Supplier<? extends Block> soilVariantDrop,
      Boolean transformsToCobble
   ) {
      super(properties);
      this.transformsTo = transformsTo;
      this.transformsFrom = transformsFrom;
      this.soilVariantDrop = soilVariantDrop;
      this.requiredInput = requiredInput;
      this.transformsToCobble = transformsToCobble;
   }

   TFCFRockySoilBlock(Properties properties, @Nullable SoilBlockType dirtType, @Nullable RegistrySoilVariant variant, Boolean transformsToCobble) {
      this(properties, variant.getDriedMudBrick(), variant.getBlock(dirtType), variant.getBlock(dirtType), variant.getBlock(dirtType), transformsToCobble);
   }

   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      if (this.requiredInput instanceof Block) {
         ItemStack stack = player.getItemInHand(hand);
         if (Helpers.isItem(stack, ((Block)this.requiredInput.get()).asItem()) && this.transformsTo != null) {
            if (this.transformsToCobble && this.soilVariantDrop != null) {
               stack.shrink(1);
               BlockState block = this.transformsTo.get().defaultBlockState();
               level.setBlockAndUpdate(pos, block);
               ItemHandlerHelper.giveItemToPlayer(player, new ItemStack((ItemLike)this.soilVariantDrop.get()));
               Helpers.playSound(level, pos, SoundType.STONE.getPlaceSound());
               return InteractionResult.SUCCESS;
            }

            stack.shrink(1);
            BlockState block = this.transformsTo.get().defaultBlockState();
            level.setBlockAndUpdate(pos, block);
            Helpers.playSound(level, pos, SoundType.BASALT.getPlaceSound());
            return InteractionResult.SUCCESS;
         }
      }

      if (this.requiredInput instanceof Item) {
         ItemStack stack = player.getItemInHand(hand);
         if (Helpers.isItem(stack, ((Item)this.requiredInput.get()).asItem()) && this.transformsTo != null) {
            if (this.transformsToCobble && this.soilVariantDrop != null) {
               stack.shrink(1);
               BlockState block = this.transformsTo.get().defaultBlockState();
               level.setBlockAndUpdate(pos, block);
               ItemHandlerHelper.giveItemToPlayer(player, new ItemStack((ItemLike)this.soilVariantDrop.get()));
               Helpers.playSound(level, pos, SoundType.STONE.getPlaceSound());
               return InteractionResult.SUCCESS;
            }

            stack.shrink(1);
            BlockState block = this.transformsTo.get().defaultBlockState();
            level.setBlockAndUpdate(pos, block);
            Helpers.playSound(level, pos, SoundType.BASALT.getPlaceSound());
            return InteractionResult.SUCCESS;
         }
      }

      return InteractionResult.PASS;
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
            if (this.requiredInput instanceof Block) {
               ItemHandlerHelper.giveItemToPlayer(context.getPlayer(), new ItemStack(((Block)this.requiredInput.get()).asItem()));
            } else if (this.requiredInput instanceof Item) {
               ItemHandlerHelper.giveItemToPlayer(context.getPlayer(), new ItemStack(((Item)this.requiredInput.get()).asItem()));
            }
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
