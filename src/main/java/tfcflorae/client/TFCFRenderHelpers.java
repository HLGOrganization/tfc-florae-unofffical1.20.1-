package tfcflorae.client;
import net.minecraft.client.gui.GuiGraphics;
import com.mojang.math.Axis;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import java.util.function.Consumer;
import net.dries007.tfc.common.entities.livestock.TFCAnimal;
import net.dries007.tfc.common.entities.livestock.TFCAnimalProperties.Age;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidStack;
import tfcflorae.util.TFCFHelpers;

public final class TFCFRenderHelpers {
   public static final ResourceLocation BLOCKS_ATLAS = TextureAtlas.LOCATION_BLOCKS;

   public TFCFRenderHelpers() {
   }

   public static void renderTexturedCuboid(
      PoseStack poseStack, VertexConsumer buffer, TextureAtlasSprite sprite, int packedLight, int packedOverlay, AABB bounds
   ) {
      renderTexturedCuboid(
         poseStack,
         buffer,
         sprite,
         packedLight,
         packedOverlay,
         (float)bounds.minX,
         (float)bounds.minY,
         (float)bounds.minZ,
         (float)bounds.maxX,
         (float)bounds.maxY,
         (float)bounds.maxZ
      );
   }

   public static void renderTexturedCuboid(
      PoseStack poseStack,
      VertexConsumer buffer,
      TextureAtlasSprite sprite,
      int packedLight,
      int packedOverlay,
      float minX,
      float minY,
      float minZ,
      float maxX,
      float maxY,
      float maxZ
   ) {
      renderTexturedCuboid(
         poseStack,
         buffer,
         sprite,
         packedLight,
         packedOverlay,
         minX,
         minY,
         minZ,
         maxX,
         maxY,
         maxZ,
         16.0F * (maxX - minX),
         16.0F * (maxY - minY),
         16.0F * (maxZ - minZ)
      );
   }

   public static void renderTexturedCuboid(
      PoseStack poseStack,
      VertexConsumer buffer,
      TextureAtlasSprite sprite,
      int packedLight,
      int packedOverlay,
      float minX,
      float minY,
      float minZ,
      float maxX,
      float maxY,
      float maxZ,
      float xPixels,
      float yPixels,
      float zPixels
   ) {
      renderTexturedQuads(
         poseStack, buffer, sprite, packedLight, packedOverlay, getXVertices(minX, minY, minZ, maxX, maxY, maxZ), zPixels, yPixels, 1.0F, 0.0F, 0.0F
      );
      renderTexturedQuads(
         poseStack, buffer, sprite, packedLight, packedOverlay, getYVertices(minX, minY, minZ, maxX, maxY, maxZ), zPixels, xPixels, 0.0F, 1.0F, 0.0F
      );
      renderTexturedQuads(
         poseStack, buffer, sprite, packedLight, packedOverlay, getZVertices(minX, minY, minZ, maxX, maxY, maxZ), xPixels, yPixels, 0.0F, 0.0F, 1.0F
      );
   }

   public static void renderTexturedTrapezoidalCuboid(
      PoseStack poseStack,
      VertexConsumer buffer,
      TextureAtlasSprite sprite,
      int packedLight,
      int packedOverlay,
      float pMinX,
      float pMaxX,
      float pMinZ,
      float pMaxZ,
      float qMinX,
      float qMaxX,
      float qMinZ,
      float qMaxZ,
      float minY,
      float maxY,
      float xPixels,
      float yPixels,
      float zPixels
   ) {
      renderTexturedQuads(
         poseStack,
         buffer,
         sprite,
         packedLight,
         packedOverlay,
         getTrapezoidalCuboidXVertices(pMinX, pMaxX, pMinZ, pMaxZ, qMinX, qMaxX, qMinZ, qMaxZ, minY, maxY),
         zPixels,
         yPixels,
         1.0F,
         0.0F,
         0.0F
      );
      renderTexturedQuads(
         poseStack,
         buffer,
         sprite,
         packedLight,
         packedOverlay,
         getTrapezoidalCuboidYVertices(pMinX, pMaxX, pMinZ, pMaxZ, qMinX, qMaxX, qMinZ, qMaxZ, minY, maxY),
         zPixels,
         xPixels,
         0.0F,
         1.0F,
         0.0F
      );
      renderTexturedQuads(
         poseStack,
         buffer,
         sprite,
         packedLight,
         packedOverlay,
         getTrapezoidalCuboidZVertices(pMinX, pMaxX, pMinZ, pMaxZ, qMinX, qMaxX, qMinZ, qMaxZ, minY, maxY),
         xPixels,
         yPixels,
         0.0F,
         0.0F,
         1.0F
      );
   }

