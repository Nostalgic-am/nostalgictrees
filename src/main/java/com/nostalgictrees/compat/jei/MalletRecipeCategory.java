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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class MalletRecipeCategory implements IRecipeCategory<MalletRecipe> {

    public static final RecipeType<MalletRecipe> RECIPE_TYPE =
            RecipeType.create(NostalgicTrees.MODID, "mallet_processing", MalletRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final Component title;

    public MalletRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(120, 36);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(NTItems.IRON_MALLET.get()));
        this.title = Component.translatable("gui.nostalgictrees.mallet_processing");
    }

    @Override
    public RecipeType<MalletRecipe> getRecipeType() {
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
    public void setRecipe(IRecipeLayoutBuilder builder, MalletRecipe recipe, IFocusGroup focuses) {
        // Input log on the left
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 9)
                .addItemStack(recipe.input());

        // All mallet tiers as catalyst in the middle
        builder.addSlot(RecipeIngredientRole.CATALYST, 50, 9)
                .addItemStack(new ItemStack(NTItems.WOODEN_MALLET.get()))
                .addItemStack(new ItemStack(NTItems.STONE_MALLET.get()))
                .addItemStack(new ItemStack(NTItems.IRON_MALLET.get()))
                .addItemStack(new ItemStack(NTItems.GOLDEN_MALLET.get()))
                .addItemStack(new ItemStack(NTItems.DIAMOND_MALLET.get()))
                .addItemStack(new ItemStack(NTItems.NETHERITE_MALLET.get()));

        // Output stripped log on the right
        builder.addSlot(RecipeIngredientRole.OUTPUT, 99, 9)
                .addItemStack(recipe.output());
    }
}
