package com.nostalgictrees.compat.jei;

import net.minecraft.world.item.ItemStack;

public record MalletRecipe(ItemStack input, ItemStack output, String treeName) {
}
