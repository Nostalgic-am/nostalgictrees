package com.nostalgictrees;

import com.nostalgictrees.data.MalletTier;
import com.nostalgictrees.data.NTTreeRegistry;
import com.nostalgictrees.data.ResourceTreeType;
import com.nostalgictrees.item.MalletItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.Map;

public class NTItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NostalgicTrees.MODID);

    // === Mallets ===
    public static final DeferredItem<Item> WOODEN_MALLET = ITEMS.register("wooden_mallet",
            () -> new MalletItem(MalletTier.WOOD));
    public static final DeferredItem<Item> STONE_MALLET = ITEMS.register("stone_mallet",
            () -> new MalletItem(MalletTier.STONE));
    public static final DeferredItem<Item> IRON_MALLET = ITEMS.register("iron_mallet",
            () -> new MalletItem(MalletTier.IRON));
    public static final DeferredItem<Item> GOLDEN_MALLET = ITEMS.register("golden_mallet",
            () -> new MalletItem(MalletTier.GOLD));
    public static final DeferredItem<Item> DIAMOND_MALLET = ITEMS.register("diamond_mallet",
            () -> new MalletItem(MalletTier.DIAMOND));
    public static final DeferredItem<Item> NETHERITE_MALLET = ITEMS.register("netherite_mallet",
            () -> new MalletItem(MalletTier.NETHERITE));

    // === Resource Beehive block item ===
    public static final DeferredItem<Item> RESOURCE_BEEHIVE_ITEM = ITEMS.register("resource_beehive",
            () -> new BlockItem(NTBlocks.RESOURCE_BEEHIVE.get(), new Item.Properties()));

    // === Drying Rack block item ===
    public static final DeferredItem<Item> DRYING_RACK_ITEM = ITEMS.register("drying_rack",
            () -> new BlockItem(NTBlocks.DRYING_RACK.get(), new Item.Properties()));

    // === RGB Dye ===
    public static final DeferredItem<Item> RGB_DYE = ITEMS.register("rgb_dye",
            () -> new Item(new Item.Properties()));

    // === Tree-specific items (dynamic from config) ===
    private static final Map<String, DeferredItem<Item>> APPLE_ITEMS = new HashMap<>();
    private static final Map<String, DeferredItem<Item>> CHUNK_ITEMS = new HashMap<>();
    private static final Map<String, DeferredItem<Item>> HONEYCOMB_ITEMS = new HashMap<>();
    private static final Map<String, DeferredItem<Item>> LOG_ITEMS = new HashMap<>();
    private static final Map<String, DeferredItem<Item>> STRIPPED_LOG_ITEMS = new HashMap<>();
    private static final Map<String, DeferredItem<Item>> LEAVES_ITEMS = new HashMap<>();
    private static final Map<String, DeferredItem<Item>> SAPLING_ITEMS = new HashMap<>();

    static {
        for (ResourceTreeType tree : NTTreeRegistry.getAllTrees()) {
            registerTreeItems(tree);
        }
    }

    private static void registerTreeItems(ResourceTreeType tree) {
        String name = tree.name();

        APPLE_ITEMS.put(name, ITEMS.register(tree.appleId(),
                () -> new Item(new Item.Properties())));

        CHUNK_ITEMS.put(name, ITEMS.register(tree.chunkId(),
                () -> new Item(new Item.Properties())));

        HONEYCOMB_ITEMS.put(name, ITEMS.register(tree.honeycombId(),
                () -> new Item(new Item.Properties())));

        LOG_ITEMS.put(name, ITEMS.register(tree.logId(),
                () -> new BlockItem(NTBlocks.getLogBlock(name), new Item.Properties())));
        STRIPPED_LOG_ITEMS.put(name, ITEMS.register(tree.strippedLogId(),
                () -> new BlockItem(NTBlocks.getStrippedLogBlock(name), new Item.Properties())));
        LEAVES_ITEMS.put(name, ITEMS.register(tree.leavesId(),
                () -> new BlockItem(NTBlocks.getLeavesBlock(name), new Item.Properties())));
        SAPLING_ITEMS.put(name, ITEMS.register(tree.saplingId(),
                () -> new BlockItem(NTBlocks.getSaplingBlock(name), new Item.Properties())));
    }

    public static Item getAppleItem(String treeName) {
        DeferredItem<Item> item = APPLE_ITEMS.get(treeName);
        return item != null ? item.get() : null;
    }

    public static Item getChunkItem(String treeName) {
        DeferredItem<Item> item = CHUNK_ITEMS.get(treeName);
        return item != null ? item.get() : null;
    }

    /**
     * Get a honeycomb ItemStack for a given tree name.
     */
    public static ItemStack getHoneycombItem(String treeName) {
        DeferredItem<Item> item = HONEYCOMB_ITEMS.get(treeName);
        return item != null ? new ItemStack(item.get()) : ItemStack.EMPTY;
    }

    public static Map<String, DeferredItem<Item>> getAllAppleItems() { return APPLE_ITEMS; }
    public static Map<String, DeferredItem<Item>> getAllChunkItems() { return CHUNK_ITEMS; }
    public static Map<String, DeferredItem<Item>> getAllHoneycombItems() { return HONEYCOMB_ITEMS; }
    public static Map<String, DeferredItem<Item>> getAllSaplingItems() { return SAPLING_ITEMS; }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
