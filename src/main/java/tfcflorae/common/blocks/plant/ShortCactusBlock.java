package tfcflorae.common.blocks.plant;

import net.dries007.tfc.common.TFCTags.Blocks;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.plant.ShortGrassBlock;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.registry.RegistryPlant;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour.OffsetType;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class ShortCactusBlock extends ShortGrassBlock {
   protected static final VoxelShape COLLISION_SHAPE = box(1.0, 0.0, 1.0, 15.0, 15.0, 15.0);
   protected static final VoxelShape OUTLINE_SHAPE = box(1.0, 0.0, 1.0, 15.0, 16.0, 15.0);

   public static ShortCactusBlock create(final RegistryPlant plant, ExtendedProperties properties) {
      return new ShortCactusBlock(properties) {
         public RegistryPlant getPlant() {
            return plant;
         }
      };
   }

   protected ShortCactusBlock(ExtendedProperties properties) {
      super(properties);
   }

   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      BlockState belowState = level.getBlockState(pos);
      return Helpers.isBlock(belowState, BlockTags.SAND)
         || Helpers.isBlock(state.getBlock(), Blocks.GRASS_PLANTABLE_ON)
         || Helpers.isBlock(state.getBlock(), Blocks.BUSH_PLANTABLE_ON);
   }

   public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return OUTLINE_SHAPE;
   }

   public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
      return COLLISION_SHAPE;
   }

   public OffsetType getOffsetType() {
      return OffsetType.NONE;
   }

   public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
      entity.hurt(entity.damageSources().cactus(), 1.0F);
   }

   public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
      return false;
   }
}
