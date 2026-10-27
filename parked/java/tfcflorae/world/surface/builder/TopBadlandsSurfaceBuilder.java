package tfcflorae.world.surface.builder;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import net.minecraft.util.RandomSource;
import java.util.function.Function;
import net.dries007.tfc.common.blocks.SandstoneBlockType;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.soil.SandBlockType;
import net.dries007.tfc.world.noise.Noise2D;
import net.dries007.tfc.world.noise.OpenSimplex2D;
import net.dries007.tfc.world.surface.SurfaceBuilderContext;
import net.dries007.tfc.world.surface.SurfaceStates;
import net.dries007.tfc.world.surface.builder.NormalSurfaceBuilder;
import net.dries007.tfc.world.surface.builder.SurfaceBuilder;
import net.dries007.tfc.world.surface.builder.SurfaceBuilderFactory;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;

public class TopBadlandsSurfaceBuilder implements SurfaceBuilder {
   private static SurfaceBuilder parent;
   public static final SurfaceBuilderFactory NORMAL = seed -> new TopBadlandsSurfaceBuilder(parent, false, seed);
   public static final SurfaceBuilderFactory INVERTED = seed -> new TopBadlandsSurfaceBuilder(parent, true, seed);
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

   public static SurfaceBuilderFactory create(SurfaceBuilderFactory parent, boolean inverted) {
      return seed -> new TopBadlandsSurfaceBuilder(parent.apply(seed), inverted, seed);
   }

   private static void fill(
      RandomSource random, BlockState[] sandLayers, BlockState[] sandstoneLayers, SandBlockType primary, SandBlockType secondary, SandBlockType uncommon
   ) {
      fill(random, sandLayers, primary, secondary, uncommon, TopBadlandsSurfaceBuilder::sand);
      fill(random, sandstoneLayers, primary, secondary, uncommon, TopBadlandsSurfaceBuilder::sandstone);
   }

   private static void fill(
      RandomSource random, BlockState[] layers, SandBlockType primary, SandBlockType secondary, SandBlockType uncommon, Function<SandBlockType, BlockState> block
   ) {
      Arrays.fill(layers, 0, 8, block.apply(primary));
      Arrays.fill(layers, 8, 13, block.apply(secondary));
      Arrays.fill(layers, 13, 16, block.apply(uncommon));
      Collections.shuffle(Arrays.asList(layers), random);
   }

   private static BlockState sand(SandBlockType color) {
      return ((Block)((RegistryObject)TFCBlocks.SAND.get(color)).get()).defaultBlockState();
   }

   private static BlockState sandstone(SandBlockType color) {
      return ((Block)((RegistryObject)((Map)TFCBlocks.SANDSTONE.get(color)).get(SandstoneBlockType.RAW)).get()).defaultBlockState();
   }

   public TopBadlandsSurfaceBuilder(SurfaceBuilder parent, boolean inverted, long seed) {
      TopBadlandsSurfaceBuilder.parent = parent;
      this.inverted = inverted;
      RandomSource random = RandomSource.create(seed);
      this.sandLayers0 = new BlockState[16];
      this.sandLayers1 = new BlockState[16];
      this.sandstoneLayers0 = new BlockState[16];
      this.sandstoneLayers1 = new BlockState[16];
      this.layerThresholds = new float[16];
      fill(random, this.sandLayers0, this.sandstoneLayers0, SandBlockType.RED, SandBlockType.BROWN, SandBlockType.YELLOW);
      fill(random, this.sandLayers1, this.sandstoneLayers1, SandBlockType.BROWN, SandBlockType.YELLOW, SandBlockType.WHITE);

      for (int i = 0; i < 16; i++) {
         this.layerThresholds[i] = random.nextFloat();
      }

      this.grassHeightVariationNoise = new OpenSimplex2D(random.nextLong()).octaves(2).scaled(77.0F, 81.0F).spread(0.5F);
      this.sandHeightOffsetNoise = new OpenSimplex2D(random.nextLong()).octaves(2).scaled(0.0F, 6.0F).spread(0.0014F);
      this.sandStyleNoise = new OpenSimplex2D(random.nextLong()).octaves(2).scaled(-0.3F, 1.3F).spread(3.0E-4F);
   }

   public void buildSurface(SurfaceBuilderContext context, int startY, int endY) {
      parent.buildSurface(context, startY, endY);
      float heightVariation = (float)(this.grassHeightVariationNoise.noise((float)context.pos().getX(), (float)context.pos().getZ()));
      float weightVariation = (float)(1.0 - context.weight()) * 23.0F;
      if (this.inverted ? !((float)(startY + 5) < heightVariation + weightVariation) : !((float)(startY - 5) > heightVariation - weightVariation)) {
         this.buildSandySurface(context, startY, endY);
      } else {
         NormalSurfaceBuilder.INSTANCE.buildSurface(context, startY, endY, SurfaceStates.GRASS, SurfaceStates.DIRT, SurfaceStates.SANDSTONE_OR_GRAVEL);
      }
   }

   private void buildSandySurface(SurfaceBuilderContext context, int startHeight, int minSurfaceHeight) {
      float style = (float)(this.sandStyleNoise.noise((float)context.pos().getX(), (float)context.pos().getZ()));
      int height = (int)this.sandHeightOffsetNoise.noise((float)context.pos().getX(), (float)context.pos().getZ());
      int surfaceDepth = -1;

      for (int y = startHeight; y >= minSurfaceHeight; y--) {
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
