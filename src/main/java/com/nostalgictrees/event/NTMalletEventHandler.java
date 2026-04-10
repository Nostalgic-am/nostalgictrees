package com.nostalgictrees.event;

import com.nostalgictrees.NTBlocks;
import com.nostalgictrees.NostalgicTrees;
import com.nostalgictrees.block.ResourceLogBlock;
import com.nostalgictrees.item.MalletItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * Handles the core mallet mechanic:
 * When a player breaks a ResourceLogBlock while holding a MalletItem,
 * the log drops its stripped variant and the mallet takes durability damage
 * based on the tree's tier and the mallet's efficiency.
 */
@EventBusSubscriber(modid = NostalgicTrees.MODID)
public class NTMalletEventHandler {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) return;

        BlockState state = event.getState();
        if (!(state.getBlock() instanceof ResourceLogBlock logBlock)) return;

        var player = event.getPlayer();
        ItemStack held = player.getMainHandItem();

        if (!(held.getItem() instanceof MalletItem mallet)) return;

        // Cancel default drop
        event.setCanceled(true);

        // Calculate durability cost
        int cost = mallet.getMalletTier().getDurabilityCostFor(logBlock.getTier());

        // Get the stripped log block
        Block strippedBlock = NTBlocks.getStrippedLogBlock(logBlock.getTreeName());
        if (strippedBlock == null) {
            NostalgicTrees.LOGGER.warn("No stripped log found for tree: {}", logBlock.getTreeName());
            return;
        }

        Level level = (Level) event.getLevel();
        BlockPos pos = event.getPos();

        // Remove the log block
        level.removeBlock(pos, false);

        // Drop the stripped log
        ItemStack strippedStack = new ItemStack(strippedBlock.asItem());
        ItemEntity itemEntity = new ItemEntity(level,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                strippedStack);
        level.addFreshEntity(itemEntity);

        // Damage the mallet
        if (player instanceof ServerPlayer serverPlayer) {
            held.hurtAndBreak(cost, serverPlayer, serverPlayer.getEquipmentSlotForItem(held));
        }
    }
}
