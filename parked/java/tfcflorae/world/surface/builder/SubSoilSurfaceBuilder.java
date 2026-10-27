package tfcflorae.world.surface.builder;

import java.util.Map;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.rock.Rock.BlockType;
import net.dries007.tfc.common.blocks.soil.SoilBlockType;
import net.dries007.tfc.world.biome.BiomeExtension;
import net.dries007.tfc.world.biome.TFCBiomes;
import net.dries007.tfc.world.noise.Noise2D;
import net.dries007.tfc.world.noise.OpenSimplex2D;
import net.dries007.tfc.world.surface.SurfaceBuilderContext;
import net.dries007.tfc.world.surface.SurfaceState;
import net.dries007.tfc.world.surface.builder.SurfaceBuilder;
import net.dries007.tfc.world.surface.builder.SurfaceBuilderFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;
import tfcflorae.common.blocks.soil.TFCFRockSand;
import tfcflorae.common.blocks.soil.TFCFRockSoil;
import tfcflorae.common.blocks.soil.TFCFSoil;
import tfcflorae.world.surface.TFCFSoilSurfaceState;

public class SubSoilSurfaceBuilder implements SurfaceBuilder {
   public static final int SEA_LEVEL_Y = 63;
   private final Noise2D lowlands;
   private final Noise2D lake;
   private final Noise2D river;
   private final Noise2D lowCanyons;
   private final SurfaceBuilder parent;

   public static SurfaceBuilderFactory create(SurfaceBuilderFactory parent) {
      return seed -> new SubSoilSurfaceBuilder(parent.apply(seed), seed);
   }

   public SubSoilSurfaceBuilder(SurfaceBuilder parent, long seed) {
      this.parent = parent;
      this.lowlands = new OpenSimplex2D(seed).octaves(6).spread(0.55F).scaled(58.0F, 65.0F).clamped(61.0F, 65.0F);
      this.lake = new OpenSimplex2D(seed).octaves(4).spread(0.15F).scaled(51.0F, 61.0F);
      this.river = new OpenSimplex2D(seed).octaves(4).spread(0.2F).scaled(52.0F, 58.0F);
      this.lowCanyons = new OpenSimplex2D(seed).octaves(4).spread(0.03F).scaled(-100.0F, 100.0F);
   }

   public void buildSurface(SurfaceBuilderContext context, int startY, int endY) {
      this.parent.buildSurface(context, startY, endY);
      float noiseRainfall = context.rainfall() + 10.0F * (float)context.random().nextGaussian();
      if (noiseRainfall >= 80.0F) {
         this.buildSubSoil(context, startY, endY);
      } else {
         this.buildSubSand(context, startY, endY);
      }
   }

