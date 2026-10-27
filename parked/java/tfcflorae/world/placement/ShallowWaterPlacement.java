package tfcflorae.world.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import java.util.stream.Stream;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.world.Codecs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public class ShallowWaterPlacement extends PlacementModifier {
   public static final Codec<ShallowWaterPlacement> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
               Codecs.POSITIVE_INT.optionalFieldOf("min_depth", 0).forGetter(c -> c.minDepth),
               Codecs.POSITIVE_INT.optionalFieldOf("max_depth", 5).forGetter(c -> c.maxDepth)
            )
            .apply(instance, ShallowWaterPlacement::new)
   );
   private final int minDepth;
   private final int maxDepth;

   public ShallowWaterPlacement(int minDepth, int maxDepth) {
      this.minDepth = minDepth;
      this.maxDepth = maxDepth;
   }

   public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos) {
      MutableBlockPos mutablePos = new MutableBlockPos().set(pos);

      for (int i = 0; i < this.maxDepth; i++) {
         mutablePos.move(0, -1, 0);
         if (!context.getLevel().isFluidAtPosition(mutablePos, state -> Helpers.isFluid(state, FluidTags.WATER))) {
            if (i < this.minDepth) {
               return Stream.empty();
            }

            return (double)random.nextFloat() > (double)i / (double)this.maxDepth ? Stream.of(pos) : Stream.empty();
         }
      }

      return Stream.empty();
   }

   public PlacementModifierType<?> type() {
      return (PlacementModifierType<?>)TFCFPlacements.SHALLOW_WATER.get();
   }
}
