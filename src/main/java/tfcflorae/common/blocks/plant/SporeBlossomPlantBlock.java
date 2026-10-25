package tfcflorae.common.blocks.plant;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.util.RandomSource;
import net.dries007.tfc.common.TFCTags.Blocks;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.plant.PlantBlock;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.common.fluids.FluidProperty;
import net.dries007.tfc.common.fluids.IFluidLoggable;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.registry.RegistryPlant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public abstract class SporeBlossomPlantBlock extends PlantBlock implements IFluidLoggable {
   public static final int ADD_PARTICLE_ATTEMPTS = 14;
   public static final int PARTICLE_XZ_RADIUS = 10;
   public static final int PARTICLE_Y_MAX = 10;
   public static final DirectionProperty FACING_SPORE = BlockStateProperties.FACING;
   public static final VoxelShape UP_SHAPE = box(0.0, 0.0, 0.0, 16.0, 12.0, 16.0);
   public static final VoxelShape DOWN_SHAPE = box(0.0, 4.0, 0.0, 16.0, 16.0, 16.0);
   public static final VoxelShape NORTH_SHAPE = box(0.0, 0.0, 4.0, 16.0, 16.0, 16.0);
   public static final VoxelShape SOUTH_SHAPE = box(0.0, 0.0, 0.0, 16.0, 16.0, 12.0);
   public static final VoxelShape WEST_SHAPE = box(4.0, 0.0, 0.0, 16.0, 16.0, 16.0);
   public static final VoxelShape EAST_SHAPE = box(0.0, 0.0, 0.0, 12.0, 16.0, 16.0);
   public static final Map<Direction, VoxelShape> SHAPES = ImmutableMap.of(
      Direction.UP,
      UP_SHAPE,
      Direction.DOWN,
      DOWN_SHAPE,
      Direction.NORTH,
      NORTH_SHAPE,
      Direction.SOUTH,
      SOUTH_SHAPE,
      Direction.WEST,
      WEST_SHAPE,
      Direction.EAST,
      EAST_SHAPE
   );

   public static SporeBlossomPlantBlock create(final RegistryPlant plant, ExtendedProperties properties, final FluidProperty fluid) {
      return new SporeBlossomPlantBlock(properties, fluid) {
         public RegistryPlant getPlant() {
            return plant;
         }

         public FluidProperty getFluidProperty() {
            return fluid;
         }
      };
   }

   public SporeBlossomPlantBlock(ExtendedProperties properties, FluidProperty fluid) {
      super(properties);
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)this.getStateDefinition().any()).setValue(FACING_SPORE, Direction.UP))
            .setValue(this.getFluidProperty(), this.getFluidProperty().keyFor(Fluids.EMPTY))
      );
   }

   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      int i = pos.getX();
      int j = pos.getY();
      int k = pos.getZ();
      double d0 = (double)i + random.nextDouble();
      double d1 = (double)j + 0.7;
      double d2 = (double)k + random.nextDouble();
      level.addParticle(ParticleTypes.FALLING_SPORE_BLOSSOM, d0, d1, d2, 0.0, 0.0, 0.0);
      MutableBlockPos blockpos$mutableBlockpos = new MutableBlockPos();

      for (int l = 0; l < 14; l++) {
         blockpos$mutableBlockpos.set(i + Mth.nextInt(random, -10, 10), j - random.nextInt(10), k + Mth.nextInt(random, -10, 10));
         BlockState blockstate = level.getBlockState(blockpos$mutableBlockpos);
         if (!blockstate.isCollisionShapeFullBlock(level, blockpos$mutableBlockpos)) {
            level.addParticle(
               ParticleTypes.SPORE_BLOSSOM_AIR,
               (double)blockpos$mutableBlockpos.getX() + random.nextDouble(),
               (double)blockpos$mutableBlockpos.getY() + random.nextDouble(),
               (double)blockpos$mutableBlockpos.getZ() + random.nextDouble(),
               0.0,
               0.0,
               0.0
            );
         }
      }
   }

   public void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
      if (!this.canSurvive(state, level, pos)) {
         level.destroyBlock(pos, false);
      }
   }

   public BlockState updateShape(BlockState state, Direction direction, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
      FluidHelpers.tickFluid(level, currentPos, state);
      return direction.getOpposite() == state.getValue(FACING_SPORE) && !Helpers.isBlock(facingState, Blocks.CREEPING_PLANTABLE_ON)
         ? net.minecraft.world.level.block.Blocks.AIR.defaultBlockState()
         : state;
   }

   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      BlockState attachedState = level.getBlockState(pos.relative(((Direction)state.getValue(FACING_SPORE)).getOpposite()));
      return Helpers.isBlock(attachedState, Blocks.CREEPING_PLANTABLE_ON);
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return SHAPES.get(state.getValue(FACING_SPORE));
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      Direction direction = context.getClickedFace();
      BlockPos pos = context.getClickedPos();
      FluidState fluidState = context.getLevel().getFluidState(pos);
      BlockState state = (BlockState)this.updateStateWithCurrentMonth(this.defaultBlockState()).setValue(FACING_SPORE, direction);
      if (this.getFluidProperty().canContain(fluidState.getType())) {
         state = (BlockState)this.updateStateWithCurrentMonth(
               (BlockState)state.setValue(this.getFluidProperty(), this.getFluidProperty().keyFor(fluidState.getType()))
            )
            .setValue(FACING_SPORE, direction);
      }

      return state;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      if (this.getPlant().getStageProperty() != null) {
         builder.add(new Property[]{this.getPlant().getStageProperty()});
      }

      builder.add(new Property[]{AGE, FACING_SPORE, this.getFluidProperty()});
   }

   public FluidState getFluidState(BlockState state) {
      return super.getFluidState(state);
   }

   public BlockState rotate(BlockState state, Rotation rotation) {
      return (BlockState)state.setValue(FACING_SPORE, rotation.rotate((Direction)state.getValue(FACING_SPORE)));
   }

   public BlockState mirror(BlockState state, Mirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.getValue(FACING_SPORE)));
   }

   public boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
      return true;
   }
}
