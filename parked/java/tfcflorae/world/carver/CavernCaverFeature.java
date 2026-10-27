package tfcflorae.world.carver;

import com.mojang.serialization.Codec;
import net.minecraft.util.RandomSource;
import java.util.function.Function;
import net.dries007.tfc.world.carver.CarverHelpers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.carver.CarvingContext;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.CaveWorldCarver;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class CavernCaverFeature extends CaveWorldCarver {
   public CavernCaverFeature(Codec<CaveCarverConfiguration> codec) {
      super(codec);
   }

   protected int getCaveBound() {
      return 10;
   }

   protected float getThickness(RandomSource random) {
      return (random.nextFloat() * 2.0F + random.nextFloat()) * 2.0F;
   }

   protected double getYScale() {
      return 5.0;
   }

   protected boolean carveBlock(
      CarvingContext context,
      CaveCarverConfiguration config,
      ChunkAccess chunk,
      Function<BlockPos, Holder<Biome>> biome,
      CarvingMask mask,
      MutableBlockPos mutablePos,
      MutableBlockPos mutablePos2,
      Aquifer aquifer,
      MutableBoolean bool
   ) {
      if (CarverHelpers.canReplaceBlock(chunk.getBlockState(mutablePos))) {
         BlockState blockstate;
         if (mutablePos.getY() <= context.getMinGenY() + 15) {
            blockstate = LAVA.createLegacyBlock();
         } else {
            blockstate = CAVE_AIR;
         }

         chunk.setBlockState(mutablePos, blockstate, false);
         return true;
      } else {
         return false;
      }
   }
}
