package com.skittlq.endernium.util;

import com.skittlq.endernium.item.tools.EnderniumAxe;
import com.skittlq.endernium.item.tools.EnderniumHoe;
import com.skittlq.endernium.item.tools.EnderniumPickaxe;
import com.skittlq.endernium.item.tools.EnderniumShovel;
import com.skittlq.endernium.item.tools.EnderniumSword;
import com.skittlq.endernium.item.tools.EnderniumVeinMiningToolHelper;
import com.skittlq.endernium.particles.EnderniumParticles;
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
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

public final class EnderniumUtils {
    public static final String VEIN_MINING_SESSION_ID_KEY = "VeinMiningSessionId";
    public static final int DEFAULT_MAX_BLOCKS = 64;
    private static final double DROP_COLLECTION_RADIUS = 1.25D;

    private EnderniumUtils() {
    }

    public static void onAutoCollectToolBlockBreak(Level level, Player player, BlockPos pos, BlockState state, boolean allowVeinMiningFallback) {
        if (level.isClientSide() || player.isCreative()) {
            return;
        }

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty() || !isEnderniumAutoCollectTool(stack)) {
            return;
        }

        if (allowVeinMiningFallback) {
            playEnderniumBreakEffects(level, pos);
            scheduleDropCollection(level, pos, player);

            if (isEnderniumVeinMiningTool(stack)
                    && EnderniumVeinMiningToolHelper.isVeinMiningEnabled(stack)
                    && !hasActiveVeinMiningOperation(stack)) {
                veinMineBlocks(stack, level, pos, state, player, DEFAULT_MAX_BLOCKS);
            }
            return;
        }

