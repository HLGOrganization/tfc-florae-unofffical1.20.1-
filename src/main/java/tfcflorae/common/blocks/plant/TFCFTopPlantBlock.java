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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.NetherVines;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.ForgeHooks;
import org.jetbrains.annotations.Nullable;
import tfcflorae.Config;
import tfcflorae.common.blocks.ICooldown;
import tfcflorae.util.TFCFHelpers;

public abstract class TFCFTopPlantBlock extends GrowingPlantHeadBlock implements IForgeBlockExtension, ICooldown {
   private final Supplier<? extends Block> bodyBlock;
   private final ExtendedProperties properties;
   public boolean canWalkThroughEffortlessly;
   public boolean isDead;
   public long cooldown;

   public static TFCFTopPlantBlock create(
      final RegistryPlant plant, ExtendedProperties properties, Supplier<? extends Block> bodyBlock, Direction direction, VoxelShape shape
   ) {
      return new TFCFTopPlantBlock(properties, bodyBlock, direction, shape) {
         @Override
         public RegistryPlant getPlant() {
            return plant;
         }
      };
   }

   protected TFCFTopPlantBlock(ExtendedProperties properties, Supplier<? extends Block> bodyBlock, Direction direction, VoxelShape shape) {
      super(properties.properties(), direction, shape, false, 0.0);
      this.bodyBlock = bodyBlock;
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

         if ((Integer)state.getValue(AGE) < 25
            && ForgeHooks.onCropsGrowPre(
               level,
               pos.relative(this.growthDirection),
               level.getBlockState(pos.relative(this.growthDirection)),
               random.nextDouble() < (Double)TFCConfig.SERVER.plantGrowthChance.get()
            )) {
            BlockPos blockpos = pos.relative(this.growthDirection);
            if (this.canGrowInto(level.getBlockState(blockpos))) {
               level.setBlockAndUpdate(blockpos, (BlockState)state.cycle(AGE));
               ForgeHooks.onCropsGrowPost(level, blockpos, level.getBlockState(blockpos));
            }
         }
      } else if (this.isDead
         && this.getCooldown(level, this.cooldown)
         && (double)Climate.getTemperature(level, pos) >= tempThreshold
         && (double)TFCFHelpers.getAverageDailyTemperature(level, pos) >= tempThreshold) {
         this.setCooldown(level, 72000L, this.cooldown);
         level.blockUpdated(pos, state.getBlock());
         return;
      }
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      BlockState state = super.getStateForPlacement(context);
      return state != null && this.getPlant().getStageProperty() != null
         ? (BlockState)((BlockState)state.setValue(AGE, Mth.nextInt(context.getLevel().getRandom(), 10, 18)))
            .setValue(this.getPlant().getStageProperty(), this.getPlant().stageFor(Calendars.SERVER.getCalendarMonthOfYear()))
         : state;
   }

   public boolean isValidBonemealTarget(BlockGetter level, BlockPos pos, BlockState state, boolean isClient) {
      return false;
   }

   public ExtendedProperties getExtendedProperties() {
      return this.properties;
   }

   protected int getBlocksToGrowWhenBonemealed(RandomSource rand) {
      return 0;
   }

   protected boolean canGrowInto(BlockState state) {
      return NetherVines.isValidGrowthState(state);
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

   protected Block getBodyBlock() {
      return this.bodyBlock.get();
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
