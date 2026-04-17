package com.nostalgictrees.compat.jade;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum DryingRackComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    @Override
    public Identifier getUid() {
        return DryingRackDataProvider.UID;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();

        if (data.contains("DryingItemName")) {
            String itemName = data.getStringOr("DryingItemName", "");
            int progress = data.getIntOr("DryingProgress", 0);
            int total = data.getIntOr("DryingTime", 0);

            tooltip.add(Component.literal("Drying: " + itemName)
                    .withStyle(ChatFormatting.YELLOW));

            if (total > 0) {
                int percent = (int) ((progress / (float) total) * 100);
                ChatFormatting color = percent >= 75 ? ChatFormatting.GREEN
                        : percent >= 25 ? ChatFormatting.YELLOW
                        : ChatFormatting.GRAY;
                tooltip.add(Component.literal("Progress: " + percent + "%")
                        .withStyle(color));
            }
        } else {
            tooltip.add(Component.literal("Empty")
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
