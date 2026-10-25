package tfcflorae.common.blockentities;

import net.dries007.tfc.common.blockentities.SluiceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TFCFSluiceBlockEntity extends SluiceBlockEntity {
   public TFCFSluiceBlockEntity(BlockPos pos, BlockState state) {
      super(pos, state);
   }

   protected TFCFSluiceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(pos, state);
   }

   public BlockEntityType<?> getType() {
      return (BlockEntityType<?>)TFCFBlockEntities.SLUICE.get();
   }
}
