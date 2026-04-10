package com.nostalgictrees.block;

import com.mojang.serialization.MapCodec;
import com.nostalgictrees.data.TreeTier;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ResourceLeavesBlock extends LeavesBlock {
    public static final MapCodec<ResourceLeavesBlock> CODEC = simpleCodec(p -> new ResourceLeavesBlock("", TreeTier.TIER_1));

    @Override
    public MapCodec<? extends LeavesBlock> codec() {
        return CODEC;
    }

    private final String treeName;
    private final TreeTier tier;

    public ResourceLeavesBlock(String treeName, TreeTier tier) {
        super(BlockBehaviour.Properties.of()
                .strength(0.2f)
                .randomTicks()
                .sound(SoundType.GRASS)
                .noOcclusion()
                .isValidSpawn((state, level, pos, type) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false)
        );
        this.treeName = treeName;
        this.tier = tier;
    }

    public String getTreeName() { return treeName; }
    public TreeTier getTier() { return tier; }
}
