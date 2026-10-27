package tfcflorae.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.util.RandomSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class BeeNestFeature extends Feature<NoneFeatureConfiguration> {
   public BeeNestFeature(Codec<NoneFeatureConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
      WorldGenLevel level = context.level();
      BlockPos pos = context.origin();
      RandomSource random = context.random();
      Direction facingDirection = this.randomDirection(random);
      this.setBlock(level, pos, (BlockState)Blocks.BEE_NEST.defaultBlockState().setValue(BeehiveBlock.FACING, facingDirection));
      level.getBlockEntity(pos, BlockEntityType.BEEHIVE).ifPresent(p_202310_ -> {
         int j = 2 + random.nextInt(2);

         for (int k = 0; k < j; k++) {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putString("id", Registry.ENTITY_TYPE.getKey(EntityType.BEE).toString());
            p_202310_.storeBee(compoundTag, random.nextInt(599), false);
         }
      });
      return true;
   }

   public Direction randomDirection(RandomSource random) {
      int randomDirection = random.nextInt(4);
      switch (randomDirection) {
         case 0:
            return Direction.NORTH;
         case 1:
            return Direction.SOUTH;
         case 2:
            return Direction.EAST;
         default:
            return Direction.WEST;
      }
   }
}
