package tfcflorae.mixin.world.biome;

import net.dries007.tfc.world.biome.VolcanoNoise;
import net.dries007.tfc.world.noise.Cellular2D;
import net.dries007.tfc.world.noise.Noise2D;
import net.dries007.tfc.world.noise.OpenSimplex2D;
import net.dries007.tfc.world.noise.Cellular2D.Cell;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin({VolcanoNoise.class})
public abstract class VolcanoNoiseMixin {
   @Shadow
   private final Cellular2D cellNoise;
   @Shadow
   private final Noise2D jitterNoise;

   @Shadow
   private static float calculateEasing(float f1) {
      return Mth.map(f1, 0.0F, 0.23F, 1.0F, 0.0F);
   }

   @Shadow
   private static float calculateClampedEasing(float f1) {
      return Mth.clamp(calculateEasing(f1), 0.0F, 1.0F);
   }

   @Shadow
   private static float calculateShape(float t) {
      if (t > 0.025F) {
         return (5.0F / (9.0F * t + 1.0F) - 0.5F) * 0.27917364F;
      } else {
         float a = t * 9.0F + 0.05F;
         return (8.0F * a * a + 2.9766326F) * 0.27917364F;
      }
   }

   public VolcanoNoiseMixin(long seed) {
      this.cellNoise = new Cellular2D(seed).spread(0.009F);
      this.jitterNoise = new OpenSimplex2D(seed + 1234123L).octaves(2).scaled(-0.0016F, 0.0016F).spread(0.128F);
   }

   @Overwrite(
      remap = false
   )
   public float modifyHeight(float x, float z, float baseHeight, int rarity, int baseVolcanoHeight, int scaleVolcanoHeight) {
      Cell cell = this.sampleCell(x, z, rarity);
      if (cell == null) {
         return baseHeight;
      } else {
         if (baseVolcanoHeight <= -15 && scaleVolcanoHeight >= 50) {
            byte var13 = -30;
         } else {
            byte var10000 = 63;
         }

         int volcanoBase = baseVolcanoHeight <= -15 && scaleVolcanoHeight <= 10 ? -200 : 63;
         float easing = Mth.clamp(calculateEasing(cell.f1()) + this.jitterNoise.noise(x, z), 0.0F, 1.0F);
         float shape = calculateShape(1.0F - easing);
         float volcanoHeight = (float)(volcanoBase + baseVolcanoHeight) + shape * (float)scaleVolcanoHeight;
         float volcanoAdditionalHeight = shape * (float)scaleVolcanoHeight;
         return Mth.lerp(easing, baseHeight, 0.5F * (volcanoHeight + Math.max(volcanoHeight, baseHeight + 0.4F * volcanoAdditionalHeight)));
      }
   }

   @Shadow
   public float calculateEasing(int x, int z, int rarity) {
      Cell cell = this.sampleCell((float)x, (float)z, rarity);
      return cell != null ? calculateClampedEasing(cell.f1()) : 0.0F;
   }

   @Shadow
   @Nullable
   public BlockPos calculateCenter(int x, int y, int z, int rarity) {
      Cell cell = this.sampleCell((float)x, (float)z, rarity);
      return cell != null ? new BlockPos((int)cell.x(), y, (int)cell.y()) : null;
   }

   @Shadow
   @Nullable
   private Cell sampleCell(float x, float z, int rarity) {
      Cell cell = this.cellNoise.cell(x, z);
      return Math.abs(cell.noise()) <= 1.0F / (float)rarity ? cell : null;
   }
}
