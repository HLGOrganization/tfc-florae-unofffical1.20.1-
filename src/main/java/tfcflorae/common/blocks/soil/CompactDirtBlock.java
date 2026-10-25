package tfcflorae.common.blocks.soil;

import java.util.function.Supplier;
import net.dries007.tfc.common.blocks.soil.SoilBlockType;
import net.dries007.tfc.util.registry.RegistrySoilVariant;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.Nullable;

public class CompactDirtBlock extends Block {
   @Nullable
   private final Supplier<? extends Block> dirt;
   private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 14.0, 16.0);

   public CompactDirtBlock(Properties properties, @Nullable Supplier<? extends Block> dirt) {
      super(properties);
      this.dirt = dirt;
   }

   CompactDirtBlock(Properties properties, SoilBlockType soil, RegistrySoilVariant variant) {
      this(properties, variant.getBlock(soil));
   }

   @Nullable
   public BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction action, boolean simulate) {
      return context.getItemInHand().canPerformAction(action) && action == ToolActions.HOE_TILL && this.dirt != null ? this.dirt.get().defaultBlockState() : null;
   }
}
