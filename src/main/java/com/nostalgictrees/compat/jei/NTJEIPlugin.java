package com.nostalgictrees.compat.jei;

import com.nostalgictrees.NTBlocks;
import com.nostalgictrees.NTRecipes;
import com.nostalgictrees.NostalgicTrees;
import com.nostalgictrees.data.NTTreeRegistry;
import com.nostalgictrees.data.ResourceTreeType;
import com.nostalgictrees.recipe.DryingRecipe;
import com.nostalgictrees.recipe.MutationRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class NTJEIPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(NostalgicTrees.MODID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new MalletRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new DryingRackRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new MutationRecipeCategory(registration.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // Mallet recipes (still manual — could be a recipe type later)
        List<MalletRecipe> malletRecipes = new ArrayList<>();
        for (ResourceTreeType tree : NTTreeRegistry.getAllTrees()) {
            Block log = NTBlocks.getLogBlock(tree.name());
            Block strippedLog = NTBlocks.getStrippedLogBlock(tree.name());

            if (log != null && strippedLog != null) {
                malletRecipes.add(new MalletRecipe(
                        new ItemStack(log),
                        new ItemStack(strippedLog),
                        tree.name()
                ));
            }
        }
        registration.addRecipes(MalletRecipeCategory.RECIPE_TYPE, malletRecipes);

        // Drying rack recipes (from recipe manager)
        var level = Minecraft.getInstance().level;
        if (level != null) {
            List<DryingRecipe> dryingRecipes = new ArrayList<>();
            for (RecipeHolder<DryingRecipe> holder : level.getRecipeManager()
                    .getAllRecipesFor(NTRecipes.DRYING_TYPE.get())) {
                dryingRecipes.add(holder.value());
            }
            registration.addRecipes(DryingRackRecipeCategory.RECIPE_TYPE, dryingRecipes);

            // Mutation recipes (from recipe manager)
            List<MutationRecipe> mutationRecipes = new ArrayList<>();
            for (RecipeHolder<MutationRecipe> holder : level.getRecipeManager()
                    .getAllRecipesFor(NTRecipes.MUTATION_TYPE.get())) {
                mutationRecipes.add(holder.value());
            }
            registration.addRecipes(MutationRecipeCategory.RECIPE_TYPE, mutationRecipes);
        }
    }
}