        scheduleVerifiedDropCollection(level, pos, player);
    }

    public static void handleBlockMine(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        Player player = entity instanceof Player foundPlayer ? foundPlayer : null;
        if (level.isClientSide() || player == null || player.isCreative() || !(player instanceof ServerPlayer serverPlayer) || state.isAir()) {
            return;
        }

        clearVeinMiningBlockProgress(level, pos, player);
        emitVanillaBreakEffects(level, pos, state);

        if (state.requiresCorrectToolForDrops() && !stack.isCorrectToolForDrops(state)) {
            level.removeBlock(pos, false);
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!level.removeBlock(pos, false)) {
            return;
        }

        Block.dropResources(state, level, pos, blockEntity, entity, stack);
        playEnderniumBreakEffects(level, pos);
        scheduleDropCollection(level, pos, player);
    }

    public static void veinMineBlocks(ItemStack stack, Level level, BlockPos origin, BlockState originState, Player player, int maxBlocks) {
        if (!canVeinMineBlock(stack, originState)) {
            return;
        }

        cancelAllVeinMiningOperations(player);

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
            if (!canVeinMineBlock(stack, currentState)) {
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

        int sessionId = new Random().nextInt();
        CompoundTag tag = getOrCreateCustomDataTag(stack);
        tag.putInt(VEIN_MINING_SESSION_ID_KEY, sessionId);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

        if (blocksToMine.isEmpty()) {
            tag.remove(VEIN_MINING_SESSION_ID_KEY);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            clearVeinMiningBlockProgress(level, origin, player);
            return;
        }

        int[] currentIndex = {0};
        Runnable[] task = new Runnable[1];
        task[0] = () -> {
            CompoundTag checkTag = getOrCreateCustomDataTag(stack);
            int currentSession = checkTag.getIntOr(VEIN_MINING_SESSION_ID_KEY, 0);
            if (currentSession != sessionId) {
                clearVeinMiningBlockProgress(level, origin, player);
                return;
            }

            if (currentIndex[0] >= blocksToMine.size()) {
                checkTag.remove(VEIN_MINING_SESSION_ID_KEY);
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(checkTag));
                return;
            }

            BlockPos nextPos = blocksToMine.get(currentIndex[0]++);
            BlockState nextState = level.getBlockState(nextPos);
            if (!nextState.isAir() && canVeinMineBlock(stack, nextState)) {
                handleBlockMine(stack, level, nextState, nextPos, player);
            }

            if (currentIndex[0] < blocksToMine.size()) {
                BlockPos upcomingPos = blocksToMine.get(currentIndex[0]);
                BlockState upcomingState = level.getBlockState(upcomingPos);
                int ticksUntilNextBlock = getVeinMiningDelayTicks(level, upcomingState, upcomingPos, player);
                scheduleVeinMiningProgress(stack, level, upcomingPos, player, sessionId, ticksUntilNextBlock);
                EnderniumTickScheduler.schedule(task[0], ticksUntilNextBlock);
            } else {
                checkTag.remove(VEIN_MINING_SESSION_ID_KEY);
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(checkTag));
                clearVeinMiningBlockProgress(level, nextPos, player);
            }
        };

        BlockPos firstPos = blocksToMine.get(0);
        BlockState firstState = level.getBlockState(firstPos);
        int initialDelay = getVeinMiningDelayTicks(level, firstState, firstPos, player);
        scheduleVeinMiningProgress(stack, level, firstPos, player, sessionId, initialDelay);
        EnderniumTickScheduler.schedule(task[0], initialDelay);
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

    public static void cancelAllVeinMiningOperations(Player player) {
        for (int index = 0; index < player.getInventory().getContainerSize(); index++) {
            ItemStack inventoryStack = player.getInventory().getItem(index);
            if (hasActiveVeinMiningOperation(inventoryStack)) {
                cancelVeinMining(inventoryStack);
            }
        }
    }

    public static CompoundTag getOrCreateCustomDataTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null ? data.copyTag() : new CompoundTag();
    }

    public static boolean isHarvestableCrop(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof CropBlock cropBlock) {
            return cropBlock.isMaxAge(state);
        }
        if (block instanceof NetherWartBlock) {
            return state.getValue(NetherWartBlock.AGE) == 3;
        }
        return false;
    }

    public static void collectNearbyDrops(Level level, BlockPos pos, Player player) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        AABB dropBox = AABB.ofSize(Vec3.atCenterOf(pos),
                DROP_COLLECTION_RADIUS * 2.0D,
                DROP_COLLECTION_RADIUS * 2.0D,
                DROP_COLLECTION_RADIUS * 2.0D);
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
            serverLevel.sendParticles(EnderniumParticles.REVERSE_ENDERNIUM_BIT.get(),
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

    private static void scheduleVerifiedDropCollection(Level level, BlockPos pos, Player player) {
        EnderniumTickScheduler.schedule(() -> {
            if (!player.isRemoved() && level.getBlockState(pos).isAir()) {
                playEnderniumBreakEffects(level, pos);
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

    private static boolean canVeinMineBlock(ItemStack stack, BlockState state) {
        if (stack.isCorrectToolForDrops(state)) {
            return true;
        }
        return stack.getItem() instanceof EnderniumHoe && isHarvestableCrop(state);
    }

    private static int getVeinMiningDelayTicks(Level level, BlockState state, BlockPos pos, Player player) {
        float destroyProgress = state.getDestroyProgress(player, level, pos);
        if (destroyProgress <= 0.0F) {
            return 20;
        }
        return Math.max(1, (int) Math.ceil(1.0F / destroyProgress));
    }

    private static void scheduleVeinMiningProgress(ItemStack stack, Level level, BlockPos pos, Player player, int sessionId, int totalTicks) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        int steps = Math.min(10, Math.max(1, totalTicks));
        int breakerId = getVeinMiningBreakerId(player);
        for (int step = 0; step < steps; step++) {
            int crackStage = Math.min(9, Math.max(0, (int) Math.floor(((step + 1) * 10.0D) / steps) - 1));
            int delay = Math.max(0, (int) Math.floor((step * (double) totalTicks) / steps));
            EnderniumTickScheduler.schedule(() -> {
                CompoundTag tag = getOrCreateCustomDataTag(stack);
                if (tag.getIntOr(VEIN_MINING_SESSION_ID_KEY, 0) != sessionId || player.isRemoved() || level.getBlockState(pos).isAir()) {
                    serverLevel.destroyBlockProgress(breakerId, pos, -1);
                    return;
                }
                serverLevel.destroyBlockProgress(breakerId, pos, crackStage);
            }, delay);
        }
    }

    private static void clearVeinMiningBlockProgress(Level level, BlockPos pos, Player player) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.destroyBlockProgress(getVeinMiningBreakerId(player), pos, -1);
        }
    }

    private static int getVeinMiningBreakerId(Player player) {
        return Integer.MIN_VALUE + player.getId();
    }
}
