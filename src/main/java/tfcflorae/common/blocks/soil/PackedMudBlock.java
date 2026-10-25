package tfcflorae.common.blocks.soil;

import java.util.function.Supplier;
import net.dries007.tfc.common.blocks.soil.SoilBlockType;
import net.dries007.tfc.util.registry.RegistrySoilVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.Nullable;

public class PackedMudBlock extends Block {
   @Nullable
   private final Supplier<? extends Block> mud;
   private static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 14.0, 16.0);

   public PackedMudBlock(Properties properties, @Nullable Supplier<? extends Block> mud) {
      super(properties);
      this.mud = mud;
   }

   PackedMudBlock(Properties properties, SoilBlockType soil, RegistrySoilVariant variant) {
      this(properties, variant.getBlock(soil));
   }

   public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return SHAPE;
   }

   public VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
      return Shapes.block();
   }

   public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return Shapes.block();
   }

   public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType path) {
      return false;
   }

   @Nullable
   public BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction action, boolean simulate) {
      return context.getItemInHand().canPerformAction(action) && action == ToolActions.HOE_TILL && this.mud != null ? this.mud.get().defaultBlockState() : null;
   }
}
