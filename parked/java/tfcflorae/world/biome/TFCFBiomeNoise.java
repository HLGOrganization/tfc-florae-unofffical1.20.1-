package tfcflorae.world.biome;

import net.minecraft.util.RandomSource;
import net.dries007.tfc.world.BiomeNoiseSampler;
import net.dries007.tfc.world.biome.BiomeNoise;
import net.dries007.tfc.world.noise.Cellular2D;
import net.dries007.tfc.world.noise.Noise2D;
import net.dries007.tfc.world.noise.Noise3D;
import net.dries007.tfc.world.noise.OpenSimplex2D;
import net.dries007.tfc.world.noise.OpenSimplex3D;
import net.minecraft.util.Mth;

public final class TFCFBiomeNoise {
   public TFCFBiomeNoise() {
   }

   public static Noise2D lowlandsNew(long seed) {
      return BiomeNoise.hills(seed, -3, -2).add(new OpenSimplex2D(seed + 1L).octaves(6).spread(0.55F).scaled(-2.0F, 2.0F).clamped(-2.0F, 1.0F));
   }

   public static Noise2D sandDunes(long seed, int baseHeight, int scaleHeight) {
      OpenSimplex2D warp = new OpenSimplex2D(seed).octaves(3).spread(0.01F).scaled(-100.0F, 100.0F);
      Noise2D baseNoise = new OpenSimplex2D(seed).octaves(3).spread(0.05F).ridged().warped(warp).scaled(-0.2F, 0.8F).ridged().map(x -> {
         float x0 = 0.125F * (x + 1.0F) * (x + 1.0F) * (x + 1.0F);
         return (float)(63 + baseHeight) + (float)scaleHeight * x0;
      });
      return (x, z) -> baseNoise.noise(x, z);
   }

   public static Noise2D sandDunesOld(long seed, int minHeight, int maxHeight) {
      OpenSimplex2D warp = new OpenSimplex2D(seed + 1L).octaves(3).spread(0.01F).scaled(0.8F, -0.8F, (float)(63 + minHeight), (float)(63 + maxHeight));
      return new OpenSimplex2D(seed + 1L)
         .octaves(5)
         .spread(0.1F)
         .ridged()
         .warped(warp)
         .scaled(1.0F, 0.0F, (float)(63 + minHeight), (float)(63 + maxHeight))
         .ridged();
   }

   public static Noise2D tablelands(long seed, int minHeight, int maxHeight) {
      OpenSimplex2D warp = new OpenSimplex2D(seed + 1L).octaves(4).spread(0.05F).scaled(-100.0F, 100.0F);
      return new Cellular2D(seed + 1L)
         .octaves(4)
         .spread(0.025F)
         .ridged()
         .warped(warp)
         .scaled(-0.4F, 0.4F)
         .ridged()
         .clamped((float)(63 + minHeight), (float)(63 + maxHeight));
   }

   public static Noise2D funk(long seed, int minHeight, int maxHeight) {
      OpenSimplex2D warp = new OpenSimplex2D(seed + 1L).octaves(3).spread(0.01F).scaled(0.8F, -0.8F, (float)(63 + minHeight), (float)(63 + maxHeight));
      return new OpenSimplex2D(seed + 1L)
         .octaves(5)
         .spread(0.001F)
         .ridged()
         .warped(warp)
         .scaled(0.4F, -0.4F, (float)(63 + minHeight), (float)(63 + maxHeight))
         .ridged();
   }

   public static Noise2D sawtoothCliffs(long seed, int minHeight, int maxHeight) {
      OpenSimplex2D warp = new OpenSimplex2D(seed + 1L).octaves(3).spread(0.05F).scaled(-100.0F, 100.0F);
      return new OpenSimplex2D(seed + 1L)
         .octaves(3)
         .spread(0.01F)
         .ridged()
         .warped(warp)
         .add(tablelands(seed + 2L, -5, 15))
         .scaled(-0.3F, 0.5F, (float)(63 + minHeight), (float)(63 + maxHeight))
         .ridged()
         .clamped((float)(63 + minHeight), (float)(63 + maxHeight));
   }

   public static Noise2D pitlandCanyons(long seed, int minHeight, int maxHeight) {
      OpenSimplex2D warp = new OpenSimplex2D(seed + 1L).octaves(3).spread(0.05F).scaled(-100.0F, 100.0F);
      return new Cellular2D(seed)
         .warped(warp)
         .ridged()
         .octaves(4)
         .spread(0.055F)
         .ridged()
         .warped(warp)
         .scaled(-0.4F, 0.8F, (float)(63 + minHeight), (float)(63 + maxHeight))
         .add(BiomeNoise.hills(seed + 1L, 20, 30))
         .terraces(8);
   }

   public static Noise2D pitlandCanyonsTweaked(long seed, int minHeight, int maxHeight) {
      OpenSimplex2D warp = new OpenSimplex2D(seed + 1L).octaves(3).spread(0.01F).scaled(-0.8F, 0.8F, (float)(63 + minHeight), (float)(63 + maxHeight));
      Noise2D baseNoise = new OpenSimplex2D(seed + 1L).octaves(5).spread(0.1F).ridged().warped(warp).scaled(-30.0F, 30.0F).ridged();
      return (x, z) -> {
         float height = baseNoise.noise(x, z);
         if (height < BiomeNoise.hills(seed, minHeight, minHeight + 10).noise(x, z)) {
            height = BiomeNoise.hills(seed, minHeight, minHeight + 10).noise(x, z);
         }

         if (height > BiomeNoise.hills(seed + 1L, maxHeight, maxHeight + 10).noise(x, z)) {
            height = BiomeNoise.hills(seed + 1L, maxHeight, maxHeight + 10).noise(x, z);
         }

         return height;
      };
   }

   public static Noise2D badlands(long seed, int minHeight, int maxHeight) {
      return new OpenSimplex2D(seed)
         .octaves(4)
         .spread(0.025F)
         .scaled((float)(63 + minHeight), (float)(63 + maxHeight))
         .add(
            new OpenSimplex2D(seed + 1L)
               .octaves(4)
               .spread(0.04F)
               .ridged()
               .map(x -> 1.3F * -(x > 0.0F ? x * x * x : 0.5F * x))
               .scaled(-1.0F, 0.3F, -1.0F, 1.0F)
               .terraces(15)
               .scaled(-19.5F, 0.0F)
         )
         .map(x -> x < 63.0F ? 63.0F - 0.3F * (63.0F - x) : x);
   }

   public static Noise2D extremeBadlands(long seed, int minHeight, int maxHeight) {
      return new OpenSimplex2D(seed)
         .octaves(4)
         .spread(0.025F * (float)minHeight * 0.6F)
         .scaled((float)(63 + minHeight), (float)(63 + maxHeight))
         .add(
            new OpenSimplex2D(seed + 1L)
               .octaves(4)
               .spread(0.04F)
               .ridged()
               .map(x -> 1.3F * -(x > 0.0F ? x * x * x : 0.5F * x))
               .scaled(-1.0F, 0.3F, -1.0F, 1.0F)
               .terraces(15)
               .scaled(-19.5F, (float)(0 + maxHeight))
         )
         .map(x -> x < 63.0F ? 63.0F - 0.3F * (63.0F - x) : x);
   }

