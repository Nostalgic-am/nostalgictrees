package com.nostalgictrees;

import com.nostalgictrees.recipe.DryingRecipe;
import com.nostalgictrees.recipe.MutationRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class NTRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, NostalgicTrees.MODID);

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, NostalgicTrees.MODID);

    // Mutation recipe type + serializer
    public static final Supplier<RecipeType<MutationRecipe>> MUTATION_TYPE =
            RECIPE_TYPES.register("mutation", () -> RecipeType.simple(
                    Identifier.fromNamespaceAndPath(NostalgicTrees.MODID, "mutation")));

    // 26.1: RecipeSerializer is a record(MapCodec, StreamCodec) — no custom class needed.
    public static final Supplier<RecipeSerializer<MutationRecipe>> MUTATION_SERIALIZER =
            SERIALIZERS.register("mutation",
                    () -> new RecipeSerializer<>(MutationRecipe.MAP_CODEC, MutationRecipe.STREAM_CODEC));

    // Drying recipe type + serializer
    public static final Supplier<RecipeType<DryingRecipe>> DRYING_TYPE =
            RECIPE_TYPES.register("drying", () -> RecipeType.simple(
                    Identifier.fromNamespaceAndPath(NostalgicTrees.MODID, "drying")));

    public static final Supplier<RecipeSerializer<DryingRecipe>> DRYING_SERIALIZER =
            SERIALIZERS.register("drying",
                    () -> new RecipeSerializer<>(DryingRecipe.MAP_CODEC, DryingRecipe.STREAM_CODEC));

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
        RECIPE_TYPES.register(eventBus);
    }
}