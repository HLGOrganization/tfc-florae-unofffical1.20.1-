package tfcflorae.common.blocks.plant;

import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;

public interface TFCFITallPlant {
   default TFCFITallPlant.Part getPlantPart(BlockGetter level, BlockPos pos) {
      if (level.getBlockState(pos.below()).getBlock() != this && level.getBlockState(pos.above()).getBlock() == this) {
         return TFCFITallPlant.Part.LOWER;
      } else if (level.getBlockState(pos.below()).getBlock() == this && level.getBlockState(pos.above()).getBlock() == this) {
         return TFCFITallPlant.Part.MIDDLE;
      } else {
         return level.getBlockState(pos.below()).getBlock() == this && level.getBlockState(pos.above()).getBlock() != this
            ? TFCFITallPlant.Part.UPPER
            : TFCFITallPlant.Part.SINGLE;
      }
   }

   public static enum Part implements StringRepresentable {
      UPPER,
      MIDDLE,
      LOWER,
      SINGLE;

      private final String serializedName = this.name().toLowerCase(Locale.ROOT);

      private Part() {
      }

      public String getSerializedName() {
         return this.serializedName;
      }
   }
}
