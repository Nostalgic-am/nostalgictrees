package com.nostalgictrees.block.entity;

import com.nostalgictrees.NTBlocks;
import com.nostalgictrees.NTItems;
import com.nostalgictrees.NostalgicTrees;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Drying Rack Block Entity.
 *
 * Holds one item and transforms it over time.
 * Recipes are registered as input → (output, ticks) pairs.
 */
public class DryingRackBlockEntity extends BlockEntity {

    private ItemStack storedItem = ItemStack.EMPTY;
    private int dryingProgress = 0;
    private int dryingTimeRequired = 0;

    // Drying recipes: input item → (output supplier, time in ticks)
    // Using suppliers because items may not be registered at static init time
    private static final Map<DryingRecipe, Boolean> RECIPES_INITIALIZED = new LinkedHashMap<>();
    private static java.util.List<DryingRecipe> RECIPES = null;

    public static class DryingRecipe {
        public final Supplier<ItemStack> input;
        public final Supplier<ItemStack> output;
        public final int ticks;

        public DryingRecipe(Supplier<ItemStack> input, Supplier<ItemStack> output, int ticks) {
            this.input = input;
            this.output = output;
            this.ticks = ticks;
        }
    }

    public static java.util.List<DryingRecipe> getRecipes() {
        if (RECIPES == null) {
            RECIPES = new java.util.ArrayList<>();

            // Dirt Sapling → Stone Sapling (30 seconds)
            RECIPES.add(new DryingRecipe(
                    () -> getSaplingStack("dirt"),
                    () -> getSaplingStack("stone"),
                    600
            ));

            // Clay Ball → Bone Meal (20 seconds)
            RECIPES.add(new DryingRecipe(
                    () -> new ItemStack(Items.CLAY_BALL),
                    () -> new ItemStack(Items.BONE_MEAL),
                    600
            ));
            RECIPES.add(new DryingRecipe(
                    () -> new ItemStack(Items.BONE_BLOCK),
                    () -> new ItemStack(Items.SNOW_BLOCK),
                    600
            ));
        }
        return RECIPES;
    }

    private static ItemStack getSaplingStack(String treeName) {
        var saplingItems = NTItems.getAllSaplingItems();
        var item = saplingItems.get(treeName);
        return item != null ? new ItemStack(item.get()) : ItemStack.EMPTY;
    }

    public DryingRackBlockEntity(BlockPos pos, BlockState state) {
        super(NTBlocks.DRYING_RACK_BE.get(), pos, state);
    }

    // ======================== TICK ========================

    public static void serverTick(Level level, BlockPos pos, BlockState state, DryingRackBlockEntity be) {
        if (be.storedItem.isEmpty()) return;

        // Find matching recipe
        DryingRecipe recipe = be.findRecipe();
        if (recipe == null) return;

        be.dryingTimeRequired = recipe.ticks;
        be.dryingProgress++;

        // Spawn occasional particles
        if (level instanceof ServerLevel serverLevel && be.dryingProgress % 40 == 0) {
            serverLevel.sendParticles(ParticleTypes.SMOKE,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    2, 0.2, 0.1, 0.2, 0.01);
        }

        if (be.dryingProgress >= be.dryingTimeRequired) {
            // Transform the item
            be.storedItem = recipe.output.get().copy();
            be.dryingProgress = 0;
            be.dryingTimeRequired = 0;
            be.setChanged();

            // Play completion sound
            level.playSound(null, pos, SoundEvents.GRASS_BREAK, SoundSource.BLOCKS, 1.0f, 1.2f);

            // Sync to client
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    private DryingRecipe findRecipe() {
        for (DryingRecipe recipe : getRecipes()) {
            ItemStack input = recipe.input.get();
            if (!input.isEmpty() && ItemStack.isSameItem(storedItem, input)) {
                return recipe;
            }
        }
        return null;
    }

    // ======================== ITEM MANAGEMENT ========================

    public ItemStack getItem() {
        return storedItem;
    }

    public void setItem(ItemStack stack) {
        storedItem = stack;
        dryingProgress = 0;
        dryingTimeRequired = 0;
        setChanged();
    }

    public ItemStack removeItem() {
        ItemStack removed = storedItem;
        storedItem = ItemStack.EMPTY;
        dryingProgress = 0;
        dryingTimeRequired = 0;
        setChanged();
        return removed;
    }

    public int getDryingProgress() {
        return dryingProgress;
    }

    public int getDryingTimeRequired() {
        return dryingTimeRequired;
    }

    // ======================== NBT ========================

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!storedItem.isEmpty()) {
            tag.put("StoredItem", (CompoundTag) storedItem.save(registries));
        }
        tag.putInt("DryingProgress", dryingProgress);
        tag.putInt("DryingTimeRequired", dryingTimeRequired);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("StoredItem")) {
            storedItem = ItemStack.parse(registries, tag.getCompound("StoredItem")).orElse(ItemStack.EMPTY);
        } else {
            storedItem = ItemStack.EMPTY;
        }
        dryingProgress = tag.getInt("DryingProgress");
        dryingTimeRequired = tag.getInt("DryingTimeRequired");
    }

    // ======================== SYNC ========================

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
