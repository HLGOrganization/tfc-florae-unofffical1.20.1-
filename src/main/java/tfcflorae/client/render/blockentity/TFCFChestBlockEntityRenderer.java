package tfcflorae.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import tfcflorae.common.blockentities.TFCFChestBlockEntity;
import tfcflorae.common.blockentities.TFCFTrappedChestBlockEntity;
import tfcflorae.common.blocks.wood.TFCFChestBlock;
import tfcflorae.util.TFCFHelpers;

public class TFCFChestBlockEntityRenderer extends ChestRenderer<TFCFChestBlockEntity> {
   private String wood = "oak";

   private static String getFolder(BlockEntity blockEntity, ChestType type) {
      String prefix = blockEntity instanceof TFCFTrappedChestBlockEntity ? "trapped" : "normal";
      return chooseForType(type, prefix, prefix + "_left", prefix + "_right");
   }

   private static String chooseForType(ChestType type, String single, String left, String right) {
      return switch (type) {
         case LEFT -> left;
         case RIGHT -> right;
         case SINGLE -> single;
         default -> throw new IncompatibleClassChangeError();
      };
   }

   public TFCFChestBlockEntityRenderer(Context context) {
      super(context);
   }

   public void render(TFCFChestBlockEntity be, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
      if (be.getBlockState().getBlock() instanceof TFCFChestBlock chestBlock) {
         this.wood = chestBlock.getTextureLocation();
      }

      super.render(be, partialTicks, poseStack, buffer, combinedLight, combinedOverlay);
   }

   protected Material getMaterial(TFCFChestBlockEntity blockEntity, ChestType chestType) {
      return new Material(Sheets.CHEST_SHEET, TFCFHelpers.identifier("entity/chest/" + getFolder(blockEntity, chestType) + "/" + this.wood));
   }
}
