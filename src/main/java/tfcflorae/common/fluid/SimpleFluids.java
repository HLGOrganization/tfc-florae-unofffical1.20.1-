package tfcflorae.common.fluid;

import java.util.Locale;

public enum SimpleFluids {
   ANTHRACITE(-14936294),
   BITUMINOUS_COAL(-14868967),
   CHARCOAL(-15263468),
   LIGNITE(-14868458),
   COKE(-13683911);

   private final String id = this.name().toLowerCase(Locale.ROOT);
   private final int color;

   private SimpleFluids(int color) {
      this.color = color;
   }

   public String getId() {
      return this.id;
   }

   public int getColor() {
      return this.color;
   }
}
