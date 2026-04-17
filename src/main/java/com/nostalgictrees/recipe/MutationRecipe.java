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
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MutationRecipe implements Recipe<SingleRecipeInput> {

    /** MapCodec used by RecipeSerializer for JSON (data pack) parsing. */
    public static final MapCodec<MutationRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Identifier.CODEC.fieldOf("base_sapling").forGetter(MutationRecipe::getBaseSapling),
                    Identifier.CODEC.listOf().fieldOf("honeycombs").forGetter(MutationRecipe::getHoneycombs),
                    Identifier.CODEC.fieldOf("result").forGetter(MutationRecipe::getResultSapling),
                    Codec.INT.optionalFieldOf("pollinations_required", 3).forGetter(MutationRecipe::getPollinationsRequired),
                    Identifier.CODEC.optionalFieldOf("catalyst").forGetter(r -> Optional.ofNullable(r.getCatalyst())),
                    Codec.INT.optionalFieldOf("catalyst_count", 0).forGetter(MutationRecipe::getCatalystCount)
            ).apply(instance, (base, combs, result, poll, catalyst, catCount) ->
                    new MutationRecipe(base, combs, result, poll, catalyst.orElse(null), catCount))
    );

    /** StreamCodec used for network sync (server -> client). */
    public static final StreamCodec<RegistryFriendlyByteBuf, MutationRecipe> STREAM_CODEC =
            StreamCodec.of(
                    (buf, recipe) -> {
                        buf.writeIdentifier(recipe.getBaseSapling());
                        buf.writeVarInt(recipe.getHoneycombs().size());
                        for (Identifier comb : recipe.getHoneycombs()) {
                            buf.writeIdentifier(comb);
                        }
                        buf.writeIdentifier(recipe.getResultSapling());
                        buf.writeVarInt(recipe.getPollinationsRequired());
                        buf.writeBoolean(recipe.hasCatalyst());
                        if (recipe.hasCatalyst()) {
                            buf.writeIdentifier(recipe.getCatalyst());
                        }
                        buf.writeVarInt(recipe.getCatalystCount());
                    },
                    buf -> {
                        Identifier baseSapling = buf.readIdentifier();
                        int combCount = buf.readVarInt();
                        List<Identifier> honeycombs = new ArrayList<>();
                        for (int i = 0; i < combCount; i++) {
                            honeycombs.add(buf.readIdentifier());
                        }
                        Identifier result = buf.readIdentifier();
                        int pollinations = buf.readVarInt();
                        boolean hasCatalyst = buf.readBoolean();
                        Identifier catalyst = hasCatalyst ? buf.readIdentifier() : null;
                        int catalystCount = buf.readVarInt();
                        return new MutationRecipe(baseSapling, honeycombs, result, pollinations, catalyst, catalystCount);
                    }
            );

    private final Identifier baseSapling;
    private final List<Identifier> honeycombs;
    private final Identifier resultSapling;
    private final int pollinationsRequired;
    @Nullable
    private final Identifier catalyst;
    private final int catalystCount;

    public MutationRecipe(Identifier baseSapling, List<Identifier> honeycombs,
                          Identifier resultSapling, int pollinationsRequired,
                          @Nullable Identifier catalyst, int catalystCount) {
        this.baseSapling = baseSapling;
        this.honeycombs = honeycombs;
        this.resultSapling = resultSapling;
        this.pollinationsRequired = pollinationsRequired;
        this.catalyst = catalyst;
        this.catalystCount = catalystCount;
    }

    // Backwards-compatible constructor (no catalyst)
    public MutationRecipe(Identifier baseSapling, List<Identifier> honeycombs,
                          Identifier resultSapling, int pollinationsRequired) {
        this(baseSapling, honeycombs, resultSapling, pollinationsRequired, null, 0);
    }

    public Identifier getBaseSapling() { return baseSapling; }
    public List<Identifier> getHoneycombs() { return honeycombs; }
    public Identifier getResultSapling() { return resultSapling; }
    public int getPollinationsRequired() { return pollinationsRequired; }
    @Nullable
    public Identifier getCatalyst() { return catalyst; }
    public int getCatalystCount() { return catalystCount; }
    public boolean hasCatalyst() { return catalyst != null && catalystCount > 0; }

    // === Recipe interface (mostly unused — we look up recipes manually) ===

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public RecipeSerializer<MutationRecipe> getSerializer() {
        return NTRecipes.MUTATION_SERIALIZER.get();
    }

    @Override
    public RecipeBookCategory recipeBookCategory() { return RecipeBookCategories.CRAFTING_MISC; }

    @Override
    public PlacementInfo placementInfo() { return PlacementInfo.NOT_PLACEABLE; }

    @Override
    public RecipeType<MutationRecipe> getType() {
        return NTRecipes.MUTATION_TYPE.get();
    }

    @Override
    public String group() {
        return "";
    }
}