   public static Noise2D moorlands(long seed, int minHeight, int maxHeight) {
      return new OpenSimplex2D(seed)
         .octaves(4)
         .spread(0.2F / (float)minHeight)
         .scaled((float)(63 + minHeight), (float)(63 + maxHeight))
         .add(
            new OpenSimplex2D(seed + 1L)
               .octaves(4)
               .spread(0.02F)
               .ridged()
               .map(x -> 1.3F * -(x > 0.0F ? x * x * x : 0.5F * x))
               .scaled(-1.0F, 0.3F, -1.3F, 1.3F)
               .scaled(-19.5F, 0.7F)
         )
         .map(x -> x < 63.0F ? 63.0F - 0.39000002F * (63.0F - x) : x);
   }

   public static Noise2D dunes(long seed, int minHeight, int maxHeight) {
      OpenSimplex2D warp = new OpenSimplex2D(seed).octaves(4).spread(0.05F).scaled(-100.0F, 100.0F);
      return new OpenSimplex2D(seed + 1L)
         .octaves(4)
         .spread(0.06F)
         .warped(warp)
         .map(x -> (double)x > 0.4 ? x - 0.8F : -x)
         .scaled(-0.4F, 1.2F, (float)(63 + minHeight), (float)(63 + maxHeight));
   }

   public static Noise2D pitlands(long seed, int minHeight, int maxHeight) {
      OpenSimplex2D warp = new OpenSimplex2D(seed).octaves(4).spread(0.03F).scaled(-100.0F, 100.0F);
      Noise2D baseNoise = new OpenSimplex2D(seed + 1L)
         .octaves(4)
         .spread(0.06F)
         .warped(warp)
         .map(x -> (double)x > 0.4 ? x - 0.8F : -x)
         .scaled(-0.4F, 0.8F, (float)(63 + minHeight), (float)(63 + maxHeight));
      Noise2D upperNoise = new OpenSimplex2D(seed).octaves(4).spread(0.05F).scaled(-0.4F, 0.8F, (float)(63 + minHeight + 8), (float)(63 + maxHeight - 8));
      return (x, z) -> {
         float height = baseNoise.noise(x, z);
         if (height > 81.0F) {
            height = upperNoise.noise(x, z);
         }

         if (height < 64.0F) {
            height = upperNoise.noise(x, z);
         }

         return height;
      };
   }

   public static Noise2D canyonCliffs(long seed, int minHeight, int maxHeight) {
      Random generator = RandomSource.create(seed);
      OpenSimplex2D warp = new OpenSimplex2D(seed).octaves(4).spread(0.03F).scaled(-100.0F, 100.0F);
      Noise2D baseNoise = new OpenSimplex2D(seed + 1L)
         .octaves(4)
         .spread(0.06F)
         .warped(warp)
         .map(x -> (double)x > 0.4 ? x - 0.8F : -x)
         .scaled(-0.4F, 0.8F, (float)(63 + minHeight), (float)(63 + maxHeight));
      int extra = generator.nextInt(3);
      Noise2D cliffNoise = new OpenSimplex2D(seed + 2L).octaves(2).spread(0.01F).scaled(0.0F, 25.0F).map(x -> x > 0.0F ? x : 0.0F);
      Noise2D cliffHeightNoiseLower = new OpenSimplex2D(seed + 3L).octaves(2).spread(0.01F).scaled(70.0F, 90.0F);
      return (x, z) -> {
         float height = baseNoise.noise(x, z);
         if (height > (float)(70 + extra)) {
            float cliffHeight = cliffHeightNoiseLower.noise(x, z) - height;
            if (cliffHeight < 0.0F) {
               float mappedCliffHeight = Mth.clampedMap(cliffHeight, 0.0F, -1.0F, 0.0F, 1.0F);
               height += mappedCliffHeight * cliffNoise.noise(x, z);
            }
         }

         return height;
      };
   }

   public static Noise2D grandCanyon(long seed) {
      OpenSimplex2D warp = new OpenSimplex2D(seed).octaves(2).spread(0.01F).scaled(-25.0F, 25.0F);
      Random generator = RandomSource.create(seed);
      Noise2D noise = new OpenSimplex2D(generator.nextLong()).octaves(4).spread(0.05F).scaled(65.0F, 77.0F).terraces(2);

      for (int layer = 0; layer < 5; layer++) {
         float threshold = 0.25F;
         float delta = 0.025F;
         noise = noise.add(
            new OpenSimplex2D(generator.nextLong())
               .octaves(3)
               .spread(0.02F + 0.01F * (float)layer)
               .warped(warp)
               .abs()
               .affine(1.0F, -0.05F * (float)layer)
               .map(t -> Mth.clampedMap(t, 0.25F, 0.275F, 0.0F, 1.0F))
               .lazyProduct(new OpenSimplex2D(generator.nextLong()).octaves(4).spread(0.1F).scaled(-10.0F, 25.0F))
         );
      }

      return noise;
   }

   public static Noise2D seamounts(long seed, int depthMin, int depthMax) {
      OpenSimplex2D warp = new OpenSimplex2D(seed).octaves(2).spread(0.015F).scaled(-30.0F, 30.0F);
      Noise2D ridgeNoise = new OpenSimplex2D(seed + 1L).octaves(4).spread(0.015F).ridged().map(x -> {
         if (x > -0.3F) {
            x = (x + 0.3F) / 1.3F;
            x = x * x * x;
            return -16.0F * x;
         } else {
            return 0.0F;
         }
      });
      Noise2D baseNoise = new OpenSimplex2D(seed + 2L)
         .octaves(4)
         .spread(0.11F)
         .scaled((float)(63 + depthMin), (float)(63 + depthMax))
         .add(ridgeNoise)
         .warped(warp);
      Noise2D cliffNoise = new OpenSimplex2D(seed + 2L).octaves(2).spread(0.01F).scaled(0.0F, 25.0F).map(x -> x > 0.0F ? x : 0.0F);
      Noise2D cliffHeightNoiseLower = new OpenSimplex2D(seed + 3L).octaves(2).spread(0.01F).scaled(0.0F, 15.0F);
      Noise2D cliffHeightNoiseMiddle = new OpenSimplex2D(seed + 3L).octaves(2).spread(0.01F).scaled(15.0F, 25.0F);
      Noise2D cliffHeightNoiseUpper = new OpenSimplex2D(seed + 3L).octaves(2).spread(0.01F).scaled(25.0F, 40.0F);
      return (x, z) -> {
         Random generator = RandomSource.create((long)baseNoise.noise(x, z));
         int extra = (int)generator.nextGaussian() * 4;
         float height = baseNoise.noise(x, z);
         if (height > (float)(-245 + extra)) {
            float cliffHeight = cliffHeightNoiseLower.noise(x, z) - height;
            if (cliffHeight < 0.0F) {
               float mappedCliffHeight = Mth.clampedMap(cliffHeight, 0.0F, -1.0F, 0.0F, 1.0F);
               height += mappedCliffHeight * cliffNoise.noise(x, z);
            }
         }

         if (height > (float)(-237 + extra)) {
            float cliffHeight = cliffHeightNoiseMiddle.noise(x, z) - height;
            if (cliffHeight < 0.0F) {
               float mappedCliffHeight = Mth.clampedMap(cliffHeight, 0.0F, -1.0F, 0.0F, 1.0F);
               height += mappedCliffHeight * cliffNoise.noise(x, z);
            }
         }

         if (height > (float)(-230 + extra)) {
            float cliffHeight = cliffHeightNoiseUpper.noise(x, z) - height;
            if (cliffHeight < 0.0F) {
               float mappedCliffHeight = Mth.clampedMap(cliffHeight, 0.0F, -1.0F, 0.0F, 1.0F);
               height += mappedCliffHeight * cliffNoise.noise(x, z);
            }
         }

         return height;
      };
   }

