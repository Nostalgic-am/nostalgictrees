package com.nostalgictrees;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Recipe registration. For now recipes are JSON-based (shapeless crafting).
 * This class exists as a hook for future custom recipe types.
 */
public class NTRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, NostalgicTrees.MODID);

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
    }
}
