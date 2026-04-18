package com.nostalgictrees.compat.jei;

import com.nostalgictrees.NostalgicTrees;
import com.nostalgictrees.recipe.MutationRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * JEI 29.5 migration notes:
 *   - mezz.jei.api.recipe.RecipeType is deprecated; use mezz.jei.api.recipe.types.IRecipeType
 *     and construct via IRecipeType.create(Identifier, Class).
 *   - IIngredientAcceptor#addItemStack(ItemStack) is deprecated; use add(ItemStack).
 */
public class MutationRecipeCategory implements IRecipeCategory<MutationRecipe> {

    public static final IRecipeType<MutationRecipe> RECIPE_TYPE = IRecipeType.create(
            Identifier.fromNamespaceAndPath(NostalgicTrees.MODID, "mutation"),
            MutationRecipe.class);

    private final IDrawable icon;
    private final Component title;

    public MutationRecipeCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(Items.BEE_SPAWN_EGG));
        this.title = Component.translatable("gui.nostalgictrees.mutation");
    }

    @Override
    public IRecipeType<MutationRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public int getWidth() {
        return 180;
    }

    @Override
    public int getHeight() {
        return 55;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MutationRecipe recipe, IFocusGroup focuses) {
        // Inputs on top row
        List<Identifier> combs = recipe.getHoneycombs();
        for (int i = 0; i < combs.size(); i++) {
            ItemStack combStack = getItemStack(combs.get(i));
            builder.addSlot(RecipeIngredientRole.INPUT, 1 + i * 18, 1)
                    .add(combStack);
        }

        // Catalyst next to combs on top row
        int nextX = 1 + combs.size() * 18;
        if (recipe.hasCatalyst()) {
            ItemStack catalystStack = getItemStack(recipe.getCatalyst());
            catalystStack.setCount(recipe.getCatalystCount());
            builder.addSlot(RecipeIngredientRole.INPUT, nextX, 1)
                    .add(catalystStack);
        }

        // Base sapling below inputs
        ItemStack baseSaplingStack = getBlockItemStack(recipe.getBaseSapling());
        builder.addSlot(RecipeIngredientRole.INPUT, 19, 33)
                .add(baseSaplingStack);

        // Bee with pollination count
        int pollinations = recipe.getPollinationsRequired();
        builder.addSlot(RecipeIngredientRole.CRAFTING_STATION, 100, 33)
                .add(new ItemStack(Items.BEE_SPAWN_EGG))
                .addRichTooltipCallback((recipeSlotView, tooltip) -> {
                    tooltip.add(Component.literal("Requires " + pollinations + " Pollinations"));
                });

        // Result sapling on the far right
        ItemStack resultStack = getBlockItemStack(recipe.getResultSapling());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 145, 33)
                .add(resultStack);
    }

    @Override
    public void draw(MutationRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        int color = 0xFF808080;

        // Down arrow from inputs to base sapling
        int arrowX = 24;
        int arrowY = 19;
        guiGraphics.fill(arrowX + 3, arrowY, arrowX + 5, arrowY + 10, color);
        guiGraphics.fill(arrowX, arrowY + 10, arrowX + 8, arrowY + 11, color);
        guiGraphics.fill(arrowX + 1, arrowY + 11, arrowX + 7, arrowY + 12, color);
        guiGraphics.fill(arrowX + 2, arrowY + 12, arrowX + 6, arrowY + 13, color);
        guiGraphics.fill(arrowX + 3, arrowY + 13, arrowX + 5, arrowY + 14, color);

        // Right arrow from sapling to bee
        int arrowRX = 42;
        int arrowRY = 39;
        guiGraphics.fill(arrowRX, arrowRY + 2, arrowRX + 54, arrowRY + 4, color);
        guiGraphics.fill(arrowRX + 54, arrowRY, arrowRX + 55, arrowRY + 6, color);
        guiGraphics.fill(arrowRX + 55, arrowRY + 1, arrowRX + 56, arrowRY + 5, color);
        guiGraphics.fill(arrowRX + 56, arrowRY + 2, arrowRX + 57, arrowRY + 4, color);

        // Right arrow from bee to result
        int arrowR2X = 120;
        int arrowR2Y = 39;
        guiGraphics.fill(arrowR2X, arrowR2Y + 2, arrowR2X + 21, arrowR2Y + 4, color);
        guiGraphics.fill(arrowR2X + 21, arrowR2Y, arrowR2X + 22, arrowR2Y + 6, color);
        guiGraphics.fill(arrowR2X + 22, arrowR2Y + 1, arrowR2X + 23, arrowR2Y + 5, color);
        guiGraphics.fill(arrowR2X + 23, arrowR2Y + 2, arrowR2X + 24, arrowR2Y + 4, color);
    }

    /**
     * 26.1: BuiltInRegistries.BLOCK.get(Identifier) returns Optional<Holder.Reference<Block>>.
     */
    private ItemStack getBlockItemStack(Identifier blockId) {
        return BuiltInRegistries.BLOCK.get(blockId)
                .map(holder -> new ItemStack(holder.value().asItem()))
                .orElse(ItemStack.EMPTY);
    }

    /**
     * 26.1: BuiltInRegistries.ITEM.get(Identifier) returns Optional<Holder.Reference<Item>>.
     */
    private ItemStack getItemStack(Identifier itemId) {
        return BuiltInRegistries.ITEM.get(itemId)
                .map(holder -> new ItemStack(holder.value()))
                .orElse(ItemStack.EMPTY);
    }
}