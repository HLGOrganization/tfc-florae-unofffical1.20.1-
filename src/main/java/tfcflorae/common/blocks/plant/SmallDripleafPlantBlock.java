package tfcflorae.common.blocks.plant;

import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.IForgeBlockExtension;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.plant.TallWaterPlantBlock;
import net.dries007.tfc.common.blocks.plant.ITallPlant.Part;
import net.dries007.tfc.common.fluids.FluidProperty;
import net.dries007.tfc.common.fluids.IFluidLoggable;
import net.dries007.tfc.util.registry.RegistryPlant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.OffsetType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public abstract class SmallDripleafPlantBlock extends TallWaterPlantBlock implements IForgeBlockExtension, IFluidLoggable {
   public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
   public static final EnumProperty<Part> PART = TFCBlockStateProperties.TALL_PLANT_PART;
   public static final float AABB_OFFSET = 6.0F;
   public static final VoxelShape PLANT_SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 13.0, 14.0);

   public static SmallDripleafPlantBlock create(final RegistryPlant plant, final FluidProperty fluid, Properties properties) {
      return new SmallDripleafPlantBlock(ExtendedProperties.of(properties)) {
         public RegistryPlant getPlant() {
            return plant;
         }

         public FluidProperty getFluidProperty() {
            return fluid;
         }
      };
   }

   protected SmallDripleafPlantBlock(ExtendedProperties properties) {
      super(properties);
      this.registerDefaultState(
         (BlockState)((BlockState)((BlockState)((BlockState)this.getStateDefinition().any())
                  .setValue(this.getFluidProperty(), this.getFluidProperty().keyFor(Fluids.EMPTY)))
               .setValue(PART, Part.LOWER))
            .setValue(FACING, Direction.NORTH)
      );
   }

   public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
      super.setPlacedBy(level, pos, state, placer, stack);
      level.setBlockAndUpdate(pos.above(), (BlockState)((BlockState)this.defaultBlockState().setValue(PART, Part.UPPER)).setValue(FACING, (Direction)state.getValue(FACING)));
   }

   public void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder.add(new Property[]{FACING}));
   }

   public BlockState rotate(BlockState state, Rotation rotation) {
      return (BlockState)state.setValue(FACING, rotation.rotate((Direction)state.getValue(FACING)));
   }

   public BlockState mirror(BlockState state, Mirror mirror) {
      return state.rotate(mirror.getRotation((Direction)state.getValue(FACING)));
   }

   public OffsetType getOffsetType() {
      return OffsetType.XYZ;
   }

   public float getMaxVerticalOffset() {
      return 0.1F;
   }
}
