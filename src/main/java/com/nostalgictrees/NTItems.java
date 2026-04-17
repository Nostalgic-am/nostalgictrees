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

    /*
     * 26.1: Item.Properties require a registry ID to be injected before the Item
     * constructor runs. DeferredRegister.Items#registerItem(name, factory, Supplier<Properties>)
     * auto-injects the ID — the factory takes Properties and passes them to the Item.
     * Properties are wrapped in a Supplier so NeoForge can create a fresh copy per item
     * and call .setId(...) on it.
     */

    // === Mallets ===
    public static final DeferredItem<Item> WOODEN_MALLET = ITEMS.registerItem("wooden_mallet",
            props -> new MalletItem(MalletTier.WOOD, malletProps(MalletTier.WOOD, props)),
            Item.Properties::new);
    public static final DeferredItem<Item> STONE_MALLET = ITEMS.registerItem("stone_mallet",
            props -> new MalletItem(MalletTier.STONE, malletProps(MalletTier.STONE, props)),
            Item.Properties::new);
    public static final DeferredItem<Item> IRON_MALLET = ITEMS.registerItem("iron_mallet",
            props -> new MalletItem(MalletTier.IRON, malletProps(MalletTier.IRON, props)),
            Item.Properties::new);
    public static final DeferredItem<Item> GOLDEN_MALLET = ITEMS.registerItem("golden_mallet",
            props -> new MalletItem(MalletTier.GOLD, malletProps(MalletTier.GOLD, props)),
            Item.Properties::new);
    public static final DeferredItem<Item> DIAMOND_MALLET = ITEMS.registerItem("diamond_mallet",
            props -> new MalletItem(MalletTier.DIAMOND, malletProps(MalletTier.DIAMOND, props)),
            Item.Properties::new);
    public static final DeferredItem<Item> NETHERITE_MALLET = ITEMS.registerItem("netherite_mallet",
            props -> new MalletItem(MalletTier.NETHERITE, malletProps(MalletTier.NETHERITE, props)),
            Item.Properties::new);

    /** Applies tier-specific durability + enchantability to the id-injected Properties. */
    private static Item.Properties malletProps(MalletTier tier, Item.Properties base) {
        return base
                .durability(tier.getMaxDurability())
                .stacksTo(1)
                .enchantable(tier.getVanillaMaterial().enchantmentValue());
    }

    // === Resource Beehive block item ===
    public static final DeferredItem<BlockItem> ADVANCED_BEEHIVE_ITEM =
            ITEMS.registerSimpleBlockItem(NTBlocks.ADVANCED_BEEHIVE);

    // === Drying Rack block item ===
    public static final DeferredItem<BlockItem> DRYING_RACK_ITEM =
            ITEMS.registerSimpleBlockItem(NTBlocks.DRYING_RACK);

    // === RGB Dye ===
    public static final DeferredItem<Item> RGB_DYE = ITEMS.registerItem("rgb_dye",
            Item::new, Item.Properties::new);

    // === Tree-specific items (dynamic from config) ===
    private static final Map<String, DeferredItem<Item>> APPLE_ITEMS = new HashMap<>();
    private static final Map<String, DeferredItem<Item>> CHUNK_ITEMS = new HashMap<>();
    private static final Map<String, DeferredItem<Item>> HONEYCOMB_ITEMS = new HashMap<>();
    private static final Map<String, DeferredItem<BlockItem>> LOG_ITEMS = new HashMap<>();
    private static final Map<String, DeferredItem<BlockItem>> STRIPPED_LOG_ITEMS = new HashMap<>();
    private static final Map<String, DeferredItem<BlockItem>> LEAVES_ITEMS = new HashMap<>();
    private static final Map<String, DeferredItem<BlockItem>> SAPLING_ITEMS = new HashMap<>();

    static {
        for (ResourceTreeType tree : NTTreeRegistry.getAllTrees()) {
            registerTreeItems(tree);
        }
    }

    private static void registerTreeItems(ResourceTreeType tree) {
        String name = tree.name();

        APPLE_ITEMS.put(name, ITEMS.registerItem(tree.appleId(),
                Item::new, Item.Properties::new));

        CHUNK_ITEMS.put(name, ITEMS.registerItem(tree.chunkId(),
                Item::new, Item.Properties::new));

        HONEYCOMB_ITEMS.put(name, ITEMS.registerItem(tree.honeycombId(),
                Item::new, Item.Properties::new));

        // BlockItems use the safe registerSimpleBlockItem helper — defers block lookup
        // until the item actually binds, so registration order doesn't matter.
        LOG_ITEMS.put(name,
                ITEMS.registerSimpleBlockItem(NTBlocks.getAllLogBlocks().get(name)));
        STRIPPED_LOG_ITEMS.put(name,
                ITEMS.registerSimpleBlockItem(NTBlocks.getAllStrippedLogBlocks().get(name)));
        LEAVES_ITEMS.put(name,
                ITEMS.registerSimpleBlockItem(NTBlocks.getAllLeavesBlocks().get(name)));
        SAPLING_ITEMS.put(name,
                ITEMS.registerSimpleBlockItem(NTBlocks.getAllSaplingBlocks().get(name)));
    }

    public static Item getAppleItem(String treeName) {
        DeferredItem<Item> item = APPLE_ITEMS.get(treeName);
        return item != null ? item.get() : null;
    }

    public static Item getChunkItem(String treeName) {
        DeferredItem<Item> item = CHUNK_ITEMS.get(treeName);
        return item != null ? item.get() : null;
    }

    public static ItemStack getHoneycombItem(String treeName) {
        DeferredItem<Item> item = HONEYCOMB_ITEMS.get(treeName);
        return item != null ? new ItemStack(item.get()) : ItemStack.EMPTY;
    }

    public static Map<String, DeferredItem<Item>> getAllAppleItems() { return APPLE_ITEMS; }
    public static Map<String, DeferredItem<Item>> getAllChunkItems() { return CHUNK_ITEMS; }
    public static Map<String, DeferredItem<Item>> getAllHoneycombItems() { return HONEYCOMB_ITEMS; }
    public static Map<String, DeferredItem<BlockItem>> getAllSaplingItems() { return SAPLING_ITEMS; }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}