package com.nostalgictrees.compat.jade;

import com.nostalgictrees.NostalgicTrees;
import com.nostalgictrees.block.entity.AdvancedBeehiveBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public enum BeehiveDataProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    static final Identifier UID = Identifier.fromNamespaceAndPath(NostalgicTrees.MODID, "advanced_beehive");

    @Override
    public int getDefaultPriority() {
        return -100;
    }

    @Override
    public Identifier getUid() {
        return UID;
    }

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
}
