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
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

/*
 * 26.1 JEI plugin.
 *
 * Mojang removed general-purpose recipe access from the client side; only
 * "displayable" recipe info (RecipeDisplayId) is synced to multiplayer clients.
 * Workaround: pull recipes from the IntegratedServer in singleplayer. In
 * multiplayer, drying/mutation recipes simply won't appear in JEI — a
 * limitation JEI itself has acknowledged for 26.1.
 *
 * Mallet recipes are unaffected since they're code-generated from NTTreeRegistry.
 */
@JeiPlugin
public class NTJEIPlugin implements IModPlugin {

    @Override
    public Identifier getPluginUid() {
        return Identifier.fromNamespaceAndPath(NostalgicTrees.MODID, "jei_plugin");
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
        // 1) Mallet recipes — always available, built from our tree registry.
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

        // 2) Drying & mutation recipes — need the RecipeManager, which is only
        //    reachable through the integrated server (singleplayer only in 26.1).
        IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
        if (server == null) return;

        RecipeManager recipeManager = server.getRecipeManager();

        List<DryingRecipe> dryingRecipes = filterRecipes(recipeManager, DryingRecipe.class);
        registration.addRecipes(DryingRackRecipeCategory.RECIPE_TYPE, dryingRecipes);

        List<MutationRecipe> mutationRecipes = filterRecipes(recipeManager, MutationRecipe.class);
        registration.addRecipes(MutationRecipeCategory.RECIPE_TYPE, mutationRecipes);
    }

    /**
     * Filter recipes by concrete type. Needed because 26.1 removed
     * RecipeManager#getAllRecipesFor(type) — have to iterate getRecipes() manually.
     */
    @SuppressWarnings("unchecked")
    private static <T extends Recipe<?>> List<T> filterRecipes(RecipeManager recipeManager, Class<T> recipeClass) {
        List<T> result = new ArrayList<>();
        for (RecipeHolder<?> holder : recipeManager.getRecipes()) {
            if (recipeClass.isInstance(holder.value())) {
                result.add((T) holder.value());
            }
        }
        return result;
    }
}