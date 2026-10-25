package tfcflorae.common.blocks.plant;

import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.plant.ShortGrassBlock;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.registry.RegistryPlant;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags.Blocks;

public abstract class DesertShortGrassBlock extends ShortGrassBlock {
   public static DesertShortGrassBlock create(final RegistryPlant plant, ExtendedProperties properties) {
      return new DesertShortGrassBlock(properties) {
         public RegistryPlant getPlant() {
            return plant;
         }
      };
   }

   public static DesertShortGrassBlock createBeachGrass(final RegistryPlant plant, ExtendedProperties properties) {
      return new DesertShortGrassBlock(properties) {
         public RegistryPlant getPlant() {
            return plant;
         }

         @Override
         protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
            return Helpers.isBlock(state.getBlock(), BlockTags.SAND) || Helpers.isBlock(state.getBlock(), Blocks.SAND);
         }
      };
   }

   protected DesertShortGrassBlock(ExtendedProperties properties) {
      super(properties);
   }

   public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
      BlockPos blockpos = pos.below();
      BlockState blockstate = level.getBlockState(blockpos);
      return this.mayPlaceOn(blockstate, level, pos);
   }

   protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
      return Helpers.isBlock(state.getBlock(), BlockTags.SAND)
         || Helpers.isBlock(state.getBlock(), net.dries007.tfc.common.TFCTags.Blocks.GRASS_PLANTABLE_ON)
         || Helpers.isBlock(state.getBlock(), net.dries007.tfc.common.TFCTags.Blocks.BUSH_PLANTABLE_ON)
         || super.mayPlaceOn(state, level, pos);
   }
}
