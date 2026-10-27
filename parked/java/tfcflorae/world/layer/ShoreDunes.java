package tfcflorae.world.layer;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.util.RandomSource;
import java.util.Set;
import net.dries007.tfc.world.FastConcurrentCache;
import net.dries007.tfc.world.layer.Plate;
import net.dries007.tfc.world.layer.framework.TypedArea;
import net.dries007.tfc.world.layer.framework.TypedAreaFactory;
import net.dries007.tfc.world.river.MidpointFractal;
import net.dries007.tfc.world.river.RiverFractal;
import net.dries007.tfc.world.river.RiverHelpers;
import net.dries007.tfc.world.river.RiverFractal.MultiParallelBuilder;
import net.dries007.tfc.world.river.RiverFractal.Vertex;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.RandomSource;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import org.jetbrains.annotations.VisibleForTesting;

public abstract class ShoreDunes {
   public static final float SHORE_DUNES_WIDTH = 0.4F;
   private static final int[] DIRECTIONS = new int[]{-1, -1, -1, 0, -1, 1, 0, -1, 0, 1, 1, -1, 1, 0, 1, 1};
   private final Plate plate;

   public static ShoreDunes create(TypedArea<Plate> area, int sampleX, int sampleZ, long seed, float sourceChance, float length, int depth, float feather) {
      Plate root = (Plate)area.get(sampleX, sampleZ);
      if (root.oceanic()) {
         return new ShoreDunes.Empty(root);
      } else {
         LongSet interior = new LongOpenHashSet();
         LongSet sources = new LongOpenHashSet();
         LongList queue = new LongArrayList();
         long first = RiverHelpers.pack(sampleX, sampleZ);
         queue.add(first);
         interior.add(first);

         while (!queue.isEmpty()) {
            long key = queue.removeLong(queue.size() - 1);
            int x0 = RiverHelpers.unpackX(key);
            int z0 = RiverHelpers.unpackZ(key);

            for (int i = 0; i < DIRECTIONS.length; i += 2) {
               int x1 = x0 + DIRECTIONS[i];
               int z1 = z0 + DIRECTIONS[i | 1];
               long next = RiverHelpers.pack(x1, z1);
               Plate plate = (Plate)area.get(x1, z1);
               if (plate.equals(root)) {
                  if (interior.add(next)) {
                     queue.add(next);
                  }
               } else if (plate.oceanic()) {
                  sources.add(next);
               }
            }
         }

         if (sources.isEmpty()) {
            return new ShoreDunes.Empty(root);
         } else {
            RandomSource random = new XoroshiroRandomSource(seed + 1L ^ (long)root.hashCode());
            return new ShoreDunes.Lines(root, interior, sources, random, sourceChance, length, depth, feather);
         }
      }
   }

   protected ShoreDunes(Plate plate) {
      this.plate = plate;
   }

   public abstract List<RiverFractal> getRivers();

   public Plate getPlate() {
      return this.plate;
   }

