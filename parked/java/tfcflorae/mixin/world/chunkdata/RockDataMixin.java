package tfcflorae.mixin.world.chunkdata;

import net.dries007.tfc.world.chunkdata.RockData;
import net.dries007.tfc.world.settings.RockSettings;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.RandomSource;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tfcflorae.util.TFCFHelpers;

@Mixin({RockData.class})
public class RockDataMixin {
   @Shadow
   private static final int SIZE = 256;
   @Shadow
   private final RockSettings[] bottomLayer;
   @Shadow
   private final RockSettings[] middleLayer;
   @Shadow
   private final RockSettings[] topLayer;
   @Shadow
   private final int[] rockLayerHeight;
   @Shadow
   @Nullable
   private int[] surfaceHeight;

   @Shadow
   private static int index(int x, int z) {
      return x & 15 | (z & 15) << 4;
   }

   public RockDataMixin(RockSettings[] bottomLayer, RockSettings[] middleLayer, RockSettings[] topLayer, int[] rockLayerHeight) {
      this.bottomLayer = bottomLayer;
      this.middleLayer = middleLayer;
      this.topLayer = topLayer;
      this.rockLayerHeight = rockLayerHeight;
      this.surfaceHeight = null;
   }

   @Overwrite(
      remap = false
   )
   public RockSettings getRock(int x, int y, int z) {
      assert this.surfaceHeight != null;

      RandomSource random = TFCFHelpers.randomSource(x, y, z);
      int i = index(x, z);
      int sh = this.surfaceHeight[i];
      int rh = this.rockLayerHeight[i];
      int gaussSpread = (int)(Mth.abs((float)random.nextGaussian()) * Mth.sqrt((float)sh * 0.5F));
      if (y >= (int)(155.0 - 0.2 * (double)((float)sh * 1.15F) + (double)rh + (double)((float)gaussSpread * 0.05F))
         && y <= (int)(169.0 - 0.2 * (double)((float)sh * 1.15F) + (double)rh + (double)((float)gaussSpread * 0.05F))) {
         return y > (int)(161.0 - 0.2 * (double)((float)sh * 1.15F) + (double)rh + (double)((float)gaussSpread * 0.05F))
            ? this.middleLayer[i]
            : this.bottomLayer[i];
      } else if (y > (int)(126.0 - 0.2 * (double)sh + (double)rh + (double)((float)gaussSpread * 0.2F))) {
         return this.topLayer[i];
      } else if (y > (int)(95.0 - 0.2 * (double)(sh + gaussSpread) + (double)rh + (double)((float)gaussSpread * 0.5F))) {
         return this.middleLayer[i];
      } else if (y > (int)(63.0 - 0.2 * (double)sh + (double)rh + (double)((float)gaussSpread * 0.25F))) {
         return this.bottomLayer[i];
      } else if (y > (int)(47.0 - 0.2 * (double)((float)sh + (float)gaussSpread * 0.25F) + (double)rh + (double)((float)gaussSpread * 0.25F))) {
         return this.topLayer[i];
      } else if (y > (int)(15.0 - 0.3 * (double)((float)sh + (float)gaussSpread * 0.25F) + (double)(rh + gaussSpread) + (double)((float)gaussSpread * 0.8F))) {
         return this.middleLayer[i];
      } else if (y > (int)(-33.0 - 0.3 * (double)sh + (double)rh + (double)gaussSpread)) {
         return this.bottomLayer[i];
      } else if (y > (int)(-93.0 - 0.2 * (double)sh + (double)rh + (double)((float)gaussSpread * 0.7F))) {
         return this.middleLayer[i];
      } else {
         return y > (int)(-113.0 - 0.2 * (double)sh + (double)rh + (double)((float)gaussSpread * 0.8F)) ? this.topLayer[i] : this.bottomLayer[i];
      }
   }
}
