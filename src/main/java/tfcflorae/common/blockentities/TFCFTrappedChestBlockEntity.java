package tfcflorae.common.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TFCFTrappedChestBlockEntity extends TFCFChestBlockEntity {
   public TFCFTrappedChestBlockEntity(BlockPos pos, BlockState state) {
      super((BlockEntityType<?>)TFCFBlockEntities.TRAPPED_CHEST.get(), pos, state);
   }

   protected void signalOpenCount(Level level, BlockPos pos, BlockState state, int opener, int openCount) {
      super.signalOpenCount(level, pos, state, opener, openCount);
      if (opener != openCount) {
         Block block = state.getBlock();
         level.updateNeighborsAt(pos, block);
         level.updateNeighborsAt(pos.below(), block);
      }
   }
}
