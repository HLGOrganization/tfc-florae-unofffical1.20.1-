package tfcflorae.common.blocks.plant;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
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
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.Tags.Blocks;
import tfcflorae.client.particle.TFCFParticles;

public abstract class FungiEpiphyteSolidBlock extends EpiphytePlantBlock {
   public static final IntegerProperty AGE = TFCBlockStateProperties.AGE_3;
   public final TFCFPlant plantTFCF;
   protected static final VoxelShape NORTH_SHAPE_COLLISION = box(0.0, 6.0, 4.0, 16.0, 10.0, 16.0);
   protected static final VoxelShape SOUTH_SHAPE_COLLISION = box(0.0, 6.0, 0.0, 16.0, 10.0, 12.0);
   protected static final VoxelShape WEST_SHAPE_COLLISION = box(4.0, 6.0, 0.0, 16.0, 10.0, 16.0);
   protected static final VoxelShape EAST_SHAPE_COLLISION = box(0.0, 6.0, 0.0, 12.0, 10.0, 16.0);
   protected static final Map<Direction, VoxelShape> SHAPES_COLLISION = ImmutableMap.of(
      Direction.NORTH,
      NORTH_SHAPE_COLLISION,
      Direction.SOUTH,
      SOUTH_SHAPE_COLLISION,
      Direction.WEST,
      WEST_SHAPE_COLLISION,
      Direction.EAST,
      EAST_SHAPE_COLLISION
   );

   public static FungiEpiphyteSolidBlock create(final RegistryPlant plant, TFCFPlant plantTFCF, ExtendedProperties properties) {
      return new FungiEpiphyteSolidBlock(properties, plantTFCF) {
         public RegistryPlant getPlant() {
            return plant;
         }
      };
   }

   protected FungiEpiphyteSolidBlock(ExtendedProperties properties, TFCFPlant plantTFCF) {
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

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return state.getValue(AGE) >= 2 ? SHAPES_COLLISION.get(state.getValue(FACING)) : (VoxelShape)SHAPES.get(state.getValue(FACING));
   }

   public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return state.getValue(AGE) >= 2 ? SHAPES_COLLISION.get(state.getValue(FACING)) : Shapes.empty();
   }

   public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
      return false;
   }
}
