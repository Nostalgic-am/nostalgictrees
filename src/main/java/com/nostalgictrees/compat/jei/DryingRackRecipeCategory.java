package com.nostalgictrees.compat.jei;

import com.nostalgictrees.NTItems;
import com.nostalgictrees.NostalgicTrees;
import com.nostalgictrees.recipe.DryingRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class DryingRackRecipeCategory implements IRecipeCategory<DryingRecipe> {

    public static final IRecipeType<DryingRecipe> RECIPE_TYPE = IRecipeType.create(
            Identifier.fromNamespaceAndPath(NostalgicTrees.MODID, "drying_rack"),
            DryingRecipe.class);

    private final IDrawable icon;
    private final Component title;

    public DryingRackRecipeCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(NTItems.DRYING_RACK_ITEM.get()));
        this.title = Component.translatable("gui.nostalgictrees.drying_rack");
    }

    @Override
    public IRecipeType<DryingRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public int getWidth() {
        return 120;
    }

    @Override
    public int getHeight() {
        return 36;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, DryingRecipe recipe, IFocusGroup focuses) {
        // Input on the left
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 9)
                .add(recipe.getInputStack());

        // Drying rack as catalyst in the middle
        builder.addSlot(RecipeIngredientRole.CRAFTING_STATION, 50, 9)
                .add(new ItemStack(NTItems.DRYING_RACK_ITEM.get()))
                .addRichTooltipCallback((recipeSlotView, tooltip) -> {
                    tooltip.add(Component.literal(String.format("%.1f seconds", recipe.getDryingTime() / 20.0)));
                });

        // Output on the right
        builder.addSlot(RecipeIngredientRole.OUTPUT, 99, 9)
                .add(recipe.getOutputStack());
    }
}