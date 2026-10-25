package tfcflorae.common.blocks.plant;

import net.minecraft.util.RandomSource;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.plant.EpiphytePlantBlock;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.Season;
import net.dries007.tfc.util.registry.RegistryPlant;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.Tags.Blocks;
import tfcflorae.client.particle.TFCFParticles;

public abstract class FungiEpiphyteBlock extends EpiphytePlantBlock {
   public static final IntegerProperty AGE = TFCBlockStateProperties.AGE_3;
   protected static final VoxelShape PLANT_SHAPE = box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);
   public final TFCFPlant plantTFCF;

   public static FungiEpiphyteBlock create(final RegistryPlant plant, TFCFPlant plantTFCF, ExtendedProperties properties) {
      return new FungiEpiphyteBlock(properties, plantTFCF) {
         public RegistryPlant getPlant() {
            return plant;
         }
      };
   }

   protected FungiEpiphyteBlock(ExtendedProperties properties, TFCFPlant plantTFCF) {
      super(properties);
      this.plantTFCF = plantTFCF;
      this.registerDefaultState((BlockState)this.defaultBlockState().setValue(FACING, Direction.NORTH));
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
      }

      level.setBlockAndUpdate(pos, this.updateStateWithCurrentMonth(state));
   }

   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      BlockState attachedState = level.getBlockState(pos.relative(((Direction)state.getValue(FACING)).getOpposite()));
      return (
            Helpers.isBlock(attachedState, Blocks.STONE)
               || Helpers.isBlock(attachedState, BlockTags.LOGS)
               || attachedState.is(BlockTags.MUSHROOM_GROW_BLOCK)
               || Helpers.isBlock(attachedState.getBlock(), net.dries007.tfc.common.TFCTags.Blocks.SEA_BUSH_PLANTABLE_ON)
               || Helpers.isBlock(attachedState.getBlock(), net.dries007.tfc.common.TFCTags.Blocks.GRASS_PLANTABLE_ON)
               || Helpers.isBlock(attachedState.getBlock(), net.dries007.tfc.common.TFCTags.Blocks.BUSH_PLANTABLE_ON)
         )
         && level.getRawBrightness(pos, 0) < 14;
   }
}
