package tfcflorae.common.blocks.wood;

import net.minecraft.util.RandomSource;
import net.dries007.tfc.common.TFCTags.Blocks;
import net.dries007.tfc.common.blocks.EntityBlockExtension;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.IForgeBlockExtension;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.common.fluids.FluidProperty;
import net.dries007.tfc.common.fluids.IFluidLoggable;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;
import tfcflorae.common.blockentities.TFCFTickCounterBlockEntity;
import tfcflorae.world.feature.tree.TFCFBambooTreeGrower;

public class TFCFBambooSaplingBlock extends SaplingBlock implements IFluidLoggable, IForgeBlockExtension, EntityBlockExtension {
   public static final FluidProperty FLUID = TFCBlockStateProperties.ALL_WATER;
   private final ExtendedProperties properties;
   private final int daysToGrow;

   public TFCFBambooSaplingBlock(TFCFBambooTreeGrower bambooTree, ExtendedProperties properties, int days) {
      super(bambooTree, properties.properties());
      this.properties = properties;
      this.daysToGrow = days;
   }

   public ExtendedProperties getExtendedProperties() {
      return this.properties;
   }

   public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      if (level.getMaxLocalRawBrightness(pos.above()) >= 9 && random.nextInt(7) == 0) {
         if (!level.isAreaLoaded(pos, 1)) {
            return;
         }

         if (level.getBlockEntity(pos) instanceof TFCFTickCounterBlockEntity counter
            && (double)counter.getTicksSinceUpdate() > (double)(24000 * this.getDaysToGrow()) * (Double)TFCConfig.SERVER.globalSaplingGrowthModifier.get()) {
            this.advanceTree(level, pos, (BlockState)state.setValue(STAGE, 1), random);
         }
      }
   }

   public boolean isValidBonemealTarget(BlockGetter level, BlockPos pos, BlockState state, boolean isClient) {
      return false;
   }

   public void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder.add(new Property[]{this.getFluidProperty()}));
   }

   public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
      FluidHelpers.tickFluid(level, currentPos, state);
      return state;
   }

   public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
      TFCFTickCounterBlockEntity.reset(level, pos);
      super.setPlacedBy(level, pos, state, placer, stack);
   }

   protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
      return super.mayPlaceOn(state, level, pos)
         || Helpers.isBlock(state.getBlock(), Blocks.BUSH_PLANTABLE_ON)
         || Helpers.isBlock(state.getBlock(), Blocks.TREE_GROWS_ON)
         || Helpers.isBlock(state.getBlock(), BlockTags.SAND)
         || Helpers.isBlock(state.getBlock(), net.minecraftforge.common.Tags.Blocks.GRAVEL);
   }

   public int getDaysToGrow() {
      return this.daysToGrow;
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
