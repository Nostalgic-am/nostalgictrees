package com.nostalgictrees.compat.jade;

import com.nostalgictrees.NostalgicTrees;
import com.nostalgictrees.block.entity.DryingRackBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public enum DryingRackDataProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    static final Identifier UID = Identifier.fromNamespaceAndPath(NostalgicTrees.MODID, "drying_rack");

    @Override
    public Identifier getUid() {
        return UID;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof DryingRackBlockEntity dryingBE) {
            ItemStack item = dryingBE.getItem();
            if (!item.isEmpty()) {
                data.putString("DryingItemName", item.getHoverName().getString());
                data.putInt("DryingProgress", dryingBE.getDryingProgress());
                data.putInt("DryingTime", dryingBE.getDryingTimeRequired());
            }
        }
    }
}
