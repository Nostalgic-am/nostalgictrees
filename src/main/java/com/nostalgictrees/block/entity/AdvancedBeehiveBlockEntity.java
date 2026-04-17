package com.nostalgictrees.block.entity;

import com.nostalgictrees.NTBlocks;
import com.nostalgictrees.NTItems;
import com.nostalgictrees.block.ResourceSaplingBlock;
import com.nostalgictrees.menu.AdvancedBeehiveMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;

public class AdvancedBeehiveBlockEntity extends BeehiveBlockEntity implements Container, MenuProvider {

    public static final int BOTTLE_SLOT = 0;
    public static final int SHEARS_SLOT = 1;
    public static final int OUTPUT_SLOT_START = 2;
    public static final int OUTPUT_SLOT_COUNT = 9;
    public static final int TOTAL_SLOTS = 11;
    public static final int MAX_BEES = 5;

    private final ItemStack[] inventory = new ItemStack[TOTAL_SLOTS];

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> {
                    BlockState state = getBlockState();
                    yield state.hasProperty(BeehiveBlock.HONEY_LEVEL) ? state.getValue(BeehiveBlock.HONEY_LEVEL) : 0;
                }
                case 1 -> getOccupantCount();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public AdvancedBeehiveBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            inventory[i] = ItemStack.EMPTY;
        }
    }

    // ======================== OVERRIDE TYPE ========================

    @Override
    public net.minecraft.world.level.block.entity.BlockEntityType<?> getType() {
        return NTBlocks.ADVANCED_BEEHIVE_BE.get();
    }

    @Override
    public boolean isFull() {
        return this.getOccupantCount() >= MAX_BEES;
    }

    @Override
    public void addOccupant(Bee pOccupant) {
        if (this.getOccupantCount() < MAX_BEES) {
            pOccupant.stopRiding();
            pOccupant.ejectPassengers();
            this.storeBee(BeehiveBlockEntity.Occupant.of(pOccupant));
            if (this.level != null) {
                BlockPos blockpos = this.getBlockPos();
                this.level.playSound(null, blockpos.getX(), blockpos.getY(), blockpos.getZ(),
                        SoundEvents.BEEHIVE_ENTER, SoundSource.BLOCKS, 1.0F, 1.0F);
                this.level.gameEvent(GameEvent.BLOCK_CHANGE, blockpos,
                        GameEvent.Context.of(pOccupant, this.getBlockState()));
            }
            pOccupant.discard();
            super.setChanged();
        }
    }

    // ======================== TICK ========================

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  AdvancedBeehiveBlockEntity be) {
        BeehiveBlockEntity.serverTick(level, pos, state, be);
        be.productionTick();
    }

    private void productionTick() {
        if (level == null) return;

        BlockState state = getBlockState();
        if (!state.hasProperty(BeehiveBlock.HONEY_LEVEL)) return;

        int honeyLevel = state.getValue(BeehiveBlock.HONEY_LEVEL);
        if (honeyLevel < 5) return;

        boolean hasShears = hasShears();
        boolean hasBottles = hasBottles();

        if (!hasShears && !hasBottles) return;

        boolean harvested = false;

        if (hasShears) {
            List<String> nearbySaplings = findNearbySaplings(6);
            if (!nearbySaplings.isEmpty()) {
                harvested = produceHoneycombs(nearbySaplings);
            } else {
                if (insertIntoOutput(new ItemStack(Items.HONEYCOMB, 3))) {
                    ItemStack shears = inventory[SHEARS_SLOT];
                    if (shears.isDamageableItem()) {
                        shears.setDamageValue(shears.getDamageValue() + 1);
                        if (shears.getDamageValue() >= shears.getMaxDamage()) {
                            inventory[SHEARS_SLOT] = ItemStack.EMPTY;
                        }
                    }
                    harvested = true;
                }
            }
        }

        if (hasBottles) {
            if (produceHoneyBottle()) {
                harvested = true;
            }
        }

        if (harvested) {
            level.setBlockAndUpdate(worldPosition, state.setValue(BeehiveBlock.HONEY_LEVEL, 0));
            setChanged();
        }
    }

    private boolean hasShears() {
        return !inventory[SHEARS_SLOT].isEmpty() && inventory[SHEARS_SLOT].is(Items.SHEARS);
    }

    private boolean hasBottles() {
        return !inventory[BOTTLE_SLOT].isEmpty() && inventory[BOTTLE_SLOT].is(Items.GLASS_BOTTLE);
    }

    private List<String> findNearbySaplings(int range) {
        List<String> found = new ArrayList<>();
        if (level == null) return found;

        for (BlockPos check : BlockPos.betweenClosed(
                worldPosition.offset(-range, -range, -range),
                worldPosition.offset(range, range, range))) {
            BlockState blockState = level.getBlockState(check);
            if (blockState.getBlock() instanceof ResourceSaplingBlock sapling) {
                if (!found.contains(sapling.getTreeName())) {
                    found.add(sapling.getTreeName());
                }
            }
        }
        return found;
    }

    private boolean produceHoneycombs(List<String> saplingTypes) {
        if (level == null || saplingTypes.isEmpty()) return false;

        String treeName = saplingTypes.get(level.getRandom().nextInt(saplingTypes.size()));
        ItemStack combStack = NTItems.getHoneycombItem(treeName);
        if (combStack.isEmpty()) return false;

        if (insertIntoOutput(combStack)) {
            ItemStack shears = inventory[SHEARS_SLOT];
            if (shears.isDamageableItem()) {
                shears.setDamageValue(shears.getDamageValue() + 1);
                if (shears.getDamageValue() >= shears.getMaxDamage()) {
                    inventory[SHEARS_SLOT] = ItemStack.EMPTY;
                }
            }
            return true;
        }
        return false;
    }

    private boolean produceHoneyBottle() {
        ItemStack honeyBottle = new ItemStack(Items.HONEY_BOTTLE);
        if (insertIntoOutput(honeyBottle)) {
            inventory[BOTTLE_SLOT].shrink(1);
            return true;
        }
        return false;
    }

    private boolean insertIntoOutput(ItemStack toInsert) {
        for (int i = OUTPUT_SLOT_START; i < OUTPUT_SLOT_START + OUTPUT_SLOT_COUNT; i++) {
            ItemStack existing = inventory[i];
            if (!existing.isEmpty() && ItemStack.isSameItemSameComponents(existing, toInsert)
                    && existing.getCount() < existing.getMaxStackSize()) {
                int space = existing.getMaxStackSize() - existing.getCount();
                int toAdd = Math.min(space, toInsert.getCount());
                existing.grow(toAdd);
                toInsert.shrink(toAdd);
                if (toInsert.isEmpty()) return true;
            }
        }
        for (int i = OUTPUT_SLOT_START; i < OUTPUT_SLOT_START + OUTPUT_SLOT_COUNT; i++) {
            if (inventory[i].isEmpty()) {
                inventory[i] = toInsert.copy();
                return true;
            }
        }
        return false;
    }

    // ======================== CONTAINER ========================

    @Override
    public int getContainerSize() {
        return TOTAL_SLOTS;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot >= 0 && slot < TOTAL_SLOTS ? inventory[slot] : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot < 0 || slot >= TOTAL_SLOTS) return ItemStack.EMPTY;
        ItemStack result = inventory[slot].split(amount);
        if (!result.isEmpty()) setChanged();
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot < 0 || slot >= TOTAL_SLOTS) return ItemStack.EMPTY;
        ItemStack stack = inventory[slot];
        inventory[slot] = ItemStack.EMPTY;
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= TOTAL_SLOTS) return;
        inventory[slot] = stack;
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            inventory[i] = ItemStack.EMPTY;
        }
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == BOTTLE_SLOT) return stack.is(Items.GLASS_BOTTLE);
        if (slot == SHEARS_SLOT) return stack.is(Items.SHEARS);
        return false;
    }

    // ======================== MENU PROVIDER ========================

    @Override
    public Component getDisplayName() {
        return Component.translatable("gui.nostalgictrees.advanced_beehive");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new AdvancedBeehiveMenu(containerId, playerInventory, this, dataAccess);
    }

// ======================== NBT ========================

    /*
     * 26.1 change: saveAdditional/loadAdditional now take ValueOutput/ValueInput
     * instead of (CompoundTag, HolderLookup.Provider). Persistence uses Codecs,
     * so we use ItemStack.OPTIONAL_CODEC for each slot.
     */

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            if (!inventory[i].isEmpty()) {
                output.store("Slot" + i, ItemStack.OPTIONAL_CODEC, inventory[i]);
            }
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            inventory[i] = input.read("Slot" + i, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        }
    }

    // ======================== SYNC ========================

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        // Default vanilla implementation reads the BE's own NBT via saveCustomOnly.
        // This produces a tag containing what saveAdditional(ValueOutput) emitted.
        return super.getUpdateTag(registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}