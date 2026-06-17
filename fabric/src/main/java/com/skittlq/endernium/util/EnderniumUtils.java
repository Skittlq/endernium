package com.skittlq.endernium.util;

import com.skittlq.endernium.particles.ModParticles;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
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

    private EnderniumUtils() {
    }

    public static void handleBlockMine(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        Player player = entity instanceof Player foundPlayer ? foundPlayer : null;
        if (level.isClientSide() || player == null || player.isCreative() || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        LootParams.Builder builder = new LootParams.Builder(serverPlayer.level())
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withParameter(LootContextParams.TOOL, stack)
                .withParameter(LootContextParams.BLOCK_STATE, state)
                .withOptionalParameter(LootContextParams.THIS_ENTITY, entity)
                .withLuck(player.getLuck());

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ModParticles.REVERSE_ENDERNIUM_BIT,
                    pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    10, 0.2D, 0.2D, 0.2D, 0.01D);
        }
        level.playSound(null, pos, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 0.04F, 1.5F);

        if (blockEntity != null) {
            builder.withParameter(LootContextParams.BLOCK_ENTITY, blockEntity);
        }

        List<ItemStack> drops = state.getDrops(builder);

        if (state.requiresCorrectToolForDrops() && !stack.isCorrectToolForDrops(state)) {
            level.removeBlock(pos, false);
            return;
        }

        for (ItemStack drop : drops) {
            if (!player.getInventory().add(drop)) {
                ItemEntity itemEntity = new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), drop);
                level.addFreshEntity(itemEntity);
            }
        }

        level.removeBlock(pos, false);
    }

    public static void veinMineBlocks(ItemStack stack, Level level, BlockPos origin, Player player, int maxBlocks) {
        BlockState originState = level.getBlockState(origin);
        if (!stack.isCorrectToolForDrops(originState)) {
            return;
        }

        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();
        List<BlockPos> blocksToMine = new ArrayList<>();
        queue.add(origin);

        while (!queue.isEmpty() && blocksToMine.size() < maxBlocks) {
            BlockPos current = queue.poll();
            if (visited.contains(current)) {
                continue;
            }
            visited.add(current);

            BlockState state = level.getBlockState(current);
            if (state.getBlock() != originState.getBlock()) {
                continue;
            }
            if (!stack.isCorrectToolForDrops(state)) {
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

            BlockPos pos = iterator.next();
            BlockState state = level.getBlockState(pos);
            if (stack.isCorrectToolForDrops(state)) {
                handleBlockMine(stack, level, state, pos, player);

                if (!level.isClientSide() && isCropBlock(state)) {
                    Item seedItem = getSeedForCrop(state);
                    int slot = findSeedSlot(player, seedItem);
                    if (seedItem != null && slot != -1) {
                        EnderniumTickScheduler.schedule(() -> {
                            BlockState afterMine = level.getBlockState(pos);
                            if (afterMine.isAir()) {
                                player.getInventory().removeItem(slot, 1);
                                BlockState cropState = getDefaultCropState(state, level, pos);
                                level.setBlockAndUpdate(pos, cropState);
                                level.playSound(null, pos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 0.7F, 1.1F);
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
}
