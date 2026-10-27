package tfcflorae.common.blocks.wood;

import net.minecraft.util.RandomSource;
import net.dries007.tfc.common.blocks.EntityBlockExtension;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.IForgeBlockExtension;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.wood.TFCSaplingBlock;
import net.dries007.tfc.common.blocks.wood.Wood.BlockType;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.common.fluids.FluidProperty;
import net.dries007.tfc.common.fluids.IFluidLoggable;
import net.dries007.tfc.common.fluids.TFCFluids;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.OffsetType;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.Tags.Blocks;
import org.jetbrains.annotations.Nullable;
import tfcflorae.common.TFCFTags;
import tfcflorae.common.blocks.TFCFBlocks;
import tfcflorae.util.TFCFHelpers;
import net.dries007.tfc.world.feature.tree.TFCTreeGrower;

public class TFCFMangrovePropaguleBlock extends TFCSaplingBlock implements IForgeBlockExtension, EntityBlockExtension, IFluidLoggable {
   private static final VoxelShape[] SHAPES = new VoxelShape[]{
      Block.box(7.0, 13.0, 7.0, 9.0, 16.0, 9.0),
      Block.box(7.0, 10.0, 7.0, 9.0, 16.0, 9.0),
      Block.box(7.0, 7.0, 7.0, 9.0, 16.0, 9.0),
      Block.box(7.0, 3.0, 7.0, 9.0, 16.0, 9.0),
      Block.box(7.0, 0.0, 7.0, 9.0, 16.0, 9.0)
   };
   public static final IntegerProperty AGE = TFCBlockStateProperties.AGE_4;
   public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
   public static final BooleanProperty HANGING = BlockStateProperties.HANGING;
   public static final FluidProperty FLUID = TFCBlockStateProperties.ALL_WATER;
   public final TFCFWood wood;
   public final ExtendedProperties properties;
   public final int daysToGrow;

   public TFCFMangrovePropaguleBlock(TFCFWood wood, ExtendedProperties properties, int days) {
      super(
         new TFCTreeGrower(
            TFCFHelpers.identifier("tree/" + wood.getSerializedName()),
            TFCFHelpers.identifier("tree/" + wood.getSerializedName() + "_tall")
         ),
         properties,
         () -> days,
         false
      );
      this.wood = wood;
      this.properties = properties;
      this.daysToGrow = days;
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(STAGE, 0)).setValue(AGE, 0))
               .setValue(WATERLOGGED, false))
            .setValue(HANGING, false)
      );
   }


   public ExtendedProperties getExtendedProperties() {
      return this.properties;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{STAGE, AGE, WATERLOGGED, HANGING, this.getFluidProperty()});
   }

   protected boolean mayPlaceOn(BlockState state, BlockGetter block, BlockPos pos) {
      return super.mayPlaceOn(state, block, pos)
         || Helpers.isBlock(state.getBlock(), BlockTags.SAND)
         || Helpers.isBlock(state.getBlock(), Blocks.SAND)
         || Helpers.isBlock(state.getBlock(), TFCFTags.Blocks.CLAY)
         || Helpers.isBlock(state.getBlock(), net.dries007.tfc.common.TFCTags.Blocks.BUSH_PLANTABLE_ON)
         || Helpers.isBlock(state, net.dries007.tfc.common.TFCTags.Blocks.SEA_BUSH_PLANTABLE_ON)
         || Helpers.isBlock(state.getBlock(), net.dries007.tfc.common.TFCTags.Blocks.TREE_GROWS_ON);
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      FluidState fluid = context.getLevel().getFluidState(context.getClickedPos());
      return (BlockState)((BlockState)this.defaultBlockState().setValue(this.getFluidProperty(), this.getFluidProperty().keyForOrEmpty(fluid.getType())))
         .setValue(AGE, 4);
   }

   public VoxelShape getShape(BlockState state, BlockGetter block, BlockPos pos, CollisionContext context) {
      Vec3 offset = state.getOffset(block, pos);
      VoxelShape shape = !state.getValue(HANGING) ? SHAPES[4] : SHAPES[state.getValue(AGE)];
      return shape.move(offset.x, offset.y, offset.z);
   }

   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      FluidState fluidState = level.getFluidState(pos);
      return isHanging(state)
         ? level.getBlockState(pos.above()).is((Block)TFCFBlocks.WOODS_SEASONAL_LEAVES.get(this.wood).get())
         : super.canSurvive(state, level, pos)
            || Helpers.isBlock(state, net.dries007.tfc.common.TFCTags.Blocks.BUSH_PLANTABLE_ON)
            || Helpers.isBlock(state, net.dries007.tfc.common.TFCTags.Blocks.SEA_BUSH_PLANTABLE_ON)
            || fluidState.getType() == TFCFluids.SALT_WATER.getSource()
            || fluidState.getType() == TFCFluids.SPRING_WATER.getSource()
            || fluidState.getType() == Fluids.WATER.getSource();
   }

   public BlockState updateShape(BlockState state, Direction direction, BlockState newState, LevelAccessor level, BlockPos pos, BlockPos newPos) {
      FluidHelpers.tickFluid(level, pos, state);
      return direction == Direction.UP && !state.canSurvive(level, pos)
         ? net.minecraft.world.level.block.Blocks.AIR.defaultBlockState()
         : super.updateShape(state, direction, newState, level, pos, newPos);
   }

   public OffsetType getOffsetType() {
      return OffsetType.XZ;
   }

   public FluidProperty getFluidProperty() {
      return FLUID;
   }

   public FluidState getFluidState(BlockState state) {
      return super.getFluidState(state);
   }

   public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      if (!isHanging(state)) {
         super.randomTick(state, level, pos, random);
      } else {
         if (!ageAtMax(state)) {
            level.setBlock(pos, (BlockState)state.cycle(AGE), 2);
         }

      }
   }

   public int getDaysToGrow() {
      return this.daysToGrow;
   }

   private static boolean isHanging(BlockState state) {
      return (Boolean)state.getValue(HANGING);
   }

   private static boolean ageAtMax(BlockState state) {
      return (Integer)state.getValue(AGE) == 4;
   }

   public BlockState createPropagule() {
      return this.createPropagule(0);
   }

   public BlockState createPropagule(int age) {
      return (BlockState)((BlockState)((Block)TFCFBlocks.WOODS.get(this.wood).get(BlockType.SAPLING).get()).defaultBlockState().setValue(HANGING, true))
         .setValue(AGE, age);
   }

}
