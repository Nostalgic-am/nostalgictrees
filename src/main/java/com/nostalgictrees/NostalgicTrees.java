package com.nostalgictrees;

import com.nostalgictrees.data.DynamicResourceGenerator;
import com.nostalgictrees.data.InMemoryPackResources;
import com.nostalgictrees.data.NTTreeRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.minecraft.server.packs.PackResources;

import java.util.Optional;

@Mod(NostalgicTrees.MODID)
public class NostalgicTrees {
    public static final String MODID = "nostalgictrees";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static InMemoryPackResources generatedPack;

    public NostalgicTrees(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Nostalgic Trees initializing...");

        NTTreeRegistry.init();

        // Generate all resources in memory — no files written to config folder
        generatedPack = DynamicResourceGenerator.generate(NTTreeRegistry.getAllTrees());

        NTBlocks.register(modEventBus);
        NTItems.register(modEventBus);
        NTCreativeTab.register(modEventBus);
        NTRecipes.register(modEventBus);

        modEventBus.addListener(this::addPackFinders);

        LOGGER.info("Nostalgic Trees initialized with {} trees!", NTTreeRegistry.getAllTrees().size());
    }

    private void addPackFinders(AddPackFindersEvent event) {
        if (generatedPack == null) return;

        event.addRepositorySource(consumer -> {
            Pack pack = Pack.readMetaAndCreate(
                    generatedPack.location(),
                    new Pack.ResourcesSupplier() {
                        @Override
                        public PackResources openPrimary(PackLocationInfo info) {
                            return generatedPack;
                        }

                        @Override
                        public PackResources openFull(PackLocationInfo info, Pack.Metadata metadata) {
                            return generatedPack;
                        }
                    },
                    event.getPackType(),
                    new PackSelectionConfig(true, Pack.Position.BOTTOM, false)
            );
            if (pack != null) {
                consumer.accept(pack);
                LOGGER.info("Registered in-memory {} pack",
                        event.getPackType() == PackType.CLIENT_RESOURCES ? "resource" : "data");
            }
        });
    }
}
