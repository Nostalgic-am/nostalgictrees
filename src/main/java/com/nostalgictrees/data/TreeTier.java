package com.nostalgictrees.data;

import net.minecraft.util.StringRepresentable;

public enum TreeTier implements StringRepresentable {
    TIER_1("tier_1", 1, 0x8B6914),   // Dirt, Sand, Gravel, Clay, Bone
    TIER_2("tier_2", 2, 0x7B7B7B),   // Coal, Copper
    TIER_3("tier_3", 4, 0xD4D4D4),   // Iron, Quartz, Redstone
    TIER_4("tier_4", 8, 0xFFD700),   // Gold, Lapis, Glowstone
    TIER_5("tier_5", 16, 0x00FFFF),  // Diamond, Emerald, Ender Pearl, Obsidian
    ;

    private final String name;
    private final int durabilityCost;
    private final int color;

    TreeTier(String name, int durabilityCost, int color) {
        this.name = name;
        this.durabilityCost = durabilityCost;
        this.color = color;
    }

    public int getDurabilityCost() {
        return durabilityCost;
    }

    public int getColor() {
        return color;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public static TreeTier fromString(String name) {
        for (TreeTier tier : values()) {
            if (tier.name.equals(name)) return tier;
        }
        return TIER_1;
    }
}
