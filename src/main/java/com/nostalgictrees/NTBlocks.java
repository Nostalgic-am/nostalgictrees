package com.nostalgictrees;

import com.nostalgictrees.block.DryingRackBlock;
import com.nostalgictrees.block.AdvancedBeehiveBlock;
import com.nostalgictrees.block.ResourceLeavesBlock;
import com.nostalgictrees.block.ResourceLogBlock;
import com.nostalgictrees.block.ResourceSaplingBlock;
import com.nostalgictrees.block.entity.DryingRackBlockEntity;
import com.nostalgictrees.block.entity.AdvancedBeehiveBlockEntity;
import com.nostalgictrees.block.entity.ResourceSaplingBlockEntity;
import com.nostalgictrees.data.NTTreeRegistry;
import com.nostalgictrees.data.ResourceTreeType;
import com.nostalgictrees.menu.AdvancedBeehiveMenu;
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
    public static final DeferredBlock<Block> ADVANCED_BEEHIVE = BLOCKS.registerBlock(
            "advanced_beehive",
            AdvancedBeehiveBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .strength(2.0f)
                    .sound(SoundType.WOOD));

    // === Resource Beehive Block Entity ===
    // 26.1: BlockEntityType.Builder is gone; use the public varargs constructor directly.
    public static final Supplier<BlockEntityType<AdvancedBeehiveBlockEntity>> ADVANCED_BEEHIVE_BE =
            BLOCK_ENTITY_TYPES.register("advanced_beehive",
                    () -> new BlockEntityType<>(AdvancedBeehiveBlockEntity::new,
                            ADVANCED_BEEHIVE.get()));

    // === Resource Beehive Menu ===
    public static final Supplier<MenuType<AdvancedBeehiveMenu>> ADVANCED_BEEHIVE_MENU =
            MENU_TYPES.register("advanced_beehive",
                    () -> IMenuTypeExtension.create(AdvancedBeehiveMenu::new));

    // === POI Types (so bees discover our hive via BeeLocateHiveGoal) ===
    public static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, NostalgicTrees.MODID);

    public static final Supplier<PoiType> ADVANCED_BEEHIVE_POI =
            POI_TYPES.register("advanced_beehive",
                    () -> new PoiType(
                            ADVANCED_BEEHIVE.get().getStateDefinition().getPossibleStates()
                                    .stream().collect(Collectors.toSet()),
                            0, 1));

    // === Drying Rack ===
    public static final DeferredBlock<Block> DRYING_RACK = BLOCKS.registerBlock(
            "drying_rack",
            DryingRackBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .strength(1.0f)
                    .sound(SoundType.WOOD)
                    .noOcclusion());

    public static final Supplier<BlockEntityType<DryingRackBlockEntity>> DRYING_RACK_BE =
            BLOCK_ENTITY_TYPES.register("drying_rack",
                    () -> new BlockEntityType<>(DryingRackBlockEntity::new,
                            DRYING_RACK.get()));

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

    // === Sapling Block Entity (registered after all saplings are created) ===
    public static final Supplier<BlockEntityType<ResourceSaplingBlockEntity>> SAPLING_BE =
            BLOCK_ENTITY_TYPES.register("resource_sapling",
                    () -> new BlockEntityType<>(ResourceSaplingBlockEntity::new,
                            SAPLING_BLOCKS.values().stream().map(DeferredBlock::get).toArray(Block[]::new)));

    private static void registerTreeBlocks(ResourceTreeType tree) {
        final String name = tree.name();

        // Log — custom Block with treeName + tier, mines with mallet tool
        LOG_BLOCKS.put(name, BLOCKS.registerBlock(
                tree.logId(),
                props -> new ResourceLogBlock(name, tree.tier(), props),
                () -> BlockBehaviour.Properties.of()
                        .strength(2.0f)
                        .sound(SoundType.WOOD)
                        .requiresCorrectToolForDrops()));

        // Stripped Log — plain Block
        STRIPPED_LOG_BLOCKS.put(name, BLOCKS.registerBlock(
                tree.strippedLogId(),
                Block::new,
                () -> BlockBehaviour.Properties.of()
                        .strength(2.0f)
                        .sound(SoundType.WOOD)));

        // Leaves — ResourceLeavesBlock with custom particle color
        LEAVES_BLOCKS.put(name, BLOCKS.registerBlock(
                tree.leavesId(),
                props -> new ResourceLeavesBlock(name, tree.tier(), props),
                () -> BlockBehaviour.Properties.of()
                        .strength(0.2f)
                        .randomTicks()
                        .sound(SoundType.GRASS)
                        .noOcclusion()
                        .isValidSpawn((state, level, pos, type) -> false)
                        .isSuffocating((state, level, pos) -> false)
                        .isViewBlocking((state, level, pos) -> false)));

        // Sapling — ResourceSaplingBlock
        SAPLING_BLOCKS.put(name, BLOCKS.registerBlock(
                tree.saplingId(),
                props -> new ResourceSaplingBlock(name, tree.tier(), props),
                () -> BlockBehaviour.Properties.of()
                        .noCollision()
                        .randomTicks()
                        .instabreak()
                        .sound(SoundType.GRASS)));
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