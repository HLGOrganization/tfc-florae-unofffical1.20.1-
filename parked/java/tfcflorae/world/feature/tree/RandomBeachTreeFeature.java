package tfcflorae.world.feature.tree;
import net.minecraft.world.level.levelgen.feature.Feature;

import com.mojang.serialization.Codec;
import net.minecraft.util.RandomSource;
import net.dries007.tfc.common.TFCTags.Blocks;
import net.dries007.tfc.common.blocks.RiverWaterBlock;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.mixin.accessor.StructureTemplateAccessor;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.world.feature.tree.RandomTreeConfig;
import net.dries007.tfc.world.feature.tree.TreeHelpers;
import net.dries007.tfc.world.feature.tree.TreePlacementConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class RandomBeachTreeFeature extends Feature<RandomTreeConfig> {
   public RandomBeachTreeFeature(Codec<RandomTreeConfig> codec) {
      super(codec);
   }

   public boolean place(FeaturePlaceContext<RandomTreeConfig> context) {
      WorldGenLevel level = context.level();
      BlockPos pos = context.origin();
      RandomSource random = context.random();
      RandomTreeConfig config = (RandomTreeConfig)context.config();
      ChunkPos chunkPos = new ChunkPos(pos);
      MutableBlockPos mutablePos = new MutableBlockPos().set(pos);
      StructureTemplateManager manager = TreeHelpers.getStructureManager(level);
      StructurePlaceSettings settings = TreeHelpers.getPlacementSettings(level, chunkPos, random);
      ResourceLocation structureId = (ResourceLocation)config.structureNames().get(random.nextInt(config.structureNames().size()));
      StructureTemplate structure = manager.getOrCreate(structureId);
      if (((StructureTemplateAccessor)structure).accessor$getPalettes().isEmpty()) {
         throw new IllegalStateException("Empty structure: " + structureId);
      } else if (!TreeHelpers.isValidLocation(level, pos, settings, config.placement()) && !isValidGround(level, pos, settings, config.placement())) {
         return false;
      } else {
         config.trunk().ifPresent(trunk -> {
            int height = TreeHelpers.placeTrunk(level, mutablePos, random, settings, trunk);
            mutablePos.move(0, height, 0);
         });
         TreeHelpers.placeTemplate(structure, settings, level, mutablePos.subtract(TreeHelpers.transformCenter(structure.getSize(), settings)));
         return true;
      }
   }

   public static boolean isValidGround(LevelAccessor level, BlockPos pos, StructurePlaceSettings settings, TreePlacementConfig config) {
      MutableBlockPos mutablePos = new MutableBlockPos();

      for (int x = (1 - config.width()) / 2; x <= config.width() / 2; x++) {
         for (int z = (1 - config.width()) / 2; z <= config.width() / 2; z++) {
            mutablePos.set(x, 0, z);
            TreeHelpers.transformMutable(mutablePos, settings.getMirror(), settings.getRotation());
            mutablePos.move(pos);
            if (false ? !isValidPositionPossiblyUnderwater(level, mutablePos) : !isValidPosition(level, mutablePos, config)) {
               return false;
            }
         }
      }

      return true;
   }

   public static boolean isValidPosition(LevelAccessor level, MutableBlockPos mutablePos, TreePlacementConfig config) {
      BlockState stateAt = level.getBlockState(mutablePos);
      boolean isInWater = stateAt.getFluidState().getType() == Fluids.WATER;
      if ((!false || !FluidHelpers.isAirOrEmptyFluid(stateAt) || !isInWater)
         && !stateAt.isAir()
         && !(stateAt.getBlock() instanceof SaplingBlock)) {
         return false;
      } else {
         mutablePos.move(0, -1, 0);
         BlockState stateBelow = level.getBlockState(mutablePos);
         boolean treeGrowsOn = Helpers.isBlock(stateBelow, Blocks.TREE_GROWS_ON) || Helpers.isBlock(stateBelow, BlockTags.SAND);
         if (false && isInWater) {
            treeGrowsOn |= Helpers.isBlock(stateBelow, Blocks.SEA_BUSH_PLANTABLE_ON);
         }

         return treeGrowsOn;
      }
   }

   public static boolean isValidPositionPossiblyUnderwater(LevelAccessor level, MutableBlockPos mutablePos) {
      BlockState stateAt = level.getBlockState(mutablePos);
      FluidState fluid = stateAt.getFluidState();
      if (Helpers.isFluid(fluid, FluidTags.WATER) && !stateAt.hasProperty(RiverWaterBlock.FLOW)) {
         mutablePos.move(0, -1, 0);
         BlockState stateBelow = level.getBlockState(mutablePos);
         return Helpers.isBlock(stateBelow, Blocks.SEA_BUSH_PLANTABLE_ON) && Helpers.isBlock(stateBelow, Blocks.TREE_GROWS_ON);
      } else {
         return false;
      }
   }
}
