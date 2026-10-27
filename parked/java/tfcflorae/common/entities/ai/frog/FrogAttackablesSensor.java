package tfcflorae.common.entities.ai.frog;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.NearestVisibleLivingEntitySensor;
import net.minecraft.world.entity.ai.sensing.Sensor;
import tfcflorae.common.entities.Frog;
import tfcflorae.common.entities.ai.TFCFBrain;

public class FrogAttackablesSensor extends NearestVisibleLivingEntitySensor {
   public FrogAttackablesSensor() {
   }

   protected boolean isMatchingEntity(LivingEntity entity, LivingEntity target) {
      boolean validFood = false;
      if (entity instanceof Frog frogEntity) {
         validFood = frogEntity.isValidFrogFoodS(target);
      }

      return !entity.getBrain().hasMemoryValue(MemoryModuleType.HAS_HUNTING_COOLDOWN)
         && Sensor.isEntityAttackable(entity, target)
         && (Frog.isValidFrogFood(target) || validFood)
         && !this.cantReachTarget(entity, target)
         && target.closerThan(entity, 10.0);
   }

   private boolean cantReachTarget(LivingEntity entity, LivingEntity target) {
      List<UUID> targets = (List<UUID>)entity.getBrain().getMemory((MemoryModuleType)TFCFBrain.UNREACHABLE_TONGUE_TARGETS.get()).orElseGet(ArrayList::new);
      return targets.contains(target.getUUID());
   }

   protected MemoryModuleType<LivingEntity> getMemory() {
      return MemoryModuleType.NEAREST_ATTACKABLE;
   }
}
