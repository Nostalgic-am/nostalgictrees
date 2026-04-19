package com.nostalgictrees.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.nostalgictrees.NostalgicTrees;
import net.minecraft.resources.Identifier;
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

        // Write any missing default tree files.
        // Per-file existence check: existing files are never touched (user edits preserved),
        // missing files regenerate from defaults (new trees appear on update; deletions
        // regenerate since modpack devs can override/remove via KubeJS or datapacks).
        writeDefaults(configDir);

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

                    TREES.put(name, new ResourceTreeType(name, tier, Identifier.parse(output), count, color, reqMod));
                } catch (Exception e) {
                    NostalgicTrees.LOGGER.error("Failed to load tree: {}", file.getFileName(), e);
                }
            }
        } catch (IOException e) { }

        NostalgicTrees.LOGGER.info("Loaded {} resource trees", TREES.size());
    }

    private static void writeDefaults(Path dir) {
        // Tier 1 (crafting recipes)
        w(dir,"dirt","tier_1","minecraft:dirt",4,"8B5E2B");
        w(dir,"stone","tier_1","minecraft:cobblestone",4,"7B7B7B");
        w(dir,"sand","tier_1","minecraft:sand",4,"DBD3A0");
        w(dir,"gravel","tier_1","minecraft:gravel",4,"7E7A73");
        w(dir,"clay","tier_1","minecraft:clay_ball",4,"9EA4B0");
        w(dir,"bone","tier_1","minecraft:bone",2,"E3DAC2");
        // Tier 2 (crafting + mutations)
        w(dir,"ice","tier_2","minecraft:ice",2,"A5D6F5");
        w(dir,"rgb","tier_2","nostalgictrees:rgb_dye",4,"FFFFFF");
        w(dir,"coal","tier_2","minecraft:coal",2,"2C2C2C");
        w(dir,"copper","tier_2","minecraft:raw_copper",2,"C06840");
        w(dir,"iron","tier_2","minecraft:raw_iron",1,"C9A78B");
        // Tier 3 (mutations, 5 pollinations)
        w(dir,"redstone","tier_3","minecraft:redstone",3,"A01010");
        w(dir,"amethyst","tier_3","minecraft:amethyst_shard",2,"9B6BC4");
        w(dir,"prismarine","tier_3","minecraft:prismarine_shard",2,"5B9E8F");
        w(dir,"experience","tier_3","minecraft:experience_bottle",1,"A8E830");
        // Tier 4 (mutations, 7 pollinations + catalyst)
        w(dir,"quartz","tier_4","minecraft:quartz",2,"E8DDD0");
        w(dir,"gold","tier_4","minecraft:raw_gold",1,"F5D63D");
        w(dir,"lapis","tier_4","minecraft:lapis_lazuli",2,"2546C4");
        w(dir,"glowstone","tier_4","minecraft:glowstone_dust",3,"C99E35");
        // Tier 5 (mutations, 10 pollinations + catalyst)
        w(dir,"diamond","tier_5","minecraft:diamond",1,"59E5D4");
        w(dir,"emerald","tier_5","minecraft:emerald",1,"30C74D");
        w(dir,"obsidian","tier_5","minecraft:obsidian",1,"5C2D8C");
        w(dir,"netherite","tier_5","minecraft:netherite_scrap",1,"4A3229");

        //Mekanism Trees
        w(dir,"osmium","tier_4","mekanism:ingot_osmium",1,"C8D4DD","mekanism");
        w(dir,"bronze","tier_4","mekanism:ingot_bronze",1,"FCC677","mekanism");
        w(dir,"steel","tier_4","mekanism:ingot_steel",1,"868683","mekanism");
        w(dir,"refined_obsidian","tier_4","mekanism:ingot_refined_obsidian",1,"9680B9","mekanism");
        w(dir,"refined_glowstone","tier_4","mekanism:ingot_refined_glowstone",1,"FCf1AA","mekanism");
        w(dir,"tin","tier_4","mekanism:ingot_tin",1,"F6F6E8","mekanism");
        w(dir,"lead","tier_4","mekanism:ingot_lead",1,"9FB4B2","mekanism");
        w(dir,"uranium","tier_4","mekanism:ingot_uranium",1,"A7F2A5","mekanism");
    }

    private static void w(Path dir, String name, String tier, String output, int count, String color) {
        w(dir, name, tier, output, count, color, null);
    }

    private static void w(Path dir, String name, String tier, String output, int count, String color, String requiredMod) {
        Path file = dir.resolve(name + ".json");
        // Per-file existence check: never overwrite. Users who edit a config keep their edits;
        // users who delete a config get it back on next launch (intentional — modpack devs
        // should use KubeJS/datapacks to remove recipes rather than deleting config files).
        if (Files.exists(file)) return;

        JsonObject o = new JsonObject();
        o.addProperty("name", name);
        o.addProperty("tier", tier);
        o.addProperty("output_item", output);
        o.addProperty("output_count", count);
        o.addProperty("color", color);
        if (requiredMod != null) {
            o.addProperty("required_mod", requiredMod);
        }
        try {
            Files.writeString(file, GSON.toJson(o));
            NostalgicTrees.LOGGER.info("Wrote default tree config: {}", name);
        } catch (IOException e) {
            NostalgicTrees.LOGGER.error("Failed to write default tree config: {}", name, e);
        }
    }

    public static Optional<ResourceTreeType> get(String name) { return Optional.ofNullable(TREES.get(name)); }
    public static Collection<ResourceTreeType> getAllTrees() { return Collections.unmodifiableCollection(TREES.values()); }
}