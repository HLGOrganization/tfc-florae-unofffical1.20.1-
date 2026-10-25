package tfcflorae.common.blocks;

import net.dries007.tfc.util.calendar.Calendars;
import net.minecraft.world.level.Level;

public interface ICooldown {
   default boolean getCooldown(Level level, long cooldown) {
      return Calendars.get(level).getTicks() < cooldown;
   }

   default void setCooldown(Level level, long timeToWait, long cooldown) {
      long ticks = Calendars.SERVER.getTicks();
      if (ticks > cooldown) {
         cooldown = ticks + timeToWait;
      }
   }
}
