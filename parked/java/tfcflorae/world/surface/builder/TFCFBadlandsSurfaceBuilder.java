package tfcflorae.world.surface.builder;

import java.util.Arrays;
import java.util.Collections;
import net.minecraft.util.RandomSource;
import java.util.function.Function;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.world.noise.Noise2D;
import net.dries007.tfc.world.noise.OpenSimplex2D;
import net.dries007.tfc.world.surface.SurfaceBuilderContext;
import net.dries007.tfc.world.surface.SurfaceState;
import net.dries007.tfc.world.surface.SurfaceStates;
import net.dries007.tfc.world.surface.builder.NormalSurfaceBuilder;
import net.dries007.tfc.world.surface.builder.SurfaceBuilder;
import net.dries007.tfc.world.surface.builder.SurfaceBuilderFactory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;
import tfcflorae.common.blocks.TFCFBlocks;
import tfcflorae.common.blocks.soil.Colors;
import tfcflorae.common.blocks.soil.TFCFSandstoneBlockType;
import tfcflorae.common.blocks.soil.TFCFSoil;
import tfcflorae.world.surface.TFCFSoilSurfaceState;

public class TFCFBadlandsSurfaceBuilder implements SurfaceBuilder {
   public static final SurfaceBuilderFactory NORMAL = seed -> new TFCFBadlandsSurfaceBuilder(false, seed);
   public static final SurfaceBuilderFactory INVERTED = seed -> new TFCFBadlandsSurfaceBuilder(true, seed);
   private static final int PRIMARY_SIZE = 8;
   private static final int SECONDARY_SIZE = 5;
   private static final int UNCOMMON_SIZE = 3;
   private static final int LAYER_SIZE = 16;
   private final boolean inverted;
   private final BlockState[] sandLayers0;
   private final BlockState[] sandLayers1;
   private final BlockState[] sandstoneLayers0;
   private final BlockState[] sandstoneLayers1;
   private final float[] layerThresholds;
   private final Noise2D grassHeightVariationNoise;
   private final Noise2D sandHeightOffsetNoise;
   private final Noise2D sandStyleNoise;
   private final Noise2D surfaceMaterialNoise;
   private final Noise2D variantNoise;

   private static void fill(RandomSource random, BlockState[] sandLayers, BlockState[] sandstoneLayers, Colors primary, Colors secondary, Colors uncommon) {
      fill(random, sandLayers, primary, secondary, uncommon, TFCFBadlandsSurfaceBuilder::sand);
      fill(random, sandstoneLayers, primary, secondary, uncommon, TFCFBadlandsSurfaceBuilder::sandstone);
   }

   private static void fill(RandomSource random, BlockState[] layers, Colors primary, Colors secondary, Colors uncommon, Function<Colors, BlockState> block) {
      Arrays.fill(layers, 0, 8, block.apply(primary));
      Arrays.fill(layers, 8, 13, block.apply(secondary));
      Arrays.fill(layers, 13, 16, block.apply(uncommon));
      Collections.shuffle(Arrays.asList(layers), random);
   }

   private static BlockState sand(Colors color) {
      return color.hasSandTFC()
         ? ((Block)((RegistryObject)TFCBlocks.SAND.get(color.toSandTFC(true))).get()).defaultBlockState()
         : ((Block)TFCFBlocks.SAND.get(color).get()).defaultBlockState();
   }

   private static BlockState sandstone(Colors color) {
      return ((Block)TFCFBlocks.SANDSTONE.get(color.hasLayered() ? color : Colors.ORANGE).get(TFCFSandstoneBlockType.LAYERED).get()).defaultBlockState();
   }

   public TFCFBadlandsSurfaceBuilder(boolean inverted, long seed) {
      this.inverted = inverted;
      RandomSource random = RandomSource.create(seed);
      this.sandLayers0 = new BlockState[16];
      this.sandLayers1 = new BlockState[16];
      this.sandstoneLayers0 = new BlockState[16];
      this.sandstoneLayers1 = new BlockState[16];
      this.layerThresholds = new float[16];

      for (int i = 0; i < 16; i++) {
         this.layerThresholds[i] = random.nextFloat();
      }

      this.grassHeightVariationNoise = new OpenSimplex2D(random.nextLong()).octaves(2).scaled(77.0F, 81.0F).spread(0.5F);
      this.sandHeightOffsetNoise = new OpenSimplex2D(random.nextLong()).octaves(2).scaled(0.0F, 6.0F).spread(0.0014F);
      this.sandStyleNoise = new OpenSimplex2D(random.nextLong()).octaves(2).scaled(-0.3F, 1.3F).spread(3.0E-4F);
      this.surfaceMaterialNoise = new OpenSimplex2D(seed + 3L).octaves(2).spread(0.04F);
      this.variantNoise = new OpenSimplex2D(seed).octaves(2).spread(0.003F).abs();
   }

