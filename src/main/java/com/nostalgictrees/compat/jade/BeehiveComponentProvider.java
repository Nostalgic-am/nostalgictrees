package com.nostalgictrees.compat.jade;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum BeehiveComponentProvider implements IBlockComponentProvider {
    INSTANCE;

    @Override
    public int getDefaultPriority() {
        return -100;
    }

    @Override
    public Identifier getUid() {
        return BeehiveDataProvider.UID;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();

        if (data.contains("BeeCount")) {
            int beeCount = data.getIntOr("BeeCount", 0);
            int maxBees = data.getIntOr("MaxBees", 0);
            ChatFormatting beeColor = beeCount >= maxBees ? ChatFormatting.GREEN
                    : beeCount > 0 ? ChatFormatting.YELLOW
                    : ChatFormatting.GRAY;
            tooltip.add(Component.literal("Bees: " + beeCount + "/" + maxBees)
                    .withStyle(beeColor));
        }

        if (data.contains("HoneyLevel")) {
            int honeyLevel = data.getIntOr("HoneyLevel", 0);
            ChatFormatting honeyColor = honeyLevel >= 5 ? ChatFormatting.GOLD
                    : honeyLevel > 0 ? ChatFormatting.YELLOW
                    : ChatFormatting.GRAY;
            tooltip.add(Component.literal("Honey: " + honeyLevel + "/5")
                    .withStyle(honeyColor));
            if (honeyLevel >= 5) {
                tooltip.add(Component.literal("  Ready to harvest!")
                        .withStyle(ChatFormatting.GREEN));
            }
        }
    }
}
