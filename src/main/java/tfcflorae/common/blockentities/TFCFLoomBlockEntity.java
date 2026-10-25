package tfcflorae.common.blockentities;

import net.dries007.tfc.common.blockentities.LoomBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TFCFLoomBlockEntity extends LoomBlockEntity {
   public TFCFLoomBlockEntity(BlockPos pos, BlockState state) {
      super(pos, state);
   }

   protected TFCFLoomBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(pos, state);
   }

   public BlockEntityType<?> getType() {
      return (BlockEntityType<?>)TFCFBlockEntities.LOOM.get();
   }
}
