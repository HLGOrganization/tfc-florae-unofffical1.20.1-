package tfcflorae.common.blocks.plant;
import net.minecraft.world.level.storage.loot.LootParams;

import java.util.Collections;
import java.util.List;
import net.minecraft.util.RandomSource;
import java.util.function.Supplier;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.IForgeBlockExtension;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.climate.Climate;
import net.dries007.tfc.util.registry.RegistryPlant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GrowingPlantBodyBlock;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import tfcflorae.Config;
import tfcflorae.common.blocks.ICooldown;
import tfcflorae.util.TFCFHelpers;

public abstract class TFCFBodyPlantBlock extends GrowingPlantBodyBlock implements IForgeBlockExtension, ICooldown {
   public static final VoxelShape BODY_SHAPE = box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);
   public static final VoxelShape THIN_BODY_SHAPE = box(5.0, 0.0, 5.0, 11.0, 16.0, 11.0);
   public static final VoxelShape WEEPING_SHAPE = box(4.0, 9.0, 4.0, 12.0, 16.0, 12.0);
   public static final VoxelShape TWISTING_SHAPE = box(4.0, 0.0, 4.0, 12.0, 15.0, 12.0);
   public static final VoxelShape TWISTING_THIN_SHAPE = box(5.0, 0.0, 5.0, 11.0, 12.0, 11.0);
   private final Supplier<? extends Block> headBlock;
   private final ExtendedProperties properties;
   public boolean canWalkThroughEffortlessly;
   public boolean isDead;
   public long cooldown;

   public static TFCFBodyPlantBlock create(
      final RegistryPlant plant, ExtendedProperties properties, Supplier<? extends Block> headBlock, VoxelShape shape, Direction direction
   ) {
      return new TFCFBodyPlantBlock(properties, headBlock, shape, direction) {
         @Override
         public RegistryPlant getPlant() {
            return plant;
         }
      };
   }

   protected TFCFBodyPlantBlock(ExtendedProperties properties, Supplier<? extends Block> headBlock, VoxelShape shape, Direction direction) {
      super(properties.properties(), direction, shape, true);
      this.headBlock = headBlock;
      this.properties = properties;
      this.cooldown = Long.MIN_VALUE;
      this.isDead = false;
      BlockState stateDefinition = (BlockState)this.getStateDefinition().any();
      IntegerProperty stageProperty = this.getPlant().getStageProperty();
      if (stageProperty != null) {
         stateDefinition = (BlockState)stateDefinition.setValue(stageProperty, 0);
      }

      this.registerDefaultState(stateDefinition);
   }

   public abstract RegistryPlant getPlant();

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return this.updateStateWithCurrentMonth(this.defaultBlockState());
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      super.createBlockStateDefinition(builder);
      if (this.getPlant().getStageProperty() != null) {
         builder.add(new Property[]{this.getPlant().getStageProperty()});
      }
   }

   public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
      if (entity != null) {
         if (!TFCFHelpers.canWalkThroughEffortlessly(entity) && !this.isDead) {
            this.canWalkThroughEffortlessly = false;
         } else {
            this.canWalkThroughEffortlessly = true;
         }
      }
   }

   public float getSpeedFactor() {
      float modifier = ((Double)TFCConfig.SERVER.plantsMovementModifier.get()).floatValue();
      return this.canWalkThroughEffortlessly ? 1.0F : Helpers.lerp(modifier, this.speedFactor, 1.0F);
   }

   protected BlockState updateStateWithCurrentMonth(BlockState state) {
      return this.getPlant().getStageProperty() != null
         ? (BlockState)state.setValue(this.getPlant().getStageProperty(), this.getPlant().stageFor(Calendars.SERVER.getCalendarMonthOfYear()))
         : state;
   }

   public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      level.setBlockAndUpdate(pos, this.updateStateWithCurrentMonth(state));
      double tempThreshold = (Double)Config.COMMON.foliageDecayThreshold.get();
      if (!this.isDead) {
         if ((double)Climate.getTemperature(level, pos) < tempThreshold && (double)TFCFHelpers.getAverageDailyTemperature(level, pos) < tempThreshold) {
            this.isDead = true;
            level.blockUpdated(pos, state.getBlock());
            return;
         }

         super.randomTick(state, level, pos, random);
      } else if (this.isDead
         && this.getCooldown(level, this.cooldown)
         && (double)Climate.getTemperature(level, pos) >= tempThreshold
         && (double)TFCFHelpers.getAverageDailyTemperature(level, pos) >= tempThreshold) {
         this.setCooldown(level, 72000L, this.cooldown);
         this.isDead = false;
         level.blockUpdated(pos, state.getBlock());
         return;
      }
   }

   public ExtendedProperties getExtendedProperties() {
      return this.properties;
   }

   public boolean isValidBonemealTarget(BlockGetter level, BlockPos pos, BlockState state, boolean isClient) {
      return false;
   }

   public boolean isBonemealSuccess(Level level, RandomSource rand, BlockPos pos, BlockState state) {
      return false;
   }

   public void performBonemeal(ServerLevel level, RandomSource rand, BlockPos pos, BlockState state) {
   }

   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      BlockPos blockpos = pos.relative(this.growthDirection.getOpposite());
      BlockState blockstate = level.getBlockState(blockpos);
      Block block = blockstate.getBlock();
      return !this.canAttachTo(blockstate)
         ? false
         : block == this.getHeadBlock()
            || block == this.getBodyBlock()
            || Helpers.isBlock(blockstate, BlockTags.LEAVES)
            || blockstate.isFaceSturdy(level, blockpos, this.growthDirection);
   }

   protected GrowingPlantHeadBlock getHeadBlock() {
      return (GrowingPlantHeadBlock)this.headBlock.get();
   }

   public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
      return new ItemStack(this.getHeadBlock());
   }

   public RenderShape getRenderShape(BlockState state) {
      return this.isDead ? RenderShape.INVISIBLE : super.getRenderShape(state);
   }

   protected boolean isAir(BlockState state) {
      return this.isDead ? true : super.isAir(state);
   }

   public List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
      return this.isDead ? Collections.emptyList() : super.getDrops(state, builder);
   }

   public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
      return this.isDead ? true : super.canBeReplaced(state, context);
   }

   public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
      return this.isDead ? 1.0F : super.getShadeBrightness(state, level, pos);
   }
}
