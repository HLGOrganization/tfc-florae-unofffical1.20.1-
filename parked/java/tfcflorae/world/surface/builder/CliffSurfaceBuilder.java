package tfcflorae.world.surface.builder;

import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.common.blocks.rock.Rock.BlockType;
import net.dries007.tfc.world.noise.Noise2D;
import net.dries007.tfc.world.noise.OpenSimplex2D;
import net.dries007.tfc.world.surface.SurfaceBuilderContext;
import net.dries007.tfc.world.surface.SurfaceState;
import net.dries007.tfc.world.surface.SurfaceStates;
import net.dries007.tfc.world.surface.builder.NormalSurfaceBuilder;
import net.dries007.tfc.world.surface.builder.SurfaceBuilder;
import net.dries007.tfc.world.surface.builder.SurfaceBuilderFactory;
import tfcflorae.common.blocks.rock.TFCFRock;
import tfcflorae.world.surface.TFCFSoilSurfaceState;

public class CliffSurfaceBuilder implements SurfaceBuilder {
   private final SurfaceBuilder parent;
   private final Noise2D jitterNoise;

   public static SurfaceBuilderFactory create(SurfaceBuilderFactory parent) {
      return seed -> new CliffSurfaceBuilder(parent.apply(seed), seed);
   }

   public CliffSurfaceBuilder(SurfaceBuilder parent, long seed) {
      this.parent = parent;
      this.jitterNoise = new OpenSimplex2D(seed + 1234123L).octaves(4).scaled(-1.0F, 1.0F).spread(0.3F);
   }

   public void buildSurface(SurfaceBuilderContext context, int startY, int endY) {
      this.parent.buildSurface(context, startY, endY);
      this.buildCliffSurface(context, startY, endY);
   }

   private void buildCliffSurface(SurfaceBuilderContext context, int startY, int endY) {
      NormalSurfaceBuilder surfaceBuilder = NormalSurfaceBuilder.INSTANCE;
      double jitterNoise = (double)this.jitterNoise.noise((float)context.pos().getX(), (float)context.pos().getZ());
      SurfaceState rockType = TFCFSoilSurfaceState.rockCustom(Rock.CHERT, BlockType.RAW);
      if (jitterNoise > 0.85F) {
         rockType = TFCFSoilSurfaceState.rockCustom(Rock.CLAYSTONE, BlockType.RAW);
      } else if (jitterNoise > 0.7F && jitterNoise <= 0.85F) {
         rockType = TFCFSoilSurfaceState.rockCustom(Rock.LIMESTONE, BlockType.RAW);
      } else if (jitterNoise > 0.55F && jitterNoise <= 0.7F) {
         rockType = TFCFSoilSurfaceState.rockCustom(Rock.CHERT, BlockType.RAW);
      } else if (jitterNoise > 0.4F && jitterNoise <= 0.55F) {
         rockType = TFCFSoilSurfaceState.rockCustom(Rock.CHALK, BlockType.RAW);
      } else if (jitterNoise > 0.25 && jitterNoise <= 0.4F) {
         rockType = TFCFSoilSurfaceState.rockCustom(Rock.MARBLE, BlockType.RAW);
      } else if (jitterNoise > 0.1F && jitterNoise <= 0.25) {
         rockType = TFCFSoilSurfaceState.rockCustom(TFCFRock.CATLINITE, BlockType.RAW);
      } else if (jitterNoise > -0.05F && jitterNoise <= 0.1F) {
         rockType = TFCFSoilSurfaceState.rockCustom(TFCFRock.MUDSTONE, BlockType.RAW);
      } else if (jitterNoise > -0.2F && jitterNoise <= -0.05F) {
         rockType = TFCFSoilSurfaceState.rockCustom(TFCFRock.SANDSTONE, BlockType.RAW);
      } else if (jitterNoise > -0.35F && jitterNoise <= -0.2F) {
         rockType = TFCFSoilSurfaceState.rockCustom(TFCFRock.SILTSTONE, BlockType.RAW);
      } else if (jitterNoise > -0.5 && jitterNoise <= -0.35F) {
         rockType = TFCFSoilSurfaceState.rockCustom(TFCFRock.SOAPSTONE, BlockType.RAW);
      } else if (jitterNoise > -0.65F && jitterNoise <= -0.5) {
         rockType = TFCFSoilSurfaceState.rockCustom(TFCFRock.TRAVERTINE, BlockType.RAW);
      } else if (jitterNoise > -0.8F && jitterNoise <= -0.65F) {
         rockType = TFCFSoilSurfaceState.rockCustom(Rock.GRANITE, BlockType.RAW);
      } else {
         rockType = TFCFSoilSurfaceState.rockCustom(Rock.CHALK, BlockType.RAW);
      }

      surfaceBuilder.buildSurface(context, startY, endY, rockType, rockType, rockType);
      context.setBlockState(startY, SurfaceStates.GRASS);
   }
}
