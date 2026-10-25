package tfcflorae.client.screen.ceramics;
import net.minecraft.client.gui.GuiGraphics;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Consumer;
import net.dries007.tfc.client.screen.BlockEntityScreen;
import net.dries007.tfc.util.Helpers;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import tfcflorae.common.blockentities.ceramics.LargeVesselBlockEntity;
import tfcflorae.common.blocks.ceramics.LargeVesselBlock;
import tfcflorae.common.container.ceramics.LargeVesselContainer;

public class LargeVesselScreen extends BlockEntityScreen<LargeVesselBlockEntity, LargeVesselContainer> {
   private static final Component SEAL = Component.translatable("tfc.tooltip.seal_barrel");
   private static final Component UNSEAL = Component.translatable("tfc.tooltip.unseal_barrel");
   public static final ResourceLocation BACKGROUND = Helpers.identifier("textures/gui/large_vessel.png");

   public LargeVesselScreen(LargeVesselContainer container, Inventory playerInventory, Component name) {
      super(container, playerInventory, name, BACKGROUND);
   }

   public void init() {
      super.init();
      // 密封/解封按钮：1.20.1 的 TFC 已提供 VesselSealButton，直接复用。
      // 移植时此按钮被搁置，导致大陶罐无法进入/退出密封（腌制）状态。
      final boolean sealed = this.isSealed();
      this.addRenderableWidget(Button.builder(sealed ? UNSEAL : SEAL, btn -> {
         final LargeVesselBlockEntity vessel = (LargeVesselBlockEntity) this.blockEntity;
         if (sealed) {
            vessel.onUnseal();
         } else {
            vessel.onSeal();
         }
         this.rebuildWidgets();
      }).bounds(this.getGuiLeft() + 9, this.getGuiTop(), 60, 16).build());
   }

   protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderLabels(guiGraphics, mouseX, mouseY);
      if (this.isSealed()) {
         this.drawDisabled(guiGraphics, 0, 8);
      }
   }

   private boolean isSealed() {
      return (Boolean)((LargeVesselBlockEntity)this.blockEntity).getBlockState().getValue(LargeVesselBlock.SEALED);
   }
}
