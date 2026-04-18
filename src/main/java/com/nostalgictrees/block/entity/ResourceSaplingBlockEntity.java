package com.nostalgictrees.block.entity;

import com.nostalgictrees.NTBlocks;
import com.nostalgictrees.NTRecipes;
import com.nostalgictrees.NostalgicTrees;
import com.nostalgictrees.recipe.MutationRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;

import java.util.*;

/**
 * Block entity for resource saplings. Tracks mutation state:
 * - Which honeycombs have been applied (right-clicked)
 * - Whether the catalyst item has been applied (if recipe requires one)
 * - How many times bees have pollinated this sapling
 *
 * 26.1 changes:
 *   - Level#random           -> level.getRandom()
 *   - Level#getRecipeManager -> level.getServer().getRecipeManager()
 *   - RecipeManager#getAllRecipesFor(type) -> filter from getRecipes()
 *   - Bee#savedFlowerPos private -> bee.hasSavedFlowerPos()/getSavedFlowerPos()
 *   - saveAdditional/loadAdditional now take ValueOutput/ValueInput (codec-based)
 */
public class ResourceSaplingBlockEntity extends BlockEntity {

    private static final Codec<List<Identifier>> IDENTIFIER_LIST_CODEC = Identifier.CODEC.listOf();
    private static final Codec<List<UUID>> UUID_LIST_CODEC = UUIDUtil.CODEC.listOf();

    private final List<Identifier> appliedHoneycombs = new ArrayList<>();
    private boolean catalystApplied = false;
    private int pollinationCount = 0;
    private final Set<UUID> countedBeeUUIDs = new HashSet<>();
    private int growthStage = 0;

    public ResourceSaplingBlockEntity(BlockPos pos, BlockState state) {
        super(NTBlocks.SAPLING_BE.get(), pos, state);
    }

    // ======================== ITEM APPLICATION (honeycombs + catalyst) ========================

    public boolean tryApplyItem(ItemStack stack) {
        if (level == null || level.isClientSide()) return false;
        if (tryApplyHoneycomb(stack)) return true;
        if (tryApplyCatalyst(stack)) return true;
        return false;
    }

    private boolean tryApplyHoneycomb(ItemStack honeycombStack) {
        Identifier honeycombId = getItemId(honeycombStack);
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
        List<Identifier> hypothetical = new ArrayList<>(appliedHoneycombs);
        hypothetical.add(honeycombId);

        for (MutationRecipe recipe : allRecipes) {
            List<Identifier> needed = new ArrayList<>(recipe.getHoneycombs());
            boolean valid = true;
            for (Identifier applied : hypothetical) {
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
                        worldPosition.getX() + 0.5 + (level.getRandom().nextDouble() - 0.5) * 0.8,
                        worldPosition.getY() + 0.5 + level.getRandom().nextDouble() * 0.5,
                        worldPosition.getZ() + 0.5 + (level.getRandom().nextDouble() - 0.5) * 0.8,
                        1, 0, 0, 0, 0);
            }
        }

