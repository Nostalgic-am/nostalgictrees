package com.nostalgictrees.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.nostalgictrees.NostalgicTrees;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import net.minecraft.server.packs.PackType;
import net.minecraft.resources.Identifier;

/*
 * 26.1 data format changes vs 1.21.1:
 *
 *   - Shaped recipe `key` entries:  {"H": {"item": "x"}}  ->  {"H": "x"}
 *   - Shapeless `ingredients`:      [{"item":"x"}]        ->  ["x"]
 *   - Smelting/smithing fields:     {"item": "x"}         ->  "x"  (bare string)
 *   - Tags in ingredients:          {"tag": "#t"}         ->  "#t"
 *
 *   - Item definitions: NEW required file at assets/<modid>/items/<name>.json which
 *     points at the actual model under assets/<modid>/models/item/<name>.json.
 *     Without this pointer file, items render as the purple/black "missing" texture.
 *     See writeItemDefinition().
 *
 *   - pack.mcmeta now requires min_format / max_format (handled in InMemoryPackResources).
 */
public class DynamicResourceGenerator {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String MODID = NostalgicTrees.MODID;
    private static InMemoryPackResources currentPack;

    public static InMemoryPackResources generate(Collection<ResourceTreeType> trees) {
        NostalgicTrees.LOGGER.info("Generating dynamic resources for {} trees...", trees.size());

        currentPack = new InMemoryPackResources(
                new net.minecraft.server.packs.PackLocationInfo(
                        MODID + "_generated",
                        net.minecraft.network.chat.Component.literal("Nostalgic Trees Generated Resources"),
                        net.minecraft.server.packs.repository.PackSource.BUILT_IN,
                        java.util.Optional.empty()));

        try {
            Path blockstates = Path.of("assets", MODID, "blockstates");
            Path blockModels = Path.of("assets", MODID, "models", "block");
            Path itemModels = Path.of("assets", MODID, "models", "item");
            // 26.1: new "items/" folder for item definition pointers
            Path itemDefs = Path.of("assets", MODID, "items");
            Path lang = Path.of("assets", MODID, "lang");
            Path lootTables = Path.of("data", MODID, "loot_table", "blocks");
            Path recipes = Path.of("data", MODID, "recipe");
            Path tagsBlock = Path.of("data", "minecraft", "tags", "block");
            Path tagsMineable = Path.of("data", "minecraft", "tags", "block", "mineable");
            Path modTagsBlock = Path.of("data", MODID, "tags", "block");
            Path modTagsItem = Path.of("data", MODID, "tags", "item");

            // Shared block models (bases for tinted logs/leaves/saplings)
            writeSharedModels(blockModels, itemModels);

            // Mallet models — handheld item models + definition pointers
            for (String tier : new String[]{"wooden", "stone", "iron", "golden", "diamond", "netherite"}) {
                writeHandheldModel(itemModels, tier + "_mallet", MODID + ":item/" + tier + "_mallet");
                writeItemDefinition(itemDefs, tier + "_mallet", MODID + ":item/" + tier + "_mallet");
            }

            // Tag collectors
            JsonArray logsTag = new JsonArray();
            JsonArray leavesTag = new JsonArray();
            JsonArray saplingsTag = new JsonArray();
            JsonArray axeTag = new JsonArray();
            JsonArray hoeTag = new JsonArray();
            JsonArray resourceLogsTag = new JsonArray();
            JsonArray strippedLogsTag = new JsonArray();
            JsonArray resourceLeavesTag = new JsonArray();
            JsonArray resourceSaplingsTag = new JsonArray();
            JsonArray applesTag = new JsonArray();
            JsonArray chunksTag = new JsonArray();
            JsonArray honeycombsTag = new JsonArray();

            JsonObject langObj = new JsonObject();
            langObj.addProperty("itemGroup.nostalgictrees", "Nostalgic Trees");
            langObj.addProperty("gui.nostalgictrees.mallet_processing", "Mallet Processing");
            langObj.addProperty("block." + MODID + ".advanced_beehive", "Advanced Beehive");
            langObj.addProperty("gui.nostalgictrees.advanced_beehive", "Advanced Beehive");
            // Jade plugin config translations (26.1: Jade requires these to exist)
            langObj.addProperty("config.jade.plugin_nostalgictrees.drying_rack", "Drying Rack");
            langObj.addProperty("config.jade.plugin_nostalgictrees.sapling_mutation", "Sapling Mutation");
            for (String tier : new String[]{"wooden", "stone", "iron", "golden", "diamond", "netherite"}) {
                langObj.addProperty("item." + MODID + "." + tier + "_mallet", cap(tier) + " Mallet");
            }

            for (ResourceTreeType tree : trees) {
                String n = tree.name();
                String d = n.equals("rgb") ? "RGB" : cap(n);

                // Lang
                langObj.addProperty("block." + MODID + "." + n + "_log", d + " Log");
                langObj.addProperty("block." + MODID + ".stripped_" + n + "_log", "Stripped " + d + " Log");
                langObj.addProperty("block." + MODID + "." + n + "_leaves", d + " Leaves");
                langObj.addProperty("block." + MODID + "." + n + "_sapling", d + " Sapling");
                langObj.addProperty("item." + MODID + "." + n + "_apple", d + " Apple");
                langObj.addProperty("item." + MODID + "." + n + "_chunk", d + " Chunk");
                langObj.addProperty("item." + MODID + "." + n + "_honeycomb", d + " Honeycomb");

                // Blockstates
                if (n.equals("rgb")) {
                    writeBlockstate(blockstates, n + "_log", MODID + ":block/rgb_log", null);
                    writeBlockstate(blockstates, "stripped_" + n + "_log", MODID + ":block/rgb_stripped_log", null);
                    writeBlockstate(blockstates, n + "_leaves", MODID + ":block/rgb_leaves", "minecraft:cutout_mipped");
                    writeBlockstate(blockstates, n + "_sapling", MODID + ":block/rgb_sapling", "minecraft:cutout");
                } else {
                    writeBlockstate(blockstates, n + "_log", MODID + ":block/nt_log", null);
                    writeBlockstate(blockstates, "stripped_" + n + "_log", MODID + ":block/nt_stripped_log", null);
                    writeBlockstate(blockstates, n + "_leaves", MODID + ":block/nt_leaves", "minecraft:cutout_mipped");
                    writeBlockstate(blockstates, n + "_sapling", MODID + ":block/nt_sapling", "minecraft:cutout");
                }

                // Item models + definitions
                if (n.equals("rgb")) {
                    registerItem(itemModels, itemDefs, n + "_log",             parent(MODID + ":block/rgb_log"));
                    registerItem(itemModels, itemDefs, "stripped_" + n + "_log", parent(MODID + ":block/rgb_stripped_log"));
                    registerItem(itemModels, itemDefs, n + "_leaves",          parent(MODID + ":block/rgb_leaves"));
                    registerItem(itemModels, itemDefs, n + "_sapling",         generated(MODID + ":block/rgb_sapling"));
                    registerItem(itemModels, itemDefs, n + "_apple",           generated(MODID + ":item/rgb_apple"));
                    registerItem(itemModels, itemDefs, n + "_chunk",           generated(MODID + ":item/rgb_chunk"));
                    registerItem(itemModels, itemDefs, n + "_honeycomb",       generated(MODID + ":item/rgb_honeycomb"));
                } else {
                    // All non-RGB tree items share grayscale base textures and get tinted
                    // via the "tints" array in the items/ definition file. This colors the
                    // inventory/JEI icons. (Placed blocks are tinted separately via
                    // BlockColors.register in NTColorHandler.)
                    int tint = tree.color() | 0xFF000000;
                    registerItem(itemModels, itemDefs, n + "_log",             parent(MODID + ":block/nt_log"),          tint);
                    registerItem(itemModels, itemDefs, "stripped_" + n + "_log", parent(MODID + ":block/nt_stripped_log"), tint);
                    registerItem(itemModels, itemDefs, n + "_leaves",          parent(MODID + ":block/nt_leaves"),       tint);
                    registerItem(itemModels, itemDefs, n + "_sapling",         parent(MODID + ":item/nt_sapling"),       tint);
                    registerItem(itemModels, itemDefs, n + "_apple",           generated(MODID + ":item/base_apple"),    tint);
                    registerItem(itemModels, itemDefs, n + "_chunk",           generated(MODID + ":item/base_chunk"),    tint);
                    registerItem(itemModels, itemDefs, n + "_honeycomb",       generated(MODID + ":item/base_honeycomb"), tint);
                }

                // Loot tables
                writeLeavesLoot(lootTables, n);
                writeSimpleLoot(lootTables, n + "_log", MODID + ":" + n + "_log");
                writeSimpleLoot(lootTables, "stripped_" + n + "_log", MODID + ":stripped_" + n + "_log");
                writeSimpleLoot(lootTables, n + "_sapling", MODID + ":" + n + "_sapling");

                // Recipes
                writeChunkRecipe(recipes, n);
                writeResourceRecipe(recipes, n, tree.outputItem().toString(), tree.outputCount());

                // Tags
                logsTag.add(MODID + ":" + n + "_log");
                resourceLogsTag.add(MODID + ":" + n + "_log");
                strippedLogsTag.add(MODID + ":stripped_" + n + "_log");
                leavesTag.add(MODID + ":" + n + "_leaves");
                saplingsTag.add(MODID + ":" + n + "_sapling");
                axeTag.add(MODID + ":" + n + "_log");
                axeTag.add(MODID + ":stripped_" + n + "_log");
                hoeTag.add(MODID + ":" + n + "_leaves");
                resourceLeavesTag.add(MODID + ":" + n + "_leaves");
                resourceSaplingsTag.add(MODID + ":" + n + "_sapling");
                applesTag.add(MODID + ":" + n + "_apple");
                chunksTag.add(MODID + ":" + n + "_chunk");
                honeycombsTag.add(MODID + ":" + n + "_honeycomb");
            }

            // Drying Rack (must come before lang write)
            writeDryingRackResources(blockstates, blockModels, itemModels, itemDefs, lootTables, recipes, langObj);

            // RGB Dye (must come before lang write)
            writeRGBDyeResources(itemModels, itemDefs, recipes, langObj);

            // Write lang
            writeJson(lang.resolve("en_us.json"), langObj);

            // Tags
            writeTag(tagsBlock.resolve("logs.json"), logsTag);
            writeTag(tagsBlock.resolve("leaves.json"), leavesTag);
            writeTag(tagsBlock.resolve("saplings.json"), saplingsTag);
            writeTag(tagsBlock.resolve("flowers.json"), saplingsTag);
            writeTag(tagsBlock.resolve("bee_attractive.json"), saplingsTag);
            writeTag(tagsMineable.resolve("axe.json"), axeTag);
            writeTag(tagsMineable.resolve("hoe.json"), hoeTag);
            writeTag(modTagsBlock.resolve("resource_logs.json"), resourceLogsTag);
            writeTag(modTagsBlock.resolve("stripped_resource_logs.json"), strippedLogsTag);
            writeTag(modTagsBlock.resolve("resource_leaves.json"), resourceLeavesTag);
            writeTag(modTagsBlock.resolve("resource_saplings.json"), resourceSaplingsTag);
            writeTag(modTagsItem.resolve("resource_apples.json"), applesTag);
            writeTag(modTagsItem.resolve("resource_chunks.json"), chunksTag);
            writeTag(modTagsItem.resolve("resource_honeycombs.json"), honeycombsTag);

            // Beehive
            writeBeehiveBlockstate(blockstates);
            writeBeehiveModel(blockModels);
            registerItem(itemModels, itemDefs, "advanced_beehive", parent(MODID + ":block/advanced_beehive"));
            writeSimpleLoot(lootTables, "advanced_beehive", MODID + ":advanced_beehive");

            JsonArray beehivesTag = new JsonArray();
            beehivesTag.add(MODID + ":advanced_beehive");
            writeTag(tagsBlock.resolve("beehives.json"), beehivesTag);

            Path poiTags = Path.of("data", "minecraft", "tags", "point_of_interest_type");
            JsonArray beeHomeTag = new JsonArray();
            beeHomeTag.add(MODID + ":advanced_beehive");
            writeTag(poiTags.resolve("bee_home.json"), beeHomeTag);

            JsonArray malletsArr = new JsonArray();
            for (String t : new String[]{"wooden","stone","iron","golden","diamond","netherite"})
                malletsArr.add(MODID + ":" + t + "_mallet");
            writeTag(modTagsItem.resolve("mallets.json"), malletsArr);

            writeMalletRecipes(recipes);
            writeTier1SaplingRecipes(recipes);
            writeDryingRecipes(recipes);
            writeMutationRecipes(recipes);
            writeBeeSpawnEggRecipe(recipes);

            NostalgicTrees.LOGGER.info("Generated all dynamic resources successfully");
        } catch (Exception e) {
            NostalgicTrees.LOGGER.error("Failed to generate dynamic resources!", e);
        }
        return currentPack;
    }

    // ===================================================================
    // ITEM DEFINITIONS (new in 26.1) + MODEL WRITERS
    // ===================================================================

    /**
     * 26.1: writes the new items/<name>.json pointer that tells the client which
     * model renders this item. Without this file, items show the missing-texture
     * purple/black checkerboard even if the underlying model exists.
     *
     * Shape:  { "model": { "type": "minecraft:model", "model": "<modelRef>" } }
     */
    private static final int NO_TINT = 0;

    private static void writeItemDefinition(Path itemDefs, String name, String modelRef, int tintColor) throws IOException {
        JsonObject root = new JsonObject();
        JsonObject model = new JsonObject();
        model.addProperty("type", "minecraft:model");
        model.addProperty("model", modelRef);

        if (tintColor != NO_TINT) {
            JsonArray tints = new JsonArray();
            JsonObject tint = new JsonObject();
            tint.addProperty("type", "minecraft:constant");
            tint.addProperty("value", tintColor);
            tints.add(tint);
            model.add("tints", tints);
        }

        root.add("model", model);
        writeJson(itemDefs.resolve(name + ".json"), root);
    }

    // Back-compat overload - no inventory tint.
    private static void writeItemDefinition(Path itemDefs, String name, String modelRef) throws IOException {
        writeItemDefinition(itemDefs, name, modelRef, NO_TINT);
    }