   public static void renderTexturedQuads(
      PoseStack poseStack,
      VertexConsumer buffer,
      TextureAtlasSprite sprite,
      int packedLight,
      int packedOverlay,
      float[][] vertices,
      float uSize,
      float vSize,
      float normalX,
      float normalY,
      float normalZ
   ) {
      for (float[] v : vertices) {
         renderTexturedVertex(
            poseStack,
            buffer,
            packedLight,
            packedOverlay,
            v[0],
            v[1],
            v[2],
            sprite.getU((double)(v[3] * uSize)),
            sprite.getV((double)(v[4] * vSize)),
            v[5] * normalX,
            v[5] * normalY,
            v[5] * normalZ
         );
      }
   }

   public static void renderTexturedVertex(
      PoseStack poseStack,
      VertexConsumer buffer,
      int packedLight,
      int packedOverlay,
      float x,
      float y,
      float z,
      float u,
      float v,
      float normalX,
      float normalY,
      float normalZ
   ) {
      float shade = getShade(normalX, normalY, normalZ);
      buffer.vertex(poseStack.last().pose(), x, y, z)
         .color(shade, shade, shade, 1.0F)
         .uv(u, v)
         .uv2(packedLight)
         .overlayCoords(packedOverlay)
         .normal(poseStack.last().normal(), normalX, normalY, normalZ)
         .endVertex();
   }

   public static float getShade(float normalX, float normalY, float normalZ) {
      return getShadeForStep(Math.round(normalX), Math.round(normalY), Math.round(normalZ));
   }

   public static float getShadeForStep(int normalX, int normalY, int normalZ) {
      if (normalY == 1) {
         return 1.0F;
      } else if (normalY == -1) {
         return 0.5F;
      } else if (normalZ != 0) {
         return 0.8F;
      } else {
         return normalX != 0 ? 0.6F : 1.0F;
      }
   }

   public static float[][] getXVertices(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
      return new float[][]{
         {minX, minY, minZ, 0.0F, 1.0F, 1.0F},
         {minX, minY, maxZ, 1.0F, 1.0F, 1.0F},
         {minX, maxY, maxZ, 1.0F, 0.0F, 1.0F},
         {minX, maxY, minZ, 0.0F, 0.0F, 1.0F},
         {maxX, minY, maxZ, 1.0F, 0.0F, -1.0F},
         {maxX, minY, minZ, 0.0F, 0.0F, -1.0F},
         {maxX, maxY, minZ, 0.0F, 1.0F, -1.0F},
         {maxX, maxY, maxZ, 1.0F, 1.0F, -1.0F}
      };
   }

   public static float[][] getYVertices(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
      return new float[][]{
         {minX, maxY, minZ, 0.0F, 1.0F, 1.0F},
         {minX, maxY, maxZ, 1.0F, 1.0F, 1.0F},
         {maxX, maxY, maxZ, 1.0F, 0.0F, 1.0F},
         {maxX, maxY, minZ, 0.0F, 0.0F, 1.0F},
         {minX, minY, maxZ, 1.0F, 0.0F, -1.0F},
         {minX, minY, minZ, 0.0F, 0.0F, -1.0F},
         {maxX, minY, minZ, 0.0F, 1.0F, -1.0F},
         {maxX, minY, maxZ, 1.0F, 1.0F, -1.0F}
      };
   }

   public static float[][] getZVertices(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
      return new float[][]{
         {maxX, minY, minZ, 0.0F, 1.0F, 1.0F},
         {minX, minY, minZ, 1.0F, 1.0F, 1.0F},
         {minX, maxY, minZ, 1.0F, 0.0F, 1.0F},
         {maxX, maxY, minZ, 0.0F, 0.0F, 1.0F},
         {minX, minY, maxZ, 1.0F, 0.0F, -1.0F},
         {maxX, minY, maxZ, 0.0F, 0.0F, -1.0F},
         {maxX, maxY, maxZ, 0.0F, 1.0F, -1.0F},
         {minX, maxY, maxZ, 1.0F, 1.0F, -1.0F}
      };
   }

