package com.nostalgictrees.compat.jei;

import com.nostalgictrees.NTItems;
import com.nostalgictrees.NostalgicTrees;
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

public class MalletRecipeCategory implements IRecipeCategory<MalletRecipe> {

    public static final IRecipeType<MalletRecipe> RECIPE_TYPE = IRecipeType.create(
            Identifier.fromNamespaceAndPath(NostalgicTrees.MODID, "mallet_processing"),
            MalletRecipe.class);

    private final IDrawable icon;
    private final Component title;

    public MalletRecipeCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(NTItems.IRON_MALLET.get()));
        this.title = Component.translatable("gui.nostalgictrees.mallet_processing");
    }

    @Override
    public IRecipeType<MalletRecipe> getRecipeType() {
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
    public void setRecipe(IRecipeLayoutBuilder builder, MalletRecipe recipe, IFocusGroup focuses) {
        // Input log on the left
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 9)
                .add(recipe.input());

        // All mallet tiers as crafting station in the middle
        builder.addSlot(RecipeIngredientRole.CRAFTING_STATION, 50, 9)
                .add(new ItemStack(NTItems.WOODEN_MALLET.get()))
                .add(new ItemStack(NTItems.STONE_MALLET.get()))
                .add(new ItemStack(NTItems.IRON_MALLET.get()))
                .add(new ItemStack(NTItems.GOLDEN_MALLET.get()))
                .add(new ItemStack(NTItems.DIAMOND_MALLET.get()))
                .add(new ItemStack(NTItems.NETHERITE_MALLET.get()));

        // Output stripped log on the right
        builder.addSlot(RecipeIngredientRole.OUTPUT, 99, 9)
                .add(recipe.output());
    }
}