    // Register an item end-to-end - writes the model AND the definition pointer.
    private static void registerItem(Path itemModels, Path itemDefs, String name, ItemModelSpec spec) throws IOException {
        registerItem(itemModels, itemDefs, name, spec, NO_TINT);
    }

    // Same as registerItem but with a constant inventory tint color (ARGB int).
    private static void registerItem(Path itemModels, Path itemDefs, String name, ItemModelSpec spec, int tintColor) throws IOException {
        spec.writeTo(itemModels, name);
        writeItemDefinition(itemDefs, name, MODID + ":item/" + name, tintColor);
    }

    /** A small strategy-object wrapper so the per-tree loop stays readable. */
    @FunctionalInterface
    private interface ItemModelSpec {
        void writeTo(Path itemModels, String name) throws IOException;
    }

    private static ItemModelSpec parent(String parent) {
        return (dir, name) -> writeParent(dir, name, parent);
    }

    private static ItemModelSpec generated(String layer0) {
        return (dir, name) -> writeGenerated(dir, name, layer0);
    }

    private static void writeBlockstate(Path dir, String name, String model, String rt) throws IOException {
        JsonObject root = new JsonObject();
        JsonObject variants = new JsonObject();
        JsonObject v = new JsonObject();
        v.addProperty("model", model);
        if (rt != null) v.addProperty("render_type", rt);
        variants.add("", v);
        root.add("variants", variants);
        writeJson(dir.resolve(name + ".json"), root);
    }

    private static void writeParent(Path dir, String name, String parent) throws IOException {
        JsonObject o = new JsonObject();
        o.addProperty("parent", parent);
        writeJson(dir.resolve(name + ".json"), o);
    }

    private static void writeGenerated(Path dir, String name, String layer0) throws IOException {
        JsonObject o = new JsonObject();
        o.addProperty("parent", "minecraft:item/generated");
        JsonObject t = new JsonObject();
        t.addProperty("layer0", layer0);
        o.add("textures", t);
        writeJson(dir.resolve(name + ".json"), o);
    }

    private static void writeHandheldModel(Path dir, String name, String layer0) throws IOException {
        JsonObject o = new JsonObject();
        o.addProperty("parent", "minecraft:item/handheld");
        JsonObject t = new JsonObject();
        t.addProperty("layer0", layer0);
        o.add("textures", t);
        writeJson(dir.resolve(name + ".json"), o);
    }

    // ===================================================================
    // SHARED MODEL WRITERS
    // ===================================================================

