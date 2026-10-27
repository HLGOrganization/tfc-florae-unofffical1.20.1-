package tfcflorae.mixin.world.river;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.dries007.tfc.world.FastConcurrentCache;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.LerpFloatLayer;
import net.dries007.tfc.world.layer.Plate;
import net.dries007.tfc.world.layer.framework.TypedArea;
import net.dries007.tfc.world.layer.framework.TypedAreaFactory;
import net.dries007.tfc.world.river.MidpointFractal;
import net.dries007.tfc.world.river.RiverFractal;
import net.dries007.tfc.world.river.RiverHelpers;
import net.dries007.tfc.world.river.Watershed;
import net.dries007.tfc.world.river.Watershed.Context;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin({Watershed.class})
public abstract class WatershedMixin {
   @Shadow
   @Mutable
   @Final
   public static final float RIVER_WIDTH = 0.039F;

   public WatershedMixin() {
   }

   @Mixin({Context.class})
   public static class ContextMixin {
      @Shadow
      private static final int PARTITION_BITS = 5;
      @Shadow
      private static final int ZOOM_BITS = 7;
      @Shadow
      private static final int WATERSHED_CACHE_BITS = 8;
      @Shadow
      private static final int PARTITION_CACHE_BITS = 10;
      @Shadow
      private static final float PARTITION_RADIUS = (float)Math.sqrt(2.0) / 2.0F;
      @Shadow
      private static final int PARTITION_TO_ZOOM_BITS = 2;
      @Shadow
      private final ThreadLocal<TypedArea<Plate>> plates;
      @Shadow
      private final FastConcurrentCache<Watershed> watershedCache;
      @Shadow
      private final FastConcurrentCache<List<MidpointFractal>> partitionCache;
      @Shadow
      private final long seed;
      @Shadow
      private final float sourceChance;
      @Shadow
      private final float length;
      @Shadow
      private final int depth;
      @Shadow
      private final float feather;
      @Unique
      @Final
      private ChunkData chunkData;
      @Unique
      @Final
      @Nullable
      private LerpFloatLayer rainfallLayer;

      public ContextMixin(TypedAreaFactory<Plate> plates, long seed, float sourceChance, float length, int depth, float feather) {
         this.plates = ThreadLocal.withInitial(plates);
         this.watershedCache = new FastConcurrentCache(256);
         this.partitionCache = new FastConcurrentCache(1024);
         this.seed = seed;
         this.sourceChance = sourceChance;
         this.length = length;
         this.depth = depth;
         this.feather = feather;
      }

      @Overwrite(
         remap = false
      )
      public List<MidpointFractal> getFractalsByPartition(int x, int z) {
         int px = x >> 5;
         int pz = z >> 5;
         List<MidpointFractal> partition = (List<MidpointFractal>)this.partitionCache.getIfPresent(px, pz);
         if (partition == null) {
            Set<Watershed> nearbySheds = new ObjectOpenHashSet(2);
            float watershedScale = 0.0078125F;
            float x0 = (float)x * 0.0078125F;
            float z0 = (float)z * 0.0078125F;
            nearbySheds.add(this.create(x0 - 0.5F, z0 - 0.5F));
            nearbySheds.add(this.create(x0 + 0.5F, z0 - 0.5F));
            nearbySheds.add(this.create(x0 + 0.5F, z0 + 0.5F));
            nearbySheds.add(this.create(x0 - 0.5F, z0 + 0.5F));
            float partitionCenterX = (float)px + 0.5F;
            float partitionCenterZ = (float)pz + 0.5F;
            float partitionToWatershedScale = 0.25F;
            float rainfall = 1.0F;
            if (this.rainfallLayer != null) {
               rainfall = this.rainfallLayer.getValue((float)((int)z0 & 15) / 16.0F, 1.0F - (float)((int)x0 & 15) / 16.0F) / 65.0F;
            } else if (this.chunkData != null) {
               rainfall = this.chunkData.getRainfall((int)x0, (int)z0) / 65.0F;
            }

            float x1 = 0.25F * partitionCenterX;
            float z1 = 0.25F * partitionCenterZ;
            float radius = 0.25F * (PARTITION_RADIUS + 0.39000002F);
            partition = new ArrayList<>(32);

            for (Watershed shed : nearbySheds) {
               for (RiverFractal river : shed.getRivers()) {
                  for (MidpointFractal fractal : river.getFractals()) {
                     if (fractal.maybeIntersect(x1, z1, radius)) {
                        partition.add(fractal);
                     }
                  }
               }
            }

            this.partitionCache.set(px, pz, partition);
         }

         return partition;
      }

      @Shadow
      public Watershed create(float x, float z) {
         return this.create(RiverHelpers.floor(x), RiverHelpers.floor(z));
      }

      @Shadow
      public Watershed create(int x, int z) {
         Watershed shed = (Watershed)this.watershedCache.getIfPresent(x, z);
         if (shed == null) {
            shed = Watershed.create(this.plates.get(), x, z, this.seed, this.sourceChance, this.length, this.depth, this.feather);
            this.watershedCache.set(x, z, shed);
         }

         return shed;
      }
   }
}
