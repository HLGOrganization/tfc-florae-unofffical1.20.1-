package tfcflorae.common.blocks.soil;

import java.util.function.Function;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public enum TFCFSandstoneBlockType {
   LAYERED(color -> Properties.of().mapColor(color.getMaterialColor()).strength(0.8F).requiresCorrectToolForDrops());

   private final Function<Colors, Properties> factory;

   private TFCFSandstoneBlockType(Function<Colors, Properties> factory) {
      this.factory = factory;
   }

   public Properties properties(Colors color) {
      return this.factory.apply(color);
   }
}
