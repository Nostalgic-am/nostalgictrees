package com.nostalgictrees.compat.jei;

import com.nostalgictrees.NTItems;
import com.nostalgictrees.NostalgicTrees;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class DryingRackRecipeCategory implements IRecipeCategory<DryingRackRecipe> {

    public static final RecipeType<DryingRackRecipe> RECIPE_TYPE =
            RecipeType.create(NostalgicTrees.MODID, "drying_rack", DryingRackRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final Component title;

    public DryingRackRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(120, 36);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(NTItems.DRYING_RACK_ITEM.get()));
        this.title = Component.translatable("gui.nostalgictrees.drying_rack");
    }

    @Override
    public RecipeType<DryingRackRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, DryingRackRecipe recipe, IFocusGroup focuses) {
        // Input on the left
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 9)
                .addItemStack(recipe.input());

        // Drying rack as catalyst in the middle
        builder.addSlot(RecipeIngredientRole.CATALYST, 50, 9)
                .addItemStack(new ItemStack(NTItems.DRYING_RACK_ITEM.get()));

        // Output on the right
        builder.addSlot(RecipeIngredientRole.OUTPUT, 99, 9)
                .addItemStack(recipe.output());
    }
}