   public static Noise2D mountainsBadlands(long seed, int baseHeight, int scaleHeight) {
      Noise2D baseNoise = new OpenSimplex2D(seed)
         .octaves(6)
         .spread(0.2F)
         .add(new OpenSimplex2D(seed + 1L).octaves(4).spread(0.02F).scaled(-1.0F, 1.0F).ridged())
         .map(x -> {
            float x0 = 0.125F * (x + 1.0F) * (x + 1.0F) * (x + 1.0F);
            return (float)(63 + baseHeight) + (float)scaleHeight * x0;
         });
      Noise2D cliffNoise = new OpenSimplex2D(seed + 2L).octaves(2).spread(0.01F).scaled(0.0F, 25.0F).map(x -> x > 0.0F ? x : 0.0F);
      Noise2D cliffHeightNoiseLower = new OpenSimplex2D(seed + 3L).octaves(2).spread(0.01F).scaled(55.0F, 115.0F);
      Noise2D cliffHeightNoiseMiddle = new OpenSimplex2D(seed + 3L).octaves(2).spread(0.01F).scaled(75.0F, 140.0F);
      Noise2D cliffHeightNoiseUpper = new OpenSimplex2D(seed + 3L).octaves(2).spread(0.01F).scaled(100.0F, 160.0F);
      return (x, z) -> {
         Random generator = RandomSource.create((long)baseNoise.noise(x, z));
         int extra = (int)generator.nextGaussian() * 4;
         float height = baseNoise.noise(x, z);
         if (height > (float)(75 + extra)) {
            float cliffHeight = cliffHeightNoiseLower.noise(x, z) - height;
            if (cliffHeight < 0.0F) {
               float mappedCliffHeight = Mth.clampedMap(cliffHeight, 0.0F, -1.0F, 0.0F, 1.0F);
               height += mappedCliffHeight * cliffNoise.noise(x, z);
            }
         }

         if (height > (float)(95 + extra)) {
            float cliffHeight = cliffHeightNoiseMiddle.noise(x, z) - height;
            if (cliffHeight < 0.0F) {
               float mappedCliffHeight = Mth.clampedMap(cliffHeight, 0.0F, -1.0F, 0.0F, 1.0F);
               height += mappedCliffHeight * cliffNoise.noise(x, z);
            }
         }

         if (height > (float)(120 + extra)) {
            float cliffHeight = cliffHeightNoiseUpper.noise(x, z) - height;
            if (cliffHeight < 0.0F) {
               float mappedCliffHeight = Mth.clampedMap(cliffHeight, 0.0F, -1.0F, 0.0F, 1.0F);
               height += mappedCliffHeight * cliffNoise.noise(x, z);
            }
         }

         return height;
      };
   }

   public static Noise2D pits(long seed, int minHeight, int maxHeight) {
      OpenSimplex2D warp = new OpenSimplex2D(seed + 1L).octaves(3).spread(0.01F).scaled(0.8F, -0.8F, (float)(63 + minHeight), (float)(63 + maxHeight));
      return new OpenSimplex2D(seed + 1L)
         .octaves(5)
         .spread(0.1F)
         .ridged()
         .warped(warp)
         .scaled(-1.0F, -0.1F, (float)(63 + minHeight), (float)(63 + maxHeight))
         .ridged();
   }

   public static Noise2D mistyPeaks(long seed, int baseHeight, int scaleHeight) {
      OpenSimplex2D warp = new OpenSimplex2D(seed).octaves(4).spread(0.03F).scaled(-100.0F, 100.0F);
      Noise2D baseNoise = new OpenSimplex2D(seed)
         .octaves(6)
         .spread(0.05F)
         .warped(warp)
         .add(new OpenSimplex2D(seed + 1L).octaves(4).spread(0.02F).warped(warp).scaled(-1.0F, 1.0F).ridged())
         .map(x -> {
            float x0 = 0.125F * (x + 1.0F) * (x + 1.0F) * (x + 1.0F);
            return (float)(63 + baseHeight) + (float)scaleHeight * x0;
         });
      Noise2D cliffNoise = new OpenSimplex2D(seed + 2L).octaves(2).spread(0.01F).scaled(0.0F, 25.0F).map(x -> x > 0.0F ? x : 0.0F);
      Noise2D cliffHeightNoiseLower = new OpenSimplex2D(seed + 3L).octaves(2).spread(0.01F).scaled(55.0F, 95.0F);
      Noise2D cliffHeightNoiseMiddle = new OpenSimplex2D(seed + 3L).octaves(2).spread(0.01F).scaled(70.0F, 100.0F);
      Noise2D cliffHeightNoiseUpper = new OpenSimplex2D(seed + 3L).octaves(2).spread(0.01F).scaled(75.0F, 125.0F);
      Noise2D cliffHeightNoiseHighUpper = new OpenSimplex2D(seed + 3L).octaves(2).spread(0.01F).scaled(90.0F, 180.0F);
      return (x, z) -> {
         Random generator = RandomSource.create((long)warp.noise(x, z));
         int extra = (int)generator.nextGaussian() * 4;
         float height = baseNoise.noise(x, z);
         if (height > (float)(70 + extra)) {
            float cliffHeight = cliffHeightNoiseLower.noise(x, z) - height;
            if (cliffHeight < 0.0F) {
               float mappedCliffHeight = Mth.clampedMap(cliffHeight, 0.0F, -1.0F, 0.0F, 1.0F);
               height += mappedCliffHeight * cliffNoise.noise(x, z);
            }
         }

         if (height > (float)(80 + extra)) {
            float cliffHeight = cliffHeightNoiseMiddle.noise(x, z) - height;
            if (cliffHeight < 0.0F) {
               float mappedCliffHeight = Mth.clampedMap(cliffHeight, 0.0F, -1.0F, 0.0F, 1.0F);
               height += mappedCliffHeight * cliffNoise.noise(x, z);
            }
         }

         if (height > (float)(90 + extra)) {
            float cliffHeight = cliffHeightNoiseUpper.noise(x, z) - height;
            if (cliffHeight < 0.0F) {
               float mappedCliffHeight = Mth.clampedMap(cliffHeight, 0.0F, -1.0F, 0.0F, 1.0F);
               height += mappedCliffHeight * cliffNoise.noise(x, z);
            }
         }

         if (height > (float)(110 + extra)) {
            float cliffHeight = cliffHeightNoiseHighUpper.noise(x, z) - height;
            if (cliffHeight < 0.0F) {
               float mappedCliffHeight = Mth.clampedMap(cliffHeight, 0.0F, -1.0F, 0.0F, 1.0F);
               height += mappedCliffHeight * cliffNoise.noise(x, z);
            }
         }

         return height;
      };
   }

