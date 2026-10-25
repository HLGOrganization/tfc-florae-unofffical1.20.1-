package tfcflorae.common.blocks.rock;

import java.util.function.Supplier;
import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.rock.RockAnvilBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import tfcflorae.common.blocks.devices.TFCFAnvilBlock;

public class TFCFRockAnvilBlock extends RockAnvilBlock {
   public static final VoxelShape SHAPE = box(0.0, 0.0, 0.0, 16.0, 14.0, 16.0);
   private final Supplier<? extends Block> raw;

   public TFCFRockAnvilBlock(ExtendedProperties properties, Supplier<? extends Block> raw) {
      super(properties, raw);
      this.raw = raw;
   }

   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      return TFCFAnvilBlock.interactWithAnvil(level, pos, player, hand);
   }
}
