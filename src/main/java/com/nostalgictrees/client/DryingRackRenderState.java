package com.nostalgictrees.client;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

public class DryingRackRenderState extends BlockEntityRenderState {
    public Direction facing = Direction.NORTH;
    public @Nullable ItemStackRenderState itemRenderState;
}
