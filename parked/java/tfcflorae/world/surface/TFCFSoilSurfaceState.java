package tfcflorae.world.surface;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;
import net.minecraft.util.RandomSource;
import java.util.function.Supplier;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.common.blocks.rock.Rock.BlockType;
import net.dries007.tfc.common.blocks.soil.SoilBlockType;
import net.dries007.tfc.common.blocks.soil.SoilBlockType.Variant;
import net.dries007.tfc.util.registry.RegistryRock;
import net.dries007.tfc.util.registry.RegistrySoilVariant;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
import net.dries007.tfc.world.settings.RockSettings;
import net.dries007.tfc.world.surface.SoilSurfaceState;
import net.dries007.tfc.world.surface.SurfaceBuilderContext;
import net.dries007.tfc.world.surface.SurfaceState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.RegistryObject;
import tfcflorae.common.blocks.TFCFBlocks;
import tfcflorae.common.blocks.rock.TFCFRock;
import tfcflorae.common.blocks.soil.Colors;
import tfcflorae.common.blocks.soil.TFCFRockSand;
import tfcflorae.common.blocks.soil.TFCFRockSoil;
import tfcflorae.common.blocks.soil.TFCFSoil;

public class TFCFSoilSurfaceState implements SurfaceState {
   public final List<SurfaceState> regions;

   public static SurfaceState buildType(TFCFSoil type) {
      ImmutableList<SurfaceState> regions = ImmutableList.of(
         sand(),
         transition(sand(), soil(type, Variant.SANDY_LOAM)),
         soil(type, Variant.SANDY_LOAM),
         transition(soil(type, Variant.SANDY_LOAM), soil(type, Variant.LOAM)),
         soil(type, Variant.LOAM),
         transition(soil(type, Variant.LOAM), soil(type, Variant.SILTY_LOAM)),
         soil(type, Variant.SILTY_LOAM),
         transition(soil(type, Variant.SILTY_LOAM), soil(type, Variant.SILT)),
         soil(type, Variant.SILT)
      );
      return new TFCFSoilSurfaceState(regions);
   }

   public static SurfaceState buildTypeGrass(TFCFSoil type) {
      ImmutableList<SurfaceState> regions = ImmutableList.of(
         sand(),
         transition(sand(), soil(type, Variant.SANDY_LOAM)),
         soil(type, Variant.SANDY_LOAM),
         transition(soil(type, Variant.SANDY_LOAM), soil(type, Variant.LOAM)),
         soil(type, Variant.LOAM),
         transition(soil(type, Variant.LOAM), soil(type, Variant.SILTY_LOAM)),
         soil(type, Variant.SILTY_LOAM),
         transition(soil(type, Variant.SILTY_LOAM), soil(type, Variant.SILT)),
         soil(type, Variant.SILT)
      );
      return new TFCFSoilSurfaceState.NeedsPostProcessing(regions);
   }

   public static SurfaceState buildTypeRock(TFCFRockSoil type) {
      ImmutableList<SurfaceState> regions = ImmutableList.of(
         sand(),
         transition(sand(), rockSoil(type, Variant.SANDY_LOAM)),
         rockSoil(type, Variant.SANDY_LOAM),
         transition(rockSoil(type, Variant.SANDY_LOAM), rockSoil(type, Variant.LOAM)),
         rockSoil(type, Variant.LOAM),
         transition(rockSoil(type, Variant.LOAM), rockSoil(type, Variant.SILTY_LOAM)),
         rockSoil(type, Variant.SILTY_LOAM),
         transition(rockSoil(type, Variant.SILTY_LOAM), rockSoil(type, Variant.SILT)),
         rockSoil(type, Variant.SILT)
      );
      return new TFCFSoilSurfaceState(regions);
   }

   public static SurfaceState buildTypeRockSand(TFCFRockSand type) {
      ImmutableList<SurfaceState> regions = ImmutableList.of(
         sand(),
         transition(sand(), rockSand(type)),
         rockSand(type),
         transition(rockSand(type), rockSand(type)),
         rockSand(type),
         transition(rockSand(type), rockSand(type)),
         rockSand(type),
         transition(rockSand(type), rockSand(type)),
         rockSand(type)
      );
      return new TFCFSoilSurfaceState(regions);
   }

   public static SurfaceState buildSandOrGravel(boolean sandIsSandstone) {
      SurfaceState sand = sandIsSandstone ? sandstone() : sand();
      SurfaceState gravel = gravel();
      return new TFCFSoilSurfaceState(ImmutableList.of(sand, transition(sand, gravel), gravel, gravel, gravel, gravel, gravel, gravel, gravel));
   }

   public static SurfaceState transition(SurfaceState first, SurfaceState second) {
      return context -> {
         BlockPos pos = context.pos();
         float noise = (float)(SoilSurfaceState.PATCH_NOISE.noise((float)pos.getX(), (float)pos.getZ()));
         return noise > 0.0F ? first.getState(context) : second.getState(context);
      };
   }

   public static SurfaceState rock(BlockType type) {
      return context -> isTFCFRock(context)
            ? ((Block)TFCFBlocks.TFCF_ROCK_BLOCKS.get(rockType(context)).get(type).get()).defaultBlockState()
            : ((Block)((RegistryObject)((Map)TFCBlocks.ROCK_BLOCKS.get(rockType(context))).get(type)).get()).defaultBlockState();
   }

   public static SurfaceState rockCustom(RegistryRock rock, BlockType type) {
      return context -> isCurrentRock(context, rock)
            ? ((Block)TFCFBlocks.TFCF_ROCK_BLOCKS.get(rockType(context)).get(type).get()).defaultBlockState()
            : ((Block)((RegistryObject)((Map)TFCBlocks.ROCK_BLOCKS.get(rockType(context))).get(type)).get()).defaultBlockState();
   }

   public static SurfaceState sand() {
      return context -> context.getRock().sand().defaultBlockState();
   }

   public static SurfaceState sandstone() {
      return context -> context.getRock().sandstone().defaultBlockState();
   }

   public static SurfaceState gravel() {
      return context -> context.getRock().gravel().defaultBlockState();
   }

   public static SurfaceState soil(TFCFSoil type, Variant variant) {
      Supplier<Block> block = (Supplier<Block>)TFCFBlocks.TFCSOIL.get(type).get(variant);
      return context -> block.get().defaultBlockState();
   }

   public static SurfaceState TFCFsoil(TFCFSoil type, TFCFSoil.TFCFVariant variant) {
      Supplier<Block> block = (Supplier<Block>)TFCFBlocks.TFCFSOIL.get(type).get(variant);
      return context -> block.get().defaultBlockState();
   }

   public static SurfaceState rockSoil(TFCFRockSoil type, Variant variant) {
      return context -> isTFCFRock(context)
            ? ((Block)TFCFBlocks.TFCROCKSOIL2.get(type).get(variant).get(rockType(context)).get()).defaultBlockState()
            : ((Block)TFCFBlocks.TFCROCKSOIL.get(type).get(variant).get(rockType(context)).get()).defaultBlockState();
   }

   public static SurfaceState rockSandGrass() {
      return context -> ((Block)TFCFBlocks.SAND_GRASS.get(sandColor(context)).get()).defaultBlockState();
   }

   public static SurfaceState rockSandSparseGrass() {
      return context -> ((Block)TFCFBlocks.SPARSE_SAND_GRASS.get(sandColor(context)).get()).defaultBlockState();
   }

   public static SurfaceState rockSandDenseGrass() {
      return context -> ((Block)TFCFBlocks.DENSE_SAND_GRASS.get(sandColor(context)).get()).defaultBlockState();
   }

   public static SurfaceState rockSand(TFCFRockSand type) {
      return context -> isTFCFRock(context)
            ? ((Block)TFCFBlocks.ROCKY_SAND_TFCF.get(type).get(sandColor(context)).get(rockType(context)).get()).defaultBlockState()
            : ((Block)TFCFBlocks.ROCKY_SAND_TFC.get(type).get(sandColor(context)).get(rockType(context)).get()).defaultBlockState();
   }

   public static SurfaceState rockRareSandGrass() {
      Supplier<Block> pinkSand = (Supplier<Block>)TFCFBlocks.SAND_GRASS.get(Colors.PINK);
      Supplier<Block> blackSand = (Supplier<Block>)TFCFBlocks.SAND_GRASS.get(Colors.BLACK);
      return context -> {
         if (context.rainfall() > 300.0F && context.averageTemperature() > 15.0F) {
            return pinkSand.get().defaultBlockState();
         } else {
            return context.rainfall() > 300.0F ? blackSand.get().defaultBlockState() : ((Block)TFCFBlocks.SAND_GRASS.get(sandColor(context)).get()).defaultBlockState();
         }
      };
   }

