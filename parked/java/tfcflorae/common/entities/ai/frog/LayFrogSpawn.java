package tfcflorae.common.entities.ai.frog;

import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import tfcflorae.client.TFCFSounds;
import tfcflorae.common.entities.Frog;
import tfcflorae.common.entities.ai.TFCFBrain;

public class LayFrogSpawn extends Behavior<Frog> {
   private final Block frogSpawn;
   private final MemoryModuleType<?> triggerMemory;

   public LayFrogSpawn(Block block, MemoryModuleType<?> triggerMemory) {
      super(
         ImmutableMap.of(
            MemoryModuleType.ATTACK_TARGET,
            MemoryStatus.VALUE_ABSENT,
            MemoryModuleType.WALK_TARGET,
            MemoryStatus.VALUE_PRESENT,
            (MemoryModuleType)TFCFBrain.IS_PREGNANT.get(),
            MemoryStatus.VALUE_PRESENT
         )
      );
      this.frogSpawn = block;
      this.triggerMemory = triggerMemory;
   }

   protected boolean checkExtraStartConditions(ServerLevel level, Frog frog) {
      return !frog.isInWaterOrBubble() && frog.onGround();
   }

   protected void start(ServerLevel level, Frog frog, long time) {
      BlockPos blockPos = frog.blockPosition().below();

      for (Direction direction : Plane.HORIZONTAL) {
         BlockPos offset = blockPos.relative(direction);
         BlockPos above = offset.above();
         if (level.getBlockState(offset).is(Blocks.WATER) && level.getBlockState(above).isAir()) {
            level.setBlock(above, this.frogSpawn.defaultBlockState(), 3);
            level.playSound(null, frog, (SoundEvent)TFCFSounds.FROG_LAY_SPAWN.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            frog.getBrain().eraseMemory(this.triggerMemory);
            return;
         }
      }
   }
}
