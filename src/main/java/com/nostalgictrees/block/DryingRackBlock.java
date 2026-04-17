package com.nostalgictrees.block;

import com.mojang.serialization.MapCodec;
import com.nostalgictrees.block.entity.DryingRackBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import net.minecraft.server.level.ServerLevel;

public class DryingRackBlock extends BaseEntityBlock {
    public static final MapCodec<DryingRackBlock> CODEC = simpleCodec(DryingRackBlock::new);
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

    private static final VoxelShape SHAPE_NORTH = Block.box(0, 14, 0, 16, 16, 4);
    private static final VoxelShape SHAPE_SOUTH = Block.box(0, 14, 12, 16, 16, 16);
    private static final VoxelShape SHAPE_WEST = Block.box(0, 14, 0, 4, 16, 16);
    private static final VoxelShape SHAPE_EAST = Block.box(12, 14, 0, 16, 16, 16);

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    /*
     * 26.1: Block.Properties require a registry ID to be injected before the superclass
     * constructor runs. NeoForge's DeferredRegister.Blocks#registerBlock(name, factory, properties)
     * auto-injects the ID — the factory receives those pre-baked Properties and passes them through.
     */
    public DryingRackBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
            default -> SHAPE_NORTH;
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();
        if (clickedFace.getAxis().isHorizontal()) {
            return this.defaultBlockState().setValue(FACING, clickedFace.getOpposite());
        }
        return null;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DryingRackBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, com.nostalgictrees.NTBlocks.DRYING_RACK_BE.get(),
                DryingRackBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof DryingRackBlockEntity rack) {
            if (!stack.isEmpty() && rack.getItem().isEmpty()) {
                ItemStack toPlace = stack.copy();
                toPlace.setCount(1);
                rack.setItem(toPlace);
                stack.shrink(1);
                level.sendBlockUpdated(pos, state, state, 3);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof DryingRackBlockEntity rack) {
            if (!rack.getItem().isEmpty()) {
                ItemStack removed = rack.removeItem();
                if (!player.getInventory().add(removed)) {
                    player.drop(removed, false);
                }
                level.sendBlockUpdated(pos, state, state, 3);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof DryingRackBlockEntity rack && !rack.getItem().isEmpty()) {
            Containers.dropItemStack(level, pos.getX(), pos.getY() + 0.5, pos.getZ(), rack.getItem());
        }
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }
}
