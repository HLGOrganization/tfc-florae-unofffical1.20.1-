package tfcflorae.mixin;

import net.minecraft.util.RandomSource;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
import net.dries007.tfc.world.chunkdata.ForestType;
import net.dries007.tfc.world.feature.tree.TFCTreeGrower;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin({TFCTreeGrower.class})
public abstract class TFCTreeGrowerMixin extends AbstractTreeGrower {
   @Shadow
   private final ResourceLocation normalTree;
   @Shadow
   private final ResourceLocation oldGrowthTree;

   public TFCTreeGrowerMixin(ResourceLocation normalTree, ResourceLocation oldGrowthFeatureFactory) {
      this.normalTree = normalTree;
      this.oldGrowthTree = oldGrowthFeatureFactory;
   }

   @Shadow
   public ConfiguredFeature<?, ?> getNormalFeature(Registry<ConfiguredFeature<?, ?>> registry) {
      return (ConfiguredFeature<?, ?>)registry.getOptional(this.normalTree)
         .orElseThrow(() -> new IllegalStateException("Missing tree feature: " + this.normalTree));
   }

   @Shadow
   public ConfiguredFeature<?, ?> getOldGrowthFeature(Registry<ConfiguredFeature<?, ?>> registry) {
      return registry.getOptional(this.oldGrowthTree).orElseGet(() -> this.getNormalFeature(registry));
   }

   @Overwrite(
      remap = false
   )
   public boolean growTree(ServerLevel level, ChunkGenerator generator, BlockPos pos, BlockState state, RandomSource random) {
      ConfiguredFeature<?, ?> feature = null;
      ConfiguredFeature<?, ?> smallTree = this.getNormalFeature(level.registryAccess().registryOrThrow(Registry.CONFIGURED_FEATURE_REGISTRY));
      ConfiguredFeature<?, ?> largeTree = this.getOldGrowthFeature(level.registryAccess().registryOrThrow(Registry.CONFIGURED_FEATURE_REGISTRY));
      ChunkDataProvider provider = ChunkDataProvider.get(level);
      ChunkData data = provider.get(level, pos);
      float rainfall = data.getRainfall(pos);
      float rainfallInverted = 500.0F - rainfall + 10.0F;
      float actualForestDensity = data.getForestDensity();
      float forestDensity = actualForestDensity == 0.0F ? 0.001F : actualForestDensity;
      ForestType forestType = data.getForestType();
      if (forestType != ForestType.OLD_GROWTH) {
         feature = forestType != ForestType.NONE && random.nextFloat(rainfallInverted * 1.2F / forestDensity) == 0.0F ? largeTree : smallTree;
      } else {
         feature = random.nextFloat(rainfallInverted * 1.2F) == 0.0F ? largeTree : smallTree;
      }

      level.setBlock(pos, Blocks.AIR.defaultBlockState(), 4);
      if (feature.place(level, generator, random, pos)) {
         return true;
      } else {
         level.setBlock(pos, state, 4);
         return false;
      }
   }
}
