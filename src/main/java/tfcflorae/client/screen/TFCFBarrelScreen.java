package tfcflorae.client.screen;
import net.minecraft.client.gui.GuiGraphics;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Consumer;
import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.client.screen.BlockEntityScreen;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.recipes.BarrelRecipe;
import net.dries007.tfc.config.TFCConfig;
import net.dries007.tfc.util.Helpers;
import net.dries007.tfc.util.Tooltips;
import net.dries007.tfc.util.calendar.Calendars;
import net.dries007.tfc.util.calendar.ICalendar;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.fluids.FluidStack;
import tfcflorae.client.screen.button.TFCFBarrelSealButton;
import tfcflorae.common.blockentities.TFCFBarrelBlockEntity;
import tfcflorae.common.blocks.devices.TFCFBarrelBlock;
import tfcflorae.common.container.TFCFBarrelContainer;

public class TFCFBarrelScreen extends BlockEntityScreen<TFCFBarrelBlockEntity, TFCFBarrelContainer> {
   private static final Component SEAL = Component.translatable("tfc.tooltip.seal_barrel");
   private static final Component UNSEAL = Component.translatable("tfc.tooltip.unseal_barrel");
   private static final int MAX_RECIPE_NAME_LENGTH = 100;
   public static final ResourceLocation BACKGROUND = Helpers.identifier("textures/gui/barrel.png");

   public TFCFBarrelScreen(TFCFBarrelContainer container, Inventory playerInventory, Component name) {
      super(container, playerInventory, name, BACKGROUND);
      this.inventoryLabelY += 12;
      this.imageHeight += 12;
   }

   public void init() {
      super.init();
      this.addRenderableWidget(new TFCFBarrelSealButton((TFCFBarrelBlockEntity)this.blockEntity, this.getGuiLeft(), this.getGuiTop(), () -> TFCFBarrelScreen.this.isSealed() ? TFCFBarrelScreen.UNSEAL : TFCFBarrelScreen.SEAL));
   }

   protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderLabels(guiGraphics, mouseX, mouseY);
      if (this.isSealed()) {
         this.drawDisabled(guiGraphics, 0, 2);
         BarrelRecipe recipe = ((TFCFBarrelBlockEntity)this.blockEntity).getRecipe();
         if (recipe != null) {
            FormattedText resultText = recipe.getTranslationComponent();
            if (this.font.width(resultText) > 100) {
               int line = 0;

               for (FormattedCharSequence text : this.font.split(resultText, 100)) {
                  guiGraphics.drawString(this.font, text, 70 + Math.floorDiv(100 - this.font.width(text), 2), this.titleLabelY + line * 9, 4210752, false);
                  line++;
               }
            } else {
               guiGraphics.drawString(this.font, resultText.getString(), 70 + Math.floorDiv(100 - this.font.width(resultText), 2), 61, 4210752, false);
            }
         }

         String date = ICalendar.getTimeAndDate(
               Calendars.CLIENT.ticksToCalendarTicks(((TFCFBarrelBlockEntity)this.blockEntity).getSealedTick()),
               (long)Calendars.CLIENT.getCalendarDaysInMonth()
            )
            .getString();
         guiGraphics.drawString(this.font, date, this.imageWidth / 2 - this.font.width(date) / 2, 74, 4210752, false);
      }
   }

   protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
      super.renderBg(guiGraphics, partialTicks, mouseX, mouseY);
      ((TFCFBarrelBlockEntity)this.blockEntity)
         .getCapability(Capabilities.FLUID)
         .ifPresent(
            fluidHandler -> {
               FluidStack fluidStack = fluidHandler.getFluidInTank(0);
               if (!fluidStack.isEmpty()) {
                  TextureAtlasSprite sprite = RenderHelpers.getAndBindFluidSprite(fluidStack);
                  int fillHeight = (int)Math.ceil(
                     (double)(50.0F * (float)fluidStack.getAmount() / (float)((Integer)TFCConfig.SERVER.barrelCapacity.get()).intValue())
                  );
                  RenderHelpers.fillAreaWithSprite(guiGraphics, sprite, this.leftPos + 8, this.topPos + 70 - fillHeight, 16, fillHeight, 16, 16);
                  this.resetToBackgroundSprite();
               }
            }
         );
      guiGraphics.blit(BACKGROUND, this.getGuiLeft() + 7, this.getGuiTop() + 19, 176, 0, 18, 52);
   }

   protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderTooltip(guiGraphics, mouseX, mouseY);
      int relX = mouseX - this.getGuiLeft();
      int relY = mouseY - this.getGuiTop();
      if (relX >= 7 && relY >= 19 && relX < 25 && relY < 71) {
         ((TFCFBarrelBlockEntity)this.blockEntity).getCapability(Capabilities.FLUID).ifPresent(fluidHandler -> {
            FluidStack fluid = fluidHandler.getFluidInTank(0);
            if (!fluid.isEmpty()) {
               guiGraphics.renderTooltip(this.font, Tooltips.fluidUnitsOf(fluid), mouseX, mouseY);
            }
         });
      }
   }

   private boolean isSealed() {
      return (Boolean)((TFCFBarrelBlockEntity)this.blockEntity).getBlockState().getValue(TFCFBarrelBlock.SEALED);
   }
}
