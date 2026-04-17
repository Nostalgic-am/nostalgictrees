package com.nostalgictrees.event;

import com.nostalgictrees.NTBlocks;
import com.nostalgictrees.NostalgicTrees;
import com.nostalgictrees.data.NTTreeRegistry;
import com.nostalgictrees.data.ResourceTreeType;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.color.block.BlockTintSources;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

import java.util.List;

/*
 * Block tinting for tree variants. Each tree type gets a constant color tint.
 *
 * 26.1 / FML 11.0 changes vs 1.21.1:
 *   - RegisterColorHandlersEvent.Block         -> RegisterColorHandlersEvent.BlockTintSources
 *   - Old color provider lambda                -> List<BlockTintSource> passed directly
 *   - @EventBusSubscriber no longer supports the mod bus (no 'bus' parameter in FML 11),
 *     so mod-bus event handlers must be registered manually via IEventBus#addListener.
 *
 * Item coloring is data-driven via each item JSON's "tints" array (handled in DynamicResourceGenerator).
 */
public class NTColorHandler {

    /** Called from the main mod constructor on the CLIENT dist to wire up the listener. */
    public static void register(IEventBus modEventBus) {
        NostalgicTrees.LOGGER.info(">>>> NTColorHandler.register called <<<<");
        modEventBus.addListener(NTColorHandler::onBlockTintSources);
    }

    public static void onBlockTintSources(RegisterColorHandlersEvent.BlockTintSources event) {
        int treeCount = NTTreeRegistry.getAllTrees().size();
        NostalgicTrees.LOGGER.info(">>>> onBlockTintSources fired for {} trees <<<<", treeCount);

        int registered = 0;
        for (ResourceTreeType tree : NTTreeRegistry.getAllTrees()) {
            String n = tree.name();
            if (n.equals("rgb")) continue;
            int color = tree.color() | 0xFF000000;

            try {
                Block log      = NTBlocks.getLogBlock(n);
                Block stripped = NTBlocks.getStrippedLogBlock(n);
                Block leaves   = NTBlocks.getLeavesBlock(n);
                Block sapling  = NTBlocks.getSaplingBlock(n);

                List<BlockTintSource> layers = List.of(BlockTintSources.constant(color));

                if (log != null)      { event.getBlockColors().register(layers, log);      registered++; }
                if (stripped != null) { event.getBlockColors().register(layers, stripped); registered++; }
                if (leaves != null)   { event.getBlockColors().register(layers, leaves);   registered++; }
                if (sapling != null)  { event.getBlockColors().register(layers, sapling);  registered++; }
            } catch (Exception e) {
                NostalgicTrees.LOGGER.error("Block tint registration FAILED for tree '{}'", n, e);
            }
        }

        NostalgicTrees.LOGGER.info(">>>> NTColorHandler registered tints for {} blocks <<<<", registered);
    }
}
