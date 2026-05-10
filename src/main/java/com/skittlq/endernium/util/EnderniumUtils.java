package com.skittlq.endernium.util;

import com.skittlq.endernium.particles.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class EnderniumUtils {
    private EnderniumUtils() {}

    public static void handleBlockMine(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        Player player = entity instanceof Player ? (Player) entity : null;
        if (!level.isClientSide() && player != null && !player.isCreative()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            LootParams.Builder builder = new LootParams.Builder(((ServerPlayer) player).level())
                    .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                    .withParameter(LootContextParams.TOOL, stack)
                    .withParameter(LootContextParams.BLOCK_STATE, state)
                    .withOptionalParameter(LootContextParams.THIS_ENTITY, entity)
                    .withLuck(player.getLuck());

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ModParticles.REVERSE_ENDERNIUM_BIT.get(),
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        10, 0.2, 0.2, 0.2, 0.01);
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
    }

    public static final String VEIN_MINING_SESSION_ID_KEY = "VeinMiningSessionId";
    public static final int DEFAULT_MAX_BLOCKS = 64;

    public static void veinMineBlocks(ItemStack stack, Level level, BlockPos origin, Player player, int maxBlocks) {
        BlockState originState = level.getBlockState(origin);
        if (!stack.isCorrectToolForDrops(originState)) return;

        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> queue = new LinkedList<>();
        List<BlockPos> blocksToMine = new ArrayList<>();
        queue.add(origin);

        while (!queue.isEmpty() && blocksToMine.size() < maxBlocks) {
            BlockPos current = queue.poll();
            if (visited.contains(current)) continue;
            visited.add(current);

            BlockState state = level.getBlockState(current);
            if (state.getBlock() != originState.getBlock()) continue;
            if (!stack.isCorrectToolForDrops(state)) continue;

            blocksToMine.add(current);

            for (Direction dir : Direction.values()) {
                BlockPos neighbor = current.relative(dir);
                if (!visited.contains(neighbor)) {
                    queue.add(neighbor);
                }
            }
        }

        float speed = stack.getDestroySpeed(originState);
        if (speed <= 0) speed = 1.0F;
        int ticksPerBlock = Math.max(1, Math.round((2.0F / speed) * 20F));

        Iterator<BlockPos> it = blocksToMine.iterator();

        int sessionId = new Random().nextInt();
        CompoundTag tag = getOrCreateCustomDataTag(stack);
        tag.putInt(VEIN_MINING_SESSION_ID_KEY, sessionId);
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));

        Runnable[] task = new Runnable[1];
        task[0] = () -> {
            CompoundTag checkTag = getOrCreateCustomDataTag(stack);
            int currentSession = checkTag.getIntOr(VEIN_MINING_SESSION_ID_KEY, 0);
            if (currentSession != sessionId) return;

            if (!it.hasNext()) {
                checkTag.remove(VEIN_MINING_SESSION_ID_KEY);
                stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(checkTag));
                return;
            }

            BlockPos pos = it.next();
            BlockState state = level.getBlockState(pos);
            if (stack.isCorrectToolForDrops(state)) {
                handleBlockMine(stack, level, state, pos, player);

                if (!level.isClientSide() && isCropBlock(state)) {
                    net.minecraft.world.item.Item seedItem = getSeedForCrop(state);
                    int slot = findSeedSlot(player, seedItem);
                    System.out.println("[EnderniumUtils] Scheduling replant for crop at " + pos + " (seed: " + (seedItem != null ? seedItem.toString() : "null") + ", slot: " + slot + ")");
                    if (seedItem != null && slot != -1) {
                        EnderniumTickScheduler.schedule(() -> {
                            BlockState afterMine = level.getBlockState(pos);
                            System.out.println("[EnderniumUtils] After mine: " + afterMine.getBlock().getDescriptionId() + " (isAir: " + afterMine.isAir() + ")");
                            if (afterMine.isAir()) {
                                player.getInventory().removeItem(slot, 1);
                                BlockState cropState = getDefaultCropState(state, level, pos);
                                level.setBlockAndUpdate(pos, cropState);
                                level.playSound(null, pos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 0.7f, 1.1f);
                                System.out.println("[EnderniumUtils] REPLANTED at " + pos);
                            }
                        }, 1);
                    }
                }
            }
            if (it.hasNext()) {
                com.skittlq.endernium.util.EnderniumTickScheduler.schedule(task[0], ticksPerBlock);
            } else {
                checkTag.remove(VEIN_MINING_SESSION_ID_KEY);
                stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(checkTag));
            }
        };

        com.skittlq.endernium.util.EnderniumTickScheduler.schedule(task[0], 0);
    }

    public static void cancelVeinMining(ItemStack stack) {
        CompoundTag tag = getOrCreateCustomDataTag(stack);
        tag.remove(VEIN_MINING_SESSION_ID_KEY);
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
    }

    public static CompoundTag getOrCreateCustomDataTag(ItemStack stack) {
        var data = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
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

    public static net.minecraft.world.item.Item getSeedForCrop(BlockState state) {
        String name = state.getBlock().getDescriptionId();
        if (name.contains("wheat")) return net.minecraft.world.item.Items.WHEAT_SEEDS;
        if (name.contains("carrot")) return net.minecraft.world.item.Items.CARROT;
        if (name.contains("potato")) return net.minecraft.world.item.Items.POTATO;
        if (name.contains("beetroot")) return net.minecraft.world.item.Items.BEETROOT_SEEDS;
        if (name.contains("nether_wart")) return net.minecraft.world.item.Items.NETHER_WART;
        if (name.contains("melon_stem")) return net.minecraft.world.item.Items.MELON_SEEDS;
        if (name.contains("pumpkin_stem")) return net.minecraft.world.item.Items.PUMPKIN_SEEDS;
        return null;
    }

    public static BlockState getDefaultCropState(BlockState oldState, Level level, BlockPos pos) {
        return oldState.getBlock().defaultBlockState();
    }

    public static int findSeedSlot(Player player, net.minecraft.world.item.Item seed) {
        if (seed == null) return -1;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() == seed) {
                return i;
            }
        }
        return -1;
    }
}
