package tfcflorae.common.blocks.soil;

import java.util.function.Supplier;
import net.dries007.tfc.common.blocks.soil.SoilBlockType;
import net.dries007.tfc.util.registry.RegistrySoilVariant;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.Nullable;

public class TFCFMudBlock extends Block {
   @Nullable
   private final Supplier<? extends Block> packedMud;

   public TFCFMudBlock(Properties properties, @Nullable Supplier<? extends Block> packedMud) {
      super(properties);
      this.packedMud = packedMud;
   }

   TFCFMudBlock(Properties properties, SoilBlockType soil, RegistrySoilVariant variant) {
      this(properties, variant.getBlock(soil));
   }

   @Nullable
   public BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction action, boolean simulate) {
      return context.getItemInHand().canPerformAction(action) && action == ToolActions.SHOVEL_FLATTEN && this.packedMud != null
         ? this.packedMud.get().defaultBlockState()
         : null;
   }
}
