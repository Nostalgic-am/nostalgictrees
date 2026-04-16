package com.nostalgictrees.recipe;

import com.nostalgictrees.NTRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

public class DryingRecipe implements Recipe<SingleRecipeInput> {

    private final ResourceLocation inputItem;
    private final ResourceLocation outputItem;
    private final int outputCount;
    private final int dryingTime;

    public DryingRecipe(ResourceLocation inputItem, ResourceLocation outputItem, int outputCount, int dryingTime) {
        this.inputItem = inputItem;
        this.outputItem = outputItem;
        this.outputCount = outputCount;
        this.dryingTime = dryingTime;
    }

    public ResourceLocation getInputItem() { return inputItem; }
    public ResourceLocation getOutputItem() { return outputItem; }
    public int getOutputCount() { return outputCount; }
    public int getDryingTime() { return dryingTime; }

    public ItemStack getInputStack() {
        var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(inputItem);
        return item != null ? new ItemStack(item) : ItemStack.EMPTY;
    }

    public ItemStack getOutputStack() {
        var item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(outputItem);
        return item != null ? new ItemStack(item, outputCount) : ItemStack.EMPTY;
    }

    // === Recipe interface ===

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return false; // Not used in standard crafting
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return getOutputStack();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return getOutputStack();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return NTRecipes.DRYING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return NTRecipes.DRYING_TYPE.get();
    }
}
