package com.nostalgictrees.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.List;

public class MutationRecipeSerializer implements RecipeSerializer<MutationRecipe> {

    public static final MapCodec<MutationRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("base_sapling").forGetter(MutationRecipe::getBaseSapling),
                    ResourceLocation.CODEC.listOf().fieldOf("honeycombs").forGetter(MutationRecipe::getHoneycombs),
                    ResourceLocation.CODEC.fieldOf("result").forGetter(MutationRecipe::getResultSapling),
                    Codec.INT.optionalFieldOf("pollinations_required", 3).forGetter(MutationRecipe::getPollinationsRequired)
            ).apply(instance, MutationRecipe::new)
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
        return new MutationRecipe(baseSapling, honeycombs, result, pollinations);
    }

    private static void toNetwork(RegistryFriendlyByteBuf buf, MutationRecipe recipe) {
        buf.writeResourceLocation(recipe.getBaseSapling());
        buf.writeVarInt(recipe.getHoneycombs().size());
        for (ResourceLocation comb : recipe.getHoneycombs()) {
            buf.writeResourceLocation(comb);
        }
        buf.writeResourceLocation(recipe.getResultSapling());
        buf.writeVarInt(recipe.getPollinationsRequired());
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
