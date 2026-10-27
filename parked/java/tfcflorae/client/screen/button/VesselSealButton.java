package tfcflorae.client.screen.button;
import net.minecraft.client.gui.GuiGraphics;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.dries007.tfc.network.PacketHandler;
import net.dries007.tfc.network.ScreenButtonPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.PacketDistributor;
import tfcflorae.client.screen.ceramics.LargeVesselScreen;
import tfcflorae.common.blockentities.ceramics.LargeVesselBlockEntity;
import tfcflorae.common.blocks.ceramics.LargeVesselBlock;

public class VesselSealButton extends Button {
   private final LargeVesselBlockEntity vessel;

   public VesselSealButton(LargeVesselBlockEntity barrel, int guiLeft, int guiTop, java.util.function.Supplier<net.minecraft.network.chat.Component> onTooltip) {
      super(guiLeft + 123, guiTop + 35, 20, 20, Component.EMPTY, b -> {
      }, net.dries007.tfc.client.RenderHelpers.NARRATION);
      this.setTooltip(net.minecraft.client.gui.components.Tooltip.create(onTooltip.get()));
      this.vessel = barrel;
   }

   public void onPress() {
      PacketHandler.send(PacketDistributor.SERVER.noArg(), new ScreenButtonPacket(0, null));
      this.playDownSound(Minecraft.getInstance().getSoundManager());
   }

   public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      RenderSystem.setShaderTexture(0, LargeVesselScreen.BACKGROUND);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      int v = this.vessel.getBlockState().getValue(LargeVesselBlock.SEALED) ? 0 : 20;
      guiGraphics.blit(LargeVesselScreen.BACKGROUND, this.getX(), this.getY(), 236.0F, (float)v, 20, 20, 256, 256);
      if (this.isHoveredOrFocused()) {
      }
   }
}
