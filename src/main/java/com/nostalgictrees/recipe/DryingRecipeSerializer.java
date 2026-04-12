package com.nostalgictrees.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class DryingRecipeSerializer implements RecipeSerializer<DryingRecipe> {

    public static final MapCodec<DryingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("input").forGetter(DryingRecipe::getInputItem),
                    ResourceLocation.CODEC.fieldOf("output").forGetter(DryingRecipe::getOutputItem),
                    Codec.INT.optionalFieldOf("output_count", 1).forGetter(DryingRecipe::getOutputCount),
                    Codec.INT.optionalFieldOf("drying_time", 600).forGetter(DryingRecipe::getDryingTime)
            ).apply(instance, DryingRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, DryingRecipe> STREAM_CODEC =
            StreamCodec.of(
                    DryingRecipeSerializer::toNetwork,
                    DryingRecipeSerializer::fromNetwork
            );

    private static DryingRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
        ResourceLocation input = buf.readResourceLocation();
        ResourceLocation output = buf.readResourceLocation();
        int outputCount = buf.readVarInt();
        int dryingTime = buf.readVarInt();
        return new DryingRecipe(input, output, outputCount, dryingTime);
    }

    private static void toNetwork(RegistryFriendlyByteBuf buf, DryingRecipe recipe) {
        buf.writeResourceLocation(recipe.getInputItem());
        buf.writeResourceLocation(recipe.getOutputItem());
        buf.writeVarInt(recipe.getOutputCount());
        buf.writeVarInt(recipe.getDryingTime());
    }

    @Override
    public MapCodec<DryingRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, DryingRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
