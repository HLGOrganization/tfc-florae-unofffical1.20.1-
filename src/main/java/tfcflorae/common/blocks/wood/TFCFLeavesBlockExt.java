package tfcflorae.common.blocks.wood;

import tfcflorae.common.TFCFBlockStateProperties;

import net.minecraft.util.RandomSource;
import java.util.function.Supplier;
import net.dries007.tfc.common.TFCTags.Blocks;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.TFCBlockStateProperties;
import net.dries007.tfc.common.blocks.wood.TFCLeavesBlock;
import net.dries007.tfc.util.EnvironmentHelpers;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.Season;
import net.dries007.tfc.world.chunkdata.ChunkData;
import net.dries007.tfc.world.chunkdata.ChunkDataProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import org.jetbrains.annotations.Nullable;
import tfcflorae.Config;

public abstract class TFCFLeavesBlockExt extends TFCLeavesBlock {
   @Nullable
   private final Supplier<? extends Block> fallenLeaves;
   @Nullable
   private final Supplier<? extends Block> fallenTwig;
   @Nullable
   private final Supplier<? extends Block> sapling;

   public static TFCFLeavesBlockExt create(ExtendedProperties properties, int maxDecayDistance) {
      return create(properties, maxDecayDistance, null, null, null);
   }

   public static TFCFLeavesBlockExt create(
      ExtendedProperties properties,
      int maxDecayDistance,
      @Nullable Supplier<? extends Block> fallenLeaves,
      @Nullable Supplier<? extends Block> fallenTwig,
      @Nullable Supplier<? extends Block> sapling
   ) {
      final IntegerProperty distanceProperty = getDistanceProperty(maxDecayDistance);
      return new TFCFLeavesBlockExt(properties, maxDecayDistance, fallenLeaves, fallenTwig, sapling) {
         protected IntegerProperty getDistanceProperty() {
            return distanceProperty;
         }
      };
   }

   private static IntegerProperty getDistanceProperty(int maxDecayDistance) {
      // TFC 3.x only ships the vanilla 1-7 distance ladder; per-wood custom ladders would need
      // blockstate JSON to carry the property range, so every leaves block uses the standard one.
      // (`maxDecayDistance` is still passed through for the parent's behaviour.)
      return net.dries007.tfc.common.blocks.TFCBlockStateProperties.DISTANCE_10;
   }

   public TFCFLeavesBlockExt(
      ExtendedProperties properties,
      int maxDecayDistance,
      @Nullable Supplier<? extends Block> fallenLeaves,
      @Nullable Supplier<? extends Block> fallenTwig,
      @Nullable Supplier<? extends Block> sapling
   ) {
      super(properties, maxDecayDistance, fallenLeaves, fallenTwig);
      this.fallenLeaves = fallenLeaves;
      this.fallenTwig = fallenTwig;
      this.sapling = sapling;
      this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(this.getDistanceProperty(), 1)).setValue(PERSISTENT, false));
   }

   public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
      // 1.18.2 的「树叶播撒树苗」（读 ChunkData）已按 1.20.1 行为移除，见 TFCFLeavesBlock 的说明。
      super.randomTick(state, level, pos, random);
   }
}
