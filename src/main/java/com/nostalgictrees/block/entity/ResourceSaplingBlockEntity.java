package com.nostalgictrees.block.entity;

import com.nostalgictrees.NTBlocks;
import com.nostalgictrees.NTRecipes;
import com.nostalgictrees.NostalgicTrees;
import com.nostalgictrees.recipe.MutationRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Block entity for resource saplings. Tracks mutation state:
 * - Which honeycombs have been applied (right-clicked)
 * - Whether the catalyst item has been applied (if recipe requires one)
 * - How many times bees have pollinated this sapling
 * - Which bee UUIDs have already been counted
 */
public class ResourceSaplingBlockEntity extends BlockEntity {

    private final List<ResourceLocation> appliedHoneycombs = new ArrayList<>();
    private boolean catalystApplied = false;
    private int pollinationCount = 0;
    private final Set<UUID> countedBeeUUIDs = new HashSet<>();

    public ResourceSaplingBlockEntity(BlockPos pos, BlockState state) {
        super(NTBlocks.SAPLING_BE.get(), pos, state);
    }

    // ======================== ITEM APPLICATION (honeycombs + catalyst) ========================

    /**
     * Try to apply an item (honeycomb or catalyst) to this sapling.
     * Called from ResourceSaplingBlock.useItemOn.
     */
    public boolean tryApplyItem(ItemStack stack) {
        if (level == null || level.isClientSide()) return false;

        // First try honeycombs
        if (tryApplyHoneycomb(stack)) return true;

        // Then try catalyst
        if (tryApplyCatalyst(stack)) return true;

        return false;
    }

