package tfcflorae.common.blocks.wood;
import tfcflorae.common.TFCFBlockStateProperties;

import net.minecraft.util.RandomSource;
import java.util.function.Supplier;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.wood.TFCLeavesBlock;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.OffsetType;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

public abstract class TFCPalmLeavesBlock extends TFCLeavesBlock {
   public static final DirectionProperty DIRECTION = DirectionProperty.create("direction", Plane.HORIZONTAL);
   public static final BooleanProperty CORNER_BLOCK = BooleanProperty.create("corner_block");
   public static final BooleanProperty CENTER_BLOCK = BooleanProperty.create("center_block");
   public static final BooleanProperty TOP_BLOCK = BooleanProperty.create("top_block");
   public final int maxDecayDistance;
   public final ExtendedProperties properties;
   @Nullable
   public final Supplier<? extends Block> fallenLeaves;
   @Nullable
   public final Supplier<? extends Block> fallenTwig;
   @Nullable
   public final Supplier<? extends Block> sapling;
   @Nullable
   public final Supplier<? extends Block> trunk;

   public static TFCPalmLeavesBlock create(
      ExtendedProperties properties,
      int autumnIndex,
      @Nullable Supplier<? extends Block> fallenLeaves,
      @Nullable Supplier<? extends Block> fallenTwig,
      @Nullable Supplier<? extends Block> sapling,
      @Nullable Supplier<? extends Block> trunk
   ) {
      final IntegerProperty distanceProperty = distanceProperty();
      return new TFCPalmLeavesBlock(properties, autumnIndex, fallenLeaves, fallenTwig, sapling, trunk) {
         @Override
         protected IntegerProperty getDistanceProperty() {
            return distanceProperty;
         }
      };
   }