   public static Noise2D mistyPeaksNew(long seed, int baseHeight, int scaleHeight) {
      OpenSimplex2D warp = new OpenSimplex2D(seed).octaves(4).spread(0.03F).scaled(-100.0F, 100.0F);
      Noise2D baseNoise = new OpenSimplex2D(seed)
         .octaves(6)
         .spread(0.1F)
         .warped(warp)
         .add(new OpenSimplex2D(seed + 1L).octaves(8).spread(0.1F).warped(warp).scaled(-0.2F, 0.8F).ridged())
         .map(x -> {
            float x0 = 0.125F * (x + 1.0F) * (x + 1.0F) * (x + 1.0F);
            return (float)(63 + baseHeight) + (float)scaleHeight * x0;
         });
      Noise2D cliffNoise = new OpenSimplex2D(seed + 2L).octaves(2).spread(0.01F).scaled(0.0F, 25.0F).map(x -> x > 0.0F ? x : 0.0F);
      Noise2D cliffHeightNoiseLower = new OpenSimplex2D(seed + 3L).octaves(2).spread(0.01F).scaled(55.0F, 95.0F);
      Noise2D cliffHeightNoiseMiddle = new OpenSimplex2D(seed + 3L).octaves(2).spread(0.01F).scaled(70.0F, 100.0F);
      Noise2D cliffHeightNoiseUpper = new OpenSimplex2D(seed + 3L).octaves(2).spread(0.01F).scaled(75.0F, 125.0F);
      Noise2D cliffHeightNoiseHighUpper = new OpenSimplex2D(seed + 3L).octaves(2).spread(0.01F).scaled(90.0F, 180.0F);
      return (x, z) -> {
         float extra = pits(seed + 1L, -70, -50).noise(x, z);
         float height = baseNoise.noise(x, z);
         if (height > 70.0F + extra) {
            float cliffHeight = cliffHeightNoiseLower.noise(x, z) - height;
            if (cliffHeight < 0.0F) {
               float mappedCliffHeight = Mth.clampedMap(cliffHeight, 0.0F, -1.0F, 0.0F, 1.0F);
               height += mappedCliffHeight * cliffNoise.noise(x, z);
            }
         }

         if (height > 80.0F + extra) {
            float cliffHeight = cliffHeightNoiseMiddle.noise(x, z) - height;
            if (cliffHeight < 0.0F) {
               float mappedCliffHeight = Mth.clampedMap(cliffHeight, 0.0F, -1.0F, 0.0F, 1.0F);
               height += mappedCliffHeight * cliffNoise.noise(x, z);
            }
         }

         if (height > 90.0F + extra) {
            float cliffHeight = cliffHeightNoiseUpper.noise(x, z) - height;
            if (cliffHeight < 0.0F) {
               float mappedCliffHeight = Mth.clampedMap(cliffHeight, 0.0F, -1.0F, 0.0F, 1.0F);
               height += mappedCliffHeight * cliffNoise.noise(x, z);
            }
         }

         if (height > 110.0F + extra) {
            float cliffHeight = cliffHeightNoiseHighUpper.noise(x, z) - height;
            if (cliffHeight < 0.0F) {
               float mappedCliffHeight = Mth.clampedMap(cliffHeight, 0.0F, -1.0F, 0.0F, 1.0F);
               height += mappedCliffHeight * cliffNoise.noise(x, z);
            }
         }

         return height;
      };
   }

   public static Noise2D mistyPeaksFractured(long seed, int baseHeight, int scaleHeight) {
      OpenSimplex2D warp = new OpenSimplex2D(seed).octaves(4).spread(0.03F).scaled(-100.0F, 100.0F);
      Noise2D baseNoise = new Cellular2D(seed)
         .octaves(4)
         .spread(0.1F)
         .warped(warp)
         .add(new Cellular2D(seed + 1L).octaves(6).spread(0.1F).warped(warp).scaled(-0.2F, 0.8F).ridged())
         .map(x -> {
            float x0 = 0.125F * (x + 1.0F) * (x + 1.0F) * (x + 1.0F);
            return (float)(63 + baseHeight) + (float)scaleHeight * x0;
         });
      Noise2D cliffNoise = new OpenSimplex2D(seed + 2L).octaves(2).spread(0.01F).scaled(0.0F, 25.0F).map(x -> x > 0.0F ? x : 0.0F);
      Noise2D cliffHeightNoiseLower = new OpenSimplex2D(seed + 3L).octaves(2).spread(0.01F).scaled(55.0F, 95.0F);
      Noise2D cliffHeightNoiseMiddle = new OpenSimplex2D(seed + 3L).octaves(2).spread(0.01F).scaled(70.0F, 100.0F);
      Noise2D cliffHeightNoiseUpper = new OpenSimplex2D(seed + 3L).octaves(2).spread(0.01F).scaled(75.0F, 125.0F);
      Noise2D cliffHeightNoiseHighUpper = new OpenSimplex2D(seed + 3L).octaves(2).spread(0.01F).scaled(90.0F, 180.0F);
      return (x, z) -> {
         float extra = pits(seed + 1L, -70, -50).noise(x, z);
         float height = baseNoise.noise(x, z);
         if (height > 70.0F + extra) {
            float cliffHeight = cliffHeightNoiseLower.noise(x, z) - height;
            if (cliffHeight < 0.0F) {
               float mappedCliffHeight = Mth.clampedMap(cliffHeight, 0.0F, -1.0F, 0.0F, 1.0F);
               height += mappedCliffHeight * cliffNoise.noise(x, z);
            }
         }

         if (height > 80.0F + extra) {
            float cliffHeight = cliffHeightNoiseMiddle.noise(x, z) - height;
            if (cliffHeight < 0.0F) {
               float mappedCliffHeight = Mth.clampedMap(cliffHeight, 0.0F, -1.0F, 0.0F, 1.0F);
               height += mappedCliffHeight * cliffNoise.noise(x, z);
            }
         }

         if (height > 90.0F + extra) {
            float cliffHeight = cliffHeightNoiseUpper.noise(x, z) - height;
            if (cliffHeight < 0.0F) {
               float mappedCliffHeight = Mth.clampedMap(cliffHeight, 0.0F, -1.0F, 0.0F, 1.0F);
               height += mappedCliffHeight * cliffNoise.noise(x, z);
            }
         }

         if (height > 110.0F + extra) {
            float cliffHeight = cliffHeightNoiseHighUpper.noise(x, z) - height;
            if (cliffHeight < 0.0F) {
               float mappedCliffHeight = Mth.clampedMap(cliffHeight, 0.0F, -1.0F, 0.0F, 1.0F);
               height += mappedCliffHeight * cliffNoise.noise(x, z);
            }
         }

         return height;
      };
   }

   public static Noise2D mistyPeaksCell(long seed, int baseHeight, int scaleHeight) {
      OpenSimplex2D warp = new OpenSimplex2D(seed).octaves(4).spread(0.03F).scaled(-100.0F, 100.0F);
      Noise2D baseNoise = new OpenSimplex2D(seed)
         .octaves(6)
         .spread(0.1F)
         .warped(warp)
         .add(new OpenSimplex2D(seed + 1L).octaves(8).spread(0.1F).warped(warp).scaled(-0.2F, 0.8F).ridged())
         .map(x -> {
            float x0 = 0.125F * (x + 1.0F) * (x + 1.0F) * (x + 1.0F);
            return (float)(63 + baseHeight) + (float)scaleHeight * x0;
         });
      Noise2D cliffNoise = new OpenSimplex2D(seed + 2L).octaves(2).spread(0.01F).scaled(0.0F, 25.0F).map(x -> x > 0.0F ? x : 0.0F);
      Noise2D cliffHeightNoiseLower = new OpenSimplex2D(seed + 3L).octaves(2).spread(0.01F).scaled(55.0F, 95.0F);
      Noise2D cliffHeightNoiseMiddle = new OpenSimplex2D(seed + 3L).octaves(2).spread(0.01F).scaled(70.0F, 100.0F);
      Noise2D cliffHeightNoiseUpper = new OpenSimplex2D(seed + 3L).octaves(2).spread(0.01F).scaled(75.0F, 125.0F);
      Noise2D cliffHeightNoiseHighUpper = new OpenSimplex2D(seed + 3L).octaves(2).spread(0.01F).scaled(90.0F, 180.0F);
      return (x, z) -> {
         float extra = sawtoothCliffs(seed + 1L, baseHeight, scaleHeight).noise(x, z);
         float height = baseNoise.noise(x, z);
         if (height > 70.0F + extra) {
            float cliffHeight = cliffHeightNoiseLower.noise(x, z) - height;
            if (cliffHeight < 0.0F) {
               float mappedCliffHeight = Mth.clampedMap(cliffHeight, 0.0F, -1.0F, 0.0F, 1.0F);
               height += mappedCliffHeight * cliffNoise.noise(x, z);
            }
         }

         if (height > 80.0F + extra) {
            float cliffHeight = cliffHeightNoiseMiddle.noise(x, z) - height;
            if (cliffHeight < 0.0F) {
               float mappedCliffHeight = Mth.clampedMap(cliffHeight, 0.0F, -1.0F, 0.0F, 1.0F);
               height += mappedCliffHeight * cliffNoise.noise(x, z);
            }
         }

         if (height > 90.0F + extra) {
            float cliffHeight = cliffHeightNoiseUpper.noise(x, z) - height;
            if (cliffHeight < 0.0F) {
               float mappedCliffHeight = Mth.clampedMap(cliffHeight, 0.0F, -1.0F, 0.0F, 1.0F);
               height += mappedCliffHeight * cliffNoise.noise(x, z);
            }
         }

         if (height > 110.0F + extra) {
            float cliffHeight = cliffHeightNoiseHighUpper.noise(x, z) - height;
            if (cliffHeight < 0.0F) {
               float mappedCliffHeight = Mth.clampedMap(cliffHeight, 0.0F, -1.0F, 0.0F, 1.0F);
               height += mappedCliffHeight * cliffNoise.noise(x, z);
            }
         }

         return height;
      };
   }

