package tfcflorae.common.blocks.rock;

import java.util.stream.Stream;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.GroundcoverBlock;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.fluids.FluidProperty;
import net.dries007.tfc.common.fluids.IFluidLoggable;
import net.dries007.tfc.common.fluids.TFCFluids;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import tfcflorae.common.TFCFTags;

public class LooseFlintBlock extends GroundcoverBlock implements IFluidLoggable {
   public static final FluidProperty ALL_WATER_AND_LAVA = FluidProperty.create(
      "fluid",
      Stream.of(
         Fluids.EMPTY,
         Fluids.FLOWING_WATER,
         Fluids.WATER,
         TFCFluids.SALT_WATER,
         TFCFluids.SPRING_WATER,
         TFCFluids.RIVER_WATER,
         Fluids.LAVA,
         Fluids.FLOWING_LAVA
      )
   );
   public static final FluidProperty FLUID = ALL_WATER_AND_LAVA;
   public static final IntegerProperty COUNT = TFCBlockStateProperties.COUNT_1_3;
   private static final VoxelShape ONE = box(5.0, 0.0, 5.0, 11.0, 2.0, 11.0);
   private static final VoxelShape TWO = box(2.0, 0.0, 2.0, 14.0, 2.0, 14.0);
   private static final VoxelShape THREE = box(5.0, 0.0, 5.0, 11.0, 4.0, 11.0);

   public LooseFlintBlock(Properties properties) {
      super(ExtendedProperties.of(properties), Shapes.empty(), null);
      this.registerDefaultState((BlockState)this.defaultBlockState().setValue(COUNT, 1));
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder.add(new Property[]{COUNT}));
   }

   public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
      ItemStack stack = player.getMainHandItem();
      if (stack.getItem() != this.asItem() && stack.getItem() != Items.FLINT && !Helpers.isItem(stack.getItem(), TFCFTags.Items.FLINT_KNAPPING)) {
         return super.use(state, worldIn, pos, player, handIn, hit);
      } else {
         if (!worldIn.isClientSide() && handIn == InteractionHand.MAIN_HAND && state.getBlock() == this) {
            int count = (Integer)state.getValue(COUNT);
            if (count < 3) {
               worldIn.setBlockAndUpdate(pos, (BlockState)state.setValue(COUNT, count + 1));
               stack.shrink(1);
               return InteractionResult.SUCCESS;
            }
         }

         return InteractionResult.PASS;
      }
   }

   public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
      switch (state.getValue(COUNT)) {
         case 1:
            return ONE;
         case 2:
            return TWO;
         case 3:
            return THREE;
         default:
            throw new IllegalStateException("Unknown value for property LooseFlintBlock: " + state.getValue(COUNT));
      }
   }

   public FluidProperty getFluidProperty() {
      return FLUID;
   }
}
