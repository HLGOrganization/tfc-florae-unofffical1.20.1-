package tfcflorae.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import tfcflorae.common.blockentities.TFCFToolRackBlockEntity;
import tfcflorae.common.blocks.wood.TFCFToolRackBlock;

public class TFCFToolRackBlockEntityRenderer implements BlockEntityRenderer<TFCFToolRackBlockEntity> {
   private static final float[][][] ITEM_LOCATION = new float[4][4][3];
   private static final float[] META_TO_ANGLE = new float[]{180.0F, 90.0F, 0.0F, 270.0F};
   private static final float ITEM_SCALE = 0.5F;

   public TFCFToolRackBlockEntityRenderer() {
   }

   public void render(
      TFCFToolRackBlockEntity toolRack, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay
   ) {
      if (toolRack.getLevel() != null) {
         toolRack.getCapability(Capabilities.ITEM).ifPresent(cap -> {
            int meta = ((Direction)toolRack.getBlockState().getValue(TFCFToolRackBlock.FACING)).get2DDataValue();

            for (int i = 0; i < 4; i++) {
               ItemStack stack = cap.getStackInSlot(i);
               if (!stack.isEmpty()) {
                  poseStack.pushPose();
                  poseStack.translate((double)ITEM_LOCATION[meta][i][0], (double)ITEM_LOCATION[meta][i][1], (double)ITEM_LOCATION[meta][i][2]);
                  poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(META_TO_ANGLE[meta]));
                  poseStack.scale(0.5F, 0.5F, 0.5F);
                  Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, false, combinedLight, combinedOverlay, poseStack, buffer, 0);
                  poseStack.popPose();
               }
            }
         });
      }
   }

   static {
      float heightLow = 0.28125F;
      float heightHigh = 0.78125F;
      float column1 = 0.25F;
      float column2 = 0.75F;
      float offset = 0.09375F;
      float offsetInv = 0.90625F;

      for (int dir = 0; dir < 4; dir++) {
         for (int slot = 0; slot < 4; slot++) {
            if (slot < 2) {
               ITEM_LOCATION[dir][slot][1] = 0.78125F;
            } else {
               ITEM_LOCATION[dir][slot][1] = 0.28125F;
            }

            if (dir % 2 == 0) {
               if (slot % 2 == 0) {
                  ITEM_LOCATION[dir][slot][0] = 0.25F;
               } else {
                  ITEM_LOCATION[dir][slot][0] = 0.75F;
               }
            } else if (slot % 2 == 0) {
               ITEM_LOCATION[dir][slot][2] = 0.25F;
            } else {
               ITEM_LOCATION[dir][slot][2] = 0.75F;
            }

            if (dir == 0) {
               ITEM_LOCATION[dir][slot][2] = 0.09375F;
            } else if (dir == 1) {
               ITEM_LOCATION[dir][slot][0] = 0.90625F;
            } else if (dir == 2) {
               ITEM_LOCATION[dir][slot][2] = 0.90625F;
            } else {
               ITEM_LOCATION[dir][slot][0] = 0.09375F;
            }
         }
      }
   }
}