    private static void writeSharedModels(Path bm, Path im) throws IOException {
        String log = """
        {"parent":"minecraft:block/cube_column","textures":{"end":"MODID:block/base_log_top","side":"MODID:block/base_log"},
         "elements":[{"from":[0,0,0],"to":[16,16,16],"faces":{
         "down":{"texture":"#end","tintindex":0},"up":{"texture":"#end","tintindex":0},
         "north":{"texture":"#side","tintindex":0},"south":{"texture":"#side","tintindex":0},
         "west":{"texture":"#side","tintindex":0},"east":{"texture":"#side","tintindex":0}}}]}
        """.replace("MODID", MODID);
        putString(bm.resolve("nt_log.json"), log);

        String stripped = """
        {"parent":"minecraft:block/cube_column","textures":{"end":"MODID:block/base_stripped_log_top","side":"MODID:block/base_stripped_log"},
         "elements":[{"from":[0,0,0],"to":[16,16,16],"faces":{
         "down":{"texture":"#end","tintindex":0},"up":{"texture":"#end","tintindex":0},
         "north":{"texture":"#side","tintindex":0},"south":{"texture":"#side","tintindex":0},
         "west":{"texture":"#side","tintindex":0},"east":{"texture":"#side","tintindex":0}}}]}
        """.replace("MODID", MODID);
        putString(bm.resolve("nt_stripped_log.json"), stripped);

        String leaves = """
        {"parent":"minecraft:block/cube_all","textures":{"all":"MODID:block/base_leaves"},
         "elements":[{"from":[0,0,0],"to":[16,16,16],"faces":{
         "down":{"texture":"#all","tintindex":0},"up":{"texture":"#all","tintindex":0},
         "north":{"texture":"#all","tintindex":0},"south":{"texture":"#all","tintindex":0},
         "west":{"texture":"#all","tintindex":0},"east":{"texture":"#all","tintindex":0}}}]}
        """.replace("MODID", MODID);
        putString(bm.resolve("nt_leaves.json"), leaves);

        String sapling = """
        {"parent":"minecraft:block/cross","textures":{"cross":"MODID:block/base_sapling"},
         "elements":[
         {"from":[0.8,0,8],"to":[15.2,16,8],"rotation":{"origin":[8,8,8],"axis":"y","angle":45,"rescale":true},"shade":false,
          "faces":{"north":{"uv":[0,0,16,16],"texture":"#cross","tintindex":0},"south":{"uv":[0,0,16,16],"texture":"#cross","tintindex":0}}},
         {"from":[8,0,0.8],"to":[8,16,15.2],"rotation":{"origin":[8,8,8],"axis":"y","angle":45,"rescale":true},"shade":false,
          "faces":{"west":{"uv":[0,0,16,16],"texture":"#cross","tintindex":0},"east":{"uv":[0,0,16,16],"texture":"#cross","tintindex":0}}}]}
        """.replace("MODID", MODID);
        putString(bm.resolve("nt_sapling.json"), sapling);

        String saplingItem = """
        {"parent":"minecraft:item/generated","textures":{"layer0":"MODID:block/base_sapling"}}
        """.replace("MODID", MODID);
        putString(im.resolve("nt_sapling.json"), saplingItem);

        String rgbLog = """
        {"parent":"minecraft:block/cube_column","textures":{"end":"MODID:block/rgb_log_top","side":"MODID:block/rgb_log"}}
        """.replace("MODID", MODID);
        putString(bm.resolve("rgb_log.json"), rgbLog);

        String rgbStripped = """
        {"parent":"minecraft:block/cube_column","textures":{"end":"MODID:block/rgb_stripped_log_top","side":"MODID:block/rgb_stripped_log"}}
        """.replace("MODID", MODID);
        putString(bm.resolve("rgb_stripped_log.json"), rgbStripped);

        String rgbLeaves = """
        {"parent":"minecraft:block/cube_all","textures":{"all":"MODID:block/rgb_leaves"}}
        """.replace("MODID", MODID);
        putString(bm.resolve("rgb_leaves.json"), rgbLeaves);

        String rgbSapling = """
        {"parent":"minecraft:block/cross","textures":{"cross":"MODID:block/rgb_sapling"}}
        """.replace("MODID", MODID);
        putString(bm.resolve("rgb_sapling.json"), rgbSapling);
    }

    // ===================================================================
    // LOOT TABLES
    // ===================================================================

    private static void writeLeavesLoot(Path dir, String name) throws IOException {
        String json = """
        {"type":"minecraft:block","pools":[
          {"rolls":1,"entries":[{"type":"minecraft:alternatives","children":[
            {"type":"minecraft:item","name":"MODID:NAME_leaves","conditions":[
              {"condition":"minecraft:match_tool","predicate":{"predicates":{"minecraft:enchantments":[
                {"enchantments":"minecraft:silk_touch","levels":{"min":1}}]}}}]},
            {"type":"minecraft:item","name":"MODID:NAME_sapling","conditions":[
              {"condition":"minecraft:random_chance","chance":0.05}]}]}],
           "conditions":[{"condition":"minecraft:survives_explosion"}]},
          {"rolls":1,"entries":[{"type":"minecraft:item","name":"MODID:NAME_apple","conditions":[
            {"condition":"minecraft:random_chance","chance":0.05}]}],
           "conditions":[{"condition":"minecraft:survives_explosion"}]}]}
        """.replace("MODID", MODID).replace("NAME", name);
        putString(dir.resolve(name + "_leaves.json"), json);
    }

    private static void writeSimpleLoot(Path dir, String name, String itemId) throws IOException {
        JsonObject root = new JsonObject();
        root.addProperty("type", "minecraft:block");
        JsonArray pools = new JsonArray();
        JsonObject pool = new JsonObject();
        pool.addProperty("rolls", 1);
        JsonArray entries = new JsonArray();
        JsonObject entry = new JsonObject();
        entry.addProperty("type", "minecraft:item");
        entry.addProperty("name", itemId);
        entries.add(entry);
        pool.add("entries", entries);
        pools.add(pool);
        root.add("pools", pools);
        writeJson(dir.resolve(name + ".json"), root);
    }

    // ===================================================================
    // RECIPE HELPERS (26.1: ingredients are bare strings, not objects)
    // ===================================================================

    private static void keyItem(JsonObject key, String letter, String itemOrTag) {
        key.addProperty(letter, itemOrTag);
    }

    private static JsonObject shapedResult(String itemId, int count) {
        JsonObject r = new JsonObject();
        r.addProperty("id", itemId);
        if (count > 1) r.addProperty("count", count);
        return r;
    }

    // ===================================================================
    // PER-TREE RECIPES
    // ===================================================================

    private static void writeChunkRecipe(Path dir, String name) throws IOException {
        JsonObject o = new JsonObject();
        o.addProperty("type", "minecraft:crafting_shapeless");
        o.addProperty("category", "misc");
        JsonArray ingredients = new JsonArray();
        ingredients.add(MODID + ":stripped_" + name + "_log");   // bare string
        o.add("ingredients", ingredients);
        o.add("result", shapedResult(MODID + ":" + name + "_chunk", 1));
        writeJson(dir.resolve(name + "_chunk_from_stripped_log.json"), o);
    }