    /**
     * Try to apply a honeycomb item to this sapling.
     */
    private boolean tryApplyHoneycomb(ItemStack honeycombStack) {
        ResourceLocation honeycombId = getItemId(honeycombStack);
        if (honeycombId == null) return false;

        List<MutationRecipe> allRecipes = findAllRecipes();
        if (allRecipes.isEmpty()) return false;

        // If all combs already applied, reject more combs
        MutationRecipe matched = findRecipe();
        if (matched != null && appliedHoneycombs.size() >= matched.getHoneycombs().size()) {
            return false;
        }

        // Only accept if adding this honeycomb keeps at least one recipe fully satisfiable
        boolean accepted = false;
        List<ResourceLocation> hypothetical = new ArrayList<>(appliedHoneycombs);
        hypothetical.add(honeycombId);

        for (MutationRecipe recipe : allRecipes) {
            List<ResourceLocation> needed = new ArrayList<>(recipe.getHoneycombs());
            boolean valid = true;
            for (ResourceLocation applied : hypothetical) {
                if (!needed.remove(applied)) {
                    valid = false;
                    break;
                }
            }
            if (valid) {
                accepted = true;
                break;
            }
        }

        if (!accepted) return false;

        appliedHoneycombs.add(honeycombId);
        honeycombStack.shrink(1);
        setChanged();

        if (level instanceof ServerLevel serverLevel) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            serverLevel.playSound(null, worldPosition, SoundEvents.BEEHIVE_WORK, SoundSource.BLOCKS, 1.0f, 1.2f);

            for (int i = 0; i < 10; i++) {
                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        worldPosition.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 0.8,
                        worldPosition.getY() + 0.5 + level.random.nextDouble() * 0.5,
                        worldPosition.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 0.8,
                        1, 0, 0, 0, 0);
            }
        }

        return true;
    }

    /**
     * Try to apply a catalyst item to this sapling.
     * Only works after all honeycombs are applied and recipe has a catalyst.
     */
    private boolean tryApplyCatalyst(ItemStack catalystStack) {
        if (catalystApplied) return false;

        MutationRecipe recipe = findRecipe();
        if (recipe == null) return false;
        if (!recipe.hasCatalyst()) return false;

        // All combs must be applied first
        if (appliedHoneycombs.size() < recipe.getHoneycombs().size()) return false;

        // Check if the held item matches the catalyst
        ResourceLocation catalystId = getItemId(catalystStack);
        if (!recipe.getCatalyst().equals(catalystId)) return false;

        // Check if player has enough
        if (catalystStack.getCount() < recipe.getCatalystCount()) return false;

        // Apply catalyst
        catalystStack.shrink(recipe.getCatalystCount());
        catalystApplied = true;
        setChanged();

        if (level instanceof ServerLevel serverLevel) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            serverLevel.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0f, 1.0f);

            // Special catalyst particles — dragon breath / soul fire
            for (int i = 0; i < 20; i++) {
                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        worldPosition.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 0.8,
                        worldPosition.getY() + 0.3 + level.random.nextDouble() * 0.7,
                        worldPosition.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 0.8,
                        1, 0, 0.05, 0, 0.02);
            }
        }

        return true;
    }

    /**
     * Check if sapling is ready for bee pollination.
     * All combs applied + catalyst applied (if required).
     */
    public boolean readyForPollination() {
        MutationRecipe recipe = findRecipe();
        if (recipe == null) return false;
        if (appliedHoneycombs.size() < recipe.getHoneycombs().size()) return false;
        if (recipe.hasCatalyst() && !catalystApplied) return false;
        return true;
    }

    // ======================== BEE POLLINATION DETECTION ========================

    public static void serverTick(Level level, BlockPos pos, BlockState state, ResourceSaplingBlockEntity be) {
        if (!be.readyForPollination()) return;

        MutationRecipe recipe = be.findRecipe();
        if (recipe == null) return;

        if (level.getGameTime() % 10 != 0) return;

        AABB searchBox = new AABB(pos).inflate(2.0);
        List<Bee> nearbyBees = level.getEntitiesOfClass(Bee.class, searchBox);

        for (Bee bee : nearbyBees) {
            UUID beeId = bee.getUUID();

            if (be.countedBeeUUIDs.contains(beeId)) continue;

            if (bee.hasNectar() && bee.savedFlowerPos != null && bee.savedFlowerPos.equals(pos)) {
                be.pollinationCount++;
                be.countedBeeUUIDs.add(beeId);
                be.setChanged();

                if (level instanceof ServerLevel serverLevel) {
                    for (int i = 0; i < 15; i++) {
                        serverLevel.sendParticles(ParticleTypes.WAX_ON,
                                pos.getX() + 0.5 + (level.random.nextDouble() - 0.5),
                                pos.getY() + 0.5 + level.random.nextDouble() * 0.5,
                                pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5),
                                1, 0, 0, 0, 0);
                    }

                    serverLevel.playSound(null, pos, SoundEvents.BEE_POLLINATE, SoundSource.BLOCKS, 1.0f, 1.0f);
                }

                if (be.pollinationCount >= recipe.getPollinationsRequired()) {
                    be.completeMutation(recipe);
                    return;
                }
            }
        }

        // Ambient particles while waiting for bees
        if (level instanceof ServerLevel serverLevel && level.getGameTime() % 40 == 0) {
            serverLevel.sendParticles(ParticleTypes.ENCHANT,
                    pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5,
                    3, 0.3, 0.2, 0.3, 0.1);
        }
    }

    private void completeMutation(MutationRecipe recipe) {
        if (level == null || !(level instanceof ServerLevel serverLevel)) return;

        ResourceLocation resultId = recipe.getResultSapling();
        String path = resultId.getPath();
        String treeName = path.endsWith("_sapling") ? path.substring(0, path.length() - 8) : path;

        Block resultBlock = NTBlocks.getSaplingBlock(treeName);
        if (resultBlock == null) {
            NostalgicTrees.LOGGER.error("Mutation result sapling not found: {}", resultId);
            return;
        }

        for (int i = 0; i < 30; i++) {
            serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                    worldPosition.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 1.5,
                    worldPosition.getY() + 0.5 + level.random.nextDouble(),
                    worldPosition.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 1.5,
                    1, 0, 0.1, 0, 0.15);
        }

        serverLevel.playSound(null, worldPosition, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 1.0f, 1.5f);
        serverLevel.setBlock(worldPosition, resultBlock.defaultBlockState(), 3);
    }

    // ======================== RECIPE LOOKUP ========================

    public List<MutationRecipe> findAllRecipes() {
        if (level == null) return List.of();

        Block thisBlock = getBlockState().getBlock();
        ResourceLocation thisBlockId = BuiltInRegistries.BLOCK.getKey(thisBlock);

        List<RecipeHolder<MutationRecipe>> recipes = level.getRecipeManager()
                .getAllRecipesFor(NTRecipes.MUTATION_TYPE.get());

        List<MutationRecipe> matching = new ArrayList<>();
        for (RecipeHolder<MutationRecipe> holder : recipes) {
            if (holder.value().getBaseSapling().equals(thisBlockId)) {
                matching.add(holder.value());
            }
        }
        return matching;
    }

    @Nullable
    public MutationRecipe findRecipe() {
        if (level == null) return null;
        if (appliedHoneycombs.isEmpty()) return null;

        List<MutationRecipe> allRecipes = findAllRecipes();

        for (MutationRecipe recipe : allRecipes) {
            List<ResourceLocation> needed = new ArrayList<>(recipe.getHoneycombs());
            boolean matches = true;
            for (ResourceLocation applied : appliedHoneycombs) {
                if (!needed.remove(applied)) {
                    matches = false;
                    break;
                }
            }
            if (matches) return recipe;
        }
        return null;
    }

    // ======================== HELPERS ========================

    private ResourceLocation getItemId(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem());
    }

    public List<ResourceLocation> getAppliedHoneycombs() {
        return Collections.unmodifiableList(appliedHoneycombs);
    }

    public boolean isCatalystApplied() {
        return catalystApplied;
    }

    public int getPollinationCount() {
        return pollinationCount;
    }

    // ======================== NBT ========================

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        ListTag combsList = new ListTag();
        for (ResourceLocation comb : appliedHoneycombs) {
            combsList.add(StringTag.valueOf(comb.toString()));
        }
        tag.put("AppliedHoneycombs", combsList);
        tag.putBoolean("CatalystApplied", catalystApplied);
        tag.putInt("PollinationCount", pollinationCount);

        ListTag uuidList = new ListTag();
        for (UUID uuid : countedBeeUUIDs) {
            CompoundTag uuidTag = new CompoundTag();
            uuidTag.putUUID("UUID", uuid);
            uuidList.add(uuidTag);
        }
        tag.put("CountedBees", uuidList);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        appliedHoneycombs.clear();
        if (tag.contains("AppliedHoneycombs")) {
            ListTag combsList = tag.getList("AppliedHoneycombs", Tag.TAG_STRING);
            for (int i = 0; i < combsList.size(); i++) {
                appliedHoneycombs.add(ResourceLocation.parse(combsList.getString(i)));
            }
        }

        catalystApplied = tag.getBoolean("CatalystApplied");
        pollinationCount = tag.getInt("PollinationCount");

        countedBeeUUIDs.clear();
        if (tag.contains("CountedBees")) {
            ListTag uuidList = tag.getList("CountedBees", Tag.TAG_COMPOUND);
            for (int i = 0; i < uuidList.size(); i++) {
                countedBeeUUIDs.add(uuidList.getCompound(i).getUUID("UUID"));
            }
        }
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