   public static float[][] getDiagonalPlaneVertices(float x1, float y1, float z1, float x2, float y2, float z2, float u1, float v1, float u2, float v2) {
      return new float[][]{
         {x1, y1, z1, u1, v1},
         {x2, y1, z1, u2, v1},
         {x2, y2, z2, u2, v2},
         {x1, y2, z2, u1, v2},
         {x2, y1, z1, u2, v1},
         {x1, y1, z1, u1, v1},
         {x1, y2, z2, u1, v2},
         {x2, y2, z2, u2, v2}
      };
   }

   public static float[][] getTrapezoidalCuboidXVertices(
      float pMinX, float pMaxX, float pMinZ, float pMaxZ, float qMinX, float qMaxX, float qMinZ, float qMaxZ, float minY, float maxY
   ) {
      return new float[][]{
         {pMinX, minY, pMinZ, 0.0F, 1.0F, 1.0F},
         {pMinX, minY, pMaxZ, 1.0F, 1.0F, 1.0F},
         {qMinX, maxY, qMaxZ, 1.0F, 0.0F, 1.0F},
         {qMinX, maxY, qMinZ, 0.0F, 0.0F, 1.0F},
         {pMaxX, minY, pMaxZ, 1.0F, 0.0F, -1.0F},
         {pMaxX, minY, pMinZ, 0.0F, 0.0F, -1.0F},
         {qMaxX, maxY, qMinZ, 0.0F, 1.0F, -1.0F},
         {qMaxX, maxY, qMaxZ, 1.0F, 1.0F, -1.0F}
      };
   }

   public static float[][] getTrapezoidalCuboidYVertices(
      float pMinX, float pMaxX, float pMinZ, float pMaxZ, float qMinX, float qMaxX, float qMinZ, float qMaxZ, float minY, float maxY
   ) {
      return new float[][]{
         {qMinX, maxY, qMinZ, 0.0F, 1.0F, 1.0F},
         {qMinX, maxY, qMaxZ, 1.0F, 1.0F, 1.0F},
         {qMaxX, maxY, qMaxZ, 1.0F, 0.0F, 1.0F},
         {qMaxX, maxY, qMinZ, 0.0F, 0.0F, 1.0F},
         {pMinX, minY, pMaxZ, 1.0F, 0.0F, -1.0F},
         {pMinX, minY, pMinZ, 0.0F, 0.0F, -1.0F},
         {pMaxX, minY, pMinZ, 0.0F, 1.0F, -1.0F},
         {pMaxX, minY, pMaxZ, 1.0F, 1.0F, -1.0F}
      };
   }

   public static float[][] getTrapezoidalCuboidZVertices(
      float pMinX, float pMaxX, float pMinZ, float pMaxZ, float qMinX, float qMaxX, float qMinZ, float qMaxZ, float minY, float maxY
   ) {
      return new float[][]{
         {pMaxX, minY, pMinZ, 0.0F, 1.0F, 1.0F},
         {pMinX, minY, pMinZ, 1.0F, 1.0F, 1.0F},
         {qMinX, maxY, qMinZ, 1.0F, 0.0F, 1.0F},
         {qMaxX, maxY, qMinZ, 0.0F, 0.0F, 1.0F},
         {pMinX, minY, pMaxZ, 1.0F, 0.0F, -1.0F},
         {pMaxX, minY, pMaxZ, 0.0F, 0.0F, -1.0F},
         {qMaxX, maxY, qMaxZ, 0.0F, 1.0F, -1.0F},
         {qMinX, maxY, qMaxZ, 1.0F, 1.0F, -1.0F}
      };
   }

   public static void setShaderColor(int color) {
      float a = (float)(color >> 24 & 0xFF) / 255.0F;
      float r = (float)(color >> 16 & 0xFF) / 255.0F;
      float g = (float)(color >> 8 & 0xFF) / 255.0F;
      float b = (float)(color & 0xFF) / 255.0F;
      RenderSystem.setShaderColor(r, g, b, a);
   }

