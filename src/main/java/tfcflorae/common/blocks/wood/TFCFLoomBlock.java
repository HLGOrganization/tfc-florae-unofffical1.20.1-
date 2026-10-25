package tfcflorae.common.blocks.wood;

import net.dries007.tfc.common.blocks.ExtendedProperties;
import net.dries007.tfc.common.blocks.wood.TFCLoomBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import tfcflorae.common.blockentities.TFCFBlockEntities;

public class TFCFLoomBlock extends TFCLoomBlock {
   private final ResourceLocation woodTexture;

   public TFCFLoomBlock(ExtendedProperties properties, ResourceLocation woodTexture) {
      super(properties, woodTexture);
      this.woodTexture = woodTexture;
      this.registerDefaultState((BlockState)((BlockState)this.getStateDefinition().any()).setValue(FACING, Direction.NORTH));
   }

   public ResourceLocation getTextureLocation() {
      return this.woodTexture;
   }

   public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
      return level.getBlockEntity(pos, TFCFBlockEntities.LOOM.get()).map(loom -> loom.onRightClick(player)).orElse(InteractionResult.PASS);
   }
}
