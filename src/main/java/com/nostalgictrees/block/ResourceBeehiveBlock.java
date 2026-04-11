package com.nostalgictrees.block;

import com.mojang.serialization.MapCodec;
import com.nostalgictrees.NTBlocks;
import com.nostalgictrees.block.entity.ResourceBeehiveBlockEntity;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Resource Beehive — extends vanilla BeehiveBlock directly.
 *
 * All bee AI, honey_level blockstate, shearing, bottling, POI, and
 * entity pathfinding works out of the box because we ARE a BeehiveBlock.
 *
 * We override:
 *   - newBlockEntity() → our ResourceBeehiveBlockEntity (extends BeehiveBlockEntity)
 *   - getTicker() → our tick method for auto-production
 *   - useWithoutItem() → opens our custom GUI instead of vanilla behavior
 */
public class ResourceBeehiveBlock extends BeehiveBlock {
    public static final MapCodec<ResourceBeehiveBlock> CODEC = simpleCodec(p -> new ResourceBeehiveBlock());

    @Override
    public MapCodec<BeehiveBlock> codec() {
        return (MapCodec) CODEC;
    }

    public ResourceBeehiveBlock() {
        super(Properties.of()
                .strength(2.0f)
                .sound(net.minecraft.world.level.block.SoundType.WOOD));
    }

    // ======================== BLOCK ENTITY ========================

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ResourceBeehiveBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null :
                createTickerHelper(type, NTBlocks.RESOURCE_BEEHIVE_BE.get(),
                        ResourceBeehiveBlockEntity::serverTick);
    }

    // ======================== GUI ========================

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ResourceBeehiveBlockEntity beehiveBE) {
                serverPlayer.openMenu(beehiveBE, pos);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    // ======================== DROPS ========================

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        // Drop our custom inventory items
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ResourceBeehiveBlockEntity beehiveBE) {
                Containers.dropContents(level, pos, beehiveBE);
            }
        }
        // Let vanilla handle bee release, silk touch, honeycomb drops etc.
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ResourceBeehiveBlockEntity beehiveBE) {
                Containers.dropContents(level, pos, beehiveBE);
            }
        }
        // Let vanilla handle bee release
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
