package tfcflorae.common.blocks.plant;

import java.util.Map;
import net.minecraft.util.RandomSource;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.plant.TFCBushBlock;
import net.dries007.tfc.common.blocks.soil.SoilBlockType;
import net.dries007.tfc.common.blocks.soil.SoilBlockType.Variant;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.Season;
import net.dries007.tfc.util.registry.RegistryPlant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.OffsetType;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.Tags.Blocks;
import net.minecraftforge.registries.RegistryObject;
import tfcflorae.client.particle.TFCFParticles;
import tfcflorae.common.blocks.TFCFBlocks;
import tfcflorae.common.blocks.soil.TFCFSoil;
import tfcflorae.util.TFCFHelpers;

public abstract class FungiBlock extends TFCBushBlock {
   public static final IntegerProperty AGE = TFCBlockStateProperties.AGE_3;
   protected static final VoxelShape PLANT_SHAPE = box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);
   public final TFCFPlant plantTFCF;
   public boolean canWalkThroughEffortlessly;

   public static FungiBlock create(final RegistryPlant plant, TFCFPlant plantTFCF, ExtendedProperties properties) {
      return new FungiBlock(properties, plantTFCF) {
         @Override
         public RegistryPlant getPlant() {
            return plant;
         }
      };
   }

   protected FungiBlock(ExtendedProperties properties, TFCFPlant plantTFCF) {
      super(properties);
      this.plantTFCF = plantTFCF;
      BlockState stateDefinition = (BlockState)((BlockState)this.getStateDefinition().any()).setValue(AGE, 0);
      IntegerProperty stageProperty = this.getPlant().getStageProperty();
      if (stageProperty != null) {
         stateDefinition = (BlockState)stateDefinition.setValue(stageProperty, 0);
      }

      this.registerDefaultState(stateDefinition);
   }

   public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
      if (this.plantTFCF.getSporeColor() != -1
         && level.getRawBrightness(pos, 0) > 9
         && (
            Calendars.get(level).getCalendarMonthOfYear().getSeason() == Season.SUMMER
               || Calendars.get(level).getCalendarMonthOfYear().getSeason() == Season.FALL
         )) {
         int i = pos.getX();
         int j = pos.getY();
         int k = pos.getZ();
         MutableBlockPos mutablePos = new MutableBlockPos();
         int sporeSpread = this.plantTFCF.getSporeSpread();

         for (int l = 0; l <= sporeSpread + random.nextInt(3); l++) {
            mutablePos.set(
               i + Mth.nextInt(random, -sporeSpread, sporeSpread), j - random.nextInt(sporeSpread), k + Mth.nextInt(random, -sporeSpread, sporeSpread)
            );
            BlockState blockstate = level.getBlockState(mutablePos);
            if (!blockstate.isCollisionShapeFullBlock(level, mutablePos)) {
               level.addParticle(
                  new BlockParticleOption((ParticleType)TFCFParticles.FALLING_SPORE.get(), state),
                  (double)mutablePos.getX() + random.nextDouble(),
                  (double)mutablePos.getY() + random.nextDouble(),
                  (double)mutablePos.getZ() + random.nextDouble(),
                  0.0,
                  0.0,
                  0.0
               );
            }
         }
      }
   }

   protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
      return Helpers.isBlock(state, Blocks.STONE)
         || state.is(BlockTags.MUSHROOM_GROW_BLOCK)
         || Helpers.isBlock(state.getBlock(), BlockTags.LOGS)
         || Helpers.isBlock(state.getBlock(), net.dries007.tfc.common.TFCTags.Blocks.SEA_BUSH_PLANTABLE_ON)
         || Helpers.isBlock(state.getBlock(), net.dries007.tfc.common.TFCTags.Blocks.GRASS_PLANTABLE_ON)
         || Helpers.isBlock(state.getBlock(), net.dries007.tfc.common.TFCTags.Blocks.BUSH_PLANTABLE_ON)
         || super.mayPlaceOn(state, level, pos);
   }

   public OffsetType getOffsetType() {
      return OffsetType.XZ;
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return PLANT_SHAPE;
   }

   public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      if (random.nextDouble() < (Double)TFCConfig.SERVER.plantGrowthChance.get()) {
         state = (BlockState)state.setValue(AGE, Math.min((Integer)state.getValue(AGE) + 1, 3));
         int i = 5;
         int j = 4;

         for (BlockPos blockpos : BlockPos.betweenClosed(pos.offset(-4, -1, -4), pos.offset(4, 1, 4))) {
            if (level.getBlockState(blockpos).is(this)) {
               if (--i <= 0) {
                  return;
               }
            }
         }

         BlockPos blockpos1 = pos.offset(random.nextInt(3) - 1, random.nextInt(2) - random.nextInt(2), random.nextInt(3) - 1);

         for (int k = 0; k < 4; k++) {
            if (level.getBlockState(blockpos1).isAir() && state.canSurvive(level, blockpos1)) {
               pos = blockpos1;
            }

            blockpos1 = pos.offset(random.nextInt(3) - 1, random.nextInt(2) - random.nextInt(2), random.nextInt(3) - 1);
         }

         if (level.getBlockState(blockpos1).isAir() && state.canSurvive(level, blockpos1)) {
            level.setBlock(blockpos1, state, 2);
         }

         BlockPos posBelow = pos.below();
         Block blockBelow = level.getBlockState(posBelow).getBlock();
         if (blockBelow == TFCFBlocks.ROOTED_BOG_IRON.get() || blockBelow == TFCFBlocks.BOG_IRON.get()) {
            level.setBlock(posBelow, ((Block)TFCFBlocks.MYCELIUM_BOG_IRON.get()).defaultBlockState(), 3);
            level.setBlockAndUpdate(pos, this.updateStateWithCurrentMonth(state));
            return;
         }

         for (Variant soilVariant : Variant.values()) {
         if (soilVariant == Variant.SILTY_LOAM) { continue; }
            if (blockBelow == ((RegistryObject)((Map)TFCBlocks.SOIL.get(SoilBlockType.ROOTED_DIRT)).get(soilVariant)).get()
               || blockBelow == ((RegistryObject)((Map)TFCBlocks.SOIL.get(SoilBlockType.DIRT)).get(soilVariant)).get()) {
               level.setBlock(posBelow, ((Block)TFCFBlocks.TFCSOIL.get(TFCFSoil.MYCELIUM_DIRT).get(soilVariant).get()).defaultBlockState(), 3);
               level.setBlockAndUpdate(pos, this.updateStateWithCurrentMonth(state));
               return;
            }
         }

         for (TFCFSoil.TFCFVariant soilVariantx : TFCFSoil.TFCFVariant.values()) {
            if (blockBelow == TFCFBlocks.TFCFSOIL.get(TFCFSoil.ROOTED_DIRT).get(soilVariantx).get()
               || blockBelow == TFCFBlocks.TFCFSOIL.get(TFCFSoil.DIRT).get(soilVariantx).get()) {
               level.setBlock(posBelow, ((Block)TFCFBlocks.TFCFSOIL.get(TFCFSoil.MYCELIUM_DIRT).get(soilVariantx).get()).defaultBlockState(), 3);
               level.setBlockAndUpdate(pos, this.updateStateWithCurrentMonth(state));
               return;
            }
         }
      }

      level.setBlockAndUpdate(pos, this.updateStateWithCurrentMonth(state));
   }

   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      BlockPos blockpos = pos.below();
      BlockState blockstate = level.getBlockState(blockpos);
      return this.mayPlaceOn(blockstate, level, pos) && level.getRawBrightness(pos, 0) < 14;
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

   public void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
      if (!this.canSurvive(state, level, pos)) {
         level.destroyBlock(pos, false);
      }
   }
}
