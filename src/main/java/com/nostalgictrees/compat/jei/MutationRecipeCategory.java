package com.nostalgictrees.compat.jei;

import com.nostalgictrees.NTBlocks;
import com.nostalgictrees.NTItems;
import com.nostalgictrees.NostalgicTrees;
import com.nostalgictrees.recipe.MutationRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class MutationRecipeCategory implements IRecipeCategory<MutationRecipe> {

    public static final RecipeType<MutationRecipe> RECIPE_TYPE =
            RecipeType.create(NostalgicTrees.MODID, "mutation", MutationRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final Component title;

    public MutationRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(160, 50);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(Items.BEE_SPAWN_EGG));
        this.title = Component.translatable("gui.nostalgictrees.mutation");
    }

    @Override
    public RecipeType<MutationRecipe> getRecipeType() {
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
    public void setRecipe(IRecipeLayoutBuilder builder, MutationRecipe recipe, IFocusGroup focuses) {
        // Base sapling on the left
        ItemStack baseSaplingStack = getBlockItemStack(recipe.getBaseSapling());
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 17)
                .addItemStack(baseSaplingStack);

        // Honeycombs in the middle
        List<ResourceLocation> combs = recipe.getHoneycombs();
        for (int i = 0; i < combs.size(); i++) {
            ItemStack combStack = getItemStack(combs.get(i));
            builder.addSlot(RecipeIngredientRole.INPUT, 30 + i * 20, 17)
                    .addItemStack(combStack);
        }

        // Bee as catalyst
        builder.addSlot(RecipeIngredientRole.CATALYST, 95, 17)
                .addItemStack(new ItemStack(Items.BEE_SPAWN_EGG))
                .addTooltipCallback((recipeSlotView, tooltip) -> {
                    tooltip.clear();
                    tooltip.add(Component.literal("Requires " + recipe.getPollinationsRequired() + " Pollinations"));
                });

        // Result sapling on the right
        ItemStack resultStack = getBlockItemStack(recipe.getResultSapling());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 135, 17)
                .addItemStack(resultStack);
    }

    private ItemStack getBlockItemStack(ResourceLocation blockId) {
        Block block = BuiltInRegistries.BLOCK.get(blockId);
        if (block != null) {
            return new ItemStack(block.asItem());
        }
        return ItemStack.EMPTY;
    }

    private ItemStack getItemStack(ResourceLocation itemId) {
        var item = BuiltInRegistries.ITEM.get(itemId);
        if (item != null) {
            return new ItemStack(item);
        }
        return ItemStack.EMPTY;
    }
}
