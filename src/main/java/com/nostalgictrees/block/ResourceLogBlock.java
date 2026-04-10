package com.nostalgictrees.block;

import com.mojang.serialization.MapCodec;
import com.nostalgictrees.data.TreeTier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ResourceLogBlock extends Block {
    public static final MapCodec<ResourceLogBlock> CODEC = simpleCodec(p -> new ResourceLogBlock("", TreeTier.TIER_1));

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    private final String treeName;
    private final TreeTier tier;

    public ResourceLogBlock(String treeName, TreeTier tier) {
        super(BlockBehaviour.Properties.of()
                .strength(2.0f)
                .sound(SoundType.WOOD)
                .requiresCorrectToolForDrops()
        );
        this.treeName = treeName;
        this.tier = tier;
    }

    public String getTreeName() { return treeName; }
    public TreeTier getTier() { return tier; }
}
