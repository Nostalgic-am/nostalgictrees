package com.nostalgictrees.data;

import net.minecraft.world.item.ToolMaterial;

public enum MalletTier {
    WOOD(ToolMaterial.WOOD, 59, 1.0f),
    STONE(ToolMaterial.STONE, 131, 0.85f),
    IRON(ToolMaterial.IRON, 250, 0.7f),
    GOLD(ToolMaterial.GOLD, 32, 0.5f),     // Gold: low durability but very efficient
    DIAMOND(ToolMaterial.DIAMOND, 1561, 0.5f),
    NETHERITE(ToolMaterial.NETHERITE, 2031, 0.35f);

    private final ToolMaterial vanillaMaterial;
    private final int maxDurability;
    private final float efficiencyMultiplier;

    MalletTier(ToolMaterial vanillaMaterial, int maxDurability, float efficiencyMultiplier) {
        this.vanillaMaterial = vanillaMaterial;
        this.maxDurability = maxDurability;
        this.efficiencyMultiplier = efficiencyMultiplier;
    }

    public ToolMaterial getVanillaMaterial() {
        return vanillaMaterial;
    }

    public int getMaxDurability() {
        return maxDurability;
    }

    public float getEfficiencyMultiplier() {
        return efficiencyMultiplier;
    }

    public int getDurabilityCostFor(TreeTier treeTier) {
        return Math.max(1, Math.round(treeTier.getDurabilityCost() * efficiencyMultiplier));
    }

    public String getSerializedName() {
        return name().toLowerCase();
    }
}