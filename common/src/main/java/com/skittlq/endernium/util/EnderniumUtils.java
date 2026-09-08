package com.skittlq.endernium.util;

import com.skittlq.endernium.config.EnderniumGameplayConfig;
import com.skittlq.endernium.item.tools.EnderniumAxe;
import com.skittlq.endernium.item.tools.EnderniumHoe;
import com.skittlq.endernium.item.tools.EnderniumPickaxe;
import com.skittlq.endernium.item.tools.EnderniumShovel;
import com.skittlq.endernium.item.tools.EnderniumSword;
import com.skittlq.endernium.item.tools.EnderniumVeinMiningToolHelper;
import com.skittlq.endernium.particles.EnderniumParticles;
import com.skittlq.endernium.progression.EnderniumBlessing;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.Objects;

public final class EnderniumUtils {
    public static final String VEIN_MINING_SESSION_ID_KEY = "VeinMiningSessionId";
    public static final int DEFAULT_MAX_BLOCKS = 64;
    private static final double DROP_COLLECTION_RADIUS = 1.25D;
    private static final double MAX_OPERATION_DISTANCE_SQUARED = 64.0D;
    private static final Map<net.minecraft.server.MinecraftServer, Map<UUID, VeinMiningOperation>> ACTIVE_OPERATIONS =
            new HashMap<>();
    private static final ThreadLocal<Set<UUID>> BREAKING_ADDITIONAL_BLOCK =
            ThreadLocal.withInitial(HashSet::new);
    private static SafeBlockBreaker safeBlockBreaker = (player, pos) -> player.gameMode.destroyBlock(pos);

    private EnderniumUtils() {
    }

    public static void bindSafeBlockBreaker(SafeBlockBreaker breaker) {
        safeBlockBreaker = Objects.requireNonNull(breaker);
    }

    public static void onAutoCollectToolBlockBreak(Level level, Player player, BlockPos pos, BlockState state, boolean allowVeinMiningFallback) {
        if (level.isClientSide() || player.isCreative() || !EnderniumBlessing.isBlessed(player)) {
            return;
        }

        ItemStack stack = player.getMainHandItem();
        if (stack.isEmpty() || !isEnderniumAutoCollectTool(stack)) {
            return;
        }
        if (BREAKING_ADDITIONAL_BLOCK.get().contains(player.getUUID())) {
            return;
        }

        if (allowVeinMiningFallback) {
            playEnderniumBreakEffects(level, pos);
            scheduleDropCollection(level, pos, player);

            if (isEnderniumVeinMiningTool(stack)
                    && EnderniumVeinMiningToolHelper.isVeinMiningEnabled(stack)
                    && !hasActiveVeinMiningOperation(player, stack)) {
                veinMineBlocks(stack, level, pos, state, player, DEFAULT_MAX_BLOCKS);
            }
            return;
        }

        scheduleVerifiedDropCollection(level, pos, player);
    }

    public static boolean handleBlockMine(ItemStack stack, Level level, BlockState state, BlockPos pos,
                                          LivingEntity entity) {
        Player player = entity instanceof Player foundPlayer ? foundPlayer : null;
        if (level.isClientSide()
                || player == null
                || player.isCreative()
                || !(player instanceof ServerPlayer serverPlayer)
                || !EnderniumBlessing.isBlessed(serverPlayer)
                || state.isAir()) {
            return false;
        }

        clearVeinMiningBlockProgress(level, pos, player);
        ServerLevel serverLevel = (ServerLevel) serverPlayer.level();
        Set<UUID> existingDrops = nearbyDropIds(serverLevel, pos);
        Set<UUID> breakingPlayers = BREAKING_ADDITIONAL_BLOCK.get();
        breakingPlayers.add(player.getUUID());
        boolean broken;
        try {
            broken = safeBlockBreaker.breakBlock(serverPlayer, pos);
        } finally {
            breakingPlayers.remove(player.getUUID());
            if (breakingPlayers.isEmpty()) {
                BREAKING_ADDITIONAL_BLOCK.remove();
            }
        }
        if (!broken) {
            return false;
        }
        playEnderniumBreakEffects(level, pos);
        collectNewDrops(serverLevel, pos, player, existingDrops);
        return true;
    }

    @FunctionalInterface
    public interface SafeBlockBreaker {
        boolean breakBlock(ServerPlayer player, BlockPos pos);
    }

    public static void veinMineBlocks(ItemStack stack, Level level, BlockPos origin, BlockState originState, Player player, int maxBlocks) {
        if (!(level instanceof ServerLevel serverLevel)
                || !(player instanceof ServerPlayer serverPlayer)
                || BREAKING_ADDITIONAL_BLOCK.get().contains(player.getUUID())
                || !EnderniumBlessing.isBlessed(serverPlayer)
                || !canVeinMineBlock(stack, originState)) {
            return;
        }

        cancelAllVeinMiningOperations(player);

        int maxAdditionalBlocks = Math.max(0, maxBlocks - 1);
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();
        List<TargetBlock> blocksToMine = new ArrayList<>();
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

            blocksToMine.add(new TargetBlock(current.immutable(), currentState));

            for (Direction direction : Direction.values()) {
                BlockPos neighbor = current.relative(direction);
                if (!visited.contains(neighbor)) {
                    queue.add(neighbor);
                }
            }
        }