   private void buildSubSoil(SurfaceBuilderContext context, int startY, int endY) {
      BlockPos pos = new BlockPos(context.pos().getX(), startY - 1, context.pos().getZ());
      SurfaceState PEBBLE_COMPACT_DIRT = TFCFSoilSurfaceState.buildTypeRock(TFCFRockSoil.PEBBLE_COMPACT_DIRT);
      SurfaceState ROCKY_COMPACT_DIRT = TFCFSoilSurfaceState.buildTypeRock(TFCFRockSoil.ROCKY_COMPACT_DIRT);
      SurfaceState ROCKIER_COMPACT_DIRT = TFCFSoilSurfaceState.buildTypeRock(TFCFRockSoil.ROCKIER_COMPACT_DIRT);
      SurfaceState ROCKIEST_COMPACT_DIRT = TFCFSoilSurfaceState.buildTypeRock(TFCFRockSoil.ROCKIEST_COMPACT_DIRT);
      SurfaceState COMPACT_DIRT = TFCFSoilSurfaceState.buildType(TFCFSoil.COMPACT_DIRT);
      BlockState localGravelType = TFCFSoilSurfaceState.rock(BlockType.GRAVEL).getState(context);
      BlockState localDirtSoilVariant = ((Block)((RegistryObject)((Map)TFCBlocks.SOIL.get(SoilBlockType.DIRT))
               .get(TFCFSoilSurfaceState.currentSoilVariant(context)))
            .get())
         .defaultBlockState();
      int grassY = startY - 1;
      int gravelY = -1;
      int transitionHeight = -1;

      for (int y = startY - 2; y >= endY; y--) {
         BlockState stateAt = context.getBlockState(y);
         if (y < startY - 2 && y > endY + 2 && !stateAt.isAir() && (stateAt == localGravelType || stateAt == localDirtSoilVariant)) {
            gravelY = y - 1;
            transitionHeight = gravelY + 4;
            break;
         }
      }

      if (gravelY != -1) {
         if (transitionHeight >= grassY - 1) {
            transitionHeight = grassY - 1;
         }

         for (int yx = gravelY; yx < transitionHeight; yx++) {
            double randomGauss = context.random().nextGaussian();
            if (pos.getY() > context.getSeaLevel()) {
               if (yx == gravelY) {
                  if (randomGauss >= -0.3F) {
                     if (randomGauss >= 0.8F) {
                        context.setBlockState(yx, ROCKY_COMPACT_DIRT.getState(context));
                     } else if (randomGauss >= 0.45F && randomGauss < 0.8F) {
                        context.setBlockState(yx, ROCKIER_COMPACT_DIRT.getState(context));
                     } else {
                        context.setBlockState(yx, ROCKIEST_COMPACT_DIRT.getState(context));
                     }
                  }
               } else if (yx == gravelY + 1) {
                  if (randomGauss >= 0.0) {
                     if (randomGauss >= 0.7F) {
                        context.setBlockState(yx, ROCKY_COMPACT_DIRT.getState(context));
                     } else if (randomGauss >= 0.45F && randomGauss < 0.7F) {
                        context.setBlockState(yx, ROCKIER_COMPACT_DIRT.getState(context));
                     } else {
                        context.setBlockState(yx, ROCKIEST_COMPACT_DIRT.getState(context));
                     }
                  }
               } else if (yx == gravelY + 2) {
                  if (randomGauss >= 0.1F) {
                     if (randomGauss >= 0.7F) {
                        context.setBlockState(yx, PEBBLE_COMPACT_DIRT.getState(context));
                     } else if (randomGauss >= 0.4F && randomGauss < 0.7F) {
                        context.setBlockState(yx, ROCKY_COMPACT_DIRT.getState(context));
                     } else {
                        context.setBlockState(yx, ROCKIER_COMPACT_DIRT.getState(context));
                     }
                  }
               } else if (yx == gravelY + 3) {
                  if (randomGauss >= 0.2F) {
                     if (randomGauss >= 0.6F) {
                        context.setBlockState(yx, COMPACT_DIRT.getState(context));
                     } else {
                        context.setBlockState(yx, PEBBLE_COMPACT_DIRT.getState(context));
                     }
                  }
               } else if (yx > gravelY + 3 && 0.3F >= randomGauss) {
                  context.setBlockState(yx, COMPACT_DIRT.getState(context));
               }
            }
         }
      }
   }

