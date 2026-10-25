package tfcflorae.common.blocks.soil;

import net.minecraft.util.RandomSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.LivingEntity.Fallsounds;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LooseMudBlock extends Block {
   private static final float HORIZONTAL_PARTICLE_MOMENTUM_FACTOR = 0.083333336F;
   private static final float IN_BLOCK_HORIZONTAL_SPEED_MULTIPLIER = 0.9F;
   private static final float IN_BLOCK_VERTICAL_SPEED_MULTIPLIER = 1.5F;
   private static final float NUM_BLOCKS_TO_FALL_INTO_BLOCK = 2.5F;
   private static final VoxelShape FALLING_COLLISION_SHAPE = Shapes.box(0.0, 0.0, 0.0, 1.0, 0.9F, 1.0);
   private static final double MINIMUM_FALL_DISTANCE_FOR_SOUND = 4.0;
   private static final double MINIMUM_FALL_DISTANCE_FOR_BIG_SOUND = 7.0;

   public LooseMudBlock(Properties properties) {
      super(properties);
   }

   public boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction pDirection) {
      return adjacentBlockState.is(this) ? true : super.skipRendering(state, adjacentBlockState, pDirection);
   }

   public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
      return Shapes.empty();
   }

   public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
      if (!(entity instanceof LivingEntity) || entity.getFeetBlockState().is(this)) {
         entity.makeStuckInBlock(state, new Vec3(0.9F, 1.5, 0.9F));
         if (level.isClientSide) {
            RandomSource random = level.getRandom();
            boolean flag = entity.xOld != entity.getX() || entity.zOld != entity.getZ();
            if (flag && random.nextBoolean()) {
               level.addParticle(
                  ParticleTypes.ASH,
                  entity.getX(),
                  (double)(pos.getY() + 1),
                  entity.getZ(),
                  (double)(Mth.randomBetween(random, -1.0F, 1.0F) * 0.083333336F),
                  0.05F,
                  (double)(Mth.randomBetween(random, -1.0F, 1.0F) * 0.083333336F)
               );
            }
         }
      }
   }

   public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float sound) {
      if (!((double)sound < 4.0) && entity instanceof LivingEntity livingentity) {
         Fallsounds $$7 = livingentity.getFallSounds();
         SoundEvent soundevent = (double)sound < 7.0 ? $$7.small() : $$7.big();
         entity.playSound(soundevent, 1.0F, 1.0F);
      }
   }

   public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      if (context instanceof EntityCollisionContext entitycollisioncontext) {
         Entity entity = entitycollisioncontext.getEntity();
         if (entity != null) {
            if (entity.fallDistance > 2.5F) {
               return FALLING_COLLISION_SHAPE;
            }

            boolean flag = entity instanceof FallingBlockEntity;
            if (flag || canEntityWalkOnPowderSnow(entity) && context.isAbove(Shapes.block(), pos, false) && !context.isDescending()) {
               return super.getCollisionShape(state, level, pos, context);
            }
         }
      }

      return Shapes.empty();
   }

   public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return Shapes.empty();
   }

   public static boolean canEntityWalkOnPowderSnow(Entity entity) {
      if (entity.getType().is(EntityTypeTags.POWDER_SNOW_WALKABLE_MOBS)) {
         return true;
      } else {
         return entity instanceof LivingEntity ? ((LivingEntity)entity).getItemBySlot(EquipmentSlot.FEET).canWalkOnPowderedSnow((LivingEntity)entity) : false;
      }
   }

   public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType pType) {
      return false;
   }

   public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
      return 0.2F;
   }
}
