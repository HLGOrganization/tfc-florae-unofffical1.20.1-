package tfcflorae.config.animals;

import java.util.function.Function;
import net.dries007.tfc.config.animals.ProducingAnimalConfig;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;

public record ParrotConfig(ProducingAnimalConfig inner, IntValue hatchDays, IntValue produceTicks, DoubleValue produceFamiliarity) {
   public static ParrotConfig build(
      net.dries007.tfc.config.ConfigBuilder builder,
      String name,
      double familiarityCap,
      int adulthoodDays,
      int uses,
      boolean eatsRottenFood,
      int produceTicks,
      double produceFamiliarity,
      int hatchDays
   ) {
      return new ParrotConfig(
         ProducingAnimalConfig.build(builder, name, familiarityCap, adulthoodDays, uses, eatsRottenFood, produceTicks, produceFamiliarity),
         builder.swap("%sProduceTicks".formatted(name))
            .comment("Ticks until produce is ready")
            .define("%sProduceTicks".formatted(name), produceTicks, 0, Integer.MAX_VALUE),
         builder.swap("%sHatchDays".formatted(name))
            .comment("Ticks until egg is ready to hatch")
            .define("%sHatchDays".formatted(name), hatchDays, 0, Integer.MAX_VALUE),
         builder.swap("%sMinProduceFamiliarity".formatted(name))
            .comment("Minimum familiarity [0-1] needed for produce. Set above 1 to disable produce.")
            .define("%sMinProduceFamiliarity".formatted(name), produceFamiliarity, 0.0, Float.MAX_VALUE)
      );
   }
}
