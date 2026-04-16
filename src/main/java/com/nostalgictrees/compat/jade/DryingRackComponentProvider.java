package com.nostalgictrees.compat.jade;

import com.nostalgictrees.NostalgicTrees;
import com.nostalgictrees.block.entity.DryingRackBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum DryingRackComponentProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(NostalgicTrees.MODID, "drying_rack");

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof DryingRackBlockEntity dryingBE) {
            ItemStack item = dryingBE.getItem();
            if (!item.isEmpty()) {
                data.putString("DryingItem", item.getDescriptionId());
                data.putString("DryingItemName", item.getHoverName().getString());
                data.putInt("DryingProgress", dryingBE.getDryingProgress());
                data.putInt("DryingTime", dryingBE.getDryingTimeRequired());
            }
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();

        if (data.contains("DryingItemName")) {
            String itemName = data.getString("DryingItemName");
            int progress = data.getInt("DryingProgress");
            int total = data.getInt("DryingTime");

            tooltip.add(Component.literal("Drying: " + itemName)
                    .withStyle(ChatFormatting.YELLOW));

            if (total > 0) {
                int percent = (int) ((progress / (float) total) * 100);
                ChatFormatting color = percent >= 75 ? ChatFormatting.GREEN : percent >= 25 ? ChatFormatting.YELLOW : ChatFormatting.GRAY;
                tooltip.add(Component.literal("Progress: " + percent + "%")
                        .withStyle(color));
            }
        } else {
            tooltip.add(Component.literal("Empty")
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}