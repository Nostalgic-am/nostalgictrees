package com.nostalgictrees.event;

import com.nostalgictrees.NTBlocks;
import com.nostalgictrees.NostalgicTrees;
import com.nostalgictrees.client.DryingRackRenderer;
import com.nostalgictrees.client.AdvancedBeehiveScreen;
import com.nostalgictrees.data.NTTreeRegistry;
import com.nostalgictrees.data.ResourceTreeType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = NostalgicTrees.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class NTClientEventHandler {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            for (ResourceTreeType tree : NTTreeRegistry.getAllTrees()) {
                Block sapling = NTBlocks.getSaplingBlock(tree.name());
                Block leaves = NTBlocks.getLeavesBlock(tree.name());

                if (sapling != null) {
                    ItemBlockRenderTypes.setRenderLayer(sapling, RenderType.cutout());
                }
                if (leaves != null) {
                    ItemBlockRenderTypes.setRenderLayer(leaves, RenderType.cutoutMipped());
                }
            }

            // Drying rack uses cutout for transparency
            ItemBlockRenderTypes.setRenderLayer(NTBlocks.DRYING_RACK.get(), RenderType.cutout());
        });
    }

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(NTBlocks.ADVANCED_BEEHIVE_MENU.get(), AdvancedBeehiveScreen::new);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(NTBlocks.DRYING_RACK_BE.get(), DryingRackRenderer::new);
    }
}
