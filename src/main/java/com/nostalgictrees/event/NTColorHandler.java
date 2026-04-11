package com.nostalgictrees.event;

import com.nostalgictrees.NTBlocks;
import com.nostalgictrees.NTItems;
import com.nostalgictrees.NostalgicTrees;
import com.nostalgictrees.data.NTTreeRegistry;
import com.nostalgictrees.data.ResourceTreeType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid = NostalgicTrees.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class NTColorHandler {

    @SubscribeEvent
    public static void onBlockColors(RegisterColorHandlersEvent.Block event) {
        for (ResourceTreeType tree : NTTreeRegistry.getAllTrees()) {
            int c = tree.color() | 0xFF000000;
            String n = tree.name();
            try {
                Block log = NTBlocks.getLogBlock(n);
                Block stripped = NTBlocks.getStrippedLogBlock(n);
                Block leaves = NTBlocks.getLeavesBlock(n);
                Block sapling = NTBlocks.getSaplingBlock(n);

                if (log != null) event.register((s,l,p,t) -> c, log);
                if (stripped != null) event.register((s,l,p,t) -> c, stripped);
                if (leaves != null) event.register((s,l,p,t) -> c, leaves);
                if (sapling != null) event.register((s,l,p,t) -> c, sapling);
            } catch (Exception e) {
                NostalgicTrees.LOGGER.error("Block color registration FAILED for: {}", n, e);
            }
        }
    }

    @SubscribeEvent
    public static void onItemColors(RegisterColorHandlersEvent.Item event) {
        for (ResourceTreeType tree : NTTreeRegistry.getAllTrees()) {
            int c = tree.color() | 0xFF000000;
            String n = tree.name();
            try {
                Block log = NTBlocks.getLogBlock(n);
                Block stripped = NTBlocks.getStrippedLogBlock(n);
                Block leaves = NTBlocks.getLeavesBlock(n);
                Block sapling = NTBlocks.getSaplingBlock(n);
                Item apple = NTItems.getAppleItem(n);
                Item chunk = NTItems.getChunkItem(n);

                if (log != null) event.register((s,t) -> c, log.asItem());
                if (stripped != null) event.register((s,t) -> c, stripped.asItem());
                if (leaves != null) event.register((s,t) -> c, leaves.asItem());
                if (sapling != null) event.register((s,t) -> c, sapling.asItem());
                if (apple != null) event.register((s,t) -> c, apple);
                if (chunk != null) event.register((s,t) -> c, chunk);

                // Honeycomb tinting
                var honeycombDef = NTItems.getAllHoneycombItems().get(n);
                if (honeycombDef != null) event.register((s,t) -> c, honeycombDef.get());
            } catch (Exception e) {
                NostalgicTrees.LOGGER.error("Item color registration FAILED for: {}", n, e);
            }
        }
    }
}
