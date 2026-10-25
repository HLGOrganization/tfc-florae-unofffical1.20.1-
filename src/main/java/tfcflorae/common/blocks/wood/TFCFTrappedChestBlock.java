package tfcflorae.common.blocks.wood;

import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import tfcflorae.common.blockentities.TFCFBlockEntities;

public class TFCFTrappedChestBlock extends TFCFChestBlock {
   public TFCFTrappedChestBlock(ExtendedProperties properties, String textureLocation) {
      super(properties, textureLocation, TFCFBlockEntities.TRAPPED_CHEST::get);
   }

   protected Stat<ResourceLocation> getOpenChestStat() {
      return Stats.CUSTOM.get(Stats.TRIGGER_TRAPPED_CHEST);
   }

   public boolean isSignalSource(BlockState state) {
      return true;
   }

   public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
      return Mth.clamp(ChestBlockEntity.getOpenCount(level, pos), 0, 15);
   }

   public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
      return direction == Direction.UP ? state.getSignal(level, pos, direction) : 0;
   }
}