        if (blocksToMine.isEmpty()) {
            clearVeinMiningBlockProgress(level, origin, player);
            return;
        }

        clearLegacyOperationTag(stack);
        VeinMiningOperation operation = new VeinMiningOperation(serverLevel, serverPlayer, stack,
                origin.immutable(), blocksToMine);
        ACTIVE_OPERATIONS.computeIfAbsent(serverLevel.getServer(), ignored -> new HashMap<>())
                .put(player.getUUID(), operation);
        scheduleNextBlock(operation);
    }

    public static void cancelVeinMining(Player player, ItemStack stack) {
        Map<UUID, VeinMiningOperation> operations = ACTIVE_OPERATIONS.get(player.level().getServer());
        if (operations == null) {
            clearLegacyOperationTag(stack);
            return;
        }
        VeinMiningOperation operation = operations.get(player.getUUID());
        if (operation != null && operation.tool == stack) {
            finishOperation(operation);
        }
        clearLegacyOperationTag(stack);
    }

    public static boolean hasActiveVeinMiningOperation(Player player, ItemStack stack) {
        Map<UUID, VeinMiningOperation> operations = ACTIVE_OPERATIONS.get(player.level().getServer());
        return operations != null && operations.containsKey(player.getUUID())
                && operations.get(player.getUUID()).tool == stack;
    }

    public static boolean hasAnyActiveVeinMiningOperation(Player player) {
        Map<UUID, VeinMiningOperation> operations = ACTIVE_OPERATIONS.get(player.level().getServer());
        return operations != null && operations.containsKey(player.getUUID());
    }

    public static void cancelAllVeinMiningOperations(Player player) {
        Map<UUID, VeinMiningOperation> operations = ACTIVE_OPERATIONS.get(player.level().getServer());
        if (operations != null) {
            VeinMiningOperation operation = operations.get(player.getUUID());
            if (operation != null) {
                finishOperation(operation);
            }
        }
        for (int index = 0; index < player.getInventory().getContainerSize(); index++) {
            clearLegacyOperationTag(player.getInventory().getItem(index));
        }
    }

    public static void clearServerState(net.minecraft.server.MinecraftServer server) {
        Map<UUID, VeinMiningOperation> operations = ACTIVE_OPERATIONS.remove(server);
        if (operations != null) {
            for (VeinMiningOperation operation : operations.values()) {
                clearVeinMiningBlockProgress(operation.level, operation.currentProgressPos, operation.player);
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
        List<ItemEntity> itemEntities = serverLevel.getEntitiesOfClass(ItemEntity.class, dropBox,
                item -> item.isAlive() && item.tickCount <= 2);
        for (ItemEntity itemEntity : itemEntities) {
            ItemStack itemStack = itemEntity.getItem();
            player.getInventory().add(itemStack);
            if (itemStack.isEmpty()) {
                itemEntity.discard();
            }
        }
    }

    private static Set<UUID> nearbyDropIds(ServerLevel level, BlockPos pos) {
        AABB dropBox = AABB.ofSize(Vec3.atCenterOf(pos),
                DROP_COLLECTION_RADIUS * 2.0D,
                DROP_COLLECTION_RADIUS * 2.0D,
                DROP_COLLECTION_RADIUS * 2.0D);
        Set<UUID> ids = new HashSet<>();
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, dropBox, ItemEntity::isAlive)) {
            ids.add(item.getUUID());
        }
        return ids;
    }

    private static void collectNewDrops(ServerLevel level, BlockPos pos, Player player, Set<UUID> existingDrops) {
        AABB dropBox = AABB.ofSize(Vec3.atCenterOf(pos),
                DROP_COLLECTION_RADIUS * 2.0D,
                DROP_COLLECTION_RADIUS * 2.0D,
                DROP_COLLECTION_RADIUS * 2.0D);
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, dropBox,
                candidate -> candidate.isAlive() && !existingDrops.contains(candidate.getUUID()))) {
            ItemStack droppedStack = item.getItem();
            player.getInventory().add(droppedStack);
            if (droppedStack.isEmpty()) {
                item.discard();
            }
        }
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
        EnderniumTickScheduler.schedule(level.getServer(), player.getUUID(), () -> {
            if (!player.isRemoved()) {
                collectNearbyDrops(level, pos, player);
            }
        }, 1);
    }

    private static void scheduleVerifiedDropCollection(Level level, BlockPos pos, Player player) {
        EnderniumTickScheduler.schedule(level.getServer(), player.getUUID(), () -> {
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

    private static void scheduleVeinMiningProgress(VeinMiningOperation operation, BlockPos pos, int totalTicks) {
        int steps = Math.min(10, Math.max(1, totalTicks));
        int breakerId = getVeinMiningBreakerId(operation.player);
        for (int step = 0; step < steps; step++) {
            int crackStage = Math.min(9, Math.max(0, (int) Math.floor(((step + 1) * 10.0D) / steps) - 1));
            int delay = Math.max(0, (int) Math.floor((step * (double) totalTicks) / steps));
            int taskId = EnderniumTickScheduler.schedule(operation.level.getServer(), operation.player.getUUID(), () -> {
                if (!isCurrentOperation(operation) || !isPlayerStateValid(operation)
                        || operation.level.getBlockState(pos).isAir()) {
                    operation.level.destroyBlockProgress(breakerId, pos, -1);
                    return;
                }
                operation.level.destroyBlockProgress(breakerId, pos, crackStage);
            }, delay);
            operation.taskIds.add(taskId);
        }
    }

    private static void scheduleNextBlock(VeinMiningOperation operation) {
        if (!isPlayerStateValid(operation) || operation.nextIndex >= operation.targets.size()) {
            finishOperation(operation);
            return;
        }
        TargetBlock target = operation.targets.get(operation.nextIndex);
        operation.currentProgressPos = target.pos;
        int delay = getVeinMiningDelayTicks(operation.level,
                operation.level.getBlockState(target.pos), target.pos, operation.player);
        scheduleVeinMiningProgress(operation, target.pos, delay);
        int taskId = EnderniumTickScheduler.schedule(operation.level.getServer(), operation.player.getUUID(),
                () -> runNextBlock(operation), delay);
        operation.taskIds.add(taskId);
    }

    private static void runNextBlock(VeinMiningOperation operation) {
        if (!isCurrentOperation(operation) || !isPlayerStateValid(operation)
                || operation.nextIndex >= operation.targets.size()) {
            finishOperation(operation);
            return;
        }
        TargetBlock target = operation.targets.get(operation.nextIndex++);
        BlockState currentState = operation.level.getBlockState(target.pos);
        if (currentState.equals(target.state) && canVeinMineBlock(operation.tool, currentState)) {
            handleBlockMine(operation.tool, operation.level, currentState, target.pos, operation.player);
        }
        clearVeinMiningBlockProgress(operation.level, target.pos, operation.player);
        scheduleNextBlock(operation);
    }

    private static boolean isPlayerStateValid(VeinMiningOperation operation) {
        ServerPlayer player = operation.player;
        return player.isAlive()
                && !player.isRemoved()
                && !player.isSpectator()
                && player.level() == operation.level
                && player.distanceToSqr(Vec3.atCenterOf(operation.currentTargetPos())) <= MAX_OPERATION_DISTANCE_SQUARED
                && (player.getMainHandItem() == operation.tool || player.getOffhandItem() == operation.tool)
                && !operation.tool.isEmpty()
                && EnderniumGameplayConfig.toolsVeinMiningEnabled()
                && EnderniumBlessing.isBlessed(player);
    }

    private static boolean isCurrentOperation(VeinMiningOperation operation) {
        Map<UUID, VeinMiningOperation> operations = ACTIVE_OPERATIONS.get(operation.level.getServer());
        return operations != null && operations.get(operation.player.getUUID()) == operation;
    }

    private static void finishOperation(VeinMiningOperation operation) {
        Map<UUID, VeinMiningOperation> operations = ACTIVE_OPERATIONS.get(operation.level.getServer());
        if (operations != null && operations.remove(operation.player.getUUID(), operation) && operations.isEmpty()) {
            ACTIVE_OPERATIONS.remove(operation.level.getServer());
        }
        for (int taskId : operation.taskIds) {
            EnderniumTickScheduler.cancel(operation.level.getServer(), taskId);
        }
        clearVeinMiningBlockProgress(operation.level, operation.currentProgressPos, operation.player);
    }

    private static void clearLegacyOperationTag(ItemStack stack) {
        CompoundTag tag = getOrCreateCustomDataTag(stack);
        if (tag.contains(VEIN_MINING_SESSION_ID_KEY)) {
            tag.remove(VEIN_MINING_SESSION_ID_KEY);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
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

    private record TargetBlock(BlockPos pos, BlockState state) {
    }

    private static final class VeinMiningOperation {
        private final ServerLevel level;
        private final ServerPlayer player;
        private final ItemStack tool;
        private final BlockPos origin;
        private final List<TargetBlock> targets;
        private final List<Integer> taskIds = new ArrayList<>();
        private int nextIndex;
        private BlockPos currentProgressPos;

        private VeinMiningOperation(ServerLevel level, ServerPlayer player, ItemStack tool,
                                    BlockPos origin, List<TargetBlock> targets) {
            this.level = level;
            this.player = player;
            this.tool = tool;
            this.origin = origin;
            this.targets = List.copyOf(targets);
            this.currentProgressPos = origin;
        }

        private BlockPos currentTargetPos() {
            return nextIndex < targets.size() ? targets.get(nextIndex).pos : origin;
        }
    }
}
