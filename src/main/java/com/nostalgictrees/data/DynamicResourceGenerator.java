package com.nostalgictrees.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.nostalgictrees.NostalgicTrees;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

public class DynamicResourceGenerator {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String MODID = NostalgicTrees.MODID;

    public static void generate(Path root, Collection<ResourceTreeType> trees) {
        NostalgicTrees.LOGGER.info("Generating dynamic resources for {} trees...", trees.size());

        try {
            // Create pack.mcmeta
            Files.createDirectories(root);
            Files.writeString(root.resolve("pack.mcmeta"),
                    "{\"pack\":{\"description\":\"Nostalgic Trees Generated Resources\",\"pack_format\":34}}");

            Path blockstates = root.resolve("assets/" + MODID + "/blockstates");
            Path blockModels = root.resolve("assets/" + MODID + "/models/block");
            Path itemModels = root.resolve("assets/" + MODID + "/models/item");
            Path lang = root.resolve("assets/" + MODID + "/lang");
            Path lootTables = root.resolve("data/" + MODID + "/loot_table/blocks");
            Path recipes = root.resolve("data/" + MODID + "/recipe");
            Path tagsBlock = root.resolve("data/minecraft/tags/block");
            Path tagsMineable = root.resolve("data/minecraft/tags/block/mineable");
            Path modTagsBlock = root.resolve("data/" + MODID + "/tags/block");
            Path modTagsItem = root.resolve("data/" + MODID + "/tags/item");

            for (Path p : new Path[]{blockstates, blockModels, itemModels, lang,
                    lootTables, recipes, tagsBlock, tagsMineable, modTagsBlock, modTagsItem}) {
                Files.createDirectories(p);
            }

            // Shared block models
            writeSharedModels(blockModels, itemModels);

            // Mallet models
            for (String tier : new String[]{"wooden", "stone", "iron", "golden", "diamond", "netherite"}) {
                writeHandheldModel(itemModels, tier + "_mallet", MODID + ":item/" + tier + "_mallet");
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

            // Lang
            JsonObject langObj = new JsonObject();
            langObj.addProperty("itemGroup.nostalgictrees", "Nostalgic Trees");
            langObj.addProperty("gui.nostalgictrees.mallet_processing", "Mallet Processing");
            langObj.addProperty("block." + MODID + ".resource_beehive", "Resource Beehive");
            langObj.addProperty("gui.nostalgictrees.resource_beehive", "Resource Beehive");
            for (String tier : new String[]{"wooden", "stone", "iron", "golden", "diamond", "netherite"}) {
                langObj.addProperty("item." + MODID + "." + tier + "_mallet", cap(tier) + " Mallet");
            }

            for (ResourceTreeType tree : trees) {
                String n = tree.name();
                String d = cap(n);

                // Lang
                langObj.addProperty("block." + MODID + "." + n + "_log", d + " Log");
                langObj.addProperty("block." + MODID + ".stripped_" + n + "_log", "Stripped " + d + " Log");
                langObj.addProperty("block." + MODID + "." + n + "_leaves", d + " Leaves");
                langObj.addProperty("block." + MODID + "." + n + "_sapling", d + " Sapling");
                langObj.addProperty("item." + MODID + "." + n + "_apple", d + " Apple");
                langObj.addProperty("item." + MODID + "." + n + "_chunk", d + " Chunk");
                langObj.addProperty("item." + MODID + "." + n + "_honeycomb", d + " Honeycomb");

                // Blockstates
                writeBlockstate(blockstates, n + "_log", MODID + ":block/nt_log", null);
                writeBlockstate(blockstates, "stripped_" + n + "_log", MODID + ":block/nt_stripped_log", null);
                writeBlockstate(blockstates, n + "_leaves", MODID + ":block/nt_leaves", "minecraft:cutout_mipped");
                writeBlockstate(blockstates, n + "_sapling", MODID + ":block/nt_sapling", "minecraft:cutout");

                // Item models
                writeParent(itemModels, n + "_log", MODID + ":block/nt_log");
                writeParent(itemModels, "stripped_" + n + "_log", MODID + ":block/nt_stripped_log");
                writeParent(itemModels, n + "_leaves", MODID + ":block/nt_leaves");
                writeParent(itemModels, n + "_sapling", MODID + ":item/nt_sapling");
                writeGenerated(itemModels, n + "_apple", MODID + ":item/base_apple");
                writeGenerated(itemModels, n + "_chunk", MODID + ":item/base_chunk");
                writeGenerated(itemModels, n + "_honeycomb", MODID + ":item/base_honeycomb");

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

            // === Drying Rack (MUST be before lang write so name is included) ===
            writeDryingRackResources(blockstates, blockModels, itemModels, lootTables, recipes, langObj);

            // Write lang (after all lang entries have been added)
            writeJson(lang.resolve("en_us.json"), langObj);

            // Write all tags
            writeTag(tagsBlock.resolve("logs.json"), logsTag);
            writeTag(tagsBlock.resolve("leaves.json"), leavesTag);
            writeTag(tagsBlock.resolve("saplings.json"), saplingsTag);
            writeTag(tagsBlock.resolve("flowers.json"), saplingsTag);
            writeTag(tagsMineable.resolve("axe.json"), axeTag);
            writeTag(tagsMineable.resolve("hoe.json"), hoeTag);
            writeTag(modTagsBlock.resolve("resource_logs.json"), resourceLogsTag);
            writeTag(modTagsBlock.resolve("stripped_resource_logs.json"), strippedLogsTag);
            writeTag(modTagsBlock.resolve("resource_leaves.json"), resourceLeavesTag);
            writeTag(modTagsBlock.resolve("resource_saplings.json"), resourceSaplingsTag);
            writeTag(modTagsItem.resolve("resource_apples.json"), applesTag);
            writeTag(modTagsItem.resolve("resource_chunks.json"), chunksTag);
            writeTag(modTagsItem.resolve("resource_honeycombs.json"), honeycombsTag);

            // Beehive blockstate
            writeBeehiveBlockstate(blockstates);
            writeBeehiveModel(blockModels);
            writeParent(itemModels, "resource_beehive", MODID + ":block/resource_beehive");
            writeSimpleLoot(lootTables, "resource_beehive", MODID + ":resource_beehive");

            JsonArray beehivesTag = new JsonArray();
            beehivesTag.add(MODID + ":resource_beehive");
            writeTag(tagsBlock.resolve("beehives.json"), beehivesTag);

            Path poiTags = root.resolve("data/minecraft/tags/point_of_interest_type");
            Files.createDirectories(poiTags);
            JsonArray beeHomeTag = new JsonArray();
            beeHomeTag.add(MODID + ":resource_beehive");
            writeTag(poiTags.resolve("bee_home.json"), beeHomeTag);

            // Mallets tag
            JsonArray malletsArr = new JsonArray();
            for (String t : new String[]{"wooden","stone","iron","golden","diamond","netherite"})
                malletsArr.add(MODID + ":" + t + "_mallet");
            writeTag(modTagsItem.resolve("mallets.json"), malletsArr);

            // Mallet recipes
            writeMalletRecipes(recipes);

            // === Tier 1 Sapling Recipes ===
            writeTier1SaplingRecipes(recipes);

            // === Drying Rack Recipes (vanilla recipe type) ===
            writeDryingRecipes(recipes);

            // === Mutation Recipes (Tier 2+) ===
            writeMutationRecipes(recipes);

            NostalgicTrees.LOGGER.info("Generated all dynamic resources successfully");
        } catch (Exception e) {
            NostalgicTrees.LOGGER.error("Failed to generate dynamic resources!", e);
        }
    }

    private static void writeSharedModels(Path bm, Path im) throws IOException {
        String log = """
        {"parent":"minecraft:block/cube_column","textures":{"end":"MODID:block/base_log_top","side":"MODID:block/base_log"},
         "elements":[{"from":[0,0,0],"to":[16,16,16],"faces":{
         "down":{"texture":"#end","tintindex":0},"up":{"texture":"#end","tintindex":0},
         "north":{"texture":"#side","tintindex":0},"south":{"texture":"#side","tintindex":0},
         "west":{"texture":"#side","tintindex":0},"east":{"texture":"#side","tintindex":0}}}]}
        """.replace("MODID", MODID);
        Files.writeString(bm.resolve("nt_log.json"), log);

        String stripped = """
        {"parent":"minecraft:block/cube_column","textures":{"end":"MODID:block/base_stripped_log_top","side":"MODID:block/base_stripped_log"},
         "elements":[{"from":[0,0,0],"to":[16,16,16],"faces":{
         "down":{"texture":"#end","tintindex":0},"up":{"texture":"#end","tintindex":0},
         "north":{"texture":"#side","tintindex":0},"south":{"texture":"#side","tintindex":0},
         "west":{"texture":"#side","tintindex":0},"east":{"texture":"#side","tintindex":0}}}]}
        """.replace("MODID", MODID);
        Files.writeString(bm.resolve("nt_stripped_log.json"), stripped);

        String leaves = """
        {"parent":"minecraft:block/cube_all","textures":{"all":"MODID:block/base_leaves"},
         "elements":[{"from":[0,0,0],"to":[16,16,16],"faces":{
         "down":{"texture":"#all","tintindex":0},"up":{"texture":"#all","tintindex":0},
         "north":{"texture":"#all","tintindex":0},"south":{"texture":"#all","tintindex":0},
         "west":{"texture":"#all","tintindex":0},"east":{"texture":"#all","tintindex":0}}}]}
        """.replace("MODID", MODID);
        Files.writeString(bm.resolve("nt_leaves.json"), leaves);

        String sapling = """
        {"parent":"minecraft:block/cross","textures":{"cross":"MODID:block/base_sapling"},
         "elements":[
         {"from":[0.8,0,8],"to":[15.2,16,8],"rotation":{"origin":[8,8,8],"axis":"y","angle":45,"rescale":true},"shade":false,
          "faces":{"north":{"uv":[0,0,16,16],"texture":"#cross","tintindex":0},"south":{"uv":[0,0,16,16],"texture":"#cross","tintindex":0}}},
         {"from":[8,0,0.8],"to":[8,16,15.2],"rotation":{"origin":[8,8,8],"axis":"y","angle":45,"rescale":true},"shade":false,
          "faces":{"west":{"uv":[0,0,16,16],"texture":"#cross","tintindex":0},"east":{"uv":[0,0,16,16],"texture":"#cross","tintindex":0}}}]}
        """.replace("MODID", MODID);
        Files.writeString(bm.resolve("nt_sapling.json"), sapling);

        String saplingItem = """
        {"parent":"minecraft:item/generated","textures":{"layer0":"MODID:block/base_sapling"}}
        """.replace("MODID", MODID);
        Files.writeString(im.resolve("nt_sapling.json"), saplingItem);
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
        Files.writeString(dir.resolve(name + "_leaves.json"), json);
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

    private static void writeChunkRecipe(Path dir, String name) throws IOException {
        JsonObject o = new JsonObject();
        o.addProperty("type", "minecraft:crafting_shapeless");
        JsonArray ing = new JsonArray();
        JsonObject i = new JsonObject();
        i.addProperty("item", MODID + ":stripped_" + name + "_log");
        ing.add(i);
        o.add("ingredients", ing);
        JsonObject r = new JsonObject();
        r.addProperty("id", MODID + ":" + name + "_chunk");
        r.addProperty("count", 4);
        o.add("result", r);
        writeJson(dir.resolve(name + "_chunk_from_stripped_log.json"), o);
    }

    private static void writeResourceRecipe(Path dir, String name, String outputItem, int outputCount) throws IOException {
        JsonObject o = new JsonObject();
        o.addProperty("type", "minecraft:crafting_shaped");
        JsonArray pattern = new JsonArray();
        pattern.add("ACA");
        pattern.add("CHC");
        pattern.add("ACA");
        o.add("pattern", pattern);
        JsonObject key = new JsonObject();
        JsonObject a = new JsonObject();
        a.addProperty("item", MODID + ":" + name + "_apple");
        key.add("A", a);
        JsonObject c = new JsonObject();
        c.addProperty("item", MODID + ":" + name + "_chunk");
        key.add("C", c);
        JsonObject h = new JsonObject();
        h.addProperty("item", MODID + ":" + name + "_honeycomb");
        key.add("H", h);
        o.add("key", key);
        JsonObject r = new JsonObject();
        r.addProperty("id", outputItem);
        r.addProperty("count", outputCount);
        o.add("result", r);
        writeJson(dir.resolve(name + "_resource_from_chunk_apple.json"), o);
    }

    private static void writeMalletRecipes(Path dir) throws IOException {
        String[][] mallets = {
                {"wooden", "minecraft:oak_log", "minecraft:stick"},
                {"stone", "minecraft:cobblestone", "minecraft:stick"},
                {"iron", "minecraft:iron_block", "minecraft:stick"},
                {"golden", "minecraft:gold_block", "minecraft:stick"},
                {"diamond", "minecraft:diamond_block", "minecraft:stick"}
        };
        for (String[] m : mallets) {
            JsonObject o = new JsonObject();
            o.addProperty("type", "minecraft:crafting_shaped");
            JsonArray pattern = new JsonArray();
            pattern.add(" HH"); pattern.add(" HH"); pattern.add("S  ");
            o.add("pattern", pattern);
            JsonObject key = new JsonObject();
            JsonObject h = new JsonObject();
            h.addProperty("item", m[1]);
            key.add("H", h);
            JsonObject s = new JsonObject();
            s.addProperty("item", m[2]);
            key.add("S", s);
            o.add("key", key);
            JsonObject result = new JsonObject();
            result.addProperty("id", MODID + ":" + m[0] + "_mallet");
            result.addProperty("count", 1);
            o.add("result", result);
            writeJson(dir.resolve(m[0] + "_mallet.json"), o);
        }
        // Netherite smithing
        JsonObject neo = new JsonObject();
        neo.addProperty("type", "minecraft:smithing_transform");
        JsonObject template = new JsonObject(); template.addProperty("item", "minecraft:netherite_upgrade_smithing_template");
        neo.add("template", template);
        JsonObject base = new JsonObject(); base.addProperty("item", MODID + ":diamond_mallet");
        neo.add("base", base);
        JsonObject addition = new JsonObject(); addition.addProperty("item", "minecraft:netherite_ingot");
        neo.add("addition", addition);
        JsonObject result = new JsonObject(); result.addProperty("id", MODID + ":netherite_mallet");
        neo.add("result", result);
        writeJson(dir.resolve("netherite_mallet.json"), neo);
    }

    private static void writeTag(Path file, JsonArray values) throws IOException {
        JsonObject o = new JsonObject();
        o.addProperty("replace", false);
        o.add("values", values);
        writeJson(file, o);
    }

    private static void writeBeehiveBlockstate(Path dir) throws IOException {
        JsonObject root = new JsonObject();
        JsonObject variants = new JsonObject();

        String model = MODID + ":block/resource_beehive";

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
        writeJson(dir.resolve("resource_beehive.json"), root);
    }

    private static void writeBeehiveModel(Path dir) throws IOException {
        String model = """
        {
          "parent": "minecraft:block/orientable_with_bottom",
          "textures": {
            "top": "minecraft:block/bee_nest_top",
            "side": "minecraft:block/bee_nest_side",
            "front": "minecraft:block/bee_nest_front",
            "bottom": "minecraft:block/bee_nest_bottom"
          }
        }
        """;
        Files.writeString(dir.resolve("resource_beehive.json"), model);
    }

    private static void writeJson(Path path, JsonObject obj) throws IOException {
        Files.writeString(path, GSON.toJson(obj));
    }

    private static String cap(String s) { return s.substring(0, 1).toUpperCase() + s.substring(1); }

    // ======================== DRYING RACK ========================

    private static void writeDryingRackResources(Path blockstates, Path blockModels, Path itemModels,
                                                 Path lootTables, Path recipes, JsonObject langObj) throws IOException {
        // Lang
        langObj.addProperty("block." + MODID + ".drying_rack", "Drying Rack");
        langObj.addProperty("gui.nostalgictrees.drying_rack", "Drying Rack");
        langObj.addProperty("gui.nostalgictrees.mutation", "Bee Mutation");

        // Blockstate with facing variants
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

        // Block model — wall-mounted shelf extending outward from north wall
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
        Files.writeString(blockModels.resolve("drying_rack.json"), model);

        // Item model
        writeParent(itemModels, "drying_rack", MODID + ":block/drying_rack");

        // Loot table
        writeSimpleLoot(lootTables, "drying_rack", MODID + ":drying_rack");

        // Crafting recipe: 3 planks top + 2 sticks below
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:crafting_shaped");
        JsonArray pattern = new JsonArray();
        pattern.add("PPP");
        pattern.add("S S");
        recipe.add("pattern", pattern);
        JsonObject key = new JsonObject();
        JsonObject p = new JsonObject();
        p.addProperty("item", "minecraft:oak_planks");
        key.add("P", p);
        JsonObject s = new JsonObject();
        s.addProperty("item", "minecraft:stick");
        key.add("S", s);
        recipe.add("key", key);
        JsonObject rackResult = new JsonObject();
        rackResult.addProperty("id", MODID + ":drying_rack");
        rackResult.addProperty("count", 1);
        recipe.add("result", rackResult);
        writeJson(recipes.resolve("drying_rack.json"), recipe);
    }

    // ======================== TIER 1 SAPLING RECIPES ========================

    private static void writeTier1SaplingRecipes(Path recipes) throws IOException {
        // 1. Dirt Sapling: 8 dirt + oak sapling center
        writeSurroundRecipe(recipes, "dirt_sapling",
                "minecraft:dirt", "minecraft:oak_sapling",
                MODID + ":dirt_sapling");

        // 2. Stone Sapling: Dirt Sapling on drying rack (handled in DryingRackBlockEntity)

        // 3. Gravel Sapling: Smelt stone sapling
        writeSmeltingRecipe(recipes, "gravel_sapling_from_smelting",
                MODID + ":stone_sapling", MODID + ":gravel_sapling");

        // 4. Sand Sapling: Smelt gravel sapling
        writeSmeltingRecipe(recipes, "sand_sapling_from_smelting",
                MODID + ":gravel_sapling", MODID + ":sand_sapling");

        // 5. Clay Sapling: Dirt, Stone, Gravel, Sand saplings in + around oak sapling
        JsonObject clay = new JsonObject();
        clay.addProperty("type", "minecraft:crafting_shaped");
        JsonArray clayPattern = new JsonArray();
        clayPattern.add(" D ");
        clayPattern.add("SOG");
        clayPattern.add(" A ");
        clay.add("pattern", clayPattern);
        JsonObject clayKey = new JsonObject();
        JsonObject d = new JsonObject(); d.addProperty("item", MODID + ":dirt_sapling");
        clayKey.add("D", d);
        JsonObject st = new JsonObject(); st.addProperty("item", MODID + ":stone_sapling");
        clayKey.add("S", st);
        JsonObject o = new JsonObject(); o.addProperty("item", "minecraft:oak_sapling");
        clayKey.add("O", o);
        JsonObject g = new JsonObject(); g.addProperty("item", MODID + ":gravel_sapling");
        clayKey.add("G", g);
        JsonObject a = new JsonObject(); a.addProperty("item", MODID + ":sand_sapling");
        clayKey.add("A", a);
        clay.add("key", clayKey);
        JsonObject clayResult = new JsonObject();
        clayResult.addProperty("id", MODID + ":clay_sapling");
        clayResult.addProperty("count", 1);
        clay.add("result", clayResult);
        writeJson(recipes.resolve("clay_sapling.json"), clay);

        // 6. Bone Sapling: 8 bone meal + sand sapling center
        writeSurroundRecipe(recipes, "bone_sapling",
                "minecraft:bone_meal", MODID + ":sand_sapling",
                MODID + ":bone_sapling");

        // 7. Coal Sapling: clay sapling surrounded by charcoal
        writeSurroundRecipe(recipes, "coal_sapling",
                "minecraft:charcoal", MODID + ":clay_sapling",
                MODID + ":coal_sapling");

        // 8. Ice Sapling: bone sapling surrounded by snowballs
        writeSurroundRecipe(recipes, "ice_sapling",
                "minecraft:snowball", MODID + ":bone_sapling",
                MODID + ":ice_sapling");
    }

    private static void writeSurroundRecipe(Path dir, String name, String surrounding, String center, String output) throws IOException {
        JsonObject o = new JsonObject();
        o.addProperty("type", "minecraft:crafting_shaped");
        JsonArray pattern = new JsonArray();
        pattern.add("SSS");
        pattern.add("SCS");
        pattern.add("SSS");
        o.add("pattern", pattern);
        JsonObject key = new JsonObject();
        JsonObject s = new JsonObject();
        s.addProperty("item", surrounding);
        key.add("S", s);
        JsonObject c = new JsonObject();
        c.addProperty("item", center);
        key.add("C", c);
        o.add("key", key);
        JsonObject r = new JsonObject();
        r.addProperty("id", output);
        r.addProperty("count", 1);
        o.add("result", r);
        writeJson(dir.resolve(name + ".json"), o);
    }

    private static void writeSmeltingRecipe(Path dir, String name, String input, String output) throws IOException {
        JsonObject o = new JsonObject();
        o.addProperty("type", "minecraft:smelting");
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("item", input);
        o.add("ingredient", ingredient);
        JsonObject result = new JsonObject();
        result.addProperty("id", output);
        result.addProperty("count", 1);
        o.add("result", result);
        o.addProperty("experience", 0.1);
        o.addProperty("cookingtime", 200);
        writeJson(dir.resolve(name + ".json"), o);
    }

    // ======================== DRYING RACK RECIPES ========================

    private static void writeDryingRecipes(Path recipes) throws IOException {
        // Dirt Sapling → Stone Sapling (30 seconds)
        writeDryingRecipe(recipes, "dirt_to_stone_sapling",
                MODID + ":dirt_sapling", MODID + ":stone_sapling", 1, 600);

        // Clay Ball → Bone Meal (30 seconds)
        writeDryingRecipe(recipes, "clay_ball_to_bone_meal",
                "minecraft:clay_ball", "minecraft:bone_meal", 1, 600);

        // Bone Block → Snow Block (30 seconds)
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

    // ======================== MUTATION RECIPES ========================

    private static void writeMutationRecipes(Path recipes) throws IOException {
        Path mutationDir = recipes;
        // Define mutations that require honeycombs + bee pollination

        // Copper Sapling: coal + bone honeycombs a sapling
        writeMutationRecipe(mutationDir, "copper_mutation",
                MODID + ":clay_sapling",
                new String[]{MODID + ":coal_honeycomb", MODID + ":bone_honeycomb"},
                MODID + ":copper_sapling", 3);

        // Iron Sapling: coal + copper honeycombs a sapling
        writeMutationRecipe(mutationDir, "iron_mutation",
                MODID + ":clay_sapling",
                new String[]{MODID + ":coal_honeycomb", MODID + ":copper_honeycomb"},
                MODID + ":iron_sapling", 5);
    }

    private static void writeMutationRecipe(Path dir, String name, String baseSapling,
                                             String[] honeycombs, String result, int pollinations) throws IOException {
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
        writeJson(dir.resolve(name + ".json"), o);
    }
}