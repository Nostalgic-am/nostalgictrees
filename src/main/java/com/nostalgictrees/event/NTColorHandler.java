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
        NostalgicTrees.LOGGER.info("NTColorHandler: Registering BLOCK colors...");
        int count = 0;
        for (ResourceTreeType tree : NTTreeRegistry.getAllTrees()) {
            int c = tree.color() | 0xFF000000;
            String n = tree.name();
            try {
                Block log = NTBlocks.getLogBlock(n);
                Block stripped = NTBlocks.getStrippedLogBlock(n);
                Block leaves = NTBlocks.getLeavesBlock(n);
                Block sapling = NTBlocks.getSaplingBlock(n);

                if (log != null) { event.register((s,l,p,t) -> c, log); count++; }
                if (stripped != null) { event.register((s,l,p,t) -> c, stripped); count++; }
                if (leaves != null) { event.register((s,l,p,t) -> c, leaves); count++; }
                if (sapling != null) { event.register((s,l,p,t) -> c, sapling); count++; }
            } catch (Exception e) {
                NostalgicTrees.LOGGER.error("Block color registration FAILED for: {}", n, e);
            }
        }
        NostalgicTrees.LOGGER.info("NTColorHandler: Registered {} BLOCK color handlers", count);
    }

    @SubscribeEvent
    public static void onItemColors(RegisterColorHandlersEvent.Item event) {
        NostalgicTrees.LOGGER.info("NTColorHandler: Registering ITEM colors...");
        int count = 0;
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

                if (log != null) { event.register((s,t) -> c, log.asItem()); count++; }
                if (stripped != null) { event.register((s,t) -> c, stripped.asItem()); count++; }
                if (leaves != null) { event.register((s,t) -> c, leaves.asItem()); count++; }
                if (sapling != null) { event.register((s,t) -> c, sapling.asItem()); count++; }
                if (apple != null) { event.register((s,t) -> c, apple); count++; }
                if (chunk != null) { event.register((s,t) -> c, chunk); count++; }
            } catch (Exception e) {
                NostalgicTrees.LOGGER.error("Item color registration FAILED for: {}", n, e);
            }
        }
        NostalgicTrees.LOGGER.info("NTColorHandler: Registered {} ITEM color handlers", count);
    }
}
