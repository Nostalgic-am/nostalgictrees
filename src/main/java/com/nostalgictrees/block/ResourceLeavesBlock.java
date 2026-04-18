package com.nostalgictrees.block;

import com.mojang.serialization.MapCodec;
import com.nostalgictrees.data.TreeTier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ResourceLeavesBlock extends LeavesBlock {

    private static final float LEAF_PARTICLE_CHANCE = 0.01f;

    public static final MapCodec<ResourceLeavesBlock> CODEC =
            simpleCodec(props -> new ResourceLeavesBlock("", TreeTier.TIER_1, props));

    @Override
    public MapCodec<? extends LeavesBlock> codec() {
        return CODEC;
    }

    private final String treeName;
    private final TreeTier tier;

    public ResourceLeavesBlock(String treeName, TreeTier tier, BlockBehaviour.Properties properties) {
        super(LEAF_PARTICLE_CHANCE, properties);
        this.treeName = treeName;
        this.tier = tier;
    }

    public String getTreeName() { return treeName; }
    public TreeTier getTier() { return tier; }

    @Override
    protected void spawnFallingLeavesParticle(Level level, BlockPos pos, RandomSource random) {
        ColorParticleOption particle = ColorParticleOption.create(
                ParticleTypes.TINTED_LEAVES,
                level.getClientLeafTintColor(pos)
        );
        ParticleUtils.spawnParticleBelow(level, pos, random, particle);
    }
}
