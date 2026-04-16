package com.nostalgictrees.data;

import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;

public enum MalletTier {
    WOOD(Tiers.WOOD, 59, 1.0f),
    STONE(Tiers.STONE, 131, 0.85f),
    IRON(Tiers.IRON, 250, 0.7f),
    GOLD(Tiers.GOLD, 32, 0.5f),     // Gold: low durability but very efficient
    DIAMOND(Tiers.DIAMOND, 1561, 0.5f),
    NETHERITE(Tiers.NETHERITE, 2031, 0.35f);

    private final Tier vanillaTier;
    private final int maxDurability;
    private final float efficiencyMultiplier;

    MalletTier(Tier vanillaTier, int maxDurability, float efficiencyMultiplier) {
        this.vanillaTier = vanillaTier;
        this.maxDurability = maxDurability;
        this.efficiencyMultiplier = efficiencyMultiplier;
    }

    public Tier getVanillaTier() {
        return vanillaTier;
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
