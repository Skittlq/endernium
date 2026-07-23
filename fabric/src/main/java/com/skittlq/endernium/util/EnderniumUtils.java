package com.skittlq.endernium.util;

import com.skittlq.endernium.item.tools.EnderniumAxe;
import com.skittlq.endernium.item.tools.EnderniumHoe;
import com.skittlq.endernium.item.tools.EnderniumPickaxe;
import com.skittlq.endernium.item.tools.EnderniumShovel;
import com.skittlq.endernium.item.tools.EnderniumSword;
import com.skittlq.endernium.particles.ModParticles;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

public final class EnderniumUtils {
    public static final String VEIN_MINING_SESSION_ID_KEY = "VeinMiningSessionId";
    public static final int DEFAULT_MAX_BLOCKS = 64;
    private static final double DROP_COLLECTION_RADIUS = 1.25D;
    private static boolean registered;

    private EnderniumUtils() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        PlayerBlockBreakEvents.AFTER.register(EnderniumUtils::afterBlockBreak);
    }

    private static void afterBlockBreak(Level level, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (level.isClientSide() || player.isCreative()) {
            return;
        }

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty() || !isEnderniumAutoCollectTool(stack)) {
            return;
        }

        playEnderniumBreakEffects(level, pos);
        scheduleDropCollection(level, pos, player);

        if (isEnderniumVeinMiningTool(stack)
                && com.skittlq.endernium.item.tools.EnderniumVeinMiningToolHelper.isVeinMiningEnabled(stack)
                && !hasActiveVeinMiningOperation(stack)) {
            veinMineBlocks(stack, level, pos, state, player, DEFAULT_MAX_BLOCKS);
        }
    }

    public static void handleBlockMine(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        Player player = entity instanceof Player foundPlayer ? foundPlayer : null;
        if (level.isClientSide() || player == null || player.isCreative() || !(player instanceof ServerPlayer serverPlayer) || state.isAir()) {
            return;
        }

        emitVanillaBreakEffects(level, pos, state);

        if (state.requiresCorrectToolForDrops() && !stack.isCorrectToolForDrops(state)) {
            level.removeBlock(pos, false);
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        LootParams.Builder builder = new LootParams.Builder(serverPlayer.level())
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withParameter(LootContextParams.TOOL, stack)
                .withParameter(LootContextParams.BLOCK_STATE, state)
                .withOptionalParameter(LootContextParams.THIS_ENTITY, entity)
                .withLuck(player.getLuck());

        if (blockEntity != null) {
            builder.withParameter(LootContextParams.BLOCK_ENTITY, blockEntity);
        }

        List<ItemStack> drops = state.getDrops(builder);
        if (!level.removeBlock(pos, false)) {
            return;
        }

        playEnderniumBreakEffects(level, pos);

        for (ItemStack drop : drops) {
            if (!player.getInventory().add(drop)) {
                ItemEntity itemEntity = new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), drop);
                level.addFreshEntity(itemEntity);
            }
        }
    }

    public static void veinMineBlocks(ItemStack stack, Level level, BlockPos origin, BlockState originState, Player player, int maxBlocks) {
        if (hasAnyActiveVeinMiningOperation(player) || !stack.isCorrectToolForDrops(originState)) {
            return;
        }

        int maxAdditionalBlocks = Math.max(0, maxBlocks - 1);
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();
        List<BlockPos> blocksToMine = new ArrayList<>();
        visited.add(origin);
        for (Direction direction : Direction.values()) {
            queue.add(origin.relative(direction));
        }

        while (!queue.isEmpty() && blocksToMine.size() < maxAdditionalBlocks) {
            BlockPos current = queue.poll();
            if (visited.contains(current)) {
                continue;
            }
            visited.add(current);

            BlockState currentState = level.getBlockState(current);
            if (currentState.isAir() || currentState.getBlock() != originState.getBlock()) {
                continue;
            }
            if (!stack.isCorrectToolForDrops(currentState)) {
                continue;
            }

            blocksToMine.add(current);

            for (Direction direction : Direction.values()) {
                BlockPos neighbor = current.relative(direction);
                if (!visited.contains(neighbor)) {
                    queue.add(neighbor);
                }
            }
        }

        float speed = stack.getDestroySpeed(originState);
        if (speed <= 0.0F) {
            speed = 1.0F;
        }
        int ticksPerBlock = Math.max(1, Math.round((2.0F / speed) * 20.0F));
        Iterator<BlockPos> iterator = blocksToMine.iterator();

        int sessionId = new Random().nextInt();
        CompoundTag tag = getOrCreateCustomDataTag(stack);
        tag.putInt(VEIN_MINING_SESSION_ID_KEY, sessionId);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        Runnable[] task = new Runnable[1];
        task[0] = () -> {
            CompoundTag checkTag = getOrCreateCustomDataTag(stack);
            int currentSession = checkTag.getIntOr(VEIN_MINING_SESSION_ID_KEY, 0);
            if (currentSession != sessionId) {
                return;
            }

            if (!iterator.hasNext()) {
                checkTag.remove(VEIN_MINING_SESSION_ID_KEY);
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(checkTag));
                return;
            }

            BlockPos nextPos = iterator.next();
            BlockState nextState = level.getBlockState(nextPos);
            if (!nextState.isAir() && stack.isCorrectToolForDrops(nextState)) {
                handleBlockMine(stack, level, nextState, nextPos, player);

                if (!level.isClientSide() && isCropBlock(nextState)) {
                    Item seedItem = getSeedForCrop(nextState);
                    int slot = findSeedSlot(player, seedItem);
                    if (seedItem != null && slot != -1) {
                        EnderniumTickScheduler.schedule(() -> {
                            BlockState afterMine = level.getBlockState(nextPos);
                            if (afterMine.isAir()) {
                                player.getInventory().removeItem(slot, 1);
                                BlockState cropState = getDefaultCropState(nextState, level, nextPos);
                                level.setBlockAndUpdate(nextPos, cropState);
                                level.playSound(null, nextPos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 0.7F, 1.1F);
                            }
                        }, 1);
                    }
                }
            }

            if (iterator.hasNext()) {
                EnderniumTickScheduler.schedule(task[0], ticksPerBlock);
            } else {
                checkTag.remove(VEIN_MINING_SESSION_ID_KEY);
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(checkTag));
            }
        };

        EnderniumTickScheduler.schedule(task[0], 0);
    }

    public static void cancelVeinMining(ItemStack stack) {
        CompoundTag tag = getOrCreateCustomDataTag(stack);
        tag.remove(VEIN_MINING_SESSION_ID_KEY);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static boolean hasActiveVeinMiningOperation(ItemStack stack) {
        CompoundTag tag = getOrCreateCustomDataTag(stack);
        return tag.contains(VEIN_MINING_SESSION_ID_KEY);
    }

    public static boolean hasAnyActiveVeinMiningOperation(Player player) {
        for (int index = 0; index < player.getInventory().getContainerSize(); index++) {
            if (hasActiveVeinMiningOperation(player.getInventory().getItem(index))) {
                return true;
            }
        }
        return false;
    }

    public static CompoundTag getOrCreateCustomDataTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null ? data.copyTag() : new CompoundTag();
    }

    public static boolean isCropBlock(BlockState state) {
        String blockName = state.getBlock().getDescriptionId();
        return blockName.contains("wheat")
                || blockName.contains("carrot")
                || blockName.contains("potato")
                || blockName.contains("beetroot")
                || blockName.contains("nether_wart")
                || blockName.contains("melon_stem")
                || blockName.contains("pumpkin_stem");
    }

    public static Item getSeedForCrop(BlockState state) {
        String name = state.getBlock().getDescriptionId();
        if (name.contains("wheat")) return Items.WHEAT_SEEDS;
        if (name.contains("carrot")) return Items.CARROT;
        if (name.contains("potato")) return Items.POTATO;
        if (name.contains("beetroot")) return Items.BEETROOT_SEEDS;
        if (name.contains("nether_wart")) return Items.NETHER_WART;
        if (name.contains("melon_stem")) return Items.MELON_SEEDS;
        if (name.contains("pumpkin_stem")) return Items.PUMPKIN_SEEDS;
        return null;
    }

    public static BlockState getDefaultCropState(BlockState oldState, Level level, BlockPos pos) {
        return oldState.getBlock().defaultBlockState();
    }

    public static int findSeedSlot(Player player, Item seed) {
        if (seed == null) {
            return -1;
        }
        for (int index = 0; index < player.getInventory().getContainerSize(); index++) {
            ItemStack stack = player.getInventory().getItem(index);
            if (!stack.isEmpty() && stack.getItem() == seed) {
                return index;
            }
        }
        return -1;
    }

    public static void collectNearbyDrops(Level level, BlockPos pos, Player player) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        AABB dropBox = AABB.ofSize(Vec3.atCenterOf(pos), DROP_COLLECTION_RADIUS * 2.0D, DROP_COLLECTION_RADIUS * 2.0D, DROP_COLLECTION_RADIUS * 2.0D);
        List<ItemEntity> itemEntities = serverLevel.getEntitiesOfClass(ItemEntity.class, dropBox, ItemEntity::isAlive);
        for (ItemEntity itemEntity : itemEntities) {
            ItemStack itemStack = itemEntity.getItem();
            player.getInventory().add(itemStack);
            if (itemStack.isEmpty()) {
                itemEntity.discard();
            }
        }
    }

    private static void emitVanillaBreakEffects(Level level, BlockPos pos, BlockState state) {
        level.levelEvent(2001, pos, Block.getId(state));
    }

    private static void playEnderniumBreakEffects(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ModParticles.REVERSE_ENDERNIUM_BIT,
                    pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    10, 0.2D, 0.2D, 0.2D, 0.01D);
        }
        level.playSound(null, pos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 0.04F, 1.5F);
    }

    private static void scheduleDropCollection(Level level, BlockPos pos, Player player) {
        EnderniumTickScheduler.schedule(() -> {
            if (!player.isRemoved()) {
                collectNearbyDrops(level, pos, player);
            }
        }, 1);
    }

    private static boolean isEnderniumAutoCollectTool(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof EnderniumSword
                || item instanceof EnderniumPickaxe
                || item instanceof EnderniumShovel
                || item instanceof EnderniumAxe
                || item instanceof EnderniumHoe;
    }

    private static boolean isEnderniumVeinMiningTool(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof EnderniumPickaxe
                || item instanceof EnderniumShovel
                || item instanceof EnderniumAxe
                || item instanceof EnderniumHoe;
    }
}