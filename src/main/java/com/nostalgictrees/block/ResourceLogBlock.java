package com.nostalgictrees.block;

import com.mojang.serialization.MapCodec;
import com.nostalgictrees.data.TreeTier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ResourceLogBlock extends Block {
    /*
     * 26.1: Block.Properties require a registry ID to be injected before the superclass
     * constructor runs. The factory passed to DeferredRegister.Blocks#registerBlock takes
     * Properties (NeoForge pre-populates the id).
     *
     * Codec: ResourceLogBlock has per-instance state (treeName, tier) that isn't part of
     * vanilla's serialization. The codec only matters for data-generation features we
     * don't use, so simpleCodec is fine — it reconstructs a default instance that won't
     * round-trip the treeName/tier, but that's an acceptable limitation.
     */
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
