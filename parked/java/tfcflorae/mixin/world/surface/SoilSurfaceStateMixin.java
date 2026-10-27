package tfcflorae.mixin.world.surface;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.soil.SandBlockType;
import net.dries007.tfc.common.blocks.soil.SoilBlockType;
import net.dries007.tfc.common.blocks.soil.SoilBlockType.Variant;
import net.dries007.tfc.world.noise.Noise2D;
import net.dries007.tfc.world.noise.OpenSimplex2D;
import net.dries007.tfc.world.surface.SoilSurfaceState;
import net.dries007.tfc.world.surface.SurfaceBuilderContext;
import net.dries007.tfc.world.surface.SurfaceState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import tfcflorae.Config;
import tfcflorae.common.blocks.TFCFBlocks;
import tfcflorae.common.blocks.soil.TFCFSoil;
import tfcflorae.util.TFCFHelpers;

@Mixin({SoilSurfaceState.class})
public class SoilSurfaceStateMixin implements SurfaceState {
   @Shadow
   public static final Noise2D PATCH_NOISE = new OpenSimplex2D(18273952837592L).octaves(2).spread(0.04F);
   @Shadow
   private final List<SurfaceState> regions;

   @Overwrite(
      remap = false
   )
   public static SurfaceState buildType(SoilBlockType type) {
      ImmutableList<SurfaceState> regions = ImmutableList.of(
         sand(),
         transitionSoil(sand(), soil(type, Variant.SANDY_LOAM), Variant.SANDY_LOAM),
         soil(type, Variant.SANDY_LOAM),
         transitionSoil(soil(type, Variant.SANDY_LOAM), soil(type, Variant.LOAM), Variant.LOAM),
         soil(type, Variant.LOAM),
         transitionSoil(soil(type, Variant.LOAM), soil(type, Variant.SILTY_LOAM), Variant.SILTY_LOAM),
         soil(type, Variant.SILTY_LOAM),
         transitionSoil(soil(type, Variant.SILTY_LOAM), soil(type, Variant.SILT), Variant.SILT),
         soil(type, Variant.SILT)
      );
      return (SurfaceState)(type == SoilBlockType.GRASS ? new SoilSurfaceStateMixin.NeedsPostProcessing(regions) : new SoilSurfaceStateMixin(regions));
   }

   @Unique
   private static SurfaceState transitionSoil(SurfaceState first, SurfaceState second, Variant variant) {
      return context -> {
         BlockPos pos = context.pos();
         RandomSource random = context.random();
         float rainfall = context.rainfall();
         float noise = PATCH_NOISE.noise((float)pos.getX(), (float)pos.getZ());
         float noiseGauss = noise + 2.0F * (float)random.nextGaussian();
         float noiseRainfall = rainfall + 10.0F * (float)random.nextGaussian();
         if (first != buildType(SoilBlockType.DIRT)) {
            if (noiseRainfall <= 80.0F) {
               if (noiseRainfall <= 50.0F) {
                  return context.getRock().sand().defaultBlockState();
               }

               if (noiseGauss > 0.2F) {
                  return context.getRock().sand().defaultBlockState();
               }

               SandBlockType sandColor = context.level() instanceof WorldGenLevel level
                  ? TFCFHelpers.getSandColor(level, pos, (Boolean)Config.COMMON.toggleCheapSandColourCalculations.get())
                  : SandBlockType.YELLOW;
               float randomRainfall = (float)random.nextInt(Math.round(rainfall)) * 0.01F;
               if (randomRainfall <= Mth.abs(noiseGauss)) {
                  return ((Block)TFCFBlocks.SPARSE_SAND_GRASS.get(sandColor).get()).defaultBlockState();
               }

               return ((Block)TFCFBlocks.DENSE_SAND_GRASS.get(sandColor).get()).defaultBlockState();
            }

            if (noiseRainfall <= 110.0F) {
               if (noiseGauss > 0.0F) {
                  return ((Block)TFCFBlocks.TFCSOIL.get(TFCFSoil.DENSE_GRASS).get(variant).get()).defaultBlockState();
               }

               return ((Block)TFCFBlocks.TFCSOIL.get(TFCFSoil.SPARSE_GRASS).get(variant).get()).defaultBlockState();
            }
         }

         return noiseGauss > 0.0F ? first.getState(context) : second.getState(context);
      };
   }

   @Shadow
   private static SurfaceState sand() {
      return context -> context.getRock().sand().defaultBlockState();
   }

   @Shadow
   private static SurfaceState sandstone() {
      return context -> context.getRock().sandstone().defaultBlockState();
   }

   @Shadow
   private static SurfaceState gravel() {
      return context -> context.getRock().gravel().defaultBlockState();
   }

   @Shadow
   private static SurfaceState soil(SoilBlockType type, Variant variant) {
      Supplier<Block> block = (Supplier<Block>)((Map)TFCBlocks.SOIL.get(type)).get(variant);
      return context -> block.get().defaultBlockState();
   }

   private SoilSurfaceStateMixin(List<SurfaceState> regions) {
      this.regions = regions;
   }

   @Shadow
   public BlockState getState(SurfaceBuilderContext context) {
      float rainfall = context.rainfall() + 20.0F;
      int index = Mth.clamp((int)Mth.clampedMap(rainfall, 0.0F, 500.0F, 0.0F, (float)this.regions.size() - 0.01F), 0, this.regions.size() - 1);
      return this.regions.get(index).getState(context);
   }

   static class NeedsPostProcessing extends SoilSurfaceStateMixin {
      private NeedsPostProcessing(List<SurfaceState> regions) {
         super(regions);
      }

      public void setState(SurfaceBuilderContext context) {
         context.chunk().setBlockState(context.pos(), this.getState(context), false);
         context.chunk().markPosForPostprocessing(context.pos());
      }
   }
}
