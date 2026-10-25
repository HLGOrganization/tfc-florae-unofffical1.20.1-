package tfcflorae.common.blocks.plant;

import net.minecraft.util.RandomSource;
import net.dries007.tfc.client.particle.TFCParticles;
import net.dries007.tfc.common.TFCTags.Blocks;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.plant.TFCBushBlock;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.Season;
import net.dries007.tfc.util.registry.RegistryPlant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.OffsetType;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import tfcflorae.util.TFCFHelpers;

public abstract class TFCFPlantBlock extends TFCBushBlock {
   public static final IntegerProperty AGE = TFCBlockStateProperties.AGE_3;
   protected static final VoxelShape PLANT_SHAPE = box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);
   public boolean canWalkThroughEffortlessly;

   public static TFCFPlantBlock create(final RegistryPlant plant, ExtendedProperties properties) {
      return new TFCFPlantBlock(properties) {
         @Override
         public RegistryPlant getPlant() {
            return plant;
         }
      };
   }

   protected TFCFPlantBlock(ExtendedProperties properties) {
      super(properties);
      BlockState stateDefinition = (BlockState)((BlockState)this.getStateDefinition().any()).setValue(AGE, 0);
      IntegerProperty stageProperty = this.getPlant().getStageProperty();
      if (stageProperty != null) {
         stateDefinition = (BlockState)stateDefinition.setValue(stageProperty, 0);
      }

      this.registerDefaultState(stateDefinition);
   }

   protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
      return Helpers.isBlock(state.getBlock(), Blocks.GRASS_PLANTABLE_ON) || Helpers.isBlock(state.getBlock(), Blocks.BUSH_PLANTABLE_ON);
   }

   public OffsetType getOffsetType() {
      return OffsetType.XZ;
   }

   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      if (random.nextInt(400) == 0 && Helpers.isBlock(state, BlockTags.FLOWERS) && Calendars.get(level).getCalendarMonthOfYear().getSeason() == Season.SPRING) {
         level.addParticle(
            (ParticleOptions)TFCParticles.BUTTERFLY.get(),
            (double)((float)pos.getX() + random.nextFloat()),
            (double)((float)pos.getY() + random.nextFloat()),
            (double)((float)pos.getZ() + random.nextFloat()),
            0.0,
            0.0,
            0.0
         );
      }
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return PLANT_SHAPE;
   }

   public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      if (random.nextDouble() < (Double)TFCConfig.SERVER.plantGrowthChance.get()) {
         state = (BlockState)state.setValue(AGE, Math.min((Integer)state.getValue(AGE) + 1, 3));
      }

      level.setBlockAndUpdate(pos, this.updateStateWithCurrentMonth(state));
   }

   public abstract RegistryPlant getPlant();

   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return this.updateStateWithCurrentMonth(this.defaultBlockState());
   }

   protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
      if (this.getPlant().getStageProperty() != null) {
         builder.add(new Property[]{this.getPlant().getStageProperty()});
      }

      builder.add(new Property[]{AGE});
   }

   public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
      if (entity != null) {
         if (TFCFHelpers.canWalkThroughEffortlessly(entity)) {
            this.canWalkThroughEffortlessly = true;
         } else {
            this.canWalkThroughEffortlessly = false;
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
}
