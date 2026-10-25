package tfcflorae.client.screen;
import net.minecraft.client.gui.GuiGraphics;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.client.screen.BlockEntityScreen;
import net.dries007.tfc.client.screen.button.AnvilPlanButton;
import net.dries007.tfc.client.screen.button.AnvilStepButton;
import net.dries007.tfc.common.blockentities.AnvilBlockEntity;
import net.dries007.tfc.common.capabilities.forge.ForgeRule;
import net.dries007.tfc.common.capabilities.forge.ForgeStep;
import net.dries007.tfc.common.capabilities.forge.ForgeSteps;
import net.dries007.tfc.common.capabilities.forge.Forging;
import net.dries007.tfc.common.recipes.AnvilRecipe;
import net.dries007.tfc.util.Helpers;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.Level;
import tfcflorae.common.blockentities.TFCFAnvilBlockEntity;
import tfcflorae.common.container.TFCFAnvilContainer;

public class TFCFAnvilScreen extends BlockEntityScreen<TFCFAnvilBlockEntity, TFCFAnvilContainer> {
   public static final ResourceLocation BACKGROUND = Helpers.identifier("textures/gui/anvil.png");

   public TFCFAnvilScreen(TFCFAnvilContainer container, Inventory playerInventory, Component name) {
      super(container, playerInventory, name, BACKGROUND);
      this.inventoryLabelY += 41;
      this.imageHeight += 41;
   }

   protected void init() {
      super.init();
      this.addRenderableWidget(
         new AnvilPlanButton(
            (AnvilBlockEntity)this.blockEntity,
            this.getGuiLeft(),
            this.getGuiTop())
      );

      for (ForgeStep step : ForgeStep.VALUES) {
         this.addRenderableWidget(new AnvilStepButton(step, this.getGuiLeft(), this.getGuiTop()));
      }
   }

   protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
      super.renderBg(guiGraphics, partialTicks, mouseX, mouseY);
      Level level = ((TFCFAnvilBlockEntity)this.blockEntity).getLevel();
      int guiLeft = this.getGuiLeft();
      int guiTop = this.getGuiTop();

      assert level != null;

      Forging forging = ((TFCFAnvilBlockEntity)this.blockEntity).getMainInputForging();
      if (forging != null) {
         int progress = forging.getWork();
         guiGraphics.blit(BACKGROUND, guiLeft + 13 + progress, guiTop + 100, 176, 0, 5, 5);
         int target = forging.getWorkTarget();
         guiGraphics.blit(BACKGROUND, guiLeft + 13 + target, guiTop + 94, 181, 0, 5, 5);
         ForgeSteps steps = forging.getSteps();
         AnvilRecipe recipe = forging.getRecipe(level);
         if (recipe != null) {
            ForgeRule[] rules = recipe.getRules();

            for (int i = 0; i < rules.length; i++) {
               ForgeRule rule = rules[i];
               if (rule != null) {
                  int xOffset = i * 19;
                  guiGraphics.blit(BACKGROUND, guiLeft + 64 + xOffset, guiTop + 10, 10, 10, (float)rule.iconX(), (float)rule.iconY(), 32, 32, 256, 256);
                  if (rule.matches(steps)) {
                     RenderSystem.setShaderColor(0.0F, 0.6F, 0.2F, 1.0F);
                  } else {
                     RenderSystem.setShaderColor(1.0F, 0.4F, 0.0F, 1.0F);
                  }

                  guiGraphics.blit(BACKGROUND, guiLeft + 59 + xOffset, guiTop + 7, 198, rule.overlayY(), 20, 22);
                  RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
               }
            }
         }

         ForgeStep[] stepSequence = new ForgeStep[]{steps.last(), steps.secondLast(), steps.thirdLast()};

         for (int ix = 0; ix < 3; ix++) {
            ForgeStep step = stepSequence[ix];
            if (step != null) {
               int xOffset = ix * 19;
               guiGraphics.blit(BACKGROUND, guiLeft + 64 + xOffset, guiTop + 31, 10, 10, (float)step.iconX(), (float)step.iconY(), 32, 32, 256, 256);
            }
         }
      }
   }

   protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderTooltip(guiGraphics, mouseX, mouseY);

      for (Renderable widget : this.renderables) {
         if (widget instanceof Button button && button.isHoveredOrFocused()) {
            return;
         }
      }

      Level level = ((TFCFAnvilBlockEntity)this.blockEntity).getLevel();
      Forging forging = ((TFCFAnvilBlockEntity)this.blockEntity).getMainInputForging();
      if (forging != null && level != null) {
         AnvilRecipe recipe = forging.getRecipe(level);
         if (recipe != null) {
            ForgeRule[] rules = recipe.getRules();

            for (int i = 0; i < rules.length; i++) {
               ForgeRule rule = rules[i];
               if (rule != null) {
                  int xOffset = i * 19;
                  int x = this.getGuiLeft() + 64 + xOffset;
                  int y = this.getGuiTop() + 10;
                  if (mouseX > x && mouseX < x + 10 && mouseY > y && mouseY < y + 10) {
                     guiGraphics.renderTooltip(this.font, rule.getDescriptionId(), mouseX, mouseY);
                  }
               }
            }
         }
      }
   }
}
