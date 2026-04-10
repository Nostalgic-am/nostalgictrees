package com.nostalgictrees;

import com.nostalgictrees.data.DynamicResourceGenerator;
import com.nostalgictrees.data.NTTreeRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import java.nio.file.Path;
import java.util.Optional;

@Mod(NostalgicTrees.MODID)
public class NostalgicTrees {
    public static final String MODID = "nostalgictrees";
    public static final Logger LOGGER = LogUtils.getLogger();
    private static Path generatedResourcesPath;

    public NostalgicTrees(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Nostalgic Trees initializing...");

        NTTreeRegistry.init();

        generatedResourcesPath = FMLPaths.CONFIGDIR.get().resolve("nostalgictrees/resources");
        DynamicResourceGenerator.generate(generatedResourcesPath, NTTreeRegistry.getAllTrees());

        NTBlocks.register(modEventBus);
        NTItems.register(modEventBus);
        NTCreativeTab.register(modEventBus);
        NTRecipes.register(modEventBus);

        modEventBus.addListener(this::addPackFinders);

        LOGGER.info("Nostalgic Trees initialized with {} trees!", NTTreeRegistry.getAllTrees().size());
    }

    private void addPackFinders(AddPackFindersEvent event) {
        if (generatedResourcesPath == null) return;

        event.addRepositorySource(consumer -> {
            PackLocationInfo locationInfo = new PackLocationInfo(
                    MODID + "_generated",
                    Component.literal("Nostalgic Trees Generated Resources"),
                    PackSource.BUILT_IN,
                    Optional.empty()
            );
            Pack pack = Pack.readMetaAndCreate(
                    locationInfo,
                    new PathPackResources.PathResourcesSupplier(generatedResourcesPath),
                    event.getPackType(),
                    new PackSelectionConfig(true, Pack.Position.BOTTOM, false)
            );
            if (pack != null) {
                consumer.accept(pack);
                LOGGER.info("Registered generated {} pack",
                        event.getPackType() == PackType.CLIENT_RESOURCES ? "resource" : "data");
            }
        });
    }
}
