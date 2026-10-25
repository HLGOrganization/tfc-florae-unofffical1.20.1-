package tfcflorae.common.blocks.devices;

import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.devices.SluiceBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class TFCFSluiceBlock extends SluiceBlock {
   public TFCFSluiceBlock(ExtendedProperties properties) {
      super(properties);
      this.registerDefaultState((BlockState)((BlockState)((BlockState)this.getStateDefinition().any()).setValue(UPPER, true)).setValue(FACING, Direction.NORTH));
   }
}
