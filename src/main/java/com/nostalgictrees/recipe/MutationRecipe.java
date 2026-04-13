package com.nostalgictrees.recipe;

import com.nostalgictrees.NTRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * A mutation recipe. Not used in a crafting container — instead looked up
 * manually by ResourceSaplingBlockEntity when honeycombs are applied.
 *
 * Optionally includes a catalyst item (e.g. diamonds) that must be right-clicked
 * onto the sapling after all honeycombs are applied, before bees can pollinate.
 */
public class MutationRecipe implements Recipe<net.minecraft.world.item.crafting.SingleRecipeInput> {

    private final ResourceLocation baseSapling;
    private final List<ResourceLocation> honeycombs;
    private final ResourceLocation resultSapling;
    private final int pollinationsRequired;
    @Nullable
    private final ResourceLocation catalyst;
    private final int catalystCount;

    public MutationRecipe(ResourceLocation baseSapling, List<ResourceLocation> honeycombs,
                          ResourceLocation resultSapling, int pollinationsRequired,
                          @Nullable ResourceLocation catalyst, int catalystCount) {
        this.baseSapling = baseSapling;
        this.honeycombs = honeycombs;
        this.resultSapling = resultSapling;
        this.pollinationsRequired = pollinationsRequired;
        this.catalyst = catalyst;
        this.catalystCount = catalystCount;
    }

    // Backwards-compatible constructor (no catalyst)
    public MutationRecipe(ResourceLocation baseSapling, List<ResourceLocation> honeycombs,
                          ResourceLocation resultSapling, int pollinationsRequired) {
        this(baseSapling, honeycombs, resultSapling, pollinationsRequired, null, 0);
    }

    public ResourceLocation getBaseSapling() { return baseSapling; }
    public List<ResourceLocation> getHoneycombs() { return honeycombs; }
    public ResourceLocation getResultSapling() { return resultSapling; }
    public int getPollinationsRequired() { return pollinationsRequired; }
    @Nullable
    public ResourceLocation getCatalyst() { return catalyst; }
    public int getCatalystCount() { return catalystCount; }
    public boolean hasCatalyst() { return catalyst != null && catalystCount > 0; }

    // === Recipe interface (mostly unused — we look up recipes manually) ===

    @Override
    public boolean matches(net.minecraft.world.item.crafting.SingleRecipeInput input, Level level) {
        return false;
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