   public static SurfaceState rockRareSandSparseGrass() {
      Supplier<Block> pinkSand = (Supplier<Block>)TFCFBlocks.SPARSE_SAND_GRASS.get(Colors.PINK);
      Supplier<Block> blackSand = (Supplier<Block>)TFCFBlocks.SPARSE_SAND_GRASS.get(Colors.BLACK);
      return context -> {
         if (context.rainfall() > 300.0F && context.averageTemperature() > 15.0F) {
            return pinkSand.get().defaultBlockState();
         } else {
            return context.rainfall() > 300.0F ? blackSand.get().defaultBlockState() : ((Block)TFCFBlocks.SPARSE_SAND_GRASS.get(sandColor(context)).get()).defaultBlockState();
         }
      };
   }

   public static SurfaceState rockRareSandDenseGrass() {
      Supplier<Block> pinkSand = (Supplier<Block>)TFCFBlocks.DENSE_SAND_GRASS.get(Colors.PINK);
      Supplier<Block> blackSand = (Supplier<Block>)TFCFBlocks.DENSE_SAND_GRASS.get(Colors.BLACK);
      return context -> {
         if (context.rainfall() > 300.0F && context.averageTemperature() > 15.0F) {
            return pinkSand.get().defaultBlockState();
         } else {
            return context.rainfall() > 300.0F ? blackSand.get().defaultBlockState() : ((Block)TFCFBlocks.DENSE_SAND_GRASS.get(sandColor(context)).get()).defaultBlockState();
         }
      };
   }

   public static SurfaceState rockRareSand(TFCFRockSand type) {
      return context -> {
         if (isTFCFRock(context)) {
            Supplier<Block> pinkSand = (Supplier<Block>)TFCFBlocks.ROCKY_SAND_TFCF.get(type).get(Colors.PINK).get(rockType(context));
            Supplier<Block> blackSand = (Supplier<Block>)TFCFBlocks.ROCKY_SAND_TFCF.get(type).get(Colors.BLACK).get(rockType(context));
            if (context.rainfall() > 300.0F && context.averageTemperature() > 15.0F) {
               return pinkSand.get().defaultBlockState();
            } else {
               return context.rainfall() > 300.0F
                  ? blackSand.get().defaultBlockState()
                  : ((Block)TFCFBlocks.ROCKY_SAND_TFCF.get(type).get(sandColor(context)).get(rockType(context)).get()).defaultBlockState();
            }
         } else {
            Supplier<Block> pinkSand = (Supplier<Block>)TFCFBlocks.ROCKY_SAND_TFC.get(type).get(Colors.PINK).get(rockType(context));
            Supplier<Block> blackSand = (Supplier<Block>)TFCFBlocks.ROCKY_SAND_TFC.get(type).get(Colors.BLACK).get(rockType(context));
            if (context.rainfall() > 300.0F && context.averageTemperature() > 15.0F) {
               return pinkSand.get().defaultBlockState();
            } else {
               return context.rainfall() > 300.0F
                  ? blackSand.get().defaultBlockState()
                  : ((Block)TFCFBlocks.ROCKY_SAND_TFC.get(type).get(sandColor(context)).get(rockType(context)).get()).defaultBlockState();
            }
         }
      };
   }

   public static Colors sandColor(SurfaceBuilderContext context) {
      if (context.getBottomRock().sand() != null) {
         for (Colors sandColors : Colors.values()) {
            if (context.getBottomRock().sand() == ((RegistryObject)TFCBlocks.SAND.get(sandColors.toSandTFC(true))).get()) {
               return sandColors;
            }

            if (context.getBottomRock().sand() == TFCFBlocks.SAND.get(sandColors.nonTFC()).get()) {
               return sandColors;
            }
         }
      } else if (Colors.fromMaterialColour(context.getBottomRock().sand().defaultBlockState().getMapColor(level, pos)) != null) {
         return Colors.fromMaterialColour(context.getBottomRock().sand().defaultBlockState().getMapColor(level, pos));
      }

      return Colors.YELLOW;
   }

