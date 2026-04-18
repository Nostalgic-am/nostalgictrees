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

/**
 * Handles syncing of DryingRecipe and MutationRecipe between server and client.
 *
 * 26.1 / 1.21.6 removed Level#getRecipeManager. Clients no longer have direct access
 * to the recipe manager — only RecipePropertySet (for slot-restriction logic) and
 * RecipeDisplays for unlocked recipes get synced by default. Custom recipe types
 * that need to be shown in JEI must be explicitly synced.
 *
 * NeoForge provides the OnDatapackSyncEvent (server) / RecipesReceivedEvent (client)
 * pair for exactly this purpose. Registration is on the GAME event bus (not mod bus).
 *
 * OnDatapackSyncEvent fires:
 *   - When a player joins the server (covers login)
 *   - When /reload is invoked (covers datapack reloads)
 *
 * Uses the StreamCodec defined on the Recipe classes themselves, so no custom packet
 * plumbing is needed.
 */
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
