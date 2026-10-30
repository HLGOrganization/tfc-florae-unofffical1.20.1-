package tfcflorae.util;

import java.util.function.Supplier;
import net.dries007.tfc.common.blocks.DirectionPropertyBlock;
import net.dries007.tfc.util.BlockItemPlacement;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.InteractionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import tfcflorae.common.TFCFTags;
import tfcflorae.common.blocks.TFCFBlocks;
import tfcflorae.common.blocks.rock.MineralSheetBlock;
import tfcflorae.common.container.TFCFContainerProviders;

public final class TFCFInteractionManager {
   public TFCFInteractionManager() {
   }

   public static void init() {
      // 与 TFC 完全一致：一个 knapping 交互，覆盖所有可打制物品。
      // 具体用哪个 KnappingType 由 TFC 的 KnappingType.get(Player) 依据主手物品决定，
      // 数量门槛取该 KnappingType 的 amountToConsume（陶土 5 / 燧石 2）。
      InteractionManager.register(
         Ingredient.of(TFCFTags.Items.KNAPPING_ANY),
         false,
         true,
         InteractionManager.createKnappingInteraction(
            (stack, player) ->
            {
               final net.dries007.tfc.util.KnappingType type = net.dries007.tfc.util.KnappingType.get(player);
               return type != null && stack.getCount() >= type.amountToConsume();
            },
            TFCFContainerProviders.KNAPPING
         )
      );
      InteractionManager.register(
         Ingredient.of(TFCFTags.Items.MINERAL_SHEETS),
         false,
         (stack, context) -> {
            Player player = context.getPlayer();
            if (player != null && player.isShiftKeyDown()) {
               Level level = context.getLevel();
               Direction clickedFace = context.getClickedFace();
               Direction sheetFace = clickedFace.getOpposite();
               BlockPos clickedPos = context.getClickedPos();
               BlockPos relativePos = clickedPos.relative(clickedFace);
               BlockState clickedState = level.getBlockState(clickedPos);
               BlockState relativeState = level.getBlockState(relativePos);
               BlockPlaceContext blockContext = new BlockPlaceContext(context);
               BooleanProperty property = DirectionPropertyBlock.getProperty(sheetFace);
               if (blockContext.replacingClickedOnBlock()) {
                  return InteractionResult.FAIL;
               }

               if (Helpers.isBlock(relativeState, (Block)TFCFBlocks.MINERAL_SHEET.get())) {
                  if (!(Boolean)relativeState.getValue(property)
                     && BlockItemPlacement.canPlace(blockContext, clickedState)
                     && clickedState.isFaceSturdy(level, clickedPos, clickedFace)) {
                     ItemStack insertStack = stack.split(1);
                     MineralSheetBlock.addSheet(level, relativePos, relativeState, sheetFace, insertStack);
                     return InteractionResult.SUCCESS;
                  }

                  return InteractionResult.FAIL;
               }

               if (level.getBlockState(relativePos).canBeReplaced(blockContext)) {
                  BlockState placingState = (BlockState)((Block)TFCFBlocks.MINERAL_SHEET.get()).defaultBlockState().setValue(property, true);
                  if (BlockItemPlacement.canPlace(blockContext, placingState) && clickedState.isFaceSturdy(level, clickedPos, clickedFace)) {
                     ItemStack insertStack = stack.split(1);
                     MineralSheetBlock.addSheet(level, relativePos, placingState, sheetFace, insertStack);
                     return InteractionResult.SUCCESS;
                  }
               }
            }

            return InteractionResult.PASS;
         }
      );

   }
}
