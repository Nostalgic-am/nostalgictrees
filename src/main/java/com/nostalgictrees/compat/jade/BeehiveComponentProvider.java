package com.nostalgictrees.compat.jade;

import com.nostalgictrees.NostalgicTrees;
import com.nostalgictrees.block.entity.AdvancedBeehiveBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum BeehiveComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(NostalgicTrees.MODID, "advanced_beehive");

    @Override
    public int getDefaultPriority() {
        return -100;
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    // === SERVER SIDE ===
    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof AdvancedBeehiveBlockEntity beehiveBE) {
            BlockState state = accessor.getBlockState();
            int honeyLevel = state.getValue(BeehiveBlock.HONEY_LEVEL);
            data.putInt("HoneyLevel", honeyLevel);
            data.putInt("BeeCount", beehiveBE.getOccupantCount());
            data.putInt("MaxBees", AdvancedBeehiveBlockEntity.MAX_BEES);
            data.remove("Bees");
        }
    }

    // === CLIENT SIDE ===
    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();

        if (data.contains("BeeCount")) {
            int beeCount = data.getInt("BeeCount");
            int maxBees = data.getInt("MaxBees");
            ChatFormatting beeColor = beeCount >= maxBees ? ChatFormatting.GREEN : beeCount > 0 ? ChatFormatting.YELLOW : ChatFormatting.GRAY;
            tooltip.add(Component.literal("Bees: " + beeCount + "/" + maxBees)
                    .withStyle(beeColor));
        }

        if (data.contains("HoneyLevel")) {
            int honeyLevel = data.getInt("HoneyLevel");
            ChatFormatting honeyColor = honeyLevel >= 5 ? ChatFormatting.GOLD : honeyLevel > 0 ? ChatFormatting.YELLOW : ChatFormatting.GRAY;
            tooltip.add(Component.literal("Honey: " + honeyLevel + "/5")
                    .withStyle(honeyColor));
            if (honeyLevel >= 5) {
                tooltip.add(Component.literal("  Ready to harvest!")
                        .withStyle(ChatFormatting.GREEN));
            }
        }
    }
}