   @Override
   public int hashCode() {
      return this.plate.hashCode();
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ShoreDunes watershed = (ShoreDunes)o;
         return this.plate.equals(watershed.plate);
      } else {
         return false;
      }
   }

   public static class Context {
      private static final int PARTITION_BITS = 5;
      private static final int ZOOM_BITS = 7;
      private static final int WATERSHED_CACHE_BITS = 8;
      private static final int PARTITION_CACHE_BITS = 10;
      private static final float PARTITION_RADIUS = (float)Math.sqrt(2.0) / 2.0F;
      private static final int PARTITION_TO_ZOOM_BITS = 2;
      private final ThreadLocal<TypedArea<Plate>> plates;
      private final FastConcurrentCache<ShoreDunes> watershedCache;
      private final FastConcurrentCache<List<MidpointFractal>> partitionCache;
      private final long seed;
      private final float sourceChance;
      private final float length;
      private final int depth;
      private final float feather;

      public Context(TypedAreaFactory<Plate> plates, long seed, float sourceChance, float length, int depth, float feather) {
         this.plates = ThreadLocal.withInitial(plates);
         this.watershedCache = new FastConcurrentCache(256);
         this.partitionCache = new FastConcurrentCache(1024);
         this.seed = seed + 1L;
         this.sourceChance = sourceChance;
         this.length = length;
         this.depth = depth;
         this.feather = feather;
      }

      public List<MidpointFractal> getFractalsByPartition(int x, int z) {
         int px = x >> 5;
         int pz = z >> 5;
         List<MidpointFractal> partition = (List<MidpointFractal>)this.partitionCache.getIfPresent(px, pz);
         if (partition == null) {
            Random random = RandomSource.create(this.seed);
            Set<ShoreDunes> nearbySheds = new ObjectOpenHashSet(2);
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
            float x1 = 0.25F * partitionCenterX;
            float z1 = 0.25F * partitionCenterZ;
            float radius = 0.25F * (PARTITION_RADIUS + 0.8F * Mth.clamp((float)Math.abs(random.nextGaussian() * 3.0), 0.0F, 3.0F));
            partition = new ArrayList<>(32);

            for (ShoreDunes shed : nearbySheds) {
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

      public ShoreDunes create(float x, float z) {
         return this.create(RiverHelpers.floor(x), RiverHelpers.floor(z));
      }

      public ShoreDunes create(int x, int z) {
         ShoreDunes shed = (ShoreDunes)this.watershedCache.getIfPresent(x, z);
         if (shed == null) {
            shed = ShoreDunes.create(this.plates.get(), x, z, this.seed, this.sourceChance, this.length, this.depth, this.feather);
            this.watershedCache.set(x, z, shed);
         }

         return shed;
      }
   }

   static class Empty extends ShoreDunes {
      Empty(Plate plate) {
         super(plate);
      }

      @Override
      public List<RiverFractal> getRivers() {
         return Collections.emptyList();
      }
   }

   public static class Lines extends ShoreDunes {
      public final LongSet interior;
      public final LongSet sources;
      private final List<RiverFractal> riverBanks;

      public Lines(Plate plate, LongSet interior, LongSet sources, RandomSource random, float sourceChance, float length, int depth, float feather) {
         super(plate);
         this.interior = interior;
         this.sources = sources;
         MultiParallelBuilder context = new ShoreDunes.Lines.Builder();
         sources.longStream().sorted().forEach(key -> {
            float x0 = (float)RiverHelpers.unpackX(key) + 0.5F;
            float z0 = (float)RiverHelpers.unpackZ(key) + 0.5F;
            if (random.nextFloat() < sourceChance) {
               float angle = random.nextFloat() * 2.0F * (float) Math.PI;

               for (int i = 0; i < 8; i++) {
                  float dx = Mth.cos(angle) * 1.4F;
                  float dz = Mth.sin(angle) * 1.4F;
                  long adj = RiverHelpers.pack(x0 + dx, z0 + dz);
                  if (this.interior.contains(adj)) {
                     context.add(new net.dries007.tfc.world.river.RiverFractal.Builder(random, x0, z0, angle, length, depth, feather));
                     break;
                  }

                  angle += (float) (Math.PI / 4);
               }
            }
         });
         this.riverBanks = context.buildFractals();
      }

      @Override
      public List<RiverFractal> getRivers() {
         return this.riverBanks;
      }

      @VisibleForTesting
      public LongSet getSources() {
         return this.sources;
      }

      class Builder extends MultiParallelBuilder {
         Builder() {
         }

         protected boolean isLegal(Vertex prev, Vertex vertex) {
            int x = RiverHelpers.floor(vertex.x());
            int z = RiverHelpers.floor(vertex.y());
            long key = RiverHelpers.pack(x, z);
            return Lines.this.interior.contains(key);
         }
      }
   }
}
