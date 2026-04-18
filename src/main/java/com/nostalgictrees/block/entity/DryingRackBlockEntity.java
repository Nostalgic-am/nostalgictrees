package com.nostalgictrees.block.entity;

import com.nostalgictrees.NTBlocks;
import com.nostalgictrees.NTRecipes;
import com.nostalgictrees.recipe.DryingRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class DryingRackBlockEntity extends BlockEntity {

    private ItemStack storedItem = ItemStack.EMPTY;
    private int dryingProgress = 0;
    private int dryingTimeRequired = 0;

    public DryingRackBlockEntity(BlockPos pos, BlockState state) {
        super(NTBlocks.DRYING_RACK_BE.get(), pos, state);
    }

    // ======================== TICK ========================

    public static void serverTick(Level level, BlockPos pos, BlockState state, DryingRackBlockEntity be) {
        if (be.storedItem.isEmpty()) return;

        // Find matching recipe from recipe manager (server-side only, safe here)
        DryingRecipe recipe = be.findRecipe();
        if (recipe == null) return;

        be.dryingTimeRequired = recipe.getDryingTime();
        be.dryingProgress++;

        // Spawn occasional particles
        if (level instanceof ServerLevel serverLevel && be.dryingProgress % 40 == 0) {
            serverLevel.sendParticles(ParticleTypes.SMOKE,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    2, 0.2, 0.1, 0.2, 0.01);
        }

        if (be.dryingProgress >= be.dryingTimeRequired) {
            // Transform the item
            be.storedItem = recipe.getOutputStack().copy();
            be.dryingProgress = 0;
            be.dryingTimeRequired = 0;
            be.setChanged();

            // Play completion sound
            level.playSound(null, pos, SoundEvents.GRASS_BREAK, SoundSource.BLOCKS, 1.0f, 1.2f);

            // Sync to client
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    @Nullable
    private DryingRecipe findRecipe() {
        if (level == null) return null;
        MinecraftServer server = level.getServer();
        if (server == null) return null; // should never happen in serverTick

        Identifier storedItemId = BuiltInRegistries.ITEM.getKey(storedItem.getItem());
        RecipeManager recipeManager = server.getRecipeManager();

        // No more getAllRecipesFor(type) in 26.1 — filter from getRecipes().
        for (RecipeHolder<?> holder : recipeManager.getRecipes()) {
            if (holder.value() instanceof DryingRecipe recipe
                    && recipe.getInputItem().equals(storedItemId)) {
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
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!storedItem.isEmpty()) {
            output.store("StoredItem", ItemStack.OPTIONAL_CODEC, storedItem);
        }
        output.putInt("DryingProgress", dryingProgress);
        output.putInt("DryingTimeRequired", dryingTimeRequired);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        storedItem = input.read("StoredItem", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        dryingProgress = input.getIntOr("DryingProgress", 0);
        dryingTimeRequired = input.getIntOr("DryingTimeRequired", 0);
    }

    // ======================== SYNC ========================

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}