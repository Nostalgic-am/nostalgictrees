package com.nostalgictrees;

import com.nostalgictrees.block.ResourceBeehiveBlock;
import com.nostalgictrees.block.ResourceLeavesBlock;
import com.nostalgictrees.block.ResourceLogBlock;
import com.nostalgictrees.block.ResourceSaplingBlock;
import com.nostalgictrees.block.entity.ResourceBeehiveBlockEntity;
import com.nostalgictrees.data.NTTreeRegistry;
import com.nostalgictrees.data.ResourceTreeType;
import com.nostalgictrees.menu.ResourceBeehiveMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import java.util.stream.Collectors;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class NTBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(NostalgicTrees.MODID);

    // Block Entity Types
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, NostalgicTrees.MODID);

    // Menu Types
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, NostalgicTrees.MODID);

    // === Resource Beehive ===
    public static final DeferredBlock<Block> RESOURCE_BEEHIVE = BLOCKS.register("resource_beehive",
            ResourceBeehiveBlock::new);

    // === Resource Beehive Block Entity ===
    public static final Supplier<BlockEntityType<ResourceBeehiveBlockEntity>> RESOURCE_BEEHIVE_BE =
            BLOCK_ENTITY_TYPES.register("resource_beehive",
                    () -> BlockEntityType.Builder.of(ResourceBeehiveBlockEntity::new,
                            RESOURCE_BEEHIVE.get()).build(null));

    // === Resource Beehive Menu ===
    public static final Supplier<MenuType<ResourceBeehiveMenu>> RESOURCE_BEEHIVE_MENU =
            MENU_TYPES.register("resource_beehive",
                    () -> IMenuTypeExtension.create(ResourceBeehiveMenu::new));
    // === POI Types (so bees discover our hive via BeeLocateHiveGoal) ===
    public static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, NostalgicTrees.MODID);

    public static final Supplier<PoiType> RESOURCE_BEEHIVE_POI =
            POI_TYPES.register("resource_beehive",
                    () -> new PoiType(
                            RESOURCE_BEEHIVE.get().getStateDefinition().getPossibleStates()
                                    .stream().collect(Collectors.toSet()),
                            0, 1));

    // === Tree blocks ===
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

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        BLOCK_ENTITY_TYPES.register(eventBus);
        MENU_TYPES.register(eventBus);
        POI_TYPES.register(eventBus);
    }
}
