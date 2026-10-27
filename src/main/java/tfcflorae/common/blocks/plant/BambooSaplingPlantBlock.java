package tfcflorae.common.blocks.plant;

import net.minecraft.util.RandomSource;
import java.util.function.Supplier;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.plant.PlantBlock;
import net.dries007.tfc.common.fluids.FluidProperty;
import net.dries007.tfc.common.fluids.IFluidLoggable;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.registry.RegistryPlant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.OffsetType;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BambooLeaves;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ToolActions;
import org.jetbrains.annotations.Nullable;

public abstract class BambooSaplingPlantBlock extends PlantBlock implements IFluidLoggable {
   protected static final float SAPLING_AABB_OFFSET = 4.0F;
   protected static final VoxelShape SAPLING_SHAPE = Block.box(4.0, 0.0, 4.0, 12.0, 12.0, 12.0);
   public static final FluidProperty FLUID = TFCBlockStateProperties.ALL_WATER;
   private final Supplier<? extends Block> bambooStem;

   public static BambooSaplingPlantBlock create(final RegistryPlant plant, ExtendedProperties properties, Supplier<? extends Block> bambooStem) {
      return new BambooSaplingPlantBlock(properties, bambooStem) {
         public RegistryPlant getPlant() {
            return plant;
         }
      };
   }

   protected BambooSaplingPlantBlock(ExtendedProperties properties, Supplier<? extends Block> bambooStem) {
      super(properties);
      this.bambooStem = bambooStem;
      this.registerDefaultState((BlockState)this.getStateDefinition().any());
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder.add(new Property[]{this.getFluidProperty()}));
   }

   public OffsetType getOffsetType() {
      return OffsetType.XZ;
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      Vec3 vec3 = state.getOffset(level, pos);
      return SAPLING_SHAPE.move(vec3.x, vec3.y, vec3.z);
   }

   public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      if (random.nextDouble() < (Double)TFCConfig.SERVER.plantGrowthChance.get() && level.getBlockState(pos.above()).isAir() && level.getRawBrightness(pos.above(), 0) >= 9) {
         this.growBamboo(level, pos);
      }
   }

   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      return super.canSurvive(state, level, pos) || level.getBlockState(pos.below()).is(BlockTags.BAMBOO_PLANTABLE_ON);
   }

   public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
      if (!state.canSurvive(level, currentPos)) {
         return Blocks.AIR.defaultBlockState();
      } else {
         if (facing == Direction.UP && facingState.is(this.bambooStem.get())) {
            level.setBlock(currentPos, this.bambooStem.get().defaultBlockState(), 2);
         }

         return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
      }
   }

   public boolean isValidBonemealTarget(BlockGetter level, BlockPos pos, BlockState state, boolean isClient) {
      return level.getBlockState(pos.above()).isAir();
   }

   public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
      // 原版这里没有实现，竹苗完全不能催熟。骨粉后向上长出 2~4 节竹块。
      final int height = 2 + random.nextInt(3);
      BlockPos cursor = pos;
      for (int i = 0; i < height; i++) {
         final BlockPos next = cursor.above();
         if (!level.getBlockState(next).isAir()) {
            break;
         }
         level.setBlock(next, this.bambooStem.get().defaultBlockState().setValue(BambooPlantBlock.LEAVES, BambooLeaves.SMALL), 3);
         cursor = next;
      }
   }

   public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
      return player.getMainHandItem().canPerformAction(ToolActions.SWORD_DIG) ? 1.0F : super.getDestroyProgress(state, player, level, pos);
   }

   public void growBamboo(Level level, BlockPos state) {
      level.setBlock(state.above(), (BlockState)this.bambooStem.get().defaultBlockState().setValue(BambooPlantBlock.LEAVES, BambooLeaves.SMALL), 3);
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      BlockPos pos = context.getClickedPos();
      FluidState fluidState = context.getLevel().getFluidState(pos);
      BlockState state = this.updateStateWithCurrentMonth(this.defaultBlockState());
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
