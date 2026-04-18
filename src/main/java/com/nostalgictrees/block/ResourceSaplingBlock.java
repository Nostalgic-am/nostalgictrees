package com.nostalgictrees.block;

import com.mojang.serialization.MapCodec;
import com.nostalgictrees.NTBlocks;
import com.nostalgictrees.block.entity.ResourceSaplingBlockEntity;
import com.nostalgictrees.data.TreeTier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class ResourceSaplingBlock extends BaseEntityBlock implements BonemealableBlock {
    /*
     * 26.1: Block.Properties require a registry ID to be injected before the superclass
     * constructor runs. DeferredRegister.Blocks#registerBlock pre-populates the id and
     * passes the Properties through to the factory.
     */
    public static final MapCodec<ResourceSaplingBlock> CODEC =
            simpleCodec(props -> new ResourceSaplingBlock("", TreeTier.TIER_1, props));

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    protected static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 12.0D, 14.0D);
    private final String treeName;
    private final TreeTier tier;

    public ResourceSaplingBlock(String treeName, TreeTier tier, BlockBehaviour.Properties properties) {
        super(properties);
        this.treeName = treeName;
        this.tier = tier;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    // ======================== BLOCK ENTITY ========================

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ResourceSaplingBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, NTBlocks.SAPLING_BE.get(),
                ResourceSaplingBlockEntity::serverTick);
    }

    // ======================== HONEYCOMB INTERACTION ========================

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        if (!stack.isEmpty()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ResourceSaplingBlockEntity saplingBE) {
                if (saplingBE.tryApplyItem(stack)) {
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    // ======================== BUSH BEHAVIOR ========================
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.SUPPORTS_VEGETATION);
    }
    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        return mayPlaceOn(level.getBlockState(below), level, below);
    }

    // ======================== TREE GROWTH ========================

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getMaxLocalRawBrightness(pos.above()) >= 9 && random.nextInt(4) == 0) {
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

    // ======================== BONEMEALABLE ========================

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return level.getRandom().nextFloat() < 0.6D;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ResourceSaplingBlockEntity saplingBE) {
            int stage = saplingBE.getGrowthStage();
            if (stage >= 2) {
                growTree(level, pos, random);
            } else {
                saplingBE.setGrowthStage(stage + 1);
                for (int i = 0; i < 8; i++) {
                    level.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                            pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.6,
                            pos.getY() + 0.5 + random.nextDouble() * 0.5,
                            pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.6,
                            1, 0, 0, 0, 0);
                }
            }
        } else {
            growTree(level, pos, random);
        }
    }

    // ======================== GETTERS ========================

    public String getTreeName() { return treeName; }
    public TreeTier getTier() { return tier; }
}