   private void buildSubSand(SurfaceBuilderContext context, int startY, int endY) {
      BlockPos pos = new BlockPos(context.pos().getX(), startY - 1, context.pos().getZ());
      SurfaceState PEBBLE = TFCFSoilSurfaceState.buildTypeRockSand(TFCFRockSand.PEBBLE);
      SurfaceState ROCKY = TFCFSoilSurfaceState.buildTypeRockSand(TFCFRockSand.ROCKY);
      SurfaceState ROCKIER = TFCFSoilSurfaceState.buildTypeRockSand(TFCFRockSand.ROCKIER);
      SurfaceState ROCKIEST = TFCFSoilSurfaceState.buildTypeRockSand(TFCFRockSand.ROCKIEST);
      SurfaceState COMPACT_DIRT = TFCFSoilSurfaceState.buildType(TFCFSoil.COMPACT_DIRT);
      BlockState localGravelType = TFCFSoilSurfaceState.rock(BlockType.GRAVEL).getState(context);
      BlockState localDirtSoilVariant = ((Block)((RegistryObject)((Map)TFCBlocks.SOIL.get(SoilBlockType.DIRT))
               .get(TFCFSoilSurfaceState.currentSoilVariant(context)))
            .get())
         .defaultBlockState();
      int grassY = startY - 1;
      int gravelY = -1;
      int transitionHeight = -1;

      for (int y = startY - 2; y >= endY; y--) {
         BlockState stateAt = context.getBlockState(y);
         if (y < startY - 2 && y > endY + 2 && !stateAt.isAir() && (stateAt == localGravelType || stateAt == localDirtSoilVariant)) {
            gravelY = y - 1;
            transitionHeight = gravelY + 4;
            break;
         }
      }

      if (gravelY != -1) {
         if (transitionHeight >= grassY - 1) {
            transitionHeight = grassY - 1;
         }

         for (int yx = gravelY; yx < transitionHeight; yx++) {
            double randomGauss = context.random().nextGaussian();
            if (pos.getY() > context.getSeaLevel()) {
               if (yx == gravelY) {
                  if (randomGauss >= -0.3F) {
                     if (randomGauss >= 0.8F) {
                        context.setBlockState(yx, ROCKY.getState(context));
                     } else if (randomGauss >= 0.45F && randomGauss < 0.8F) {
                        context.setBlockState(yx, ROCKIER.getState(context));
                     } else {
                        context.setBlockState(yx, ROCKIEST.getState(context));
                     }
                  }
               } else if (yx == gravelY + 1) {
                  if (randomGauss >= 0.0) {
                     if (randomGauss >= 0.7F) {
                        context.setBlockState(yx, ROCKY.getState(context));
                     } else if (randomGauss >= 0.45F && randomGauss < 0.7F) {
                        context.setBlockState(yx, ROCKIER.getState(context));
                     } else {
                        context.setBlockState(yx, ROCKIEST.getState(context));
                     }
                  }
               } else if (yx == gravelY + 2) {
                  if (randomGauss >= 0.1F) {
                     if (randomGauss >= 0.7F) {
                        context.setBlockState(yx, PEBBLE.getState(context));
                     } else if (randomGauss >= 0.4F && randomGauss < 0.7F) {
                        context.setBlockState(yx, ROCKY.getState(context));
                     } else {
                        context.setBlockState(yx, ROCKIER.getState(context));
                     }
                  }
               } else if (yx == gravelY + 3) {
                  if (randomGauss >= 0.2F) {
                     if (randomGauss >= 0.6F) {
                        context.setBlockState(yx, COMPACT_DIRT.getState(context));
                     } else {
                        context.setBlockState(yx, PEBBLE.getState(context));
                     }
                  }
               } else if (yx > gravelY + 3 && 0.3F >= randomGauss) {
                  context.setBlockState(yx, COMPACT_DIRT.getState(context));
               }
            }
         }
      }
   }

   public static boolean isLake(BiomeExtension biome) {
      return biome == TFCBiomes.LAKE
         || biome == TFCBiomes.OCEANIC_MOUNTAIN_LAKE
         || biome == TFCBiomes.OLD_MOUNTAIN_LAKE
         || biome == TFCBiomes.MOUNTAIN_LAKE
         || biome == TFCBiomes.VOLCANIC_OCEANIC_MOUNTAIN_LAKE
         || biome == TFCBiomes.VOLCANIC_MOUNTAIN_LAKE
         || biome == TFCBiomes.PLATEAU_LAKE;
   }

   public static boolean isRiver(BiomeExtension biome) {
      return biome == TFCBiomes.RIVER
         || biome == TFCBiomes.OCEANIC_MOUNTAIN_RIVER
         || biome == TFCBiomes.OLD_MOUNTAIN_RIVER
         || biome == TFCBiomes.MOUNTAIN_RIVER
         || biome == TFCBiomes.VOLCANIC_OCEANIC_MOUNTAIN_RIVER
         || biome == TFCBiomes.VOLCANIC_MOUNTAIN_RIVER;
   }

   public static boolean isLow(BiomeExtension biome) {
      return biome == TFCBiomes.PLAINS || biome == TFCBiomes.HILLS || biome == TFCBiomes.LOWLANDS;
   }

   public static boolean isCanyon(BiomeExtension biome) {
      return biome == TFCBiomes.LOW_CANYONS || biome == TFCBiomes.CANYONS;
   }
}
