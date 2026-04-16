package com.nostalgictrees.compat.jade;

import com.nostalgictrees.NostalgicTrees;
import com.nostalgictrees.block.entity.ResourceSaplingBlockEntity;
import com.nostalgictrees.recipe.MutationRecipe;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import java.util.ArrayList;
import java.util.List;

public enum SaplingComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(NostalgicTrees.MODID, "sapling_mutation");

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    // === SERVER SIDE: Send data to client ===
    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof ResourceSaplingBlockEntity saplingBE) {
            List<ResourceLocation> combs = saplingBE.getAppliedHoneycombs();
            if (combs.isEmpty()) {
                // Check if any mutation recipes exist for this sapling
                List<MutationRecipe> recipes = saplingBE.findAllRecipes();
                if (!recipes.isEmpty()) {
                    data.putBoolean("HasRecipes", true);
                    data.putInt("RecipeCount", recipes.size());
                }
                return;
            }

            // Applied honeycombs
            ListTag combsList = new ListTag();
            for (ResourceLocation comb : combs) {
                combsList.add(net.minecraft.nbt.StringTag.valueOf(comb.toString()));
            }
            data.put("AppliedCombs", combsList);

            // Catalyst state
            data.putBoolean("CatalystApplied", saplingBE.isCatalystApplied());

            // Pollination progress
            data.putInt("Pollinations", saplingBE.getPollinationCount());

            // Recipe info (if matched)
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

    // === CLIENT SIDE: Render tooltip ===
    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();

        // No mutation in progress — show hint if recipes exist
        if (!data.contains("AppliedCombs")) {
            if (data.getBoolean("HasRecipes")) {
                tooltip.add(Component.literal("Apply honeycombs to mutate")
                        .withStyle(ChatFormatting.GRAY));
            }
            return;
        }

        // Show applied honeycombs
        ListTag combsList = data.getList("AppliedCombs", Tag.TAG_STRING);
        int requiredCombs = data.getInt("RequiredCombs");
        int appliedCount = combsList.size();

        tooltip.add(Component.literal("Honeycombs: " + appliedCount + "/" + requiredCombs)
                .withStyle(ChatFormatting.GOLD));

        // List each applied comb
        for (int i = 0; i < combsList.size(); i++) {
            ResourceLocation combId = ResourceLocation.parse(combsList.getString(i));
            Item combItem = BuiltInRegistries.ITEM.get(combId);
            if (combItem != null) {
                tooltip.add(Component.literal("  ✓ ")
                        .withStyle(ChatFormatting.GREEN)
                        .append(combItem.getDescription().copy().withStyle(ChatFormatting.WHITE)));
            }
        }

        // Show catalyst status
        if (data.getBoolean("HasCatalyst")) {
            boolean catalystApplied = data.getBoolean("CatalystApplied");
            if (catalystApplied) {
                tooltip.add(Component.literal("  ✓ Catalyst applied")
                        .withStyle(ChatFormatting.GREEN));
            } else if (appliedCount >= requiredCombs) {
                // All combs done, waiting for catalyst
                ResourceLocation catalystId = ResourceLocation.parse(data.getString("CatalystItem"));
                int catalystCount = data.getInt("CatalystCount");
                Item catalystItem = BuiltInRegistries.ITEM.get(catalystId);
                String itemName = catalystItem != null ? catalystItem.getDescription().getString() : catalystId.getPath();
                tooltip.add(Component.literal("  ✗ Needs " + catalystCount + "x " + itemName)
                        .withStyle(ChatFormatting.RED));
            }
        }

        // Pollination progress
        int pollinations = data.getInt("Pollinations");
        int requiredPollinations = data.getInt("RequiredPollinations");
        boolean readyForBees = appliedCount >= requiredCombs
                && (!data.getBoolean("HasCatalyst") || data.getBoolean("CatalystApplied"));

        if (readyForBees) {
            tooltip.add(Component.literal("Pollinations: " + pollinations + "/" + requiredPollinations)
                    .withStyle(pollinations > 0 ? ChatFormatting.YELLOW : ChatFormatting.GRAY));
        }

        // Show what it will become
        if (data.contains("ResultSapling")) {
            ResourceLocation resultId = ResourceLocation.parse(data.getString("ResultSapling"));
            Block resultBlock = BuiltInRegistries.BLOCK.get(resultId);
            if (resultBlock != null) {
                tooltip.add(Component.literal("→ ")
                        .withStyle(ChatFormatting.AQUA)
                        .append(resultBlock.getName().copy().withStyle(ChatFormatting.AQUA)));
            }
        }
    }
}
