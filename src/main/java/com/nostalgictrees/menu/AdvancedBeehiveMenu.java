package com.nostalgictrees.menu;

import com.nostalgictrees.NTBlocks;
import com.nostalgictrees.block.entity.AdvancedBeehiveBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class AdvancedBeehiveMenu extends AbstractContainerMenu {

    private final Container container;
    private final ContainerData data;

    public AdvancedBeehiveMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory,
                new SimpleContainer(AdvancedBeehiveBlockEntity.TOTAL_SLOTS),
                new SimpleContainerData(2));
    }

    public AdvancedBeehiveMenu(int containerId, Inventory playerInventory, Container container, ContainerData data) {
        super(NTBlocks.ADVANCED_BEEHIVE_MENU.get(), containerId);
        this.container = container;
        this.data = data;
        checkContainerSize(container, AdvancedBeehiveBlockEntity.TOTAL_SLOTS);

        // Bottle slot
        addSlot(new FilteredSlot(container, AdvancedBeehiveBlockEntity.BOTTLE_SLOT,
                83, 50, s -> s.is(Items.GLASS_BOTTLE)));
        // Shears slot
        addSlot(new FilteredSlot(container, AdvancedBeehiveBlockEntity.SHEARS_SLOT,
                83, 74, s -> s.is(Items.SHEARS)));

        // Output slots 3x3 grid
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int slotIndex = AdvancedBeehiveBlockEntity.OUTPUT_SLOT_START + row * 3 + col;
                addSlot(new FilteredSlot(container, slotIndex,
                        108 + col * 18, 43 + row * 18, true));
            }
        }

        // Player inventory (3x9)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 140 + row * 18));
            }
        }

        // Hotbar
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 198));
        }

        addDataSlots(data);
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            result = slotStack.copy();

            int beehiveSlotCount = AdvancedBeehiveBlockEntity.TOTAL_SLOTS;
            int playerInvStart = beehiveSlotCount;
            int playerInvEnd = playerInvStart + 36;

            if (index < beehiveSlotCount) {
                if (!this.moveItemStackTo(slotStack, playerInvStart, playerInvEnd, true))
                    return ItemStack.EMPTY;
            } else {
                if (slotStack.is(Items.GLASS_BOTTLE)) {
                    if (!this.moveItemStackTo(slotStack, 0, 1, false)) return ItemStack.EMPTY;
                } else if (slotStack.is(Items.SHEARS)) {
                    if (!this.moveItemStackTo(slotStack, 1, 2, false)) return ItemStack.EMPTY;
                } else if (index < playerInvStart + 27) {
                    if (!this.moveItemStackTo(slotStack, playerInvStart + 27, playerInvEnd, false))
                        return ItemStack.EMPTY;
                } else {
                    if (!this.moveItemStackTo(slotStack, playerInvStart, playerInvStart + 27, false))
                        return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) slot.set(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return result;
    }

    public int getHoneyLevel() { return data.get(0); }
    public int getBeeCount() { return data.get(1); }
}