   public TFCPalmLeavesBlock(
      ExtendedProperties properties,
      int autumnIndex,
      @Nullable Supplier<? extends Block> fallenLeaves,
      @Nullable Supplier<? extends Block> fallenTwig,
      @Nullable Supplier<? extends Block> sapling,
      @Nullable Supplier<? extends Block> trunk
   ) {
      super(properties, autumnIndex, fallenLeaves, fallenTwig);
      this.maxDecayDistance = java.util.Collections.max(getDistanceProperty().getPossibleValues()) - 1;
      this.properties = properties;
      this.fallenLeaves = fallenLeaves;
      this.fallenTwig = fallenTwig;
      this.sapling = sapling;
      this.trunk = trunk;
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(PERSISTENT, false)).setValue(CORNER_BLOCK, false))
               .setValue(CENTER_BLOCK, false))
            .setValue(TOP_BLOCK, false)
      );
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{PERSISTENT, this.getDistanceProperty(), this.getFluidProperty(), DIRECTION, CORNER_BLOCK, CENTER_BLOCK, TOP_BLOCK});
   }

   /**
    * 与 TFC 的 {@code TFCLeavesBlock.getDistanceProperty()} 一致：返回范围 1..10 的
    * {@code TFCBlockStateProperties.DISTANCE_10}。父类 {@code maxDecayDistance} 由该范围推导（max - 1），
    * 因此这里不能换成原版 1..7 的 {@code BlockStateProperties.DISTANCE}，
    * 否则上限只有 6，新长出的树叶（距离 7/8）会立刻被判定超距删除。
    */
   public static IntegerProperty distanceProperty() {
      return net.dries007.tfc.common.blocks.TFCBlockStateProperties.DISTANCE_10;
   }

   @Override
   protected IntegerProperty getDistanceProperty() {
      return distanceProperty();
   }

   public boolean isTopBlock(BlockState state, LevelReader level, BlockPos pos) {
      return Helpers.isBlock(level.getBlockState(pos.below()), this.trunk.get()) && (Boolean)level.getBlockState(pos.below()).getValue(PalmTrunkBlock.TOP_BLOCK);
   }

   public boolean isCornerBlock(BlockState state, LevelReader level, BlockPos pos) {
      int connectedFaces = 0;

      for (Direction direction : Direction.values()) {
         connectedFaces += Helpers.isBlock(level.getBlockState(pos.relative(direction)), this.trunk.get()) ? 1 : 0;
      }

      return connectedFaces <= 0;
   }

   public boolean isCenterBlock(BlockState state, LevelReader level, BlockPos pos) {
      int connectedFaces = 0;

      for (Direction direction : Direction.values()) {
         connectedFaces += Helpers.isBlock(level.getBlockState(pos.relative(direction)), this.trunk.get()) ? 1 : 0;
      }

      return connectedFaces > 0;
   }

   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      BlockPos relativePos = pos.relative(((Direction)state.getValue(DIRECTION)).getOpposite());
      return level.getBlockState(relativePos).getBlock() != this.trunk.get()
            && level.getBlockState(relativePos.below()).getBlock() != this.trunk.get()
            && !(level.getBlockState(relativePos).getBlock() instanceof TFCPalmLeavesBlock)
         ? this.isTopBlock(state, level, pos)
         : true;
   }

   public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
      super.updateShape(state, facing, facingState, level, currentPos, facingPos);
      FluidHelpers.tickFluid(level, currentPos, state);
      if (!state.canSurvive(level, currentPos)) {
         level.destroyBlock(currentPos, !(Boolean)state.getValue(PERSISTENT));
         return Blocks.AIR.defaultBlockState();
      } else {
         for (Direction direction : Plane.HORIZONTAL) {
            if (this.isCornerBlock(state, level, currentPos)) {
               ((BlockState)((BlockState)state.setValue(DIRECTION, direction)).setValue(CORNER_BLOCK, true)).setValue(CENTER_BLOCK, false);
            } else if (this.isCenterBlock(state, level, currentPos)) {
               ((BlockState)((BlockState)state.setValue(DIRECTION, direction)).setValue(CORNER_BLOCK, false)).setValue(CENTER_BLOCK, true);
            } else if (this.isTopBlock(state, level, currentPos)) {
               ((BlockState)state.setValue(DIRECTION, direction)).setValue(TOP_BLOCK, true);
            }
         }

         return state;
      }
   }

   public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      super.tick(state, level, pos, random);
      if (!state.canSurvive(level, pos)) {
         level.destroyBlock(pos, true);
      }
   }

   public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      if ((Integer)state.getValue(this.getDistanceProperty()) > 2 && !(Boolean)state.getValue(PERSISTENT)) {
         level.removeBlock(pos, false);
         if (random.nextFloat() < 0.01F) {
            this.createDestructionEffects(state, level, pos, random, false);
         }

         doParticles(
            level,
            (double)((float)pos.getX() + random.nextFloat()),
            (double)((float)pos.getY() + random.nextFloat()),
            (double)((float)pos.getZ() + random.nextFloat()),
            1
         );
      }

      super.randomTick(state, level, pos, random);
   }

   public BlockState rotate(BlockState state, Rotation rot) {
      return (BlockState)state.setValue(DIRECTION, rot.rotate((Direction)state.getValue(DIRECTION)));
   }

   public BlockState mirror(BlockState state, Mirror mirrorIn) {
      return state.rotate(mirrorIn.getRotation((Direction)state.getValue(DIRECTION)));
   }

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      Level level = context.getLevel();
      BlockPos pos = context.getClickedPos();
      BlockState state = this.defaultBlockState();
      Direction direction = context.getHorizontalDirection().getOpposite();
      return FluidHelpers.fillWithFluid(
         (BlockState)((BlockState)((BlockState)((BlockState)state.setValue(DIRECTION, direction)).setValue(CORNER_BLOCK, this.isCornerBlock(state, level, pos)))
               .setValue(CENTER_BLOCK, this.isCenterBlock(state, level, pos)))
            .setValue(TOP_BLOCK, this.isTopBlock(state, level, pos)),
         level.getBlockState(pos).getFluidState().getType()
      );
   }

   public OffsetType getOffsetType() {
      return OffsetType.XYZ;
   }
}
