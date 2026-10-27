package tfcflorae.world.feature;
import net.minecraft.world.level.material.MapColor;

import com.mojang.serialization.Codec;
import java.util.Map;
import net.minecraft.util.RandomSource;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.rock.Rock;
import net.dries007.tfc.common.blocks.rock.Rock.BlockType;
import net.dries007.tfc.util.EnvironmentHelpers;
import net.dries007.tfc.world.biome.BiomeExtension;
import net.dries007.tfc.world.biome.TFCBiomes;
import net.dries007.tfc.world.noise.Noise3D;
import net.dries007.tfc.world.noise.OpenSimplex3D;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.RegistryObject;
import tfcflorae.Config;
import tfcflorae.common.TFCFTags;
import tfcflorae.interfaces.TFCBiomesMixinInterface;

public class SubTerraneanRiver extends Feature<NoneFeatureConfiguration> {
   private static TFCBiomes staticBiomes = new TFCBiomes();
   public static final BiomeExtension ALPINE_MOUNTAINS = (net.dries007.tfc.world.biome.BiomeExtension) null;
   public static final BiomeExtension CHASMS = (net.dries007.tfc.world.biome.BiomeExtension) null;
   public static final int REFERENCE_DEPTH = -246;
   public static final float HOLLOWING_THRESHOLD = 0.85F;

   public SubTerraneanRiver(Codec<NoneFeatureConfiguration> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
      if (!(Boolean)Config.COMMON.enableUndergroundRivers.get()) {
         return false;
      } else {
         WorldGenLevel level = context.level();
         BlockPos pos = context.origin();
         RandomSource random = context.random();
         MutableBlockPos mutablePos = new MutableBlockPos();

         for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
               int x = pos.getX() + i;
               int z = pos.getZ() + j;
               mutablePos.set(x, 0, z);
               BiomeExtension biome = TFCBiomes.getExtension(level, (Biome)level.getBiome(mutablePos).value());
               if (biome.isRiver()
                  || biome.equals(CHASMS)
                  || biome.equals(ALPINE_MOUNTAINS)
                  || biome.toString().equals(TFCFTags.Biomes.IS_RIVER.toString())
                  || biome.toString().equals(TFCFTags.Biomes.IS_LAKE.toString())) {
                  for (int y = -256; y < -223; y++) {
                     mutablePos.set(x, y, z);
                     double carving = this.carvingNoise(level.getSeed() + 1L, x, y, z);
                     if (y > level.getMinBuildHeight() && y < level.getMaxBuildHeight() && level.getBlockState(mutablePos).getBlock() != Blocks.BEDROCK) {
                        if (carving >= 0.85F
                           && (
                              EnvironmentHelpers.isWorldgenReplaceable(level, mutablePos)
                                 || level.getBlockState(mutablePos).getBlock().defaultBlockState().isSolid()
                                 || level.getBlockState(mutablePos).getBlock().defaultMapColor() == MapColor.DIRT
                           )) {
                           if (y <= -246) {
                              level.setBlock(mutablePos, Blocks.LAVA.defaultBlockState(), 3);
                           } else {
                              level.setBlock(mutablePos, Blocks.AIR.defaultBlockState(), 3);
                           }
                        } else if (carving < 0.85F
                           && carving >= (double)(Helpers.triangle(random, 3.0F) * 0.1F)
                           && y < -248
                           && EnvironmentHelpers.isWorldgenReplaceable(level, mutablePos)) {
                           level.setBlock(
                              mutablePos, ((Block)((RegistryObject)((Map)TFCBlocks.ROCK_BLOCKS.get(Rock.BASALT)).get(BlockType.RAW)).get()).defaultBlockState(), 3
                           );
                        }
                     }
                  }
               }
            }
         }

         return false;
      }
   }

   public double carvingNoise(long seed, int x, int y, int z) {
      Noise3D cliffNoise = new OpenSimplex3D(seed).octaves(2).spread(0.1F).scaled(0.0F, 3.0F);
      if (y > -226) {
         return 0.0;
      } else if (y > -236) {
         double easing = (double)(1.0F - (float)(y - -246 - 10) / 10.0F);
         return easing * (double)cliffNoise.noise((float)x, (float)y, (float)z);
      } else if (y > -246) {
         return (double)cliffNoise.noise((float)x, (float)y, (float)z);
      } else if (y > -254) {
         double easing = (double)(y - -246 + 8) / 8.0;
         return easing * (double)cliffNoise.noise((float)x, (float)y, (float)z);
      } else {
         return 0.0;
      }
   }
}