    private static void writeResourceRecipe(Path dir, String name, String outputItem, int outputCount) throws IOException {
        JsonObject o = new JsonObject();
        o.addProperty("type", "minecraft:crafting_shaped");
        o.addProperty("category", "misc");
        JsonArray pattern = new JsonArray();
        pattern.add("ACA");
        pattern.add("CHC");
        pattern.add("ACA");
        o.add("pattern", pattern);
        JsonObject key = new JsonObject();
        keyItem(key, "A", MODID + ":" + name + "_apple");
        keyItem(key, "C", MODID + ":" + name + "_chunk");
        keyItem(key, "H", MODID + ":" + name + "_honeycomb");
        o.add("key", key);
        o.add("result", shapedResult(outputItem, outputCount));
        writeJson(dir.resolve(name + "_resource_from_chunk_apple.json"), o);
    }

    // ===================================================================
    // MALLET RECIPES
    // ===================================================================

    private static void writeMalletRecipes(Path dir) throws IOException {
        String[][] mallets = {
                {"wooden",  "minecraft:oak_log",       "minecraft:stick"},
                {"stone",   "minecraft:cobblestone",   "minecraft:stick"},
                {"iron",    "minecraft:iron_block",    "minecraft:stick"},
                {"golden",  "minecraft:gold_block",    "minecraft:stick"},
                {"diamond", "minecraft:diamond_block", "minecraft:stick"}
        };
        for (String[] m : mallets) {
            JsonObject o = new JsonObject();
            o.addProperty("type", "minecraft:crafting_shaped");
            o.addProperty("category", "equipment");
            JsonArray pattern = new JsonArray();
            pattern.add(" HH");
            pattern.add(" HH");
            pattern.add("S  ");
            o.add("pattern", pattern);
            JsonObject key = new JsonObject();
            keyItem(key, "H", m[1]);
            keyItem(key, "S", m[2]);
            o.add("key", key);
            o.add("result", shapedResult(MODID + ":" + m[0] + "_mallet", 1));
            writeJson(dir.resolve(m[0] + "_mallet.json"), o);
        }

        // Netherite smithing
        JsonObject neo = new JsonObject();
        neo.addProperty("type", "minecraft:smithing_transform");
        neo.addProperty("template", "minecraft:netherite_upgrade_smithing_template");
        neo.addProperty("base", MODID + ":diamond_mallet");
        neo.addProperty("addition", "minecraft:netherite_ingot");
        neo.add("result", shapedResult(MODID + ":netherite_mallet", 1));
        writeJson(dir.resolve("netherite_mallet.json"), neo);
    }

    // ===================================================================
    // TAGS
    // ===================================================================

    private static void writeTag(Path file, JsonArray values) throws IOException {
        JsonObject o = new JsonObject();
        o.addProperty("replace", false);
        o.add("values", values);
        writeJson(file, o);
    }

    // ===================================================================
    // ADVANCED BEEHIVE
    // ===================================================================

    private static void writeBeehiveBlockstate(Path dir) throws IOException {
        JsonObject root = new JsonObject();
        JsonObject variants = new JsonObject();
        String model = MODID + ":block/advanced_beehive";

        JsonObject north = new JsonObject();
        north.addProperty("model", model);
        variants.add("facing=north", north);

        JsonObject south = new JsonObject();
        south.addProperty("model", model);
        south.addProperty("y", 180);
        variants.add("facing=south", south);

        JsonObject west = new JsonObject();
        west.addProperty("model", model);
        west.addProperty("y", 270);
        variants.add("facing=west", west);

        JsonObject east = new JsonObject();
        east.addProperty("model", model);
        east.addProperty("y", 90);
        variants.add("facing=east", east);

        root.add("variants", variants);
        writeJson(dir.resolve("advanced_beehive.json"), root);
    }

    private static void writeBeehiveModel(Path dir) throws IOException {
        String model = """
        {
          "parent": "minecraft:block/orientable_with_bottom",
          "textures": {
            "top": "MODID:block/advanced_beehive_top",
            "side": "MODID:block/advanced_beehive_side",
            "front": "MODID:block/advanced_beehive_front",
            "bottom": "MODID:block/advanced_beehive_bottom"
          }
        }
        """.replace("MODID", MODID);
        putString(dir.resolve("advanced_beehive.json"), model);
    }

    // ===================================================================
    // OUTPUT HELPERS
    // ===================================================================

    private static void writeJson(Path path, JsonObject obj) throws IOException {
        putString(path, GSON.toJson(obj));
    }

    private static void putString(Path path, String content) {
        String fullPath = path.toString().replace(java.io.File.separatorChar, '/');
        PackType type;
        String resourcePath;
        String namespace;
        if (fullPath.startsWith("assets/")) {
            type = PackType.CLIENT_RESOURCES;
            String afterAssets = fullPath.substring("assets/".length());
            int slashIdx = afterAssets.indexOf('/');
            namespace = afterAssets.substring(0, slashIdx);
            resourcePath = afterAssets.substring(slashIdx + 1);
        } else if (fullPath.startsWith("data/")) {
            type = PackType.SERVER_DATA;
            String afterData = fullPath.substring("data/".length());
            int slashIdx = afterData.indexOf('/');
            namespace = afterData.substring(0, slashIdx);
            resourcePath = afterData.substring(slashIdx + 1);
        } else {
            NostalgicTrees.LOGGER.warn("Unknown path prefix for resource: {}", fullPath);
            return;
        }
        Identifier loc = Identifier.fromNamespaceAndPath(namespace, resourcePath);
        currentPack.putJson(type, loc, content);
    }

    private static String cap(String s) { return s.substring(0, 1).toUpperCase() + s.substring(1); }

    // ===================================================================
    // DRYING RACK
    // ===================================================================