   public static RegistryRock rockType(SurfaceBuilderContext context) {
      if (surfaceRock(context) != null) {
         for (Rock rockTFC : Rock.values()) {
            if (surfaceRock(context).raw() == ((RegistryObject)((Map)TFCBlocks.ROCK_BLOCKS.get(rockTFC)).get(BlockType.RAW)).get()) {
               return rockTFC;
            }

            for (TFCFRock rockTFCF : TFCFRock.values()) {
               if (surfaceRock(context).raw() == TFCFBlocks.TFCF_ROCK_BLOCKS.get(rockTFCF).get(BlockType.RAW).get()) {
                  return rockTFCF;
               }
            }
         }
      }

      return Rock.GRANITE;
   }

   public static boolean isTFCFRock(SurfaceBuilderContext context) {
      if (surfaceRock(context) != null) {
         for (TFCFRock r2 : TFCFRock.values()) {
            if (surfaceRock(context).raw() == TFCFBlocks.TFCF_ROCK_BLOCKS.get(r2).get(BlockType.RAW).get()) {
               return true;
            }
         }
      }

      return false;
   }

   public static boolean isCurrentRock(SurfaceBuilderContext context, RegistryRock rock) {
      return rockType(context) == rock;
   }

   public static RegistrySoilVariant currentSoilVariant(SurfaceBuilderContext context) {
      for (SoilBlockType type : SoilBlockType.values()) {
         for (Variant variantTFC : Variant.values()) {
            if (SoilSurfaceState.buildType(type).getState(context).getBlock() == ((RegistryObject)((Map)TFCBlocks.SOIL.get(type)).get(variantTFC)).get()) {
               return variantTFC;
            }
         }
      }

      return Variant.LOAM;
   }

   public static RockSettings surfaceRock(SurfaceBuilderContext context) {
      return context.getRockData().getRock(context.pos());
   }

   public TFCFSoilSurfaceState(List<SurfaceState> regions) {
      this.regions = regions;
   }

   public BlockState getState(SurfaceBuilderContext context) {
      float rainfall = context.rainfall() + 20.0F;
      int index = Mth.clamp((int)Mth.clampedMap(rainfall, 0.0F, 500.0F, 0.0F, (float)this.regions.size() - 0.01F), 0, this.regions.size() - 1);
      return this.regions.get(index).getState(context);
   }

   public static RegistrySoilVariant getSoilVariant(WorldGenLevel level, BlockPos pos) {
      RandomSource random = level.getRandom();
      ChunkDataProvider provider = ChunkDataProvider.get(level);
      ChunkData data = provider.get(level, pos);
      float rainfall = data.getRainfall(pos) + 20.0F;
      int index = Mth.clamp((int)Mth.clampedMap(rainfall, 0.0F, 500.0F, 0.0F, 8.99F), 0, 8);
      switch (index) {
         case 1:
            return transitionSoil(Variant.SANDY_LOAM, Variant.SANDY_LOAM, pos, random);
         case 2:
            return Variant.SANDY_LOAM;
         case 3:
            return transitionSoil(Variant.SANDY_LOAM, Variant.LOAM, pos, random);
         case 4:
            return Variant.LOAM;
         case 5:
            return transitionSoil(Variant.LOAM, Variant.SILTY_LOAM, pos, random);
         case 6:
            return Variant.SILTY_LOAM;
         case 7:
            return transitionSoil(Variant.SILTY_LOAM, Variant.SILT, pos, random);
         case 8:
            return Variant.SILT;
         default:
            return Variant.SANDY_LOAM;
      }
   }

   public static RegistrySoilVariant transitionSoil(RegistrySoilVariant first, RegistrySoilVariant second, BlockPos pos, RandomSource random) {
      float noise = (float)(SoilSurfaceState.PATCH_NOISE.noise((float)pos.getX(), (float)pos.getZ()));
      float noiseGauss = noise + 2.0F * (float)random.nextGaussian();
      return noiseGauss > 0.0F ? first : second;
   }

   static class NeedsPostProcessing extends TFCFSoilSurfaceState {
      private NeedsPostProcessing(List<SurfaceState> regions) {
         super(regions);
      }

      public void setState(SurfaceBuilderContext context) {
         context.chunk().setBlockState(context.pos(), this.getState(context), false);
         context.chunk().markPosForPostprocessing(context.pos());
      }
   }
}
