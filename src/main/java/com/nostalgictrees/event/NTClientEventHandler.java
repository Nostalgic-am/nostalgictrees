package com.nostalgictrees.event;

import com.nostalgictrees.NTBlocks;
import com.nostalgictrees.NostalgicTrees;
import com.nostalgictrees.client.DryingRackRenderer;
import com.nostalgictrees.client.AdvancedBeehiveScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = NostalgicTrees.MODID, value = Dist.CLIENT)
public class NTClientEventHandler {

    // Render layer assignment (cutout for saplings/leaves/drying rack) is now handled via
    // data: blockstate JSONs specify "render_type" for each variant.
    // See: src/main/resources/assets/nostalgictrees/blockstates/*.json

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(NTBlocks.ADVANCED_BEEHIVE_MENU.get(), AdvancedBeehiveScreen::new);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(NTBlocks.DRYING_RACK_BE.get(), DryingRackRenderer::new);
    }
}