package tfcflorae.common.blocks.wood;

import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.wood.ToolRackBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import tfcflorae.common.blockentities.TFCFBlockEntities;
import tfcflorae.common.blockentities.TFCFToolRackBlockEntity;

public class TFCFToolRackBlock extends ToolRackBlock {
   public TFCFToolRackBlock(ExtendedProperties properties) {
      super(properties);
   }

   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      TFCFToolRackBlockEntity toolRack = (TFCFToolRackBlockEntity)level.getBlockEntity(pos, TFCFBlockEntities.TOOL_RACK.get()).orElse(null);
      return toolRack != null
         ? toolRack.onRightClick(
            player, this.getSlotFromPos(state, hit.getLocation().subtract((double)pos.getX(), (double)pos.getY(), (double)pos.getZ()))
         )
         : InteractionResult.PASS;
   }
}