    private static void writeDryingRackResources(Path blockstates, Path blockModels, Path itemModels, Path itemDefs,
                                                 Path lootTables, Path recipes, JsonObject langObj) throws IOException {
        langObj.addProperty("block." + MODID + ".drying_rack", "Drying Rack");
        langObj.addProperty("gui.nostalgictrees.drying_rack", "Drying Rack");
        langObj.addProperty("gui.nostalgictrees.mutation", "Bee Mutation");

        JsonObject bs = new JsonObject();
        JsonObject variants = new JsonObject();
        String rackModel = MODID + ":block/drying_rack";

        JsonObject north = new JsonObject();
        north.addProperty("model", rackModel);
        variants.add("facing=north", north);

        JsonObject south = new JsonObject();
        south.addProperty("model", rackModel);
        south.addProperty("y", 180);
        variants.add("facing=south", south);

        JsonObject west = new JsonObject();
        west.addProperty("model", rackModel);
        west.addProperty("y", 270);
        variants.add("facing=west", west);

        JsonObject east = new JsonObject();
        east.addProperty("model", rackModel);
        east.addProperty("y", 90);
        variants.add("facing=east", east);

        bs.add("variants", variants);
        writeJson(blockstates.resolve("drying_rack.json"), bs);

        String model = """
        {
          "parent": "minecraft:block/block",
          "textures": {
            "planks": "minecraft:block/oak_planks",
            "particle": "minecraft:block/oak_planks"
          },
          "elements": [
            {
              "from": [0, 14, 0],
              "to": [16, 16, 4],
              "faces": {
                "north": {"texture": "#planks"},
                "south": {"texture": "#planks"},
                "west":  {"texture": "#planks"},
                "east":  {"texture": "#planks"},
                "up":    {"texture": "#planks"},
                "down":  {"texture": "#planks"}
              }
            }
          ]
        }
        """;
        putString(blockModels.resolve("drying_rack.json"), model);

        registerItem(itemModels, itemDefs, "drying_rack", parent(MODID + ":block/drying_rack"));
        writeSimpleLoot(lootTables, "drying_rack", MODID + ":drying_rack");

        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shaped");
        recipe.addProperty("category", "misc");
        JsonArray pattern = new JsonArray();
        pattern.add("PPP");
        pattern.add("S S");
        recipe.add("pattern", pattern);
        JsonObject key = new JsonObject();
        keyItem(key, "P", "minecraft:oak_planks");
        keyItem(key, "S", "minecraft:stick");
        recipe.add("key", key);
        recipe.add("result", shapedResult(MODID + ":drying_rack", 1));
        writeJson(recipes.resolve("drying_rack.json"), recipe);
    }

    // ===================================================================
    // TIER 1 SAPLING RECIPES
    // ===================================================================

    private static void writeTier1SaplingRecipes(Path recipes) throws IOException {
        writeSurroundRecipe(recipes, "dirt_sapling",
                "minecraft:dirt", "minecraft:oak_sapling",
                MODID + ":dirt_sapling");

        writeSmeltingRecipe(recipes, "gravel_sapling_from_smelting",
                MODID + ":stone_sapling", MODID + ":gravel_sapling");

        writeSmeltingRecipe(recipes, "sand_sapling_from_smelting",
                MODID + ":gravel_sapling", MODID + ":sand_sapling");

        JsonObject clay = new JsonObject();
        clay.addProperty("type", "minecraft:crafting_shaped");
        clay.addProperty("category", "misc");
        JsonArray clayPattern = new JsonArray();
        clayPattern.add(" D ");
        clayPattern.add("SOG");
        clayPattern.add(" A ");
        clay.add("pattern", clayPattern);
        JsonObject clayKey = new JsonObject();
        keyItem(clayKey, "D", MODID + ":dirt_sapling");
        keyItem(clayKey, "S", MODID + ":stone_sapling");
        keyItem(clayKey, "O", "minecraft:oak_sapling");
        keyItem(clayKey, "G", MODID + ":gravel_sapling");
        keyItem(clayKey, "A", MODID + ":sand_sapling");
        clay.add("key", clayKey);
        clay.add("result", shapedResult(MODID + ":clay_sapling", 1));
        writeJson(recipes.resolve("clay_sapling.json"), clay);

        writeSurroundRecipe(recipes, "bone_sapling",
                "minecraft:bone_meal", MODID + ":sand_sapling",
                MODID + ":bone_sapling");

        writeSurroundRecipe(recipes, "coal_sapling",
                "minecraft:charcoal", MODID + ":clay_sapling",
                MODID + ":coal_sapling");

        writeSurroundRecipe(recipes, "ice_sapling",
                "minecraft:snowball", MODID + ":bone_sapling",
                MODID + ":ice_sapling");

        JsonObject rgb = new JsonObject();
        rgb.addProperty("type", "minecraft:crafting_shaped");
        rgb.addProperty("category", "misc");
        JsonArray rgbPattern = new JsonArray();
        rgbPattern.add("WRW");
        rgbPattern.add("GCB");
        rgbPattern.add("WYW");
        rgb.add("pattern", rgbPattern);
        JsonObject rgbKey = new JsonObject();
        keyItem(rgbKey, "W", "minecraft:white_dye");
        keyItem(rgbKey, "R", "minecraft:red_dye");
        keyItem(rgbKey, "G", "minecraft:green_dye");
        keyItem(rgbKey, "B", "minecraft:blue_dye");
        keyItem(rgbKey, "Y", "minecraft:yellow_dye");
        keyItem(rgbKey, "C", MODID + ":bone_sapling");
        rgb.add("key", rgbKey);
        rgb.add("result", shapedResult(MODID + ":rgb_sapling", 1));
        writeJson(recipes.resolve("rgb_sapling.json"), rgb);

        writeSurroundRecipe(recipes, "advanced_beehive",
                "minecraft:oak_log", MODID + ":rgb_sapling",
                MODID + ":advanced_beehive");
    }

