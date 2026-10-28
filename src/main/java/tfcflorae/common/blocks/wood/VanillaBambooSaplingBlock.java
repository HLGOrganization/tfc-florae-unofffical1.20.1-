package tfcflorae.common.blocks.wood;

import net.dries007.tfc.common.TFCTags.Blocks;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.wood.TFCSaplingBlock;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.common.fluids.FluidProperty;
import net.dries007.tfc.common.fluids.IFluidLoggable;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.dries007.tfc.world.feature.tree.TFCTreeGrower;
import org.jetbrains.annotations.Nullable;

public class VanillaBambooSaplingBlock extends TFCSaplingBlock implements IFluidLoggable {
   public static final FluidProperty FLUID = TFCBlockStateProperties.ALL_WATER;
   public VanillaBambooSaplingBlock(TFCTreeGrower bambooTree, ExtendedProperties properties, int daysToGrow) {
      super(bambooTree, properties, () -> daysToGrow, false);
   }

   public boolean isValidBonemealTarget(BlockGetter level, BlockPos pos, BlockState state, boolean isClient) {
      // 上方为空才能长高（与 vanilla 竹笋一致）
      return level.getBlockState(pos.above()).isAir();
   }

   @Override
   public boolean isBonemealSuccess(net.minecraft.world.level.Level level, net.minecraft.util.RandomSource random, BlockPos pos, BlockState state) {
      // 原版 SaplingBlock 只有 45% 成功率，玩家连点骨粉经常「没反应」。
      // 竹子是一次性长成的植物，这里让每次骨粉都必定生效。
      return true;
   }

   public void performBonemeal(net.minecraft.server.level.ServerLevel level, net.minecraft.util.RandomSource random, BlockPos pos, BlockState state) {
      // vanilla SaplingBlock 的两段式需要两次骨粉（STAGE 0 -> 1 -> 长成），
      // 这里直接推进到 STAGE 1 再调用 advanceTree，一次骨粉即可长出竹子。
      // advanceTree 正是 TFC 树苗 randomTick 里使用的同一条生长路径。
      final BlockState grown = state.hasProperty(net.minecraft.world.level.block.SaplingBlock.STAGE)
         ? state.setValue(net.minecraft.world.level.block.SaplingBlock.STAGE, 1)
         : state;
      this.advanceTree(level, pos, grown, random);
   }

   public void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder.add(new Property[]{this.getFluidProperty()}));
   }

   public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
      FluidHelpers.tickFluid(level, currentPos, state);
      return state;
   }

   protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
      return super.mayPlaceOn(state, level, pos)
         || Helpers.isBlock(state.getBlock(), Blocks.BUSH_PLANTABLE_ON)
         || Helpers.isBlock(state.getBlock(), Blocks.TREE_GROWS_ON)
         || Helpers.isBlock(state.getBlock(), BlockTags.SAND)
         || Helpers.isBlock(state.getBlock(), net.minecraftforge.common.Tags.Blocks.GRAVEL);
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      BlockPos pos = context.getClickedPos();
      FluidState fluidState = context.getLevel().getFluidState(pos);
      BlockState state = this.defaultBlockState();
      if (this.getFluidProperty().canContain(fluidState.getType())) {
         state = (BlockState)state.setValue(this.getFluidProperty(), this.getFluidProperty().keyFor(fluidState.getType()));
      }

      return state;
   }

   public FluidProperty getFluidProperty() {
      return FLUID;
   }

   public FluidState getFluidState(BlockState state) {
      return super.getFluidState(state);
   }
}