        return true;
    }

    private boolean tryApplyCatalyst(ItemStack catalystStack) {
        if (catalystApplied) return false;

        MutationRecipe recipe = findRecipe();
        if (recipe == null) return false;
        if (!recipe.hasCatalyst()) return false;

        if (appliedHoneycombs.size() < recipe.getHoneycombs().size()) return false;

        Identifier catalystId = getItemId(catalystStack);
        if (!recipe.getCatalyst().equals(catalystId)) return false;

        if (catalystStack.getCount() < recipe.getCatalystCount()) return false;

        catalystStack.shrink(recipe.getCatalystCount());
        catalystApplied = true;
        setChanged();

        if (level instanceof ServerLevel serverLevel) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            serverLevel.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0f, 1.0f);

            for (int i = 0; i < 20; i++) {
                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                        worldPosition.getX() + 0.5 + (level.getRandom().nextDouble() - 0.5) * 0.8,
                        worldPosition.getY() + 0.3 + level.getRandom().nextDouble() * 0.7,
                        worldPosition.getZ() + 0.5 + (level.getRandom().nextDouble() - 0.5) * 0.8,
                        1, 0, 0.05, 0, 0.02);
            }
        }

        return true;
    }

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

            if (bee.hasNectar() && bee.hasSavedFlowerPos() && bee.getSavedFlowerPos().equals(pos)) {
                be.pollinationCount++;
                be.countedBeeUUIDs.add(beeId);
                be.setChanged();

                if (level instanceof ServerLevel serverLevel) {
                    for (int i = 0; i < 15; i++) {
                        serverLevel.sendParticles(ParticleTypes.WAX_ON,
                                pos.getX() + 0.5 + (level.getRandom().nextDouble() - 0.5),
                                pos.getY() + 0.5 + level.getRandom().nextDouble() * 0.5,
                                pos.getZ() + 0.5 + (level.getRandom().nextDouble() - 0.5),
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

        if (level instanceof ServerLevel serverLevel && level.getGameTime() % 40 == 0) {
            serverLevel.sendParticles(ParticleTypes.ENCHANT,
                    pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5,
                    3, 0.3, 0.2, 0.3, 0.1);
        }
    }

    private void completeMutation(MutationRecipe recipe) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        Identifier resultId = recipe.getResultSapling();
        String path = resultId.getPath();
        String treeName = path.endsWith("_sapling") ? path.substring(0, path.length() - 8) : path;

        Block resultBlock = NTBlocks.getSaplingBlock(treeName);
        if (resultBlock == null) {
            NostalgicTrees.LOGGER.error("Mutation result sapling not found: {}", resultId);
            return;
        }

        for (int i = 0; i < 30; i++) {
            serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                    worldPosition.getX() + 0.5 + (level.getRandom().nextDouble() - 0.5) * 1.5,
                    worldPosition.getY() + 0.5 + level.getRandom().nextDouble(),
                    worldPosition.getZ() + 0.5 + (level.getRandom().nextDouble() - 0.5) * 1.5,
                    1, 0, 0.1, 0, 0.15);
        }

        serverLevel.playSound(null, worldPosition, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 1.0f, 1.5f);
        serverLevel.setBlock(worldPosition, resultBlock.defaultBlockState(), 3);
    }

    // ======================== RECIPE LOOKUP ========================

    public List<MutationRecipe> findAllRecipes() {
        if (level == null) return List.of();
        MinecraftServer server = level.getServer();
        if (server == null) return List.of(); // client-side or pre-server

        Block thisBlock = getBlockState().getBlock();
        Identifier thisBlockId = BuiltInRegistries.BLOCK.getKey(thisBlock);

        RecipeManager recipeManager = server.getRecipeManager();
        List<MutationRecipe> matching = new ArrayList<>();

        // 26.1: no more getAllRecipesFor(type) — filter manually.
        for (RecipeHolder<?> holder : recipeManager.getRecipes()) {
            if (holder.value() instanceof MutationRecipe recipe
                    && recipe.getBaseSapling().equals(thisBlockId)) {
                matching.add(recipe);
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
            List<Identifier> needed = new ArrayList<>(recipe.getHoneycombs());
            boolean matches = true;
            for (Identifier applied : appliedHoneycombs) {
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

    private Identifier getItemId(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem());
    }

    public List<Identifier> getAppliedHoneycombs() {
        return Collections.unmodifiableList(appliedHoneycombs);
    }

    public boolean isCatalystApplied() {
        return catalystApplied;
    }

    public int getPollinationCount() {
        return pollinationCount;
    }

    public int getGrowthStage() { return growthStage; }

    public void setGrowthStage(int stage) {
        this.growthStage = stage;
        setChanged();
    }

    // ======================== NBT ========================
    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("AppliedHoneycombs", IDENTIFIER_LIST_CODEC, appliedHoneycombs);
        output.putBoolean("CatalystApplied", catalystApplied);
        output.putInt("PollinationCount", pollinationCount);
        output.putInt("GrowthStage", growthStage);
        output.store("CountedBees", UUID_LIST_CODEC, new ArrayList<>(countedBeeUUIDs));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        appliedHoneycombs.clear();
        appliedHoneycombs.addAll(input.read("AppliedHoneycombs", IDENTIFIER_LIST_CODEC).orElse(List.of()));

        catalystApplied = input.getBooleanOr("CatalystApplied", false);
        pollinationCount = input.getIntOr("PollinationCount", 0);
        growthStage = input.getIntOr("GrowthStage", 0);

        countedBeeUUIDs.clear();
        countedBeeUUIDs.addAll(input.read("CountedBees", UUID_LIST_CODEC).orElse(List.of()));
    }

    // ======================== SYNC ========================

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return super.getUpdateTag(registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}