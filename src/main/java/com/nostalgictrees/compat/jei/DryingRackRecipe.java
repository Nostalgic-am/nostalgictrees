package com.nostalgictrees.compat.jei;

import net.minecraft.world.item.ItemStack;

public record DryingRackRecipe(ItemStack input, ItemStack output, int ticks) {
}
