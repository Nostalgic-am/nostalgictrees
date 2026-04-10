package com.nostalgictrees.block;

import com.mojang.serialization.MapCodec;
import com.nostalgictrees.NTBlocks;
import com.nostalgictrees.data.TreeTier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ResourceSaplingBlock extends BushBlock implements BonemealableBlock {
    public static final MapCodec<ResourceSaplingBlock> CODEC = simpleCodec(p -> new ResourceSaplingBlock("", TreeTier.TIER_1));

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    protected static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 12.0D, 14.0D);
    private final String treeName;
    private final TreeTier tier;

    public ResourceSaplingBlock(String treeName, TreeTier tier) {
        super(BlockBehaviour.Properties.of()
                .noCollission()
                .randomTicks()
                .instabreak()
                .sound(SoundType.GRASS)
        );
        this.treeName = treeName;
        this.tier = tier;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getMaxLocalRawBrightness(pos.above()) >= 9 && random.nextInt(7) == 0) {
            if (!level.isAreaLoaded(pos, 1)) return;
            growTree(level, pos, random);
        }
    }

    public void growTree(ServerLevel level, BlockPos pos, RandomSource random) {
        Block logBlock = NTBlocks.getLogBlock(treeName);
        Block leavesBlock = NTBlocks.getLeavesBlock(treeName);

        if (logBlock == null || leavesBlock == null) return;

        int trunkHeight = 4 + random.nextInt(2);

        for (int y = 1; y <= trunkHeight + 2; y++) {
            if (!canReplace(level, pos.above(y))) return;
        }

        BlockState logState = logBlock.defaultBlockState();
        BlockState leafState = leavesBlock.defaultBlockState();

        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 4);

        for (int y = 0; y < trunkHeight; y++) {
            level.setBlock(pos.above(y), logState, 3);
        }

        int leafBase = trunkHeight - 2;

        for (int layer = 0; layer < 2; layer++) {
            int y = leafBase + layer;
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    if (Math.abs(x) == 2 && Math.abs(z) == 2) {
                        if (random.nextBoolean()) continue;
                    }
                    if (x == 0 && z == 0) continue;
                    placeLeafIfEmpty(level, pos.offset(x, y, z), leafState);
                }
            }
        }

        for (int layer = 2; layer < 4; layer++) {
            int y = leafBase + layer;
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (layer == 3 && Math.abs(x) == 1 && Math.abs(z) == 1) {
                        if (random.nextBoolean()) continue;
                    }
                    if (layer == 2 && x == 0 && z == 0) continue;
                    placeLeafIfEmpty(level, pos.offset(x, y, z), leafState);
                }
            }
        }

        placeLeafIfEmpty(level, pos.above(trunkHeight), leafState);
    }

    private boolean canReplace(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.is(BlockTags.LEAVES) || state.is(BlockTags.SAPLINGS);
    }

    private void placeLeafIfEmpty(ServerLevel level, BlockPos pos, BlockState leafState) {
        BlockState existing = level.getBlockState(pos);
        if (existing.isAir() || existing.is(BlockTags.LEAVES)) {
            level.setBlock(pos, leafState, 3);
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return level.random.nextFloat() < 0.45D;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        growTree(level, pos, random);
    }

    public String getTreeName() { return treeName; }
    public TreeTier getTier() { return tier; }
}
