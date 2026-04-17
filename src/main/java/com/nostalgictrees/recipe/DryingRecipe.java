package com.nostalgictrees.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.nostalgictrees.NTRecipes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.PlacementInfo;

public class DryingRecipe implements Recipe<SingleRecipeInput> {

    /** MapCodec used by RecipeSerializer for JSON (data pack) parsing. */
    public static final MapCodec<DryingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Identifier.CODEC.fieldOf("input").forGetter(DryingRecipe::getInputItem),
                    Identifier.CODEC.fieldOf("output").forGetter(DryingRecipe::getOutputItem),
                    Codec.INT.optionalFieldOf("output_count", 1).forGetter(DryingRecipe::getOutputCount),
                    Codec.INT.optionalFieldOf("drying_time", 600).forGetter(DryingRecipe::getDryingTime)
            ).apply(instance, DryingRecipe::new)
    );

    /** StreamCodec used for network sync (server -> client). */
    public static final StreamCodec<RegistryFriendlyByteBuf, DryingRecipe> STREAM_CODEC =
            StreamCodec.of(
                    (buf, recipe) -> {
                        buf.writeIdentifier(recipe.getInputItem());
                        buf.writeIdentifier(recipe.getOutputItem());
                        buf.writeVarInt(recipe.getOutputCount());
                        buf.writeVarInt(recipe.getDryingTime());
                    },
                    buf -> new DryingRecipe(
                            buf.readIdentifier(),
                            buf.readIdentifier(),
                            buf.readVarInt(),
                            buf.readVarInt()
                    )
            );

    private final Identifier inputItem;
    private final Identifier outputItem;
    private final int outputCount;
    private final int dryingTime;

    public DryingRecipe(Identifier inputItem, Identifier outputItem, int outputCount, int dryingTime) {
        this.inputItem = inputItem;
        this.outputItem = outputItem;
        this.outputCount = outputCount;
        this.dryingTime = dryingTime;
    }

    public Identifier getInputItem() { return inputItem; }
    public Identifier getOutputItem() { return outputItem; }
    public int getOutputCount() { return outputCount; }
    public int getDryingTime() { return dryingTime; }

    public ItemStack getInputStack() {
        return net.minecraft.core.registries.BuiltInRegistries.ITEM.get(inputItem)
                .map(holder -> new ItemStack(holder.value()))
                .orElse(ItemStack.EMPTY);
    }

    public ItemStack getOutputStack() {
        return net.minecraft.core.registries.BuiltInRegistries.ITEM.get(outputItem)
                .map(holder -> new ItemStack(holder.value(), outputCount))
                .orElse(ItemStack.EMPTY);
    }

    // === Recipe interface ===

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return false; // Not used in standard crafting
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input) {
        return getOutputStack();
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public RecipeSerializer<DryingRecipe> getSerializer() {
        return NTRecipes.DRYING_SERIALIZER.get();
    }

    @Override
    public RecipeBookCategory recipeBookCategory() { return RecipeBookCategories.CRAFTING_MISC; }

    @Override
    public PlacementInfo placementInfo() { return PlacementInfo.NOT_PLACEABLE; }

    @Override
    public RecipeType<DryingRecipe> getType() {
        return NTRecipes.DRYING_TYPE.get();
    }

    @Override
    public String group() {
        return "";
    }
}