# Nostalgic Trees

---
## What It Does

Nostalgic Trees introduces **unique resource trees**, each producing a different item — from dirt and stone to diamond and netherite. Players progress through tiers of trees by:

1. **Crafting** basic saplings from vanilla materials
2. **Mutating** higher-tier saplings by applying honeycombs and waiting for bees to pollinate them
3. **Catalyzing** mutations with rare resources (gold, diamond, netherite) as part of the recipe
4. **Processing** grown logs with mallets to extract resource chunks
5. **Crafting** (e.g. 4 iron chunks + 4 iron apples + 1 iron bee comb = 4 iron ingots)

**All recipes are visible in JEI. Saplings display their mutation progress via Jade tooltips.**

---
## Core Mechanics

### Mutations

1. Plant the **base sapling** on dirt or grass
2. Right-click with each required **honeycomb**
3. If the recipe has a **catalyst**, right-click with the catalyst item
4. Place a nearby **Advanced Beehive** with bees
5. Bees will pollinate the sapling until it **transforms** into the result

Mutation progress is shown in the Jade tooltip when looking at the sapling.

### Advanced Beehive

- Holds up to 5 bees
- Outputs **mod-specific honeycombs** when bees return from resource trees
- 9 output slots for collected honey bottles and honeycombs
- Slots for bottles and shears to auto-collect when honey is full

### Mallets (Log → Chunk Processing)

To process a resource tree, **break** the logs with a **mallet** in hand. This will give you the stripped log.
Which can be turned into resource chunks.

### Drying Rack

Place a drying rack and right-click with a valid input to begin drying.

Built-in recipes:
- Dirt Sapling → Stone Sapling
- Clay Ball → Bone Meal
- Bone Block → Snow Block

**Drying recipes are visible in JEI.**

---
## Configuration

### Tree Configs

Each tree is defined by a JSON file in `config/nostalgictrees/trees/`. Edit these to customize:

- `tier` — which tier the tree belongs to (determines durability use on mallet)
- `outputItem` — what item the resource recipe produces (e.g. `minecraft:diamond`)
- `outputCount` — how many items are produced per resource recipe
- `color` — hex color for tinting leaves and chunks

### Creating Custom Trees
Tool to make help make custom trees: https://nostalgic.am/treeconfig/

Drop a new JSON file into `config/nostalgictrees/trees/` like:

```json
{
  "name": "slime",
  "tier": "tier_2",
  "output_item": "minecraft:slime_ball",
  "output_count": 1,
  "color": "5EF765"
}
```

Name the file `{treename}.json`. All textures, blockstates, models, recipes, loot tables, and tags generate automatically.

---
## License

All Rights Reserved. See LICENSE file for details.