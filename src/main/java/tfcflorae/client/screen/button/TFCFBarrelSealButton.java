package tfcflorae.client.screen.button;
import net.minecraft.client.gui.GuiGraphics;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.dries007.tfc.client.screen.button.BarrelSealButton;
import net.dries007.tfc.network.PacketHandler;
import net.dries007.tfc.network.ScreenButtonPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraftforge.network.PacketDistributor;
import tfcflorae.client.screen.TFCFBarrelScreen;
import tfcflorae.common.blockentities.TFCFBarrelBlockEntity;
import tfcflorae.common.blocks.devices.TFCFBarrelBlock;

public class TFCFBarrelSealButton extends BarrelSealButton {
   private final TFCFBarrelBlockEntity barrel;

   public TFCFBarrelSealButton(TFCFBarrelBlockEntity barrel, int guiLeft, int guiTop, java.util.function.Supplier<net.minecraft.network.chat.Component> onTooltip) {
      super(barrel, guiLeft, guiTop, onTooltip.get());
      this.barrel = barrel;
   }

   public void onPress() {
      PacketHandler.send(PacketDistributor.SERVER.noArg(), new ScreenButtonPacket(0, null));
      this.playDownSound(Minecraft.getInstance().getSoundManager());
   }

   public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
      RenderSystem.setShader(GameRenderer::getPositionTexShader);
      RenderSystem.setShaderTexture(0, TFCFBarrelScreen.BACKGROUND);
      RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
      int v = this.barrel.getBlockState().getValue(TFCFBarrelBlock.SEALED) ? 0 : 20;
      guiGraphics.blit(TFCFBarrelScreen.BACKGROUND, this.getX(), this.getY(), 236.0F, (float)v, 20, 20, 256, 256);
      if (this.isHoveredOrFocused()) {
      }
   }
}
