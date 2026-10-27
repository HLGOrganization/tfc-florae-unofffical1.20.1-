package tfcflorae.world.feature.tree;
import net.minecraft.world.level.levelgen.feature.Feature;

import com.mojang.serialization.Codec;
import net.minecraft.util.RandomSource;
import net.dries007.tfc.mixin.accessor.StructureTemplateAccessor;
import net.dries007.tfc.world.feature.tree.RandomTreeConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class TFCFRandomTreeFeature extends Feature<RandomTreeConfig> {
   public TFCFRandomTreeFeature(Codec<RandomTreeConfig> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<RandomTreeConfig> context) {
      WorldGenLevel level = context.level();
      BlockPos pos = context.origin();
      RandomSource random = context.random();
      RandomTreeConfig config = (RandomTreeConfig)context.config();
      ChunkPos chunkPos = new ChunkPos(pos);
      MutableBlockPos mutablePos = new MutableBlockPos().set(pos);
      StructureTemplateManager manager = TFCFTreeHelpers.getStructureManager(level);
      StructurePlaceSettings settings = TFCFTreeHelpers.getPlacementSettings(level, chunkPos, random);
      ResourceLocation structureId = (ResourceLocation)config.structureNames().get(random.nextInt(config.structureNames().size()));
      StructureTemplate structure = manager.getOrCreate(structureId);
      if (((StructureTemplateAccessor)structure).accessor$getPalettes().isEmpty()) {
         throw new IllegalStateException("Empty structure: " + structureId);
      } else if (TFCFTreeHelpers.isValidLocation(level, pos, settings, config.placement())) {
         config.trunk().ifPresent(trunk -> {
            int height = TFCFTreeHelpers.placeTrunk(level, mutablePos, random, settings, trunk);
            mutablePos.move(0, height - config.placement().height(), 0);
         });
         TFCFTreeHelpers.placeTemplate(structure, settings, level, mutablePos.subtract(TFCFTreeHelpers.transformCenter(structure.getSize(), settings)));
         return true;
      } else {
         return false;
      }
   }
}
