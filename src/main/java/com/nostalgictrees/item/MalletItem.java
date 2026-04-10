package com.nostalgictrees.item;

import com.nostalgictrees.data.MalletTier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;

public class MalletItem extends Item {
    private final MalletTier malletTier;

    public MalletItem(MalletTier malletTier) {
        super(new Item.Properties()
                .durability(malletTier.getMaxDurability())
                .stacksTo(1)
        );
        this.malletTier = malletTier;
    }

    public MalletTier getMalletTier() { return malletTier; }

    @Override
    public boolean isEnchantable(ItemStack stack) { return true; }

    @Override
    public int getEnchantmentValue() { return malletTier.getVanillaTier().getEnchantmentValue(); }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (state.getBlock() instanceof com.nostalgictrees.block.ResourceLogBlock) {
            return malletTier.getVanillaTier().getSpeed() * 0.7f;
        }
        return 1.0f;
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        if (state.getBlock() instanceof com.nostalgictrees.block.ResourceLogBlock) {
            return true;
        }
        return false;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (!level.isClientSide && state.getDestroySpeed(level, pos) != 0.0F) {
            stack.hurtAndBreak(1, entity, EquipmentSlot.MAINHAND);
        }
        return true;
    }
}
