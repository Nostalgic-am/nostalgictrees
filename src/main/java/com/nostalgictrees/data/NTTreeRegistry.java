package com.nostalgictrees.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.nostalgictrees.NostalgicTrees;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLPaths;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class NTTreeRegistry {
    private static final Map<String, ResourceTreeType> TREES = new LinkedHashMap<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void init() {
        TREES.clear();
        Path configDir = FMLPaths.CONFIGDIR.get().resolve("nostalgictrees/trees");
        try { Files.createDirectories(configDir); } catch (IOException e) { }

        // Write defaults if empty
        try {
            if (Files.list(configDir).noneMatch(p -> p.toString().endsWith(".json"))) {
                writeDefaults(configDir);
            }
        } catch (IOException e) { }

        // Load all JSONs
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(configDir, "*.json")) {
            for (Path file : stream) {
                try {
                    JsonObject obj = GSON.fromJson(Files.readString(file), JsonObject.class);
                    String name = obj.get("name").getAsString();
                    TreeTier tier = TreeTier.fromString(obj.has("tier") ? obj.get("tier").getAsString() : "tier_1");
                    String output = obj.get("output_item").getAsString();
                    int count = obj.has("output_count") ? obj.get("output_count").getAsInt() : 1;
                    int color = Integer.parseInt(obj.has("color") ? obj.get("color").getAsString() : "FFFFFF", 16);
                    Optional<String> reqMod = obj.has("required_mod") ? Optional.of(obj.get("required_mod").getAsString()) : Optional.empty();

                    if (reqMod.isPresent() && !net.neoforged.fml.ModList.get().isLoaded(reqMod.get())) continue;

                    TREES.put(name, new ResourceTreeType(name, tier, ResourceLocation.parse(output), count, color, reqMod));
                } catch (Exception e) {
                    NostalgicTrees.LOGGER.error("Failed to load tree: {}", file.getFileName(), e);
                }
            }
        } catch (IOException e) { }

        NostalgicTrees.LOGGER.info("Loaded {} resource trees", TREES.size());
    }

    private static void writeDefaults(Path dir) {
        w(dir,"dirt","tier_1","minecraft:dirt",4,"8B5E2B");
        w(dir,"sand","tier_1","minecraft:sand",4,"DBD3A0");
        w(dir,"gravel","tier_1","minecraft:gravel",4,"7E7A73");
        w(dir,"clay","tier_1","minecraft:clay_ball",4,"9EA4B0");
        w(dir,"bone","tier_1","minecraft:bone",2,"E3DAC2");
        w(dir,"coal","tier_2","minecraft:coal",2,"2C2C2C");
        w(dir,"copper","tier_2","minecraft:raw_copper",2,"C06840");
        w(dir,"iron","tier_3","minecraft:raw_iron",1,"C9A78B");
        w(dir,"quartz","tier_3","minecraft:quartz",2,"E8DDD0");
        w(dir,"redstone","tier_3","minecraft:redstone",3,"A01010");
        w(dir,"gold","tier_4","minecraft:raw_gold",1,"F5D63D");
        w(dir,"lapis","tier_4","minecraft:lapis_lazuli",2,"2546C4");
        w(dir,"glowstone","tier_4","minecraft:glowstone_dust",3,"C99E35");
        w(dir,"diamond","tier_5","minecraft:diamond",1,"59E5D4");
        w(dir,"emerald","tier_5","minecraft:emerald",1,"30C74D");
        w(dir,"ender","tier_5","minecraft:ender_pearl",1,"0B4A3A");
        w(dir,"obsidian","tier_5","minecraft:obsidian",1,"5C2D8C");
    }

    private static void w(Path dir, String name, String tier, String output, int count, String color) {
        JsonObject o = new JsonObject();
        o.addProperty("name", name); o.addProperty("tier", tier);
        o.addProperty("output_item", output); o.addProperty("output_count", count);
        o.addProperty("color", color);
        try { Files.writeString(dir.resolve(name + ".json"), GSON.toJson(o)); } catch (IOException e) { }
    }

    public static Optional<ResourceTreeType> get(String name) { return Optional.ofNullable(TREES.get(name)); }
    public static Collection<ResourceTreeType> getAllTrees() { return Collections.unmodifiableCollection(TREES.values()); }
}