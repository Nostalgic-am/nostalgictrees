package com.nostalgictrees.event;

import com.nostalgictrees.NTRecipes;
import com.nostalgictrees.NostalgicTrees;
import com.nostalgictrees.recipe.DryingRecipe;
import com.nostalgictrees.recipe.MutationRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class NTRecipeSyncHandler {

    // Client-side caches. Populated by recipesReceived(). Read by NTJEIPlugin during
    // its registerRecipes() call. Cleared on logout.
    private static volatile List<DryingRecipe> dryingRecipes = Collections.emptyList();
    private static volatile List<MutationRecipe> mutationRecipes = Collections.emptyList();

    private NTRecipeSyncHandler() {}

    // ====================================================================
    // Server side — tell clients which recipe types to receive
    // ====================================================================

    /** Fires on login + on /reload. See class javadoc. */
    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        event.sendRecipes(NTRecipes.DRYING_TYPE.get());
        event.sendRecipes(NTRecipes.MUTATION_TYPE.get());
    }

    // ====================================================================
    // Client side — receive and cache
    // ====================================================================

    @SubscribeEvent
    public static void onRecipesReceived(RecipesReceivedEvent event) {
        List<DryingRecipe> drying = new ArrayList<>();
        for (RecipeHolder<DryingRecipe> holder : event.getRecipeMap().byType(NTRecipes.DRYING_TYPE.get())) {
            drying.add(holder.value());
        }
        dryingRecipes = List.copyOf(drying);

        List<MutationRecipe> mutation = new ArrayList<>();
        for (RecipeHolder<MutationRecipe> holder : event.getRecipeMap().byType(NTRecipes.MUTATION_TYPE.get())) {
            mutation.add(holder.value());
        }
        mutationRecipes = List.copyOf(mutation);

        NostalgicTrees.LOGGER.debug("Received {} drying and {} mutation recipes from server",
                dryingRecipes.size(), mutationRecipes.size());
    }

    @SubscribeEvent
    public static void onClientLogOut(ClientPlayerNetworkEvent.LoggingOut event) {
        // Prevent stale recipes from leaking across worlds/servers.
        dryingRecipes = Collections.emptyList();
        mutationRecipes = Collections.emptyList();
    }

    // ====================================================================
    // Accessors for JEI plugin
    // ====================================================================

    public static List<DryingRecipe> getDryingRecipes() {
        return dryingRecipes;
    }

    public static List<MutationRecipe> getMutationRecipes() {
        return mutationRecipes;
    }
}