   public static Noise2D frostedGlass(long seed, int baseHeight, int scaleHeight) {
      OpenSimplex2D warp = new OpenSimplex2D(seed).octaves(4).spread(0.03F).scaled(-100.0F, 100.0F);
      Noise2D baseNoise = new OpenSimplex2D(seed)
         .octaves(6)
         .spread(0.1F)
         .warped(warp)
         .add(new OpenSimplex2D(seed + 1L).octaves(2).spread(0.1F).warped(warp).scaled(-0.2F, 0.8F).ridged())
         .map(x -> {
            float x0 = 0.125F * (x + 1.0F) * (x + 1.0F) * (x + 1.0F);
            return (float)(63 + baseHeight) + (float)scaleHeight * x0;
         });
      Noise2D cliffNoise = new OpenSimplex2D(seed + 2L).octaves(2).spread(0.01F).scaled(0.0F, 25.0F).map(x -> x > 0.0F ? x : 0.0F);
      Noise2D cliffHeightNoiseLower = new OpenSimplex2D(seed + 3L).octaves(2).spread(0.01F).scaled(55.0F, 95.0F);
      Noise2D cliffHeightNoiseMiddle = new OpenSimplex2D(seed + 3L).octaves(2).spread(0.01F).scaled(70.0F, 100.0F);
      Noise2D cliffHeightNoiseUpper = new OpenSimplex2D(seed + 3L).octaves(2).spread(0.01F).scaled(75.0F, 125.0F);
      Noise2D cliffHeightNoiseHighUpper = new OpenSimplex2D(seed + 3L).octaves(2).spread(0.01F).scaled(90.0F, 180.0F);
      return (x, z) -> {
         float extra = pits(seed + 1L, -70, -50).noise(x, z);
         float height = baseNoise.noise(x, z);
         if (height > 70.0F + extra) {
            float cliffHeight = cliffHeightNoiseLower.noise(x, z) - height;
            if (cliffHeight < 0.0F) {
               float mappedCliffHeight = Mth.clampedMap(cliffHeight, 0.0F, -1.0F, 0.0F, 1.0F);
               height += mappedCliffHeight * cliffNoise.noise(x, z);
            }
         }

         if (height > 80.0F + extra) {
            float cliffHeight = cliffHeightNoiseMiddle.noise(x, z) - height;
            if (cliffHeight < 0.0F) {
               float mappedCliffHeight = Mth.clampedMap(cliffHeight, 0.0F, -1.0F, 0.0F, 1.0F);
               height += mappedCliffHeight * cliffNoise.noise(x, z);
            }
         }

         if (height > 90.0F + extra) {
            float cliffHeight = cliffHeightNoiseUpper.noise(x, z) - height;
            if (cliffHeight < 0.0F) {
               float mappedCliffHeight = Mth.clampedMap(cliffHeight, 0.0F, -1.0F, 0.0F, 1.0F);
               height += mappedCliffHeight * cliffNoise.noise(x, z);
            }
         }

         if (height > 110.0F + extra) {
            float cliffHeight = cliffHeightNoiseHighUpper.noise(x, z) - height;
            if (cliffHeight < 0.0F) {
               float mappedCliffHeight = Mth.clampedMap(cliffHeight, 0.0F, -1.0F, 0.0F, 1.0F);
               height += mappedCliffHeight * cliffNoise.noise(x, z);
            }
         }

         return height;
      };
   }

   public static Noise2D coastalCliffs(long seed, int minHeight, int maxHeight) {
      OpenSimplex2D warp = new OpenSimplex2D(seed).octaves(3).spread(0.03F).scaled(-100.0F, 100.0F);
      Noise2D baseNoise = new OpenSimplex2D(seed + 1L)
         .octaves(6)
         .spread(0.06F)
         .warped(warp)
         .map(x -> (double)x > 0.4 ? x - 0.8F : -x)
         .scaled(-0.4F, 0.8F, (float)(63 + minHeight), (float)(63 + maxHeight));
      Noise2D cliffNoise = new OpenSimplex2D(seed + 2L).octaves(2).spread(0.01F).scaled(0.0F, 25.0F).map(x -> x > 0.0F ? x : 0.0F);
      Noise2D cliffHeightNoiseLower = new OpenSimplex2D(seed + 3L).octaves(2).spread(0.01F).scaled(60.0F, 70.0F);
      Noise2D cliffHeightNoiseMiddle = new OpenSimplex2D(seed + 3L).octaves(2).spread(0.01F).scaled(70.0F, 90.0F);
      return (x, z) -> {
         float height = baseNoise.noise(x, z);
         if (height > 60.0F) {
            float cliffHeight = cliffHeightNoiseLower.noise(x, z) - height;
            if (cliffHeight < 0.0F) {
               float mappedCliffHeight = Mth.clampedMap(cliffHeight, 0.0F, -1.0F, 0.0F, 1.0F);
               height += mappedCliffHeight * cliffNoise.noise(x, z);
            }
         }

         if (height > 70.0F) {
            float cliffHeight = cliffHeightNoiseMiddle.noise(x, z) - height;
            if (cliffHeight < 0.0F) {
               float mappedCliffHeight = Mth.clampedMap(cliffHeight, 0.0F, -1.0F, 0.0F, 1.0F);
               height += mappedCliffHeight * cliffNoise.noise(x, z);
            }
         }

         return height;
      };
   }

   public static Noise2D rainbowMountains(long seed, int baseHeight, int scaleHeight) {
      OpenSimplex2D warp = new OpenSimplex2D(seed).octaves(4).spread(0.03F).scaled(-100.0F, 100.0F);
      return new OpenSimplex2D(seed)
         .octaves(6)
         .spread(0.08F)
         .warped(warp)
         .add(new OpenSimplex2D(seed + 1L).octaves(4).spread(0.02F).warped(warp).scaled(-0.5F, 1.0F).ridged())
         .map(x -> {
            float x0 = 0.125F * (x + 1.0F) * (x + 1.0F) * (x + 1.0F);
            return (float)(63 + baseHeight) + (float)scaleHeight * x0;
         });
   }

   public static Noise2D riverShore(long seed) {
      return new OpenSimplex2D(seed).octaves(6).spread(0.15F).scaled(61.5F, 64.3F);
   }

