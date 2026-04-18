package com.nostalgictrees.block;

import com.mojang.serialization.MapCodec;
import com.nostalgictrees.data.TreeTier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ResourceLogBlock extends Block {

    public static final MapCodec<ResourceLogBlock> CODEC =
            simpleCodec(props -> new ResourceLogBlock("", TreeTier.TIER_1, props));

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    private final String treeName;
    private final TreeTier tier;

    public ResourceLogBlock(String treeName, TreeTier tier, BlockBehaviour.Properties properties) {
        super(properties);
        this.treeName = treeName;
        this.tier = tier;
    }

    public String getTreeName() { return treeName; }
    public TreeTier getTier() { return tier; }
}
