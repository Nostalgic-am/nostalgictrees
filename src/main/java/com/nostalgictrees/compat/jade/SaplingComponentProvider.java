package com.nostalgictrees.compat.jade;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum SaplingComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    @Override
    public Identifier getUid() {
        return SaplingDataProvider.UID;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();

        if (!data.contains("AppliedCombs")) {
            if (data.getBooleanOr("HasRecipes", false)) {
                tooltip.add(Component.literal("Apply honeycombs to mutate")
                        .withStyle(ChatFormatting.GRAY));
            }
            return;
        }

        ListTag combsList = data.getList("AppliedCombs").orElseGet(ListTag::new);
        int requiredCombs = data.getIntOr("RequiredCombs", 0);
        int appliedCount = combsList.size();

        tooltip.add(Component.literal("Honeycombs: " + appliedCount + "/" + requiredCombs)
                .withStyle(ChatFormatting.GOLD));

        for (int i = 0; i < combsList.size(); i++) {
            String combIdStr = combsList.getString(i).orElse("");
            if (combIdStr.isEmpty()) continue;

            Identifier combId = Identifier.parse(combIdStr);
            BuiltInRegistries.ITEM.get(combId).ifPresent(holder ->
                    tooltip.add(Component.literal("  ✓ ")
                            .withStyle(ChatFormatting.GREEN)
                            .append(new ItemStack(holder.value()).getHoverName().copy().withStyle(ChatFormatting.WHITE))));
        }

        boolean hasCatalyst = data.getBooleanOr("HasCatalyst", false);
        boolean catalystApplied = data.getBooleanOr("CatalystApplied", false);

        if (hasCatalyst) {
            if (catalystApplied) {
                tooltip.add(Component.literal("  ✓ Catalyst applied")
                        .withStyle(ChatFormatting.GREEN));
            } else if (appliedCount >= requiredCombs) {
                Identifier catalystId = Identifier.parse(data.getStringOr("CatalystItem", ""));
                int catalystCount = data.getIntOr("CatalystCount", 0);
                String itemName = BuiltInRegistries.ITEM.get(catalystId)
                        .map(holder -> new ItemStack(holder.value()).getHoverName().getString())
                        .orElse(catalystId.getPath());
                tooltip.add(Component.literal("  ✗ Needs " + catalystCount + "x " + itemName)
                        .withStyle(ChatFormatting.RED));
            }
        }

        int pollinations = data.getIntOr("Pollinations", 0);
        int requiredPollinations = data.getIntOr("RequiredPollinations", 0);
        boolean readyForBees = appliedCount >= requiredCombs && (!hasCatalyst || catalystApplied);

        if (readyForBees) {
            tooltip.add(Component.literal("Pollinations: " + pollinations + "/" + requiredPollinations)
                    .withStyle(pollinations > 0 ? ChatFormatting.YELLOW : ChatFormatting.GRAY));
        }

        if (data.contains("ResultSapling")) {
            Identifier resultId = Identifier.parse(data.getStringOr("ResultSapling", ""));
            BuiltInRegistries.BLOCK.get(resultId).ifPresent(holder ->
                    tooltip.add(Component.literal("→ ")
                            .withStyle(ChatFormatting.AQUA)
                            .append(holder.value().getName().copy().withStyle(ChatFormatting.AQUA))));
        }
    }
}
