package tfcflorae.common.blocks;

import java.util.function.Supplier;
import net.dries007.tfc.common.blocks.ExtendedBlock;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.GroundcoverBlock;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.common.fluids.FluidProperty;
import net.dries007.tfc.common.fluids.IFluidLoggable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TFCFGroundcoverBlock extends ExtendedBlock implements IFluidLoggable {
   public static final FluidProperty FLUID = TFCBlockStateProperties.ALL_WATER;
   public static final VoxelShape FLAT = box(2.0, 0.0, 2.0, 14.0, 2.0, 14.0);
   public static final VoxelShape SMALL = box(5.0, 0.0, 5.0, 11.0, 2.0, 11.0);
   public static final VoxelShape MEDIUM = box(5.0, 0.0, 5.0, 11.0, 4.0, 11.0);
   public static final VoxelShape PIXEL_HIGH = box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
   public static final VoxelShape TWIG = box(2.0, 0.0, 2.0, 14.0, 2.0, 14.0);
   private final VoxelShape shape;
   @Nullable
   private final Supplier<? extends Item> pickBlock;

   public static GroundcoverBlock twig(ExtendedProperties properties) {
      return new GroundcoverBlock(properties.flammable(60, 30), TWIG, null);
   }

   public static GroundcoverBlock looseOre(Properties properties) {
      return new GroundcoverBlock(ExtendedProperties.of(properties), SMALL, null);
   }

   public TFCFGroundcoverBlock(TFCFGroundcoverBlockType cover) {
      this(
         ExtendedProperties.of(MapColor.PLANT).noCollission().replaceable().strength(0.05F, 0.0F).sound(SoundType.NETHER_WART).noCollission(),
         cover.getShape(),
         cover.getVanillaItem()
      );
   }

   public TFCFGroundcoverBlock(ExtendedProperties properties, VoxelShape shape, @Nullable Supplier<? extends Item> pickBlock) {
      super(properties);
      this.shape = shape;
      this.pickBlock = pickBlock;
      this.registerDefaultState((BlockState)((BlockState)this.getStateDefinition().any()).setValue(this.getFluidProperty(), this.getFluidProperty().keyFor(Fluids.EMPTY)));
   }

   public boolean canPlaceLiquid(BlockGetter level, BlockPos pos, BlockState state, Fluid fluid) {
      return fluid instanceof FlowingFluid && !this.getFluidProperty().canContain(fluid);
   }

   public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidStateIn) {
      if (fluidStateIn.getType() instanceof FlowingFluid && !this.getFluidProperty().canContain(fluidStateIn.getType())) {
         level.destroyBlock(pos, true);
         level.setBlock(pos, fluidStateIn.createLegacyBlock(), 2);
         return true;
      } else {
         return false;
      }
   }

   @NotNull
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
      BlockState state = this.defaultBlockState();
      return this.getFluidProperty().canContain(fluidState.getType())
         ? (BlockState)state.setValue(this.getFluidProperty(), this.getFluidProperty().keyFor(fluidState.getType()))
         : state;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{this.getFluidProperty()});
   }

   public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
      FluidHelpers.tickFluid(level, currentPos, state);
      return state.canSurvive(level, currentPos) ? super.updateShape(state, facing, facingState, level, currentPos, facingPos) : state.getFluidState().createLegacyBlock();
   }

   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
      if (!player.isCreative() && level instanceof ServerLevel serverLevel) {
         BlockEntity entity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
         getDrops(state, serverLevel, pos, entity, null, ItemStack.EMPTY).forEach(stackToSpawn -> ItemHandlerHelper.giveItemToPlayer(player, stackToSpawn));
      }

      level.removeBlock(pos, false);
      return InteractionResult.SUCCESS;
   }

   public FluidState getFluidState(BlockState state) {
      return super.getFluidState(state);
   }

   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      return level.getBlockState(pos.below()).isFaceSturdy(level, pos, Direction.UP);
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return this.shape;
   }

   public FluidProperty getFluidProperty() {
      return FLUID;
   }

   public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
      return this.pickBlock != null ? new ItemStack((ItemLike)this.pickBlock.get()) : super.getCloneItemStack(state, target, level, pos, player);
   }
}
