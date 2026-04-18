package com.nostalgictrees.client;

import com.nostalgictrees.NostalgicTrees;
import com.nostalgictrees.menu.AdvancedBeehiveMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class AdvancedBeehiveScreen extends AbstractContainerScreen<AdvancedBeehiveMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(NostalgicTrees.MODID, "textures/gui/advanced_beehive.png");
    private static final Identifier BEE_TEXTURE =
            Identifier.withDefaultNamespace("textures/entity/bee/bee.png");

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
        super(menu, playerInventory, title, 174, 222);
    }

    /*
     * 26.1 GUI overhaul:
     *   - renderBg(...)     -> extractBackground(...)   (submit background draws)
     *   - renderLabels(...) -> extractLabels(...)        (default renders titleLabel + inventoryLabel)
     *   - GuiGraphics#drawString -> GuiGraphicsExtractor#text
     *   - Tooltips set via setTooltipForNextFrame()
     */

    @Override
    public void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        // The background texture bakes "Advanced Beehive" into the top-left, so the default
        // extractLabels behaviour (which draws this.title there) would produce duplicate text.
        // We render only the "Inventory" label that vanilla normally draws.
        //
        // If the baked-in title ever gets removed from the PNG, delete this override and the
        // default implementation will handle both labels correctly.
        guiGraphics.text(this.font, this.playerInventoryTitle,
                this.inventoryLabelX, this.inventoryLabelY, 4210752, false);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);

        int x = this.leftPos;
        int y = this.topPos;

        // Draw main background - 256x256 texture, full panel region starting at (0, 0)
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE,
                x, y,
                0.0f, 0.0f,
                this.imageWidth, this.imageHeight,
                256, 256);

        // Draw bee indicators in hex cells.
        // Source region: 10x7 starting at (14, 14) on the 64x64 bee entity texture.
        int beeCount = this.menu.getBeeCount();
        for (int i = 0; i < Math.min(beeCount, BEE_POSITIONS.length); i++) {
            int bx = x + BEE_POSITIONS[i][0];
            int by = y + BEE_POSITIONS[i][1];
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BEE_TEXTURE,
                    bx + 1, by + 2,
                    10.0f, 10.0f,    // UV offset (was wrongly "14, 14" before)
                    14, 14,          // display size on screen
                    7, 7,            // source sample size
                    64, 64);         // texture dimensions
        }

        // Tooltips — now scheduled via setTooltipForNextFrame.
        // Honey level tooltip (between tool slots).
        if (mouseX >= x + 106 && mouseX <= x + 162 && mouseY >= y + 10 && mouseY <= y + 39) {
            guiGraphics.setTooltipForNextFrame(this.font,
                    Component.literal("Honey: " + this.menu.getHoneyLevel() + " / 5"),
                    mouseX, mouseY);
        }

        // Bee count tooltip (over honeycomb area).
        if (mouseX >= x + 5 && mouseX <= x + 75 && mouseY >= y + 40 && mouseY <= y + 92) {
            guiGraphics.setTooltipForNextFrame(this.font,
                    Component.literal("Bees: " + this.menu.getBeeCount() + " / 5"),
                    mouseX, mouseY);
        }
    }
}