package com.nostalgictrees.item;

import com.nostalgictrees.data.MalletTier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class MalletItem extends Item {
    private final MalletTier malletTier;

    /*
     * 26.1: Like Block.Properties, Item.Properties require a registry ID to be injected
     * before the Item constructor runs (Item#<init> calls effectiveDescriptionId which
     * reads itemIdOrThrow). NeoForge's DeferredRegister.Items#registerItem pre-populates
     * the id — so the constructor must accept Properties as a parameter rather than
     * building them internally.
     *
     * Also note: Item#isEnchantable / #getEnchantmentValue are gone in 26.1 —
     * enchantability is set via Properties.enchantable(int) at construction time.
     */
    public MalletItem(MalletTier malletTier, Item.Properties properties) {
        super(properties);
        this.malletTier = malletTier;
    }

    public MalletTier getMalletTier() { return malletTier; }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (state.getBlock() instanceof com.nostalgictrees.block.ResourceLogBlock) {
            // ToolMaterial#speed() — was Tier.getSpeed() in 1.21.1
            return malletTier.getVanillaMaterial().speed() * 0.7f;
        }
        return 1.0f;
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return state.getBlock() instanceof com.nostalgictrees.block.ResourceLogBlock;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (!level.isClientSide() && state.getDestroySpeed(level, pos) != 0.0F) {
            stack.hurtAndBreak(1, entity, EquipmentSlot.MAINHAND);
        }
        return true;
    }
}