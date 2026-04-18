package com.nostalgictrees.block;

import com.mojang.serialization.MapCodec;
import com.nostalgictrees.NTBlocks;
import com.nostalgictrees.block.entity.AdvancedBeehiveBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import net.minecraft.server.level.ServerLevel;

public class AdvancedBeehiveBlock extends BeehiveBlock {

    public static final MapCodec<AdvancedBeehiveBlock> CODEC = simpleCodec(AdvancedBeehiveBlock::new);

    @Override
    public MapCodec<BeehiveBlock> codec() {
        return (MapCodec) CODEC;
    }

    public AdvancedBeehiveBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    // ======================== BLOCK ENTITY ========================

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AdvancedBeehiveBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null :
                createTickerHelper(type, NTBlocks.ADVANCED_BEEHIVE_BE.get(),
                        AdvancedBeehiveBlockEntity::serverTick);
    }

    // ======================== GUI ========================

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof AdvancedBeehiveBlockEntity beehiveBE) {
                serverPlayer.openMenu(beehiveBE, pos);
            }
        }
        return InteractionResult.SUCCESS;
    }

    // ======================== DROPS ========================

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof AdvancedBeehiveBlockEntity beehiveBE) {
                Containers.dropContents(level, pos, beehiveBE);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof AdvancedBeehiveBlockEntity beehiveBE) {
            Containers.dropContents(level, pos, beehiveBE);
        }
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }
}
