package tfcflorae.world.feature.tree;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.soil.SoilBlockType;
import net.dries007.tfc.common.blocks.soil.SoilBlockType.Variant;
import net.dries007.tfc.world.feature.tree.TreePlacementConfig;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.FeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraftforge.registries.RegistryObject;
import tfcflorae.world.feature.tree.mangrove.MangroveRootPlacer;

public class RootedTreeConfig extends TreeConfiguration {
   public static final Codec<RootedTreeConfig> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
               BlockStateProvider.CODEC.fieldOf("trunk_provider").forGetter(config -> config.trunkProvider),
               TrunkPlacer.CODEC.fieldOf("trunk_placer").forGetter(config -> config.trunkPlacer),
               BlockStateProvider.CODEC.fieldOf("foliage_provider").forGetter(config -> config.foliageProvider),
               FoliagePlacer.CODEC.fieldOf("foliage_placer").forGetter(config -> config.foliagePlacer),
               MangroveRootPlacer.CODEC.fieldOf("root_placer").forGetter(config -> config.rootPlacer),
               BlockStateProvider.CODEC.fieldOf("dirt_provider").forGetter(config -> config.dirtProvider),
               FeatureSize.CODEC.fieldOf("minimum_size").forGetter(config -> config.minimumSize),
               TreeDecorator.CODEC.listOf().fieldOf("decorators").forGetter(config -> config.decorators),
               Codec.BOOL.fieldOf("ignore_vines").orElse(false).forGetter(config -> config.ignoreVines),
               Codec.BOOL.fieldOf("force_dirt").orElse(false).forGetter(config -> config.forceDirt),
               Codec.INT.fieldOf("radius").forGetter(config -> config.radius),
               TreePlacementConfig.CODEC.fieldOf("placement").forGetter(config -> config.placement)
            )
            .apply(instance, RootedTreeConfig::new)
   );
   public final MangroveRootPlacer rootPlacer;
   public final int radius;
   public final TreePlacementConfig placement;

   protected RootedTreeConfig(
      BlockStateProvider trunkProvider,
      TrunkPlacer trunkPlacer,
      BlockStateProvider foliageProvider,
      FoliagePlacer foliagePlacer,
      MangroveRootPlacer rootPlacer,
      BlockStateProvider dirtProvider,
      FeatureSize minimumSize,
      List<TreeDecorator> decorators,
      boolean ignoreVines,
      boolean forceDirt,
      int radius,
      TreePlacementConfig placement
   ) {
      super(trunkProvider, trunkPlacer, foliageProvider, foliagePlacer, dirtProvider, minimumSize, decorators, ignoreVines, forceDirt);
      this.rootPlacer = rootPlacer;
      this.radius = radius;
      this.placement = placement;
   }

   public static class TreeConfigurationBuilder {
      public final BlockStateProvider trunkProvider;
      private final TrunkPlacer trunkPlacer;
      public final BlockStateProvider foliageProvider;
      private final FoliagePlacer foliagePlacer;
      private final MangroveRootPlacer rootPlacer;
      private BlockStateProvider dirtProvider;
      private final FeatureSize minimumSize;
      private List<TreeDecorator> decorators = ImmutableList.of();
      private boolean ignoreVines;
      private boolean forceDirt;
      private int radius;
      private TreePlacementConfig placement;

      public TreeConfigurationBuilder(
         BlockStateProvider trunkProvider,
         TrunkPlacer trunkPlacer,
         BlockStateProvider foliageProvider,
         FoliagePlacer foliagePlacer,
         MangroveRootPlacer rootPlacer,
         FeatureSize minimumSize,
         int radius,
         TreePlacementConfig placement
      ) {
         this.trunkProvider = trunkProvider;
         this.trunkPlacer = trunkPlacer;
         this.foliageProvider = foliageProvider;
         this.dirtProvider = BlockStateProvider.simple((Block)((RegistryObject)((Map)TFCBlocks.SOIL.get(SoilBlockType.DIRT)).get(Variant.LOAM)).get());
         this.foliagePlacer = foliagePlacer;
         this.rootPlacer = rootPlacer;
         this.minimumSize = minimumSize;
         this.radius = radius;
         this.placement = placement;
      }

      public RootedTreeConfig.TreeConfigurationBuilder dirtProvider(BlockStateProvider dirtProvider) {
         this.dirtProvider = dirtProvider;
         return this;
      }

      public RootedTreeConfig.TreeConfigurationBuilder decorators(List<TreeDecorator> decorators) {
         this.decorators = decorators;
         return this;
      }

      public RootedTreeConfig.TreeConfigurationBuilder ignoreVines() {
         this.ignoreVines = true;
         return this;
      }

      public RootedTreeConfig.TreeConfigurationBuilder forceDirt() {
         this.forceDirt = true;
         return this;
      }

      public RootedTreeConfig.TreeConfigurationBuilder radius(int radius) {
         this.radius = radius;
         return this;
      }

      public RootedTreeConfig.TreeConfigurationBuilder placement(TreePlacementConfig placement) {
         this.placement = placement;
         return this;
      }

      public RootedTreeConfig build() {
         return new RootedTreeConfig(
            this.trunkProvider,
            this.trunkPlacer,
            this.foliageProvider,
            this.foliagePlacer,
            this.rootPlacer,
            this.dirtProvider,
            this.minimumSize,
            this.decorators,
            this.ignoreVines,
            this.forceDirt,
            this.radius,
            this.placement
         );
      }
   }
}
