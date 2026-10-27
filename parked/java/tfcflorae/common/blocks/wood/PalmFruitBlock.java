package tfcflorae.common.blocks.wood;

import net.minecraft.util.RandomSource;
import java.util.function.Supplier;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.IForgeBlockExtension;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.common.fluids.FluidProperty;
import net.dries007.tfc.common.fluids.IFluidLoggable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.OffsetType;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;
import tfcflorae.common.blocks.ICooldown;

public class PalmFruitBlock extends Block implements IForgeBlockExtension, IFluidLoggable, ICooldown {
   public static final DirectionProperty DIRECTION = DirectionProperty.create("direction", Plane.HORIZONTAL);
   public static final IntegerProperty AGE = TFCBlockStateProperties.AGE_3;
   public static final FluidProperty FLUID = TFCBlockStateProperties.ALL_WATER;
   public final ExtendedProperties properties;
   @Nullable
   public final Supplier<? extends Block> trunk;
   @Nullable
   public final Supplier<? extends Item> productItem;

   public PalmFruitBlock(ExtendedProperties properties, @Nullable Supplier<? extends Block> trunk, Supplier<? extends Item> productItem) {
      super(properties.properties());
      this.properties = properties;
      this.trunk = trunk;
      this.productItem = productItem;
   }

   public ExtendedProperties getExtendedProperties() {
      return this.properties;
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      builder.add(new Property[]{this.getFluidProperty(), AGE, DIRECTION});
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      Direction direction = context.getHorizontalDirection().getOpposite();
      FluidState fluid = context.getLevel().getFluidState(context.getClickedPos());
      return (BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(this.getFluidProperty(), this.getFluidProperty().keyForOrEmpty(fluid.getType())))
            .setValue(DIRECTION, direction))
         .setValue(AGE, 0);
   }

   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      return level.getBlockState(pos.relative(((Direction)state.getValue(DIRECTION)).getOpposite())).getBlock().equals(this.trunk.get());
   }

   public BlockState updateShape(BlockState state, Direction direction, BlockState newState, LevelAccessor level, BlockPos pos, BlockPos newPos) {
      FluidHelpers.tickFluid(level, pos, state);

      for (Direction around : Plane.HORIZONTAL) {
         if (level.getBlockState(pos.relative(direction.getOpposite()).above()).getBlock().equals(this.trunk.get())) {
            state.setValue(DIRECTION, around);
         }
      }

      return state.canSurvive(level, pos)
            && level.getBlockState(pos.relative(((Direction)state.getValue(DIRECTION)).getOpposite()).above()).getBlock().equals(this.trunk.get())
         ? state
         : Blocks.AIR.defaultBlockState();
   }

   public OffsetType getOffsetType() {
      return OffsetType.XYZ;
   }

   public FluidProperty getFluidProperty() {
      return FLUID;
   }

   public FluidState getFluidState(BlockState state) {
      return super.getFluidState(state);
   }

   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      if ((Integer)state.getValue(AGE) == 3 && this.productItem != null) {
         level.playSound(player, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.PLAYERS, 1.0F, level.getRandom().nextFloat() + 0.7F + 0.3F);
         if (!level.isClientSide()) {
            ItemHandlerHelper.giveItemToPlayer(player, this.getProductItem(level.random));
            level.destroyBlock(pos, false);
         }

         return InteractionResult.SUCCESS;
      } else {
         return InteractionResult.PASS;
      }
   }

   @Nullable
   public ItemStack getProductItem(RandomSource random) {
      return new ItemStack((ItemLike)this.productItem.get());
   }
}