   public static void renderTwoHandedItem(
      PoseStack poseStack, MultiBufferSource source, int combinedLight, float pitch, float equipProgress, float swingProgress, ItemStack stack
   ) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player != null) {
         float swingSqrt = Mth.sqrt(swingProgress);
         float y = -0.2F * Mth.sin(swingProgress * (float) Math.PI);
         float z = -0.4F * Mth.sin(swingSqrt * (float) Math.PI);
         poseStack.translate(0.0, (double)(-y / 2.0F), (double)z);
         float tilt = calculateTilt(pitch);
         poseStack.translate(0.0, (double)(0.04F + equipProgress * -1.2F + tilt * -0.5F), -0.72F);
         poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(Mth.clamp(tilt, 0.3F, 0.51F) * 85.0F));
         if (!mc.player.isInvisible()) {
            poseStack.pushPose();
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90.0F));
            renderMapHand(mc, poseStack, source, combinedLight, HumanoidArm.RIGHT);
            renderMapHand(mc, poseStack, source, combinedLight, HumanoidArm.LEFT);
            poseStack.popPose();
         }

         addSiftingMovement(mc.player, poseStack);
         poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(Mth.sin(swingSqrt * (float) Math.PI) * 20.0F));
         poseStack.scale(2.0F, 2.0F, 2.0F);
         boolean right = mc.player.getMainArm() == HumanoidArm.RIGHT;
         mc.getEntityRenderDispatcher().getItemInHandRenderer()
            .renderItem(
               mc.player,
               stack,
               right ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
               !right,
               poseStack,
               source,
               combinedLight
            );
      }
   }

   public static ModelLayerLocation modelIdentifier(String name, String part) {
      return new ModelLayerLocation(TFCFHelpers.identifier(name), part);
   }

   public static ModelLayerLocation modelIdentifier(String name) {
      return modelIdentifier(name, "main");
   }

   public static ModelPart bakeSimple(Context ctx, String layerName) {
      return ctx.bakeLayer(modelIdentifier(layerName));
   }

   public static float itemTimeRotation() {
      return (float)(360.0 * (double)(System.currentTimeMillis() & 16383L) / 16383.0);
   }

   public static int getFluidColor(FluidStack fluid) {
      return getFluidColor(fluid.getFluid());
   }

   public static int getFluidColor(Fluid fluid) {
      return net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions.of(fluid).getTintColor();
   }

   public static void renderFluidFace(
      PoseStack poseStack,
      FluidStack fluidStack,
      MultiBufferSource buffer,
      float minX,
      float minZ,
      float maxX,
      float maxZ,
      float y,
      int combinedOverlay,
      int combinedLight
   ) {
      renderFluidFace(poseStack, fluidStack, buffer, getFluidColor(fluidStack), minX, minZ, maxX, maxZ, y, combinedOverlay, combinedLight);
   }

   public static void renderFluidFace(
      PoseStack poseStack,
      FluidStack fluidStack,
      MultiBufferSource buffer,
      int color,
      float minX,
      float minZ,
      float maxX,
      float maxZ,
      float y,
      int combinedOverlay,
      int combinedLight
   ) {
      Fluid fluid = fluidStack.getFluid();
      FluidType attributes = fluid.getFluidType();
      ResourceLocation texture = net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions.of(fluidStack.getFluid()).getStillTexture(fluidStack);
      TextureAtlasSprite sprite = (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(BLOCKS_ATLAS).apply(texture);
      VertexConsumer builder = buffer.getBuffer(RenderType.entityTranslucentCull(BLOCKS_ATLAS));
      Matrix4f matrix4f = poseStack.last().pose();
      builder.vertex(matrix4f, minX, y, minZ)
         .color(color)
         .uv(sprite.getU((double)(minX * 16.0F)), sprite.getV((double)(minZ * 16.0F)))
         .overlayCoords(combinedOverlay)
         .uv2(combinedLight)
         .normal(0.0F, 0.0F, 1.0F)
         .endVertex();
      builder.vertex(matrix4f, minX, y, maxZ)
         .color(color)
         .uv(sprite.getU((double)(minX * 16.0F)), sprite.getV((double)(maxZ * 16.0F)))
         .overlayCoords(combinedOverlay)
         .uv2(combinedLight)
         .normal(0.0F, 0.0F, 1.0F)
         .endVertex();
      builder.vertex(matrix4f, maxX, y, maxZ)
         .color(color)
         .uv(sprite.getU((double)(maxX * 16.0F)), sprite.getV((double)(maxZ * 16.0F)))
         .overlayCoords(combinedOverlay)
         .uv2(combinedLight)
         .normal(0.0F, 0.0F, 1.0F)
         .endVertex();
      builder.vertex(matrix4f, maxX, y, minZ)
         .color(color)
         .uv(sprite.getU((double)(maxX * 16.0F)), sprite.getV((double)(minX * 16.0F)))
         .overlayCoords(combinedOverlay)
         .uv2(combinedLight)
         .normal(0.0F, 0.0F, 1.0F)
         .endVertex();
   }

   public static ResourceLocation getTextureForAge(TFCAnimal animal, ResourceLocation young, ResourceLocation old) {
      return animal.getAgeType() == Age.OLD ? old : young;
   }

   public static TextureAtlasSprite getAndBindFluidSprite(FluidStack fluid) {
      setShaderColor(getFluidColor(fluid));
      RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
      return (TextureAtlasSprite)Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
         .apply(net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions.of(fluid.getFluid()).getStillTexture(fluid));
   }

   public static void fillAreaWithSprite(GuiGraphics guiGraphics, int left, int top, TextureAtlasSprite sprite, int startX, int endX, int endY, int fillHeight) {
      for (int yPos = endY; fillHeight > 0; yPos -= 16) {
         int yPixels = Math.min(fillHeight, 16);
         int fillWidth = endX - startX;

         for (int xPos = endX; fillWidth > 0; xPos -= 16) {
            int xPixels = Math.min(fillWidth, 16);
            guiGraphics.blit(left + xPos - xPixels, top + yPos - yPixels, 0, xPixels, yPixels, sprite);
            fillWidth -= 16;
         }

         fillHeight -= 16;
      }
   }

   /**
    * 1.20.1 widgets carry their tooltip through {@code setTooltip}; this helper now just
    * hands the component back so call sites can do {@code setTooltip(Tooltip.create(...))}.
    */
   public static java.util.function.Supplier<net.minecraft.network.chat.Component> makeButtonTooltip(final Screen screen, final Component component) {
      return () -> component;
   }

   private static float calculateTilt(float pitch) {
      float deg = Mth.clamp(1.0F - pitch / 45.0F + 0.1F, 0.0F, 1.0F) * (float) Math.PI;
      return -Mth.cos(deg) * 0.5F + 0.5F;
   }

   private static void renderMapHand(Minecraft mc, PoseStack poseStack, MultiBufferSource source, int combinedLight, HumanoidArm arm) {
      RenderSystem.setShaderTexture(0, mc.player.getSkinTextureLocation());
      PlayerRenderer playerrenderer = (PlayerRenderer)mc.getEntityRenderDispatcher().getRenderer(mc.player);
      poseStack.pushPose();
      float side = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
      poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(92.0F));
      poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(45.0F));
      poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(side * -41.0F + calculateArmMovement(mc.player)));
      poseStack.translate((double)side * 0.3, -1.1, 0.45);
      if (arm == HumanoidArm.RIGHT) {
         playerrenderer.renderRightHand(poseStack, source, combinedLight, mc.player);
      } else {
         playerrenderer.renderLeftHand(poseStack, source, combinedLight, mc.player);
      }

      poseStack.popPose();
   }

   private static void addSiftingMovement(LocalPlayer player, PoseStack stack) {
      float degrees = (float)player.getUseItemRemainingTicks() * (float) Math.PI / 10.0F;
      if (degrees > 0.0F) {
         float scale = 0.1F;
         stack.translate((double)(0.1F * Mth.cos(degrees)), 0.0, (double)(0.1F * Mth.sin(degrees)));
      }
   }

   private static float calculateArmMovement(LocalPlayer player) {
      float degrees = (float)player.getUseItemRemainingTicks() * (float) Math.PI / 10.0F;
      return degrees > 0.0F ? 10.0F * Mth.cos(degrees) : 0.0F;
   }
}
