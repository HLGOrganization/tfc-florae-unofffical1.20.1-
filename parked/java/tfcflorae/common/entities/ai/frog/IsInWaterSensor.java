package tfcflorae.common.entities.ai.frog;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import tfcflorae.common.entities.ai.TFCFBrain;

public class IsInWaterSensor extends Sensor<LivingEntity> {
   public IsInWaterSensor() {
   }

   public Set<MemoryModuleType<?>> requires() {
      return ImmutableSet.of((MemoryModuleType)TFCFBrain.IS_IN_WATER_MEMORY.get());
   }

   protected void doTick(ServerLevel world, LivingEntity entity) {
      if (entity.isInWater()) {
         entity.getBrain().setMemory((MemoryModuleType)TFCFBrain.IS_IN_WATER_MEMORY.get(), Unit.INSTANCE);
      } else {
         entity.getBrain().eraseMemory((MemoryModuleType)TFCFBrain.IS_IN_WATER_MEMORY.get());
      }
   }
}
