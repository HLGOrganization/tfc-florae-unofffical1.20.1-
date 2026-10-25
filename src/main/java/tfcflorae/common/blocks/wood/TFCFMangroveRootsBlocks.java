package tfcflorae.common.blocks.wood;

import net.dries007.tfc.common.TFCTags.Entities;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.common.fluids.FluidProperty;
import net.dries007.tfc.common.fluids.IFluidLoggable;
import net.dries007.tfc.common.fluids.FluidProperty.FluidKey;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import tfcflorae.common.blocks.TFCFBlocks;

public class TFCFMangroveRootsBlocks extends Block implements IFluidLoggable {
   public static final FluidProperty FLUID = TFCBlockStateProperties.ALL_WATER;
   public final TFCFWood wood;
   public final ExtendedProperties properties;

   public TFCFMangroveRootsBlocks(TFCFWood wood, ExtendedProperties properties) {
      super(properties.properties());
      this.wood = wood;
      this.properties = properties;
      this.registerDefaultState((BlockState)this.getStateDefinition().any());
   }

   public ExtendedProperties getExtendedProperties() {
      return this.properties;
   }

   public boolean skipRendering(BlockState state, BlockState stateFrom, Direction direction) {
      return stateFrom.is((Block)TFCFBlocks.MANGROVE_ROOTS.get(this.wood).get()) && direction.getAxis() == Axis.Y;
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      FluidState fluid = context.getLevel().getFluidState(context.getClickedPos());
      return (BlockState)this.defaultBlockState().setValue(this.getFluidProperty(), this.getFluidProperty().keyForOrEmpty(fluid.getType()));
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{this.getFluidProperty()});
   }

   public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
      FluidHelpers.tickFluid(level, currentPos, state);
      return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
   }

   public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
      return 1;
   }

   public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return Shapes.empty();
   }

   public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
      return 0.2F;
   }

   public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
      if (entity.getBoundingBox().getYsize() > 1.0 || !(entity instanceof WaterAnimal)) {
         float modifier = ((Double)TFCConfig.SERVER.leavesMovementModifier.get()).floatValue();
         if (modifier < 1.0F && ((FluidKey)state.getValue(this.getFluidProperty())).getFluid() == Fluids.EMPTY) {
            entity.makeStuckInBlock(state, new net.minecraft.world.phys.Vec3(0.8F, 0.15F, 0.8F));
         }

         if (Helpers.isEntity(entity, Entities.DESTROYED_BY_LEAVES)) {
            entity.kill();
         }
      }
   }

   public FluidProperty getFluidProperty() {
      return FLUID;
   }

   public FluidState getFluidState(BlockState state) {
      return super.getFluidState(state);
   }
}