   public void buildSurface(SurfaceBuilderContext context, int startY, int endY) {
      RandomSource random = RandomSource.create(context.getSeed());
      float noiseGrass = this.surfaceMaterialNoise.noise((float)context.pos().getX(), (float)context.pos().getZ()) * 0.9F
         + context.random().nextFloat() * 0.1F;
      SurfaceState SPARSE_GRASS = TFCFSoilSurfaceState.buildType(TFCFSoil.SPARSE_GRASS);
      SurfaceState DENSE_GRASS = TFCFSoilSurfaceState.buildType(TFCFSoil.DENSE_GRASS);
      SurfaceState COMPACT_DIRT = TFCFSoilSurfaceState.buildType(TFCFSoil.COMPACT_DIRT);
      SurfaceState grass = SurfaceStates.GRASS;
      double randomGauss = Math.abs(context.random().nextGaussian()) * 0.1F;
      int randomChance = context.random().nextInt(7);
      if ((double)noiseGrass >= -0.3F + randomGauss && (double)noiseGrass < -0.1F + randomGauss) {
         if (randomChance > 3) {
            grass = SPARSE_GRASS;
         } else {
            grass = COMPACT_DIRT;
         }
      } else if ((double)noiseGrass >= -0.1F + randomGauss) {
         grass = DENSE_GRASS;
      }

      float variantNoiseValue = (float)(this.variantNoise.noise((float)context.pos().getX(), (float)context.pos().getZ()));
      if (variantNoiseValue > 0.0F) {
         if (context.rainfall() > 300.0F && context.averageTemperature() > 15.0F) {
            fill(random, this.sandLayers0, this.sandstoneLayers0, Colors.GREEN, Colors.WHITE, Colors.YELLOW);
            fill(random, this.sandLayers1, this.sandstoneLayers1, Colors.GREEN, Colors.YELLOW, Colors.LIGHT_GREEN);
         } else if (context.rainfall() > 200.0F && context.getChunkData().getForestDensity() < 0.5F) {
            fill(random, this.sandLayers0, this.sandstoneLayers0, Colors.RED, Colors.YELLOW, Colors.WHITE);
            fill(random, this.sandLayers1, this.sandstoneLayers1, Colors.RED, Colors.ORANGE, Colors.YELLOW);
         } else if (context.rainfall() > 100.0F && context.getChunkData().getForestDensity() > 0.5F && context.averageTemperature() < 15.0F) {
            fill(random, this.sandLayers0, this.sandstoneLayers0, Colors.BLACK, Colors.WHITE, Colors.RED);
            fill(random, this.sandLayers1, this.sandstoneLayers1, Colors.GRAY, Colors.BLACK, Colors.WHITE);
         } else {
            fill(random, this.sandLayers0, this.sandstoneLayers0, Colors.PINK, Colors.RED, Colors.WHITE);
            fill(random, this.sandLayers1, this.sandstoneLayers1, Colors.PURPLE, Colors.PINK, Colors.BLUE);
         }
      } else {
         fill(random, this.sandLayers0, this.sandstoneLayers0, Colors.RED, Colors.BROWN, Colors.YELLOW);
         fill(random, this.sandLayers1, this.sandstoneLayers1, Colors.BROWN, Colors.YELLOW, Colors.WHITE);
      }

      float heightVariation = (float)(this.grassHeightVariationNoise.noise((float)context.pos().getX(), (float)context.pos().getZ()));
      float weightVariation = (float)(1.0 - context.weight()) * 23.0F;
      if (this.inverted ? !((float)(startY + 5) < heightVariation + weightVariation) : !((float)(startY - 5) > heightVariation - weightVariation)) {
         if ((double)startY < 81.0 + context.random().nextGaussian() * 4.0) {
            NormalSurfaceBuilder.INSTANCE.buildSurface(context, startY, endY, grass, SurfaceStates.DIRT, SurfaceStates.SANDSTONE_OR_GRAVEL);
         } else {
            this.buildSandySurface(context, startY, endY);
         }
      } else {
         NormalSurfaceBuilder.INSTANCE.buildSurface(context, startY, endY, grass, SurfaceStates.DIRT, SurfaceStates.SANDSTONE_OR_GRAVEL);
      }
   }

   private void buildSandySurface(SurfaceBuilderContext context, int startHeight, int minSurfaceHeight) {
      float style = (float)(this.sandStyleNoise.noise((float)context.pos().getX(), (float)context.pos().getZ()));
      int height = (int)this.sandHeightOffsetNoise.noise((float)context.pos().getX(), (float)context.pos().getZ());
      int surfaceDepth = -1;
      int randomDepth = (int)context.random().nextGaussian() * 4;

      for (int y = startHeight; y >= minSurfaceHeight - 15 + randomDepth; y--) {
         BlockState stateAt = context.getBlockState(y);
         if (stateAt.isAir()) {
            surfaceDepth = -1;
         } else if (context.isDefaultBlock(stateAt)) {
            if (surfaceDepth == -1) {
               if (y < context.getSeaLevel() - 1) {
                  context.setBlockState(y, SurfaceStates.SAND_OR_GRAVEL);
               } else {
                  context.setBlockState(y, this.sampleLayer(this.sandLayers0, this.sandLayers1, y + height, style));
                  surfaceDepth = this.inverted ? 9 : 3;
               }
            } else if (surfaceDepth > 0) {
               surfaceDepth--;
               context.setBlockState(y, this.sampleLayer(this.sandstoneLayers0, this.sandstoneLayers1, y + height, style));
            }
         }
      }
   }

   private BlockState sampleLayer(BlockState[] layers0, BlockState[] layers1, int y, float threshold) {
      int index = Math.floorMod(y, 16);
      return (this.layerThresholds[index] < threshold ? layers0 : layers1)[index];
   }
}
