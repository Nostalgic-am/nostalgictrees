package com.nostalgictrees.compat.jade;

import com.nostalgictrees.NostalgicTrees;
import com.nostalgictrees.block.entity.ResourceSaplingBlockEntity;
import com.nostalgictrees.recipe.MutationRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

import java.util.List;

public enum SaplingDataProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    static final Identifier UID = Identifier.fromNamespaceAndPath(NostalgicTrees.MODID, "sapling_mutation");

    @Override
    public Identifier getUid() {
        return UID;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof ResourceSaplingBlockEntity saplingBE) {
            List<Identifier> combs = saplingBE.getAppliedHoneycombs();
            if (combs.isEmpty()) {
                List<MutationRecipe> recipes = saplingBE.findAllRecipes();
                if (!recipes.isEmpty()) {
                    data.putBoolean("HasRecipes", true);
                    data.putInt("RecipeCount", recipes.size());
                }
                return;
            }

            ListTag combsList = new ListTag();
            for (Identifier comb : combs) {
                combsList.add(StringTag.valueOf(comb.toString()));
            }
            data.put("AppliedCombs", combsList);

            data.putBoolean("CatalystApplied", saplingBE.isCatalystApplied());
            data.putInt("Pollinations", saplingBE.getPollinationCount());

            MutationRecipe recipe = saplingBE.findRecipe();
            if (recipe != null) {
                data.putInt("RequiredCombs", recipe.getHoneycombs().size());
                data.putInt("RequiredPollinations", recipe.getPollinationsRequired());
                data.putString("ResultSapling", recipe.getResultSapling().toString());
                data.putBoolean("HasCatalyst", recipe.hasCatalyst());
                if (recipe.hasCatalyst()) {
                    data.putString("CatalystItem", recipe.getCatalyst().toString());
                    data.putInt("CatalystCount", recipe.getCatalystCount());
                }
            }
        }
    }
}