   public static Noise2D lakeShore(long seed, int minHeight, int maxHeight) {
      return new OpenSimplex2D(seed).octaves(4).spread(0.17F).scaled((float)(63 + minHeight), (float)(63 + maxHeight));
   }

   public static Noise2D lake(long seed, int minHeight, int maxHeight) {
      return new OpenSimplex2D(seed).octaves(4).spread(0.15F).scaled((float)(63 + minHeight), (float)(63 + maxHeight));
   }

   public static Noise2D sharpHills(long seed) {
      Noise2D base = new OpenSimplex2D(seed).octaves(4).spread(0.08F);
      Noise2D lerp = new OpenSimplex2D(seed + 7198234123L).spread(0.013F).scaled(-0.3F, 1.6F).clamped(0.0F, 1.0F);
      Noise2D lerpMapped = (x, z) -> {
         float in = base.noise(x, z);
         return Mth.lerp(lerp.noise(x, z), in, sharpHillsMap(in));
      };
      OpenSimplex2D variance = new OpenSimplex2D(seed + 67981832123L).octaves(3).spread(0.06F).scaled(-0.2F, 0.2F);
      return lerpMapped.add(variance).scaled(-0.75F, 0.7F, 60.0F, 91.0F);
   }

   public static float sharpHillsMap(float in) {
      float in0 = 1.0F;
      float in1 = 0.67F;
      float in2 = 0.15F;
      float in3 = -0.15F;
      float in4 = -0.67F;
      float in5 = -1.0F;
      float out0 = 1.0F;
      float out1 = 0.7F;
      float out2 = 0.5F;
      float out3 = -0.5F;
      float out4 = -0.7F;
      float out5 = -1.0F;
      if (in > 0.67F) {
         return Mth.map(in, 0.67F, 1.0F, 0.7F, 1.0F);
      } else if (in > 0.15F) {
         return Mth.map(in, 0.15F, 0.67F, 0.5F, 0.7F);
      } else if (in > -0.15F) {
         return Mth.map(in, -0.15F, 0.15F, -0.5F, 0.5F);
      } else {
         return in > -0.67F ? Mth.map(in, -0.67F, -0.15F, -0.7F, -0.5F) : Mth.map(in, -1.0F, -0.67F, -1.0F, -0.7F);
      }
   }

   public static BiomeNoiseSampler riverSampler(long seed) {
      final Noise2D riverHeight = new OpenSimplex2D(seed).octaves(4).spread(0.2F).scaled(52.0F, 58.0F);
      final Noise3D cliffNoise = new OpenSimplex3D(seed).octaves(2).spread(0.1F).scaled(0.0F, 3.0F);
      return new BiomeNoiseSampler() {
         private double height;
         private int x;
         private int z;

         public void setColumn(int x, int z) {
            this.height = (double)riverHeight.noise((float)x, (float)z);
            this.x = x;
            this.z = z;
         }

         public double height() {
            return this.height;
         }

         public double noise(int y) {
            if (y > 83) {
               return 0.0;
            } else if (y > 73) {
               double easing = (double)(1.0F - (float)(y - 63 - 10) / 10.0F);
               return easing * (double)cliffNoise.noise((float)this.x, (float)y, (float)this.z);
            } else if (y > 63) {
               return (double)cliffNoise.noise((float)this.x, (float)y, (float)this.z);
            } else if (y > 55) {
               double easing = (double)(y - 63 + 8) / 8.0;
               return easing * (double)cliffNoise.noise((float)this.x, (float)y, (float)this.z);
            } else {
               return 0.4;
            }
         }
      };
   }

   public static BiomeNoiseSampler riverBankSampler(long seed) {
      final Noise2D riverBankHeight = new OpenSimplex2D(seed).octaves(4).spread(0.2F).scaled(52.0F, 74.0F);
      final Noise3D cliffNoise = new OpenSimplex3D(seed).octaves(2).spread(0.1F).scaled(0.0F, 3.0F);
      return new BiomeNoiseSampler() {
         private double height;
         private int x;
         private int z;

         public void setColumn(int x, int z) {
            this.height = (double)riverBankHeight.noise((float)x, (float)z);
            this.x = x;
            this.z = z;
         }

         public double height() {
            return this.height;
         }

         public double noise(int y) {
            if ((float)y > 103.0F + cliffNoise.noise((float)this.x, (float)y, (float)this.z) / 2.0F) {
               return -((Math.pow(1.08, (double)y) - 3969.0) / -2800.0) * (double)(cliffNoise.noise((float)this.x, (float)y, (float)this.z) / 2.0F);
            } else if (y > 65) {
               return (Math.sin((double)(y * 8)) / 15.0 + Math.pow(1.02, (double)(y - 63)) - 0.4)
                  * (double)(cliffNoise.noise((float)this.x, (float)y, (float)this.z) / 2.0F);
            } else if (y >= 63) {
               return (Math.pow(1.08, (double)y) - 132.3) / 40.0;
            } else if (y > 55) {
               double easing = (double)(y - 63 + 8) / 8.0;
               return easing * (double)cliffNoise.noise((float)this.x, (float)y, (float)this.z);
            } else {
               return y <= 55 ? 0.0 : 0.0;
            }
         }
      };
   }

   public static BiomeNoiseSampler chasmSampler(long seed) {
      final Noise2D riverBankHeight = new OpenSimplex2D(seed).octaves(4).spread(0.2F).scaled(62.0F, 74.0F);
      final Noise3D cliffNoise = new OpenSimplex3D(seed).octaves(2).spread(0.1F).scaled(0.0F, 3.0F);
      return new BiomeNoiseSampler() {
         private double height;
         private int x;
         private int z;

         public void setColumn(int x, int z) {
            this.height = (double)riverBankHeight.noise((float)x, (float)z);
            this.x = x;
            this.z = z;
         }

         public double height() {
            return this.height;
         }

         public double noise(int y) {
            if ((float)y > 103.0F + cliffNoise.noise((float)this.x, (float)y, (float)this.z) / 2.0F) {
               return -((Math.pow(1.08, (double)y) - 3969.0) / -2800.0) * (double)(cliffNoise.noise((float)this.x, (float)y, (float)this.z) / 2.0F);
            } else if (y > 65) {
               return (Math.sin((double)(y * 8)) / 15.0 + Math.pow(1.02, (double)(y - 63)) - 0.4)
                  * (double)(cliffNoise.noise((float)this.x, (float)y, (float)this.z) / 2.0F);
            } else if (y > 63) {
               return (Math.pow(1.08, (double)y) - 132.3) / 40.0;
            } else {
               return y > 0 ? 0.0 : 0.0;
            }
         }
      };
   }

   public static BiomeNoiseSampler plateauCliffsSampler(long seed) {
      final Noise2D riverBankHeight = new OpenSimplex2D(seed).octaves(4).spread(0.2F).scaled(62.0F, 74.0F);
      final Noise3D cliffNoise = new OpenSimplex3D(seed).octaves(2).spread(0.1F).scaled(0.0F, 3.0F);
      return new BiomeNoiseSampler() {
         private double height;
         private int x;
         private int z;

         public void setColumn(int x, int z) {
            this.height = (double)riverBankHeight.noise((float)x, (float)z);
            this.x = x;
            this.z = z;
         }

         public double height() {
            return this.height;
         }

         public double noise(int y) {
            if ((float)y > 103.0F + cliffNoise.noise((float)this.x, (float)y, (float)this.z) / 2.0F) {
               return -((Math.pow(1.08, (double)y) - 3969.0) / -2800.0) * (double)(cliffNoise.noise((float)this.x, (float)y, (float)this.z) / 2.0F);
            } else {
               return y > 71
                  ? (Math.sin((double)(y * 8)) / 15.0 + Math.pow(1.02, (double)(y - 63)) - 0.4)
                     * (double)(cliffNoise.noise((float)this.x, (float)y, (float)this.z) / 2.0F)
                  : 0.0;
            }
         }
      };
   }

   public static BiomeNoiseSampler coastalCliffsSampler(long seed) {
      final Noise2D riverHeight = new OpenSimplex2D(seed).octaves(5).spread(0.2F).scaled(43.0F, 163.0F);
      final Noise3D cliffNoise = new OpenSimplex3D(seed).octaves(8).spread(0.1F).scaled(0.0F, 5.0F);
      return new BiomeNoiseSampler() {
         private double height;
         private int x;
         private int z;

         public void setColumn(int x, int z) {
            this.height = (double)riverHeight.noise((float)x, (float)z);
            this.x = x;
            this.z = z;
         }

         public double height() {
            return this.height;
         }

         public double noise(int y) {
            return 0.0 * (double)cliffNoise.noise((float)this.x, (float)y, (float)this.z);
         }
      };
   }

   public static BiomeNoiseSampler undergroundRivers(long seed, Noise2D heightNoise) {
      Noise2D carvingCenterNoise = new OpenSimplex2D(seed).octaves(2).spread(0.02F).scaled(60.0F, 66.0F);
      Noise2D carvingHeightNoise = new OpenSimplex2D(seed + 1L).octaves(4).spread(0.15F).scaled(8.0F, 14.0F);
      return BiomeNoiseSampler.fromHeightAndCarvingNoise(heightNoise, carvingCenterNoise, carvingHeightNoise);
   }

   public static BiomeNoiseSampler carvingBadlands(long seed, final Noise2D heightNoise) {
      final Noise2D blobsNoise = new OpenSimplex2D(seed + 1L).octaves(6).spread(0.016F).abs();
      final Noise2D depthNoise = new OpenSimplex2D(seed + 2L).octaves(6).scaled(10.0F, 60.0F).spread(0.02F);
      final Noise2D centerNoise = new OpenSimplex2D(seed + 3L).octaves(6).spread(0.01F).scaled(63.0F, 163.0F);
      return new BiomeNoiseSampler() {
         private float surfaceHeight;
         private float center;
         private float height;

         public void setColumn(int x, int z) {
            float h0 = Mth.clamp((0.5F - blobsNoise.noise((float)x, (float)z)) * 3.3333333F, 0.0F, 100.0F);
            float h1 = depthNoise.noise((float)x, (float)z);
            this.surfaceHeight = heightNoise.noise((float)x, (float)z);
            this.center = centerNoise.noise((float)x, (float)z);
            this.height = h0 * h1;
         }

         public double height() {
            return (double)this.surfaceHeight;
         }

         public double noise(int y) {
            float delta = Math.abs(this.center - (float)y);
            return (double)Mth.clamp(0.4F + 0.05F * (this.height - delta), 0.0F, 20.0F);
         }
      };
   }

   public static BiomeNoiseSampler undergroundCavesUpper1(long seed, final Noise2D heightNoise) {
      final Noise2D blobsNoise = new OpenSimplex2D(seed + 1L).octaves(4).spread(0.016F).abs();
      final Noise2D depthNoise = new OpenSimplex2D(seed + 2L).octaves(4).scaled(0.0F, 18.0F).spread(0.02F);
      final Noise2D centerNoise = new OpenSimplex2D(seed + 3L).octaves(2).spread(0.01F).scaled(13.0F, 43.0F);
      return new BiomeNoiseSampler() {
         private float surfaceHeight;
         private float center;
         private float height;

         public void setColumn(int x, int z) {
            float h0 = Mth.clamp((0.7F - blobsNoise.noise((float)x, (float)z)) * 3.3333333F, 0.0F, 1.0F);
            float h1 = depthNoise.noise((float)x, (float)z);
            this.surfaceHeight = heightNoise.noise((float)x, (float)z);
            this.center = centerNoise.noise((float)x, (float)z);
            this.height = h0 * h1;
         }

         public double height() {
            return (double)this.surfaceHeight;
         }

         public double noise(int y) {
            float delta = Math.abs(this.center - (float)y);
            return (double)Mth.clamp(0.4F + 0.05F * (this.height - delta), 0.0F, 1.0F);
         }
      };
   }

   public static BiomeNoiseSampler undergroundCavesUpper2(long seed, final Noise2D heightNoise) {
      final Noise2D blobsNoise = new OpenSimplex2D(seed + 2L).octaves(4).spread(0.016F).abs();
      final Noise2D depthNoise = new OpenSimplex2D(seed + 3L).octaves(4).scaled(0.0F, 18.0F).spread(0.02F);
      final Noise2D centerNoise = new OpenSimplex2D(seed + 4L).octaves(2).spread(0.01F).scaled(13.0F, 43.0F);
      return new BiomeNoiseSampler() {
         private float surfaceHeight;
         private float center;
         private float height;

         public void setColumn(int x, int z) {
            float h0 = Mth.clamp((0.7F - blobsNoise.noise((float)x, (float)z)) * 3.3333333F, 0.0F, 1.0F);
            float h1 = depthNoise.noise((float)x, (float)z);
            this.surfaceHeight = heightNoise.noise((float)x, (float)z);
            this.center = centerNoise.noise((float)x, (float)z);
            this.height = h0 * h1;
         }

         public double height() {
            return (double)this.surfaceHeight;
         }

         public double noise(int y) {
            float delta = Math.abs(this.center - (float)y);
            return (double)Mth.clamp(0.4F + 0.05F * (this.height - delta), 0.0F, 1.0F);
         }
      };
   }

   public static BiomeNoiseSampler undergroundCavesUpper3(long seed, final Noise2D heightNoise) {
      final Noise2D blobsNoise = new OpenSimplex2D(seed + 3L).octaves(4).spread(0.016F).abs();
      final Noise2D depthNoise = new OpenSimplex2D(seed + 4L).octaves(4).scaled(0.0F, 18.0F).spread(0.02F);
      final Noise2D centerNoise = new OpenSimplex2D(seed + 5L).octaves(2).spread(0.01F).scaled(13.0F, 43.0F);
      return new BiomeNoiseSampler() {
         private float surfaceHeight;
         private float center;
         private float height;

         public void setColumn(int x, int z) {
            float h0 = Mth.clamp((0.7F - blobsNoise.noise((float)x, (float)z)) * 3.3333333F, 0.0F, 1.0F);
            float h1 = depthNoise.noise((float)x, (float)z);
            this.surfaceHeight = heightNoise.noise((float)x, (float)z);
            this.center = centerNoise.noise((float)x, (float)z);
            this.height = h0 * h1;
         }

         public double height() {
            return (double)this.surfaceHeight;
         }

         public double noise(int y) {
            float delta = Math.abs(this.center - (float)y);
            return (double)Mth.clamp(0.4F + 0.05F * (this.height - delta), 0.0F, 1.0F);
         }
      };
   }

   public static BiomeNoiseSampler undergroundCaves(long seed, final Noise2D heightNoise) {
      final Noise2D blobsNoise = new OpenSimplex2D(seed + 4L).octaves(4).spread(0.016F).abs();
      final Noise2D depthNoise = new OpenSimplex2D(seed + 5L).octaves(4).scaled(2.0F, 18.0F).spread(0.02F);
      final Noise2D centerNoise = new OpenSimplex2D(seed + 6L).octaves(2).spread(0.01F).scaled(-30.0F, 10.0F);
      return new BiomeNoiseSampler() {
         private float surfaceHeight;
         private float center;
         private float height;

         public void setColumn(int x, int z) {
            float h0 = Mth.clamp((0.7F - blobsNoise.noise((float)x, (float)z)) * 3.3333333F, 0.0F, 1.0F);
            float h1 = depthNoise.noise((float)x, (float)z);
            this.surfaceHeight = heightNoise.noise((float)x, (float)z);
            this.center = centerNoise.noise((float)x, (float)z);
            this.height = h0 * h1;
         }

         public double height() {
            return (double)this.surfaceHeight;
         }

         public double noise(int y) {
            float delta = Math.abs(this.center - (float)y);
            return (double)Mth.clamp(0.4F + 0.05F * (this.height - delta), 0.0F, 1.0F);
         }
      };
   }

   public static BiomeNoiseSampler undergroundCavesMiddle1(long seed, final Noise2D heightNoise) {
      final Noise2D blobsNoise = new OpenSimplex2D(seed + 5L).octaves(4).spread(0.018F).abs();
      final Noise2D depthNoise = new OpenSimplex2D(seed + 6L).octaves(4).scaled(2.0F, 18.0F).spread(0.05F);
      final Noise2D centerNoise = new OpenSimplex2D(seed + 7L).octaves(3).spread(0.08F).scaled(-120.0F, -100.0F);
      return new BiomeNoiseSampler() {
         private float surfaceHeight;
         private float center;
         private float height;

         public void setColumn(int x, int z) {
            float h0 = Mth.clamp((0.7F - blobsNoise.noise((float)x, (float)z)) * 3.3333333F, 0.0F, 1.0F);
            float h1 = depthNoise.noise((float)x, (float)z);
            this.surfaceHeight = heightNoise.noise((float)x, (float)z);
            this.center = centerNoise.noise((float)x, (float)z);
            this.height = h0 * h1;
         }

         public double height() {
            return (double)this.surfaceHeight;
         }

         public double noise(int y) {
            float delta = Math.abs(this.center - (float)y);
            return (double)Mth.clamp(0.4F + 0.05F * (this.height - delta), 0.0F, 1.0F);
         }
      };
   }

   public static BiomeNoiseSampler undergroundCavesMiddle2(long seed, final Noise2D heightNoise) {
      final Noise2D blobsNoise = new OpenSimplex2D(seed + 6L).octaves(4).spread(0.018F).abs();
      final Noise2D depthNoise = new OpenSimplex2D(seed + 7L).octaves(4).scaled(2.0F, 18.0F).spread(0.05F);
      final Noise2D centerNoise = new OpenSimplex2D(seed + 8L).octaves(3).spread(0.08F).scaled(-120.0F, -100.0F);
      return new BiomeNoiseSampler() {
         private float surfaceHeight;
         private float center;
         private float height;

         public void setColumn(int x, int z) {
            float h0 = Mth.clamp((0.7F - blobsNoise.noise((float)x, (float)z)) * 3.3333333F, 0.0F, 1.0F);
            float h1 = depthNoise.noise((float)x, (float)z);
            this.surfaceHeight = heightNoise.noise((float)x, (float)z);
            this.center = centerNoise.noise((float)x, (float)z);
            this.height = h0 * h1;
         }

         public double height() {
            return (double)this.surfaceHeight;
         }

         public double noise(int y) {
            float delta = Math.abs(this.center - (float)y);
            return (double)Mth.clamp(0.4F + 0.05F * (this.height - delta), 0.0F, 1.0F);
         }
      };
   }

   public static BiomeNoiseSampler undergroundCavesDeep1(long seed, final Noise2D heightNoise) {
      final Noise2D blobsNoise = new OpenSimplex2D(seed + 7L).octaves(5).spread(0.018F).abs();
      final Noise2D depthNoise = new OpenSimplex2D(seed + 8L).octaves(5).scaled(2.0F, 26.0F).spread(0.2F);
      final Noise2D centerNoise = new OpenSimplex2D(seed + 9L).octaves(5).spread(0.25F).scaled(-210.0F, -190.0F);
      return new BiomeNoiseSampler() {
         private float surfaceHeight;
         private float center;
         private float height;

         public void setColumn(int x, int z) {
            float h0 = Mth.clamp((0.7F - blobsNoise.noise((float)x, (float)z)) * 3.3333333F, 0.0F, 1.0F);
            float h1 = depthNoise.noise((float)x, (float)z);
            this.surfaceHeight = heightNoise.noise((float)x, (float)z);
            this.center = centerNoise.noise((float)x, (float)z);
            this.height = h0 * h1;
         }

         public double height() {
            return (double)this.surfaceHeight;
         }

         public double noise(int y) {
            float delta = Math.abs(this.center - (float)y);
            return (double)Mth.clamp(0.4F + 0.05F * (this.height - delta), 0.0F, 1.0F);
         }
      };
   }

   public static BiomeNoiseSampler undergroundCavesDeep2(long seed, final Noise2D heightNoise) {
      final Noise2D blobsNoise = new OpenSimplex2D(seed + 8L).octaves(5).spread(0.018F).abs();
      final Noise2D depthNoise = new OpenSimplex2D(seed + 9L).octaves(5).scaled(2.0F, 26.0F).spread(0.2F);
      final Noise2D centerNoise = new OpenSimplex2D(seed + 10L).octaves(5).spread(0.25F).scaled(-210.0F, -190.0F);
      return new BiomeNoiseSampler() {
         private float surfaceHeight;
         private float center;
         private float height;

         public void setColumn(int x, int z) {
            float h0 = Mth.clamp((0.7F - blobsNoise.noise((float)x, (float)z)) * 3.3333333F, 0.0F, 1.0F);
            float h1 = depthNoise.noise((float)x, (float)z);
            this.surfaceHeight = heightNoise.noise((float)x, (float)z);
            this.center = centerNoise.noise((float)x, (float)z);
            this.height = h0 * h1;
         }

         public double height() {
            return (double)this.surfaceHeight;
         }

         public double noise(int y) {
            float delta = Math.abs(this.center - (float)y);
            return (double)Mth.clamp(0.4F + 0.05F * (this.height - delta), 0.0F, 1.0F);
         }
      };
   }

   public static BiomeNoiseSampler undergroundCavesDeep3(long seed, final Noise2D heightNoise) {
      final Noise2D blobsNoise = new OpenSimplex2D(seed + 9L).octaves(5).spread(0.018F).abs();
      final Noise2D depthNoise = new OpenSimplex2D(seed + 10L).octaves(5).scaled(2.0F, 26.0F).spread(0.2F);
      final Noise2D centerNoise = new OpenSimplex2D(seed + 11L).octaves(5).spread(0.25F).scaled(-210.0F, -190.0F);
      return new BiomeNoiseSampler() {
         private float surfaceHeight;
         private float center;
         private float height;

         public void setColumn(int x, int z) {
            float h0 = Mth.clamp((0.7F - blobsNoise.noise((float)x, (float)z)) * 3.3333333F, 0.0F, 1.0F);
            float h1 = depthNoise.noise((float)x, (float)z);
            this.surfaceHeight = heightNoise.noise((float)x, (float)z);
            this.center = centerNoise.noise((float)x, (float)z);
            this.height = h0 * h1;
         }

         public double height() {
            return (double)this.surfaceHeight;
         }

         public double noise(int y) {
            float delta = Math.abs(this.center - (float)y);
            return (double)Mth.clamp(0.4F + 0.05F * (this.height - delta), 0.0F, 1.0F);
         }
      };
   }
}
