package com.nostalgictrees.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.nostalgictrees.block.DryingRackBlock;
import com.nostalgictrees.block.entity.DryingRackBlockEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;

public class DryingRackRenderer implements BlockEntityRenderer<DryingRackBlockEntity> {

    private final ItemRenderer itemRenderer;

    public DryingRackRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(DryingRackBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack item = be.getItem();
        if (item.isEmpty()) return;

        poseStack.pushPose();

        Direction facing = be.getBlockState().getValue(DryingRackBlock.FACING);

        double itemX = 0.5;
        double itemZ = 0.5;
        float yaw = 0f;

        switch (facing) {
            case NORTH -> { itemZ = 0.19; yaw = 180f; }
            case SOUTH -> { itemZ = 0.81; yaw = 0f; }
            case WEST ->  { itemX = 0.19; yaw = 90f; }
            case EAST ->  { itemX = 0.81; yaw = 270f; }
        }

        poseStack.translate(itemX, 0.65, itemZ);
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        poseStack.scale(0.6f, 0.6f, 0.6f);

        BlockPos pos = be.getBlockPos().above();
        int light = be.getLevel() != null ?
                LightTexture.pack(
                        be.getLevel().getBrightness(LightLayer.BLOCK, pos),
                        be.getLevel().getBrightness(LightLayer.SKY, pos))
                : packedLight;

        itemRenderer.renderStatic(item, ItemDisplayContext.FIXED, light, packedOverlay,
                poseStack, bufferSource, be.getLevel(), 0);

        poseStack.popPose();
    }
}