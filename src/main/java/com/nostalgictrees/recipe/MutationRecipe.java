package com.nostalgictrees.recipe;

import com.nostalgictrees.NTRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * A mutation recipe. Not used in a crafting container — instead looked up
 * manually by ResourceSaplingBlockEntity when honeycombs are applied.
 *
 * Uses SingleRecipeInput as a dummy since we don't use standard crafting.
 */
public class MutationRecipe implements Recipe<net.minecraft.world.item.crafting.SingleRecipeInput> {

    private final ResourceLocation baseSapling;
    private final List<ResourceLocation> honeycombs;
    private final ResourceLocation resultSapling;
    private final int pollinationsRequired;

    public MutationRecipe(ResourceLocation baseSapling, List<ResourceLocation> honeycombs,
                          ResourceLocation resultSapling, int pollinationsRequired) {
        this.baseSapling = baseSapling;
        this.honeycombs = honeycombs;
        this.resultSapling = resultSapling;
        this.pollinationsRequired = pollinationsRequired;
    }

    public ResourceLocation getBaseSapling() { return baseSapling; }
    public List<ResourceLocation> getHoneycombs() { return honeycombs; }
    public ResourceLocation getResultSapling() { return resultSapling; }
    public int getPollinationsRequired() { return pollinationsRequired; }

    // === Recipe interface (mostly unused — we look up recipes manually) ===

    @Override
    public boolean matches(net.minecraft.world.item.crafting.SingleRecipeInput input, Level level) {
        return false; // Not used in crafting
    }

    @Override
    public ItemStack assemble(net.minecraft.world.item.crafting.SingleRecipeInput input, HolderLookup.Provider registries) {
        return getResultItem(registries);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return NTRecipes.MUTATION_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return NTRecipes.MUTATION_TYPE.get();
    }
}
