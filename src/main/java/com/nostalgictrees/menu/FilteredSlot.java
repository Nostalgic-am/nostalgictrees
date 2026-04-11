package com.nostalgictrees.menu;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

/**
 * A slot that uses a predicate to decide what items can be placed.
 * Also supports marking a slot as output-only (no manual insertion).
 */
public class FilteredSlot extends Slot {
    private final Predicate<ItemStack> filter;
    private final boolean outputOnly;

    public FilteredSlot(Container container, int index, int x, int y, Predicate<ItemStack> filter) {
        super(container, index, x, y);
        this.filter = filter;
        this.outputOnly = false;
    }

    public FilteredSlot(Container container, int index, int x, int y, boolean outputOnly) {
        super(container, index, x, y);
        this.filter = s -> true;
        this.outputOnly = outputOnly;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (outputOnly) return false;
        return filter.test(stack);
    }
}
