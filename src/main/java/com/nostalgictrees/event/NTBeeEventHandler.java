package com.nostalgictrees.event;

import com.nostalgictrees.NTItems;
import com.nostalgictrees.NostalgicTrees;
import com.nostalgictrees.block.ResourceSaplingBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Listens for when a player right-clicks a vanilla beehive with shears.
 * If resource saplings are nearby and the hive is full (honey_level 5),
 * drops resource honeycombs as bonus items.
 */
@EventBusSubscriber(modid = NostalgicTrees.MODID)
public class NTBeeEventHandler {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide()) return;

        Player player = event.getEntity();
        ItemStack held = player.getMainHandItem();

        // Only trigger on shears
        if (!held.is(Items.SHEARS)) return;

        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);

        // Only trigger on beehive/bee_nest blocks
        if (!(state.getBlock() instanceof BeehiveBlock)) return;

        // Only trigger when hive is full
        if (state.getValue(BeehiveBlock.HONEY_LEVEL) < 5) return;

        ServerLevel serverLevel = (ServerLevel) level;

        // Find nearby resource saplings
        List<String> nearbySaplings = findNearbySaplings(serverLevel, pos, 6);
        if (nearbySaplings.isEmpty()) return;

        // Drop 1-3 resource honeycombs
        int combCount = 1 + serverLevel.random.nextInt(3);
        for (int i = 0; i < combCount; i++) {
            String treeName = nearbySaplings.get(serverLevel.random.nextInt(nearbySaplings.size()));
            ItemStack combStack = NTItems.getHoneycombItem(treeName);
            if (!combStack.isEmpty()) {
                ItemEntity item = new ItemEntity(level,
                        pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                        combStack);
                level.addFreshEntity(item);
            }
        }
    }

    private static List<String> findNearbySaplings(ServerLevel level, BlockPos center, int range) {
        List<String> found = new ArrayList<>();
        for (BlockPos check : BlockPos.betweenClosed(
                center.offset(-range, -range, -range),
                center.offset(range, range, range))) {
            BlockState state = level.getBlockState(check);
            if (state.getBlock() instanceof ResourceSaplingBlock sapling) {
                if (!found.contains(sapling.getTreeName())) {
                    found.add(sapling.getTreeName());
                }
            }
        }
        return found;
    }
}
