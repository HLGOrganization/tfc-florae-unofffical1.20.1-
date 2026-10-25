package tfcflorae.common.blocks.wood;

import net.minecraft.util.RandomSource;
import net.dries007.tfc.util.calendar.Calendars;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface ISeasonalLeavesBlock {
   static void randomTick(ISeasonalLeavesBlock bush, BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      int rarity = Math.max(
         1,
         (int)(24000.0F * (float)level.getGameRules().getInt(GameRules.RULE_RANDOMTICKING) * 2.4414062E-4F * (float)(Calendars.SERVER.getCalendarDaysInMonth() / 15 + 1))
      );
      if (random.nextInt(rarity) == 0) {
         bush.onUpdate(level, pos, state);
      }
   }

   void onUpdate(Level var1, BlockPos var2, BlockState var3);
}
