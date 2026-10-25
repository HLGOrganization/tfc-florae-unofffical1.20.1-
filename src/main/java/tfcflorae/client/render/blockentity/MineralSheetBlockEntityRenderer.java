package tfcflorae.client.render.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Function;
import net.dries007.tfc.common.blocks.DirectionPropertyBlock;
import net.dries007.tfc.util.Helpers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.util.RandomSource;
import tfcflorae.client.TFCFRenderHelpers;
import tfcflorae.common.blockentities.MineralSheetBlockEntity;
import tfcflorae.common.blocks.rock.Mineral;
import tfcflorae.common.blocks.rock.MineralSheetBlock;
import tfcflorae.util.TFCFHelpers;

public class MineralSheetBlockEntityRenderer implements BlockEntityRenderer<MineralSheetBlockEntity> {
   public MineralSheetBlockEntityRenderer() {
   }

   public RandomSource at(int posX, int posY, int posZ, int direction, BlockEntity entity) {
      long i = Mth.getSeed(posX + direction, posY + direction, posZ + direction);
      long j = i ^ (long)direction;
      if (entity.getLevel().getServer() instanceof WorldGenLevel level) {
         j = i ^ level.getSeed();
      }

      return new LegacyRandomSource(j);
   }

   public void render(MineralSheetBlockEntity pile, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
      BlockState state = pile.getBlockState();
      if (state.getBlock() instanceof DirectionPropertyBlock) {
         Function<ResourceLocation, TextureAtlasSprite> textureAtlas = Minecraft.getInstance().getTextureAtlas(TFCFRenderHelpers.BLOCKS_ATLAS);
         VertexConsumer builder = buffer.getBuffer(RenderType.cutout());

         for (Direction direction : Helpers.DIRECTIONS) {
            if ((Boolean)state.getValue(DirectionPropertyBlock.getProperty(direction))) {
               RandomSource forkedRandom = this.at(
                  pile.getBlockPos().getX(), pile.getBlockPos().getY(), pile.getBlockPos().getZ(), direction.ordinal(), pile
               );
               Mineral mineral = pile.getOrCacheMineral(direction);
               String mineralName = pile.mineralNameRandom(mineral, forkedRandom);
               ResourceLocation textureId = TFCFHelpers.identifier(mineralName);
               TextureAtlasSprite sprite = textureAtlas.apply(textureId);
               this.renderSheet(poseStack, sprite, builder, direction, packedLight, packedOverlay);
            }
         }
      }
   }

   private void renderSheet(PoseStack poseStack, TextureAtlasSprite sprite, VertexConsumer buffer, Direction direction, int packedLight, int packedOverlay) {
      TFCFRenderHelpers.renderTexturedCuboid(
         poseStack, buffer, sprite, packedLight, packedOverlay, MineralSheetBlock.getShapeForSingleFace(direction).bounds()
      );
   }
}
