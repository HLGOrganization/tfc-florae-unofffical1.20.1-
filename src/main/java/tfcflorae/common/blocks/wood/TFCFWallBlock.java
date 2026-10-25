package tfcflorae.common.blocks.wood;

import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.IForgeBlockExtension;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class TFCFWallBlock extends WallBlock implements IForgeBlockExtension {
   private final ExtendedProperties extendedProperties;

   public TFCFWallBlock(ExtendedProperties extendedProperties, Properties properties) {
      super(properties);
      this.extendedProperties = extendedProperties;
   }

   public ExtendedProperties getExtendedProperties() {
      return this.extendedProperties;
   }
}
