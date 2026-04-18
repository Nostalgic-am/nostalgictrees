package com.nostalgictrees.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.nostalgictrees.block.DryingRackBlock;
import com.nostalgictrees.block.entity.DryingRackBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/*
 * 26.1 BlockEntityRenderer migration:
 *   - Split into extractRenderState + submit phases (two-phase renderer).
 *   - ItemRenderer.renderStatic removed; use ItemModelResolver + ItemStackRenderState.
 *   - MultiBufferSource -> SubmitNodeCollector in the submit phase.
 *
 * Rendering logic matches the 1.21.1 original: hang the item below the shelf like an
 * item frame, using ItemDisplayContext.FIXED (vertical wall-mount orientation).
 * No X-axis rotation — that would lay the item flat, which is campfire behaviour.
 */
public class DryingRackRenderer implements BlockEntityRenderer<DryingRackBlockEntity, DryingRackRenderState> {
    private final ItemModelResolver itemModelResolver;

    public DryingRackRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public DryingRackRenderState createRenderState() {
        return new DryingRackRenderState();
    }

    @Override
    public void extractRenderState(
            DryingRackBlockEntity blockEntity,
            DryingRackRenderState state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.facing = blockEntity.getBlockState().getValue(DryingRackBlock.FACING);

        ItemStack itemStack = blockEntity.getItem();
        if (!itemStack.isEmpty()) {
            ItemStackRenderState renderState = new ItemStackRenderState();
            this.itemModelResolver.updateForTopItem(
                    renderState,
                    itemStack,
                    ItemDisplayContext.FIXED,
                    blockEntity.getLevel(),
                    null,
                    0
            );
            state.itemRenderState = renderState;
        } else {
            state.itemRenderState = null;
        }
    }

    @Override
    public void submit(
            DryingRackRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera
    ) {
        if (state.itemRenderState == null || state.itemRenderState.isEmpty()) return;

        // Position the item hanging below the shelf. The shelf model occupies y=14..16
        // and z=0..4 (facing north). Y=0.65 puts the item hanging just below the shelf;
        // the X/Z offset based on facing centers it on the shelf surface.
        double itemX = 0.5;
        double itemZ = 0.5;
        float yaw = 0f;
        switch (state.facing) {
            case NORTH -> { itemZ = 0.19; yaw = 180f; }
            case SOUTH -> { itemZ = 0.81; yaw = 0f; }
            case WEST ->  { itemX = 0.19; yaw = 90f; }
            case EAST ->  { itemX = 0.81; yaw = 270f; }
            default -> {}
        }

        poseStack.pushPose();
        poseStack.translate(itemX, 0.65, itemZ);
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        poseStack.scale(0.6f, 0.6f, 0.6f);

        state.itemRenderState.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }
}