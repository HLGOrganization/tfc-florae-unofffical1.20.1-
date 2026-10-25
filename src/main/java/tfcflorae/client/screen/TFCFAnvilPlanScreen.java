package tfcflorae.client.screen;
import net.minecraft.client.gui.GuiGraphics;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.List;
import net.dries007.tfc.client.RenderHelpers;
import net.dries007.tfc.client.screen.BlockEntityScreen;
import net.dries007.tfc.client.screen.button.AnvilPlanSelectButton;
import net.dries007.tfc.client.screen.button.NextPageButton;
import net.dries007.tfc.common.capabilities.Capabilities;
import net.dries007.tfc.common.recipes.AnvilRecipe;
import net.dries007.tfc.util.Helpers;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import tfcflorae.common.blockentities.TFCFAnvilBlockEntity;
import tfcflorae.common.container.TFCFAnvilPlanContainer;

public class TFCFAnvilPlanScreen extends BlockEntityScreen<TFCFAnvilBlockEntity, TFCFAnvilPlanContainer> {
   public static final ResourceLocation BACKGROUND = Helpers.identifier("textures/gui/anvil_plan.png");
   @Nullable
   private Button leftButton;
   @Nullable
   private Button rightButton;
   @Nullable
   private List<AnvilPlanSelectButton> recipeButtons;
   private int maxPageInclusive;
   private int currentPage = 0;

   public TFCFAnvilPlanScreen(TFCFAnvilPlanContainer container, Inventory playerInventory, Component name) {
      super(container, playerInventory, name, BACKGROUND);
      this.maxPageInclusive = 0;
   }

   protected void init() {
      super.init();
      int recipesPerPage = 18;
      int guiLeft = this.getGuiLeft();
      int guiTop = this.getGuiTop();
      ItemStack inputStack = ((TFCFAnvilBlockEntity)this.blockEntity)
         .getCapability(Capabilities.ITEM, null)
         .map(t -> t.getStackInSlot(0))
         .orElse(ItemStack.EMPTY);
      List<AnvilRecipe> recipes = AnvilRecipe.getAll(this.playerInventory.player.level(), inputStack, ((TFCFAnvilBlockEntity)this.blockEntity).getTier());
      this.recipeButtons = new ArrayList<>();

      for (int i = 0; i < recipes.size(); i++) {
         int page = i / 18;
         int index = i % 18;
         int posX = 7 + index % 9 * 18;
         int posY = 17 + index % 18 / 9 * 18;
         AnvilRecipe recipe = recipes.get(i);
         AnvilPlanSelectButton button = new AnvilPlanSelectButton(
            guiLeft + posX, guiTop + posY, page, recipe, Component.empty());
         button.setCurrentPage(0);
         this.recipeButtons.add(button);
         this.addRenderableWidget(button);
      }

      this.maxPageInclusive = (recipes.size() - 1) / 18;
      this.addRenderableWidget(this.leftButton = NextPageButton.left(guiLeft + 7, guiTop + 56, buttonx -> {
         if (this.currentPage > 0) {
            this.currentPage--;
            this.updateCurrentPage();
         }
      }));
      this.addRenderableWidget(this.rightButton = NextPageButton.right(guiLeft + 160, guiTop + 56, buttonx -> {
         if (this.currentPage < this.maxPageInclusive) {
            this.currentPage++;
            this.updateCurrentPage();
         }
      }));
      this.updateCurrentPage();
   }

   protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
      // 1.20.1 handles the background through renderBackground(guiGraphics, ...).
   }

   protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
      super.renderTooltip(guiGraphics, mouseX, mouseY);

      for (Renderable widget : this.renderables) {
         if (widget instanceof AnvilPlanSelectButton button && button.isHoveredOrFocused()) {
            return;
         }
      }
   }

   private void updateCurrentPage() {
      assert this.recipeButtons != null && this.leftButton != null && this.rightButton != null;

      for (AnvilPlanSelectButton button : this.recipeButtons) {
         button.setCurrentPage(this.currentPage);
      }

      this.leftButton.active = this.leftButton.visible = this.currentPage > 0;
      this.rightButton.active = this.rightButton.visible = this.currentPage < this.maxPageInclusive;
   }
}
