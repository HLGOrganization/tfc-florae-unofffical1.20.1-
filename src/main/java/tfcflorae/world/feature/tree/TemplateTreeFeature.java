package tfcflorae.world.feature.tree;

import com.mojang.serialization.Codec;
import net.dries007.tfc.world.feature.tree.RandomTreeConfig;
import net.dries007.tfc.world.feature.tree.RootConfig;
import net.dries007.tfc.world.feature.tree.TreeHelpers;
import net.dries007.tfc.world.feature.tree.TrunkConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

/**
 * Template-backed TFC tree placement with a complete preflight before any
 * template blocks are written. Initially enabled only for African Padauk.
 */
public final class TemplateTreeFeature extends Feature<RandomTreeConfig> {
   public TemplateTreeFeature(Codec<RandomTreeConfig> codec) {
      super(codec);
   }

   @Override
   public boolean place(FeaturePlaceContext<RandomTreeConfig> context) {
      WorldGenLevel level = context.level();
      BlockPos origin = context.origin();
      RandomSource random = context.random();
      RandomTreeConfig config = context.config();
      if (config.structureNames().isEmpty()) {
         return false;
      }

      ChunkPos chunkPos = new ChunkPos(origin);
      StructureTemplateManager manager = TreeHelpers.getStructureManager(level);
      StructurePlaceSettings settings = TreeHelpers.getPlacementSettings(level, chunkPos, random);
      ResourceLocation structureId = config.structureNames().get(random.nextInt(config.structureNames().size()));
      StructureTemplate template = manager.getOrCreate(structureId);
      if (!TreeHelpers.isValidLocation(level, origin, settings, config.placement())) {
         return false;
      }

      BlockPos.MutableBlockPos templateOrigin = new BlockPos.MutableBlockPos().set(origin);
      if (config.rootSystem().isPresent()) {
         RootConfig roots = config.rootSystem().get();
         if (!TreeHelpers.placeRoots(level, templateOrigin, roots, random) && roots.required()) {
            return false;
         }
         TreeHelpers.placeRoots(level, templateOrigin, roots, random);
      }
      if (config.trunk().isPresent()) {
         TrunkConfig trunk = config.trunk().get();
         int trunkHeight = TreeHelpers.placeTrunk(level, templateOrigin, random, settings, trunk);
         templateOrigin.move(0, trunkHeight, 0);
      }

      BlockPos placementOrigin = templateOrigin.subtract(TreeHelpers.transformCenter(template.getSize(), settings));
      TreeHelpers.placeTemplate(template, settings, level, placementOrigin);
      return true;
   }
}
