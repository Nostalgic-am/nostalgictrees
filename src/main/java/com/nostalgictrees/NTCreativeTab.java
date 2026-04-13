package com.nostalgictrees;

import com.nostalgictrees.data.NTTreeRegistry;
import com.nostalgictrees.data.ResourceTreeType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public class NTCreativeTab {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NostalgicTrees.MODID);

    public static final Supplier<CreativeModeTab> NOSTALGIC_TAB = TABS.register("nostalgictrees_tab",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.nostalgictrees"))
                    .icon(() -> new ItemStack(NTItems.IRON_MALLET.get()))
                    .displayItems((params, output) -> {
                        // Mallets
                        output.accept(NTItems.WOODEN_MALLET.get());
                        output.accept(NTItems.STONE_MALLET.get());
                        output.accept(NTItems.IRON_MALLET.get());
                        output.accept(NTItems.GOLDEN_MALLET.get());
                        output.accept(NTItems.DIAMOND_MALLET.get());
                        output.accept(NTItems.NETHERITE_MALLET.get());

                        // Resource Beehive
                        output.accept(NTItems.RESOURCE_BEEHIVE_ITEM.get());

                        // Drying Rack
                        output.accept(NTItems.DRYING_RACK_ITEM.get());

                        // RGB Dye
                        output.accept(NTItems.RGB_DYE.get());

                        // Saplings
                        for (ResourceTreeType tree : NTTreeRegistry.getAllTrees()) {
                            var sapling = NTItems.getAllSaplingItems().get(tree.name());
                            if (sapling != null) output.accept(sapling.get());
                        }

                        // Logs
                        for (ResourceTreeType tree : NTTreeRegistry.getAllTrees()) {
                            Block log = NTBlocks.getLogBlock(tree.name());
                            if (log != null) output.accept(log.asItem());
                        }

                        // Stripped Logs
                        for (ResourceTreeType tree : NTTreeRegistry.getAllTrees()) {
                            Block stripped = NTBlocks.getStrippedLogBlock(tree.name());
                            if (stripped != null) output.accept(stripped.asItem());
                        }

                        // Leaves
                        for (ResourceTreeType tree : NTTreeRegistry.getAllTrees()) {
                            Block leaves = NTBlocks.getLeavesBlock(tree.name());
                            if (leaves != null) output.accept(leaves.asItem());
                        }

                        // Apples
                        for (ResourceTreeType tree : NTTreeRegistry.getAllTrees()) {
                            var apple = NTItems.getAllAppleItems().get(tree.name());
                            if (apple != null) output.accept(apple.get());
                        }

                        // Chunks
                        for (ResourceTreeType tree : NTTreeRegistry.getAllTrees()) {
                            var chunk = NTItems.getAllChunkItems().get(tree.name());
                            if (chunk != null) output.accept(chunk.get());
                        }

                        // Honeycombs
                        for (ResourceTreeType tree : NTTreeRegistry.getAllTrees()) {
                            var comb = NTItems.getAllHoneycombItems().get(tree.name());
                            if (comb != null) output.accept(comb.get());
                        }
                    })
                    .build()
    );

    public static void register(IEventBus eventBus) {
        TABS.register(eventBus);
    }
}
