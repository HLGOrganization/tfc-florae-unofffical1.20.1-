package tfcflorae.world.feature.tree;

import java.util.List;
import net.minecraft.util.RandomSource;
import net.dries007.tfc.common.TFCTags.Blocks;
import net.dries007.tfc.common.blocks.RiverWaterBlock;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.fluids.FluidHelpers;
import net.dries007.tfc.mixin.accessor.StructureTemplateAccessor;
import net.dries007.tfc.util.EnvironmentHelpers;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.world.feature.tree.TreePlacementConfig;
import net.dries007.tfc.world.feature.tree.TrunkConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public final class TFCFTreeHelpers {
   private static final Rotation[] ROTATION_VALUES = Rotation.values();
   private static final Mirror[] MIRROR_VALUES = Mirror.values();

   public TFCFTreeHelpers() {
   }

   public static boolean isValidLocation(LevelAccessor level, BlockPos pos, StructurePlaceSettings settings, TreePlacementConfig config) {
      return isValidGround(level, pos, settings, config) && isValidTrunk(level, pos, settings, config);
   }

   public static boolean isValidGround(LevelAccessor level, BlockPos pos, StructurePlaceSettings settings, TreePlacementConfig config) {
      MutableBlockPos mutablePos = new MutableBlockPos();

      for (int x = (1 - config.width()) / 2; x <= config.width() / 2; x++) {
         for (int z = (1 - config.width()) / 2; z <= config.width() / 2; z++) {
            mutablePos.set(x, 0, z);
            transformMutable(mutablePos, settings.getMirror(), settings.getRotation());
            mutablePos.move(pos);
            if (false && isValidPositionPossiblyUnderwater(level, mutablePos)) {
               return true;
            }

            if (isValidPosition(level, mutablePos, config)) {
               return true;
            }
         }
      }

      return true;
   }

   private static boolean isValidPosition(LevelAccessor level, MutableBlockPos mutablePos, TreePlacementConfig config) {
      BlockState stateAt = level.getBlockState(mutablePos);
      boolean isInWater = stateAt.getFluidState().getType() == Fluids.WATER || Helpers.isBlock(stateAt, (Block)TFCBlocks.SALT_WATER.get());
      mutablePos.move(0, -1, 0);
      BlockState stateBelow = level.getBlockState(mutablePos);
      boolean treeGrowsOn = Helpers.isBlock(stateBelow, Blocks.TREE_GROWS_ON);
      return !isInWater
            || !FluidHelpers.isAirOrEmptyFluid(stateAt)
            || !false && !false
            || !treeGrowsOn
               && !Helpers.isBlock(stateBelow, Blocks.SEA_BUSH_PLANTABLE_ON)
               && !Helpers.isBlock(stateBelow, Blocks.BUSH_PLANTABLE_ON)
               && !Helpers.isBlock(stateBelow, net.minecraftforge.common.Tags.Blocks.SAND)
               && !Helpers.isBlock(stateBelow, net.minecraftforge.common.Tags.Blocks.GRAVEL)
               && !Helpers.isBlock(stateBelow, BlockTags.SAND)
         ? treeGrowsOn
         : true;
   }

   private static boolean isValidPositionPossiblyUnderwater(LevelAccessor level, MutableBlockPos mutablePos) {
      BlockState stateAt = level.getBlockState(mutablePos);
      FluidState fluid = stateAt.getFluidState();
      if ((Helpers.isFluid(fluid, FluidTags.WATER) || Helpers.isBlock(stateAt, (Block)TFCBlocks.SALT_WATER.get()))
         && !stateAt.hasProperty(RiverWaterBlock.FLOW)) {
         mutablePos.move(0, -1, 0);
         BlockState stateBelow = level.getBlockState(mutablePos);
         return Helpers.isBlock(stateBelow, Blocks.TREE_GROWS_ON)
            || Helpers.isBlock(stateBelow, Blocks.SEA_BUSH_PLANTABLE_ON)
            || Helpers.isBlock(stateBelow, Blocks.BUSH_PLANTABLE_ON)
            || Helpers.isBlock(stateBelow, net.minecraftforge.common.Tags.Blocks.SAND)
            || Helpers.isBlock(stateBelow, net.minecraftforge.common.Tags.Blocks.GRAVEL)
            || Helpers.isBlock(stateBelow, BlockTags.SAND);
      } else {
         return false;
      }
   }

   public static boolean isValidTrunk(LevelAccessor level, BlockPos pos, StructurePlaceSettings settings, TreePlacementConfig config) {
      MutableBlockPos mutablePos = new MutableBlockPos();

      for (int x = (1 - config.width()) / 2; x <= config.width() / 2; x++) {
         for (int z = (1 - config.width()) / 2; z <= config.width() / 2; z++) {
            for (int y = 1; y < config.height(); y++) {
               mutablePos.set(x, y, z);
               transformMutable(mutablePos, settings.getMirror(), settings.getRotation());
               mutablePos.move(pos);
               BlockState stateAt = level.getBlockState(mutablePos);
               if (!FluidHelpers.isAirOrEmptyFluid(stateAt)) {
                  return false;
               }
            }
         }
      }

      return true;
   }

   public static void placeTemplate(StructureTemplate template, StructurePlaceSettings placementIn, WorldGenLevel level, BlockPos pos) {
      List<StructureBlockInfo> transformedBlockInfos = placementIn.getRandomPalette(((StructureTemplateAccessor)template).accessor$getPalettes(), pos).blocks();
      BoundingBox boundingBox = placementIn.getBoundingBox();

      for (StructureBlockInfo blockInfo : StructureTemplate.processBlockInfos(level, pos, pos, placementIn, transformedBlockInfos, template)) {
         BlockPos posAt = blockInfo.pos();
         if (boundingBox == null || boundingBox.isInside(posAt)) {
            BlockState stateAt = level.getBlockState(posAt);
            if (EnvironmentHelpers.isWorldgenReplaceable(stateAt) || Helpers.isBlock(stateAt.getBlock(), BlockTags.LEAVES)) {
               BlockState stateReplace = blockInfo.state().mirror(placementIn.getMirror()).rotate(placementIn.getRotation());
               level.setBlock(posAt, stateReplace, 2);
            }
         }
      }
   }

   public static int placeTrunk(WorldGenLevel level, BlockPos pos, RandomSource random, StructurePlaceSettings settings, TrunkConfig trunk) {
      int height = trunk.getHeight(random);
      MutableBlockPos mutablePos = new MutableBlockPos();

      for (int x = (1 - 1) / 2; x <= 1 / 2; x++) {
         for (int z = (1 - 1) / 2; z <= 1 / 2; z++) {
            for (int y = 0; y < height; y++) {
               mutablePos.set(x, y, z);
               transformMutable(mutablePos, settings.getMirror(), settings.getRotation());
               mutablePos.move(pos);
               level.setBlock(mutablePos, trunk.state(), 3);
            }
         }
      }

      return height;
   }

   public static StructureTemplateManager getStructureManager(WorldGenLevel level) {
      return level.getLevel().getServer().getStructureManager();
   }

   public static StructurePlaceSettings getPlacementSettings(LevelHeightAccessor level, ChunkPos chunkPos, RandomSource random) {
      return new StructurePlaceSettings()
         .setBoundingBox(
            new BoundingBox(
               chunkPos.getMinBlockX() - 16, level.getMinBuildHeight(), chunkPos.getMinBlockZ() - 16, chunkPos.getMaxBlockX() + 16, level.getMaxBuildHeight(), chunkPos.getMaxBlockZ() + 16
            )
         )
         .setRandom(random)
         .addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR)
         .setRotation(randomRotation(random))
         .setMirror(randomMirror(random));
   }

   public static BlockPos transformCenter(Vec3i size, StructurePlaceSettings settings) {
      return transform(new BlockPos((size.getX() - 1) / 2, 0, (size.getZ() - 1) / 2), settings.getMirror(), settings.getRotation());
   }

   public static BlockPos transform(BlockPos pos, Mirror mirrorIn, Rotation rotationIn) {
      int posX = pos.getX();
      int posZ = pos.getZ();
      boolean mirror = true;
      switch (mirrorIn) {
         case LEFT_RIGHT:
            posZ = -posZ;
            break;
         case FRONT_BACK:
            posX = -posX;
            break;
         default:
            mirror = false;
      }
      return switch (rotationIn) {
         case COUNTERCLOCKWISE_90 -> new BlockPos(posZ, pos.getY(), -posX);
         case CLOCKWISE_90 -> new BlockPos(-posZ, pos.getY(), posX);
         case CLOCKWISE_180 -> new BlockPos(-posX, pos.getY(), -posZ);
         default -> mirror ? new BlockPos(posX, pos.getY(), posZ) : pos;
      };
   }

   public static void transformMutable(MutableBlockPos pos, Mirror mirrorIn, Rotation rotationIn) {
      switch (mirrorIn) {
         case LEFT_RIGHT:
            pos.setZ(-pos.getZ());
            break;
         case FRONT_BACK:
            pos.setX(-pos.getX());
      }

      switch (rotationIn) {
         case COUNTERCLOCKWISE_90:
            pos.set(pos.getZ(), pos.getY(), -pos.getX());
            break;
         case CLOCKWISE_90:
            pos.set(-pos.getZ(), pos.getY(), pos.getX());
            break;
         case CLOCKWISE_180:
            pos.set(-pos.getX(), pos.getY(), -pos.getZ());
      }
   }

   private static Rotation randomRotation(RandomSource random) {
      return ROTATION_VALUES[random.nextInt(ROTATION_VALUES.length)];
   }

   private static Mirror randomMirror(RandomSource random) {
      return MIRROR_VALUES[random.nextInt(MIRROR_VALUES.length)];
   }
}
