package tfcflorae.common.blockentities;

import net.dries007.tfc.common.blockentities.FarmlandBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TFCFFarmlandBlockEntity extends FarmlandBlockEntity {
   public TFCFFarmlandBlockEntity(BlockPos pos, BlockState state) {
      super(pos, state);
   }

   protected TFCFFarmlandBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(pos, state);
   }

   public BlockEntityType<?> getType() {
      return (BlockEntityType<?>)TFCFBlockEntities.FARMLAND.get();
   }
}