    private static void writeSurroundRecipe(Path dir, String name, String surrounding, String center, String output) throws IOException {
        JsonObject o = new JsonObject();
        o.addProperty("type", "minecraft:crafting_shaped");
        o.addProperty("category", "misc");
        JsonArray pattern = new JsonArray();
        pattern.add("SSS");
        pattern.add("SCS");
        pattern.add("SSS");
        o.add("pattern", pattern);
        JsonObject key = new JsonObject();
        keyItem(key, "S", surrounding);
        keyItem(key, "C", center);
        o.add("key", key);
        o.add("result", shapedResult(output, 1));
        writeJson(dir.resolve(name + ".json"), o);
    }

    private static void writeSmeltingRecipe(Path dir, String name, String input, String output) throws IOException {
        JsonObject o = new JsonObject();
        o.addProperty("type", "minecraft:smelting");
        o.addProperty("category", "misc");
        o.addProperty("ingredient", input);          // bare string
        o.add("result", shapedResult(output, 1));
        o.addProperty("experience", 0.1);
        o.addProperty("cookingtime", 200);
        writeJson(dir.resolve(name + ".json"), o);
    }

    // ===================================================================
    // DRYING RACK RECIPES (custom format)
    // ===================================================================

    private static void writeDryingRecipes(Path recipes) throws IOException {
        writeDryingRecipe(recipes, "dirt_to_stone_sapling",
                MODID + ":dirt_sapling", MODID + ":stone_sapling", 1, 600);

        writeDryingRecipe(recipes, "clay_ball_to_bone_meal",
                "minecraft:clay_ball", "minecraft:bone_meal", 1, 600);

        writeDryingRecipe(recipes, "bone_block_to_snow_block",
                "minecraft:bone_block", "minecraft:snow_block", 1, 600);
    }

    private static void writeDryingRecipe(Path dir, String name, String input, String output,
                                           int outputCount, int dryingTime) throws IOException {
        JsonObject o = new JsonObject();
        o.addProperty("type", MODID + ":drying");
        o.addProperty("input", input);
        o.addProperty("output", output);
        o.addProperty("output_count", outputCount);
        o.addProperty("drying_time", dryingTime);
        writeJson(dir.resolve(name + ".json"), o);
    }

    // ===================================================================
    // MUTATION RECIPES (custom format)
    // ===================================================================

    private static void writeMutationRecipes(Path recipes) throws IOException {
        writeMutationRecipe(recipes, "copper_mutation",
                MODID + ":coal_sapling",
                new String[]{MODID + ":bone_honeycomb", MODID + ":stone_honeycomb", MODID + ":rgb_honeycomb"},
                MODID + ":copper_sapling", 5, null, 0);

        writeMutationRecipe(recipes, "iron_mutation",
                MODID + ":ice_sapling",
                new String[]{MODID + ":clay_honeycomb", MODID + ":sand_honeycomb", MODID + ":rgb_honeycomb"},
                MODID + ":iron_sapling", 5, null, 0);

        writeMutationRecipe(recipes, "redstone_mutation",
                MODID + ":iron_sapling",
                new String[]{MODID + ":coal_honeycomb", MODID + ":copper_honeycomb", MODID + ":stone_honeycomb"},
                MODID + ":redstone_sapling", 5, null, 0);

        writeMutationRecipe(recipes, "amethyst_mutation",
                MODID + ":copper_sapling",
                new String[]{MODID + ":ice_honeycomb", MODID + ":clay_honeycomb", MODID + ":redstone_honeycomb"},
                MODID + ":amethyst_sapling", 5, null, 0);

        writeMutationRecipe(recipes, "prismarine_mutation",
                MODID + ":ice_sapling",
                new String[]{MODID + ":stone_honeycomb", MODID + ":bone_honeycomb", MODID + ":rgb_honeycomb"},
                MODID + ":prismarine_sapling", 5, null, 0);

        writeMutationRecipe(recipes, "experience_mutation",
                MODID + ":amethyst_sapling",
                new String[]{MODID + ":rgb_honeycomb", MODID + ":amethyst_honeycomb", MODID + ":redstone_honeycomb"},
                MODID + ":experience_sapling", 5, null, 0);

        writeMutationRecipe(recipes, "quartz_mutation",
                MODID + ":redstone_sapling",
                new String[]{MODID + ":amethyst_honeycomb", MODID + ":sand_honeycomb"},
                MODID + ":quartz_sapling", 7, "minecraft:quartz", 2);

        writeMutationRecipe(recipes, "gold_mutation",
                MODID + ":quartz_sapling",
                new String[]{MODID + ":iron_honeycomb", MODID + ":quartz_honeycomb"},
                MODID + ":gold_sapling", 7, "minecraft:gold_ingot", 2);

        writeMutationRecipe(recipes, "lapis_mutation",
                MODID + ":amethyst_sapling",
                new String[]{MODID + ":redstone_honeycomb", MODID + ":prismarine_honeycomb", MODID + ":rgb_honeycomb"},
                MODID + ":lapis_sapling", 7, "minecraft:lapis_lazuli", 4);

        writeMutationRecipe(recipes, "glowstone_mutation",
                MODID + ":experience_sapling",
                new String[]{MODID + ":redstone_honeycomb", MODID + ":experience_honeycomb", MODID + ":lapis_honeycomb"},
                MODID + ":glowstone_sapling", 7, "minecraft:glowstone_block", 4);

        writeMutationRecipe(recipes, "diamond_mutation",
                MODID + ":gold_sapling",
                new String[]{MODID + ":iron_honeycomb", MODID + ":gold_honeycomb", MODID + ":lapis_honeycomb", MODID + ":redstone_honeycomb"},
                MODID + ":diamond_sapling", 10, "minecraft:diamond", 4);

        writeMutationRecipe(recipes, "emerald_mutation",
                MODID + ":gold_sapling",
                new String[]{MODID + ":experience_honeycomb", MODID + ":prismarine_honeycomb", MODID + ":glowstone_honeycomb"},
                MODID + ":emerald_sapling", 10, "minecraft:emerald", 4);

        writeMutationRecipe(recipes, "obsidian_mutation",
                MODID + ":diamond_sapling",
                new String[]{MODID + ":stone_honeycomb", MODID + ":quartz_honeycomb", MODID + ":diamond_honeycomb"},
                MODID + ":obsidian_sapling", 10, "minecraft:obsidian", 8);

        writeMutationRecipe(recipes, "netherite_mutation",
                MODID + ":diamond_sapling",
                new String[]{MODID + ":gold_honeycomb", MODID + ":obsidian_honeycomb", MODID + ":diamond_honeycomb", MODID + ":quartz_honeycomb"},
                MODID + ":netherite_sapling", 10, "minecraft:netherite_ingot", 1);
    }

    private static void writeMutationRecipe(Path dir, String name, String baseSapling,
                                             String[] honeycombs, String result, int pollinations,
                                             String catalyst, int catalystCount) throws IOException {
        JsonObject o = new JsonObject();
        o.addProperty("type", MODID + ":mutation");
        o.addProperty("base_sapling", baseSapling);
        JsonArray combsArray = new JsonArray();
        for (String comb : honeycombs) {
            combsArray.add(comb);
        }
        o.add("honeycombs", combsArray);
        o.addProperty("result", result);
        o.addProperty("pollinations_required", pollinations);
        if (catalyst != null && catalystCount > 0) {
            o.addProperty("catalyst", catalyst);
            o.addProperty("catalyst_count", catalystCount);
        }
        writeJson(dir.resolve(name + ".json"), o);
    }

    // ===================================================================
    // RGB DYE
    // ===================================================================

    private static void writeRGBDyeResources(Path itemModels, Path itemDefs, Path recipes, JsonObject langObj) throws IOException {
        langObj.addProperty("item." + MODID + ".rgb_dye", "RGB Dye");

        registerItem(itemModels, itemDefs, "rgb_dye", generated(MODID + ":item/rgb_dye"));

        String D = MODID + ":rgb_dye";

        writeShapedDyeRecipe(recipes, "white_dye",      new String[]{"DDD"},                      D, 2);
        writeShapedDyeRecipe(recipes, "orange_dye",     new String[]{"D", "D", "D"},              D, 2);
        writeShapedDyeRecipe(recipes, "magenta_dye",    new String[]{"D  ", " D ", "  D"},        D, 2);
        writeShapedDyeRecipe(recipes, "light_blue_dye", new String[]{"  D", " D ", "D  "},        D, 2);
        writeShapedDyeRecipe(recipes, "yellow_dye",     new String[]{"DD", "DD"},                 D, 2);
        writeShapedDyeRecipe(recipes, "lime_dye",       new String[]{"D ", "D ", "DD"},           D, 2);
        writeShapedDyeRecipe(recipes, "pink_dye",       new String[]{"D D", "   ", "D D"},        D, 2);
        writeShapedDyeRecipe(recipes, "gray_dye",       new String[]{" D ", "DDD"},               D, 2);
        writeShapedDyeRecipe(recipes, "light_gray_dye", new String[]{" D", " D", "DD"},           D, 2);
        writeShapedDyeRecipe(recipes, "cyan_dye",       new String[]{"DD ", " DD"},               D, 2);
        writeShapedDyeRecipe(recipes, "purple_dye",     new String[]{" D ", "DDD", " D "},        D, 2);
        writeShapedDyeRecipe(recipes, "blue_dye",       new String[]{"DDD", " D ", " D "},        D, 2);
        writeShapedDyeRecipe(recipes, "brown_dye",      new String[]{" D ", "D D", " D "},        D, 2);
        writeShapedDyeRecipe(recipes, "green_dye",      new String[]{"DDD", "DDD"},               D, 2);
        writeShapedDyeRecipe(recipes, "red_dye",        new String[]{"DD", "DD", "DD"},           D, 2);
        writeShapedDyeRecipe(recipes, "black_dye",      new String[]{"DDD", "D D", "DDD"},        D, 2);
    }

    private static void writeShapedDyeRecipe(Path dir, String dyeName, String[] pattern,
                                              String dyeItem, int outputCount) throws IOException {
        JsonObject o = new JsonObject();
        o.addProperty("type", "minecraft:crafting_shaped");
        o.addProperty("category", "misc");
        JsonArray patternArr = new JsonArray();
        for (String row : pattern) {
            patternArr.add(row);
        }
        o.add("pattern", patternArr);
        JsonObject key = new JsonObject();
        keyItem(key, "D", dyeItem);
        o.add("key", key);
        o.add("result", shapedResult("minecraft:" + dyeName, outputCount));
        writeJson(dir.resolve("rgb_dye_to_" + dyeName + ".json"), o);
    }

    // ===================================================================
    // BEE SPAWN EGG
    // ===================================================================

    private static void writeBeeSpawnEggRecipe(Path recipes) throws IOException {
        JsonObject o = new JsonObject();
        o.addProperty("type", "minecraft:crafting_shaped");
        o.addProperty("category", "misc");
        JsonArray pattern = new JsonArray();
        pattern.add("WGO");
        pattern.add("BEY");
        pattern.add("PUR");
        o.add("pattern", pattern);
        JsonObject key = new JsonObject();
        keyItem(key, "W", "minecraft:white_dye");
        keyItem(key, "G", "minecraft:green_dye");
        keyItem(key, "O", "minecraft:orange_dye");
        keyItem(key, "B", "minecraft:black_dye");
        keyItem(key, "E", "minecraft:egg");
        keyItem(key, "Y", "minecraft:yellow_dye");
        keyItem(key, "P", "minecraft:purple_dye");
        keyItem(key, "U", "minecraft:blue_dye");
        keyItem(key, "R", "minecraft:red_dye");
        o.add("key", key);
        o.add("result", shapedResult("minecraft:bee_spawn_egg", 1));
        writeJson(recipes.resolve("bee_spawn_egg.json"), o);
    }
}
