package com.nostalgictrees.client;

import com.nostalgictrees.NostalgicTrees;
import com.nostalgictrees.menu.AdvancedBeehiveMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class AdvancedBeehiveScreen extends AbstractContainerScreen<AdvancedBeehiveMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(NostalgicTrees.MODID, "textures/gui/advanced_beehive.png");

    // Hex cell centers from the texture (where bee icons render)
    // Bee icon is 16x16, so render at (center - 8, center - 8)
    private static final int[][] BEE_POSITIONS = {
            {18 - 8, 56 - 8},   // 0: top-left
            {40 - 8, 56 - 8},   // 1: top-center
            {62 - 8, 56 - 8},   // 2: top-right
            {29 - 8, 75 - 8},   // 3: bottom-left
            {51 - 8, 75 - 8},   // 4: bottom-right
    };

    public AdvancedBeehiveScreen(AdvancedBeehiveMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 174;
        this.imageHeight = 222;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // Draw main background (full panel from texture)
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        // Draw bee indicators in hex cells
        int beeCount = this.menu.getBeeCount();
        ResourceLocation beeTexture = ResourceLocation.withDefaultNamespace("textures/entity/bee/bee.png");
        for (int i = 0; i < BEE_POSITIONS.length; i++) {
            if (i < beeCount) {
                int bx = x + BEE_POSITIONS[i][0];
                int by = y + BEE_POSITIONS[i][1];
                guiGraphics.blit(beeTexture, bx + 1, by + 2, 14, 14, 10, 10, 7, 7, 64, 64);
            }
        }
    }
    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Text is baked into the texture
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // Honey level tooltip over the area between tool slots
        if (mouseX >= x + 106 && mouseX <= x + 162 && mouseY >= y + 10 && mouseY <= y + 39) {
            guiGraphics.renderTooltip(this.font,
                    Component.literal("Honey: " + this.menu.getHoneyLevel() + " / 5"),
                    mouseX, mouseY);
        }

        // Bee count tooltip over honeycomb area
        if (mouseX >= x + 5 && mouseX <= x + 75 && mouseY >= y + 40 && mouseY <= y + 92) {
            guiGraphics.renderTooltip(this.font,
                    Component.literal("Bees: " + this.menu.getBeeCount() + " / 5"),
                    mouseX, mouseY);
        }
    }
}
