package com.nostalgictrees;

import com.nostalgictrees.block.ResourceLeavesBlock;
import com.nostalgictrees.block.ResourceLogBlock;
import com.nostalgictrees.block.ResourceSaplingBlock;
import com.nostalgictrees.data.NTTreeRegistry;
import com.nostalgictrees.data.ResourceTreeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.Map;

public class NTBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(NostalgicTrees.MODID);

    private static final Map<String, DeferredBlock<Block>> LOG_BLOCKS = new HashMap<>();
    private static final Map<String, DeferredBlock<Block>> STRIPPED_LOG_BLOCKS = new HashMap<>();
    private static final Map<String, DeferredBlock<Block>> LEAVES_BLOCKS = new HashMap<>();
    private static final Map<String, DeferredBlock<Block>> SAPLING_BLOCKS = new HashMap<>();

    static {
        for (ResourceTreeType tree : NTTreeRegistry.getAllTrees()) {
            registerTreeBlocks(tree);
        }
    }

    private static void registerTreeBlocks(ResourceTreeType tree) {
        String name = tree.name();
        LOG_BLOCKS.put(name, BLOCKS.register(tree.logId(),
                () -> new ResourceLogBlock(name, tree.tier())));
        STRIPPED_LOG_BLOCKS.put(name, BLOCKS.register(tree.strippedLogId(),
                () -> new Block(BlockBehaviour.Properties.of().strength(2.0f).sound(SoundType.WOOD))));
        LEAVES_BLOCKS.put(name, BLOCKS.register(tree.leavesId(),
                () -> new ResourceLeavesBlock(name, tree.tier())));
        SAPLING_BLOCKS.put(name, BLOCKS.register(tree.saplingId(),
                () -> new ResourceSaplingBlock(name, tree.tier())));
    }

    public static Block getLogBlock(String treeName) {
        DeferredBlock<Block> b = LOG_BLOCKS.get(treeName); return b != null ? b.get() : null;
    }
    public static Block getStrippedLogBlock(String treeName) {
        DeferredBlock<Block> b = STRIPPED_LOG_BLOCKS.get(treeName); return b != null ? b.get() : null;
    }
    public static Block getLeavesBlock(String treeName) {
        DeferredBlock<Block> b = LEAVES_BLOCKS.get(treeName); return b != null ? b.get() : null;
    }
    public static Block getSaplingBlock(String treeName) {
        DeferredBlock<Block> b = SAPLING_BLOCKS.get(treeName); return b != null ? b.get() : null;
    }

    public static Map<String, DeferredBlock<Block>> getAllLogBlocks() { return LOG_BLOCKS; }
    public static Map<String, DeferredBlock<Block>> getAllStrippedLogBlocks() { return STRIPPED_LOG_BLOCKS; }
    public static Map<String, DeferredBlock<Block>> getAllLeavesBlocks() { return LEAVES_BLOCKS; }
    public static Map<String, DeferredBlock<Block>> getAllSaplingBlocks() { return SAPLING_BLOCKS; }

    public static void register(IEventBus eventBus) { BLOCKS.register(eventBus); }
}
