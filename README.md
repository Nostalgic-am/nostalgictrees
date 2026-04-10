# Nostalgic Trees

A NeoForge 1.21.1 mod inspired by Sky Orchards from Sky Factory. Grow resource trees, harvest apples, and process logs with mallets to obtain raw materials.

## How It Works

### The Pipeline
```
Sapling → Tree (1 log + leaves) → Break leaves for Apples
                                 → Break log with Mallet for Stripped Log
Stripped Log → Crafting → 4 Chunks
Chunk + Apple → Crafting → Raw Resource
```

### Tree Tiers
Trees are organized into 5 tiers that affect mallet durability cost:

| Tier | Durability Cost | Resources |
|------|----------------|-----------|
| Tier 1 | 1 per hit | Dirt, Sand, Gravel, Clay, Bone |
| Tier 2 | 2 per hit | Coal, Copper |
| Tier 3 | 4 per hit | Iron, Quartz, Redstone |
| Tier 4 | 8 per hit | Gold, Lapis, Glowstone |
| Tier 5 | 16 per hit | Diamond, Emerald, Ender Pearl, Obsidian |

### Mallets
Tiered tools that convert resource logs into stripped logs when used to break them.

| Mallet | Durability | Efficiency | Tier 5 Cost |
|--------|-----------|------------|-------------|
| Wooden | 59 | 1.0x | 16 (~3 logs) |
| Stone | 131 | 0.85x | 14 (~9 logs) |
| Iron | 250 | 0.7x | 12 (~20 logs) |
| Gold | 32 | 0.5x | 8 (~4 logs) |
| Diamond | 1561 | 0.5x | 8 (~195 logs) |
| Netherite | 2031 | 0.35x | 6 (~338 logs) |

Gold mallets are low durability but very efficient — a niche choice for specific situations.

### Recipes
- **Mallets**: Shaped recipe (material + sticks in a hammer pattern)
- **Netherite Mallet**: Smithing table (diamond mallet + netherite ingot + template)
- **Chunks**: Shapeless (1 stripped log → 4 chunks)
- **Resources**: Shapeless (1 chunk + 1 apple → raw resource)

## For Modpack Developers

### Adding Custom Trees via Datapack
Place a JSON file in your datapack at:
```
data/<namespace>/nostalgictrees/trees/<tree_name>.json
```

Example (`data/mypack/nostalgictrees/trees/tin.json`):
```json
{
  "name": "tin",
  "tier": "tier_2",
  "output_item": "mekanism:raw_tin",
  "output_count": 2,
  "required_mod": "mekanism"
}
```

### Tier Values
- `tier_1` — 1 durability cost (basic materials)
- `tier_2` — 2 durability cost (common ores)
- `tier_3` — 4 durability cost (mid-tier ores)
- `tier_4` — 8 durability cost (valuable resources)
- `tier_5` — 16 durability cost (endgame resources)

### Required Assets
When adding a custom tree, you also need to provide in a resource pack:
- Textures: `textures/block/<name>_log.png`, `stripped_<name>_log.png`, `<name>_leaves.png`, `<name>_sapling.png`
- Textures: `textures/item/<name>_apple.png`, `<name>_chunk.png`
- Blockstates and models (follow the pattern of built-in trees)
- Lang entries

## Built-in Resources (17 tree types)

**Tier 1:** Dirt, Sand, Gravel, Clay, Bone
**Tier 2:** Coal, Copper
**Tier 3:** Iron, Quartz, Redstone
**Tier 4:** Gold, Lapis, Glowstone
**Tier 5:** Diamond, Emerald, Ender Pearl, Obsidian

## Installation
1. Install NeoForge for Minecraft 1.21.1
2. Place the mod jar in your `mods` folder
3. Launch the game

## Building from Source
```bash
./gradlew build
```
The built jar will be in `build/libs/`.

## License
MIT
