package tfcflorae.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public final class RenderUtils {
   public RenderUtils() {
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

   public static void renderCross(
      float x,
      float z,
      float y1,
      float y2,
      PoseStack poseStack,
      VertexConsumer buffer,
      int combinedLight,
      int combinedOverlay,
      TextureAtlasSprite sprite,
      float u1,
      float v1,
      float u2,
      float v2
   ) {
      for (float[] ver : getCrossHalfVertices(x, y1, x, z, y2, z, u1, v1, u2, v2)) {
         renderTexturedVertex(
            poseStack,
            buffer,
            combinedLight,
            combinedOverlay,
            ver[0],
            ver[1],
            ver[2],
            sprite.getU((double)(ver[3] * 16.0F)),
            sprite.getV((double)(16.0F - ver[4] * 16.0F)),
            -1.414F,
            0.0F,
            -1.414F
         );
      }

      for (float[] ver : getCrossHalfVertices(x, y1, z, z, y2, x, u1, v1, u2, v2)) {
         renderTexturedVertex(
            poseStack,
            buffer,
            combinedLight,
            combinedOverlay,
            ver[0],
            ver[1],
            ver[2],
            sprite.getU((double)(ver[3] * 16.0F)),
            sprite.getV((double)(16.0F - ver[4] * 16.0F)),
            1.414F,
            0.0F,
            1.414F
         );
      }
   }

   public static float[][] getCrossHalfVertices(float x1, float y1, float z1, float x2, float y2, float z2, float u1, float v1, float u2, float v2) {
      return new float[][]{
         {x1, y1, z1, u1, v1},
         {x2, y1, z2, u2, v1},
         {x2, y2, z2, u2, v2},
         {x1, y2, z1, u1, v2},
         {x2, y1, z2, u2, v1},
         {x1, y1, z1, u1, v1},
         {x1, y2, z1, u1, v2},
         {x2, y2, z2, u2, v2}
      };
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
      buffer.vertex(poseStack.last().pose(), x, y, z)
         .color(1.0F, 1.0F, 1.0F, 1.0F)
         .uv(u, v)
         .uv2(packedLight)
         .overlayCoords(packedOverlay)
         .normal(poseStack.last().normal(), normalX, normalY, normalZ)
         .endVertex();
   }
}
