package tfcflorae.mixin;

import net.minecraft.util.RandomSource;
import net.dries007.tfc.common.blocks.DirectionPropertyBlock;
import net.dries007.tfc.common.blocks.TFCBlocks;
import net.dries007.tfc.common.blocks.rock.RockCategory;
import net.dries007.tfc.common.fluids.TFCFluids;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.world.noise.OpenSimplex2D;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import tfcflorae.Config;
import tfcflorae.common.blocks.TFCFBlocks;
import tfcflorae.common.blocks.rock.Mineral;
import tfcflorae.common.blocks.rock.MineralSheetBlock;
import tfcflorae.common.items.TFCFItems;
import tfcflorae.util.TFCFHelpers;

@Mixin({LiquidBlock.class})
public abstract class LiquidBlockMixin {
   public LiquidBlockMixin() {
   }

   @Overwrite(
      remap = true
   )
   public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      state.getFluidState().randomTick(level, pos, random);
      if (level.isAreaLoaded(pos, 10) && random.nextInt((Integer)Config.COMMON.mineralGenFrequency.get()) == 0) {
         Fluid type = level.getFluidState(pos).getType();
         Mineral mineral = null;
         if (type == Fluids.LAVA.getSource() || state.getBlock() == Blocks.LAVA) {
            mineral = this.lavaMineral(level, pos, random, mineral);
         }

         if (type == TFCFluids.SPRING_WATER.getSource() || state.getBlock() == TFCBlocks.SPRING_WATER.get()) {
            mineral = this.waterMineral(level, pos, random, mineral);
         }

         if (mineral != null && type != null) {
            ItemStack itemMineral = new ItemStack(((Item)TFCFItems.MINERALS.get(mineral).get()).asItem());
            BlockPos genPos = pos.offset(random.nextInt(7) - 3, random.nextInt(7) - 3, random.nextInt(7) - 3);
            BlockState stateAt = level.getBlockState(genPos);
            Direction face = Direction.getRandom(random);
            Direction sheetFace = face.getOpposite();
            BooleanProperty property = DirectionPropertyBlock.getProperty(face);
            BlockPos adjacentPos = genPos.relative(face);
            BlockState adjacentState = level.getBlockState(adjacentPos);
            if (Helpers.isBlock(stateAt, (Block)TFCFBlocks.MINERAL_SHEET.get())) {
               if (!(Boolean)stateAt.getValue(property) && adjacentState.isFaceSturdy(level, adjacentPos, sheetFace)) {
                  MineralSheetBlock.addSheet(level, genPos, stateAt, face, itemMineral);
               }
            } else if (stateAt.getMaterial().isReplaceable()
               && stateAt.getMaterial() != MapColor.FIRE
               && stateAt.getMaterial() != MapColor.WATER
               && adjacentState.isFaceSturdy(level, adjacentPos, sheetFace)) {
               BlockState placingState = (BlockState)((Block)TFCFBlocks.MINERAL_SHEET.get()).defaultBlockState().setValue(property, true);
               MineralSheetBlock.addSheet(level, genPos, placingState, face, itemMineral);
            }
         }
      }
   }

   @Unique
   private Mineral lavaMineral(WorldGenLevel level, BlockPos pos, RandomSource random, Mineral mineral) {
      float variantNoiseValue = new OpenSimplex2D(level.getSeed()).octaves(2).spread(0.01F).abs().noise((float)pos.getX(), (float)pos.getZ()) * 0.9F
         + random.nextFloat() * 0.1F;
      Boolean categoryIgneousExtrusive = TFCFHelpers.rockType(level, pos).category() == RockCategory.IGNEOUS_EXTRUSIVE;
      Boolean categoryIgneousIntrusive = TFCFHelpers.rockType(level, pos).category() == RockCategory.IGNEOUS_INTRUSIVE;
      Boolean categoryMetamorphic = TFCFHelpers.rockType(level, pos).category() == RockCategory.METAMORPHIC;
      Boolean categorySedimentary = TFCFHelpers.rockType(level, pos).category() == RockCategory.SEDIMENTARY;
      if (variantNoiseValue > 0.25F) {
         if (categoryIgneousExtrusive) {
            mineral = Mineral.GREIGITE;
         } else if (categoryIgneousIntrusive) {
            mineral = Mineral.SMITHSONITE;
         } else if (categoryMetamorphic) {
            mineral = Mineral.BRIMSTONE;
         } else if (categorySedimentary) {
            mineral = Mineral.MAGNESITE;
         }
      } else if (variantNoiseValue > -0.75F) {
         if (categoryIgneousExtrusive) {
            mineral = Mineral.ZABUYELITE;
         } else if (categoryIgneousIntrusive) {
            mineral = Mineral.SPHEROCOBALTITE;
         } else if (categoryMetamorphic) {
            mineral = Mineral.ALABANDITE;
         } else if (categorySedimentary) {
            mineral = Mineral.GREIGITE;
         }
      } else if (categoryIgneousExtrusive) {
         mineral = Mineral.BRIMSTONE;
      } else if (categoryIgneousIntrusive) {
         mineral = Mineral.BRIMSTONE;
      } else if (categoryMetamorphic) {
         mineral = Mineral.BASTNASITE;
      } else if (categorySedimentary) {
         mineral = Mineral.APATITE;
      }

      return mineral;
   }

   @Unique
   private Mineral waterMineral(WorldGenLevel level, BlockPos pos, RandomSource random, Mineral mineral) {
      float variantNoiseValue = new OpenSimplex2D(level.getSeed()).octaves(2).spread(0.01F).abs().noise((float)pos.getX(), (float)pos.getZ()) * 0.9F
         + random.nextFloat() * 0.1F;
      Boolean categoryIgneousExtrusive = TFCFHelpers.rockType(level, pos).category() == RockCategory.IGNEOUS_EXTRUSIVE;
      Boolean categoryIgneousIntrusive = TFCFHelpers.rockType(level, pos).category() == RockCategory.IGNEOUS_INTRUSIVE;
      Boolean categoryMetamorphic = TFCFHelpers.rockType(level, pos).category() == RockCategory.METAMORPHIC;
      Boolean categorySedimentary = TFCFHelpers.rockType(level, pos).category() == RockCategory.SEDIMENTARY;
      if (variantNoiseValue > 0.25F) {
         if (categoryIgneousExtrusive) {
            mineral = Mineral.SALMIAK;
         } else if (categoryIgneousIntrusive) {
            mineral = Mineral.SALTPETER;
         } else if (categoryMetamorphic) {
            mineral = Mineral.SALT;
         } else if (categorySedimentary) {
            mineral = Mineral.CALCITE;
         }
      }

      return mineral;
   }
}
