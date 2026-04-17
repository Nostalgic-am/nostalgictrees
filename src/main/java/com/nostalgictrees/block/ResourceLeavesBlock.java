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
    /**
     * Chance (per random tick) that a leaf particle spawns beneath this block.
     * Vanilla oak uses 0.01; we match that for a familiar feel.
     */
    private static final float LEAF_PARTICLE_CHANCE = 0.01f;

    /*
     * 26.1: Block.Properties require a registry ID to be injected before the superclass
     * constructor runs. DeferredRegister.Blocks#registerBlock pre-populates the id and
     * passes the Properties through to the factory.
     *
     * LeavesBlock's constructor also takes a float leafParticleChance (new in 26.1) as
     * its first arg, which we thread through in the factory.
     */
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

    /**
     * 26.1: LeavesBlock requires falling-leaves particles.
     * We use TINTED_LEAVES with the block's own tint color (registered via NTColorHandler),
     * so particle color automatically matches the tree variant's color.
     */
    @Override
    protected void spawnFallingLeavesParticle(Level level, BlockPos pos, RandomSource random) {
        ColorParticleOption particle = ColorParticleOption.create(
                ParticleTypes.TINTED_LEAVES,
                level.getClientLeafTintColor(pos)
        );
        ParticleUtils.spawnParticleBelow(level, pos, random, particle);
    }
}
