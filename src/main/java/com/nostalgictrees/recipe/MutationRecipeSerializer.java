package com.nostalgictrees.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.List;
import java.util.Optional;

public class MutationRecipeSerializer implements RecipeSerializer<MutationRecipe> {

    public static final MapCodec<MutationRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("base_sapling").forGetter(MutationRecipe::getBaseSapling),
                    ResourceLocation.CODEC.listOf().fieldOf("honeycombs").forGetter(MutationRecipe::getHoneycombs),
                    ResourceLocation.CODEC.fieldOf("result").forGetter(MutationRecipe::getResultSapling),
                    Codec.INT.optionalFieldOf("pollinations_required", 3).forGetter(MutationRecipe::getPollinationsRequired),
                    ResourceLocation.CODEC.optionalFieldOf("catalyst").forGetter(r ->
                            Optional.ofNullable(r.getCatalyst())),
                    Codec.INT.optionalFieldOf("catalyst_count", 0).forGetter(MutationRecipe::getCatalystCount)
            ).apply(instance, (base, combs, result, poll, catalyst, catCount) ->
                    new MutationRecipe(base, combs, result, poll, catalyst.orElse(null), catCount))
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, MutationRecipe> STREAM_CODEC =
            StreamCodec.of(
                    MutationRecipeSerializer::toNetwork,
                    MutationRecipeSerializer::fromNetwork
            );

    private static MutationRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
        ResourceLocation baseSapling = buf.readResourceLocation();
        int combCount = buf.readVarInt();
        List<ResourceLocation> honeycombs = new java.util.ArrayList<>();
        for (int i = 0; i < combCount; i++) {
            honeycombs.add(buf.readResourceLocation());
        }
        ResourceLocation result = buf.readResourceLocation();
        int pollinations = buf.readVarInt();
        boolean hasCatalyst = buf.readBoolean();
        ResourceLocation catalyst = hasCatalyst ? buf.readResourceLocation() : null;
        int catalystCount = buf.readVarInt();
        return new MutationRecipe(baseSapling, honeycombs, result, pollinations, catalyst, catalystCount);
    }

    private static void toNetwork(RegistryFriendlyByteBuf buf, MutationRecipe recipe) {
        buf.writeResourceLocation(recipe.getBaseSapling());
        buf.writeVarInt(recipe.getHoneycombs().size());
        for (ResourceLocation comb : recipe.getHoneycombs()) {
            buf.writeResourceLocation(comb);
        }
        buf.writeResourceLocation(recipe.getResultSapling());
        buf.writeVarInt(recipe.getPollinationsRequired());
        buf.writeBoolean(recipe.hasCatalyst());
        if (recipe.hasCatalyst()) {
            buf.writeResourceLocation(recipe.getCatalyst());
        }
        buf.writeVarInt(recipe.getCatalystCount());
    }

    @Override
    public MapCodec<MutationRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, MutationRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
