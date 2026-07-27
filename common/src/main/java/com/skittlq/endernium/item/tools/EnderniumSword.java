package com.skittlq.endernium.item.tools;

import com.skittlq.endernium.advancement.EnderniumSwordSweepTrigger;
import com.skittlq.endernium.client.EnderniumKeyBindings;
import com.skittlq.endernium.config.EnderniumGameplayConfig;
import com.skittlq.endernium.item.ModToolTiers;
import com.skittlq.endernium.network.EnderniumNetworking;
import com.skittlq.endernium.particles.EnderniumParticles;
import com.skittlq.endernium.util.EnderniumTickScheduler;
import com.skittlq.endernium.util.EnderniumTargeting;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class EnderniumSword extends Item {
    private static final Map<UUID, List<Integer>> ACTIVE_TASKS = new HashMap<>();
    private static final Map<UUID, AtomicInteger> MOBS_HIT_MAP = new HashMap<>();

    public EnderniumSword(Properties properties) {
        super(properties.sword(ModToolTiers.ENDERNIUM, 3.0F, -2.4F));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    public InteractionResult activateAbility(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        UUID uuid = player.getUUID();
        if (ACTIVE_TASKS.containsKey(uuid) && !ACTIVE_TASKS.get(uuid).isEmpty()) {
            for (int taskId : ACTIVE_TASKS.get(uuid)) {
                EnderniumTickScheduler.cancel(taskId);
            }
            ACTIVE_TASKS.get(uuid).clear();

            int mobsHit = MOBS_HIT_MAP.getOrDefault(uuid, new AtomicInteger(0)).get();
            addConfiguredCooldown(player, player.getItemInHand(hand), mobsHit);
            MOBS_HIT_MAP.remove(uuid);
            return InteractionResult.SUCCESS;
        }

        if (player.getCooldowns().isOnCooldown(player.getItemInHand(hand))) {
            return InteractionResult.SUCCESS;
        }

        double range = EnderniumTargeting.SWORD_RANGE;
        double arc = EnderniumTargeting.SWORD_ARC;
        Vec3 lookVec = player.getLookAngle();
        Vec3 playerPos = EnderniumTargeting.eyePosition(player);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENCHANTMENT_TABLE_USE, player.getSoundSource(), 0.6F, 1.0F);

        List<Mob> targets = level.getEntitiesOfClass(
                Mob.class,
                new AABB(
                        playerPos.x - range, playerPos.y - 2.0D, playerPos.z - range,
                        playerPos.x + range, playerPos.y + 2.0D, playerPos.z + range
                ),
                mob -> EnderniumTargeting.isValidSwordTarget(mob, playerPos, lookVec, range, arc)
        );

        List<Mob> sortedTargets = targets.stream()
                .sorted(Comparator.comparingDouble(mob -> {
                    Vec3 toMob = mob.position().add(0.0D, mob.getBbHeight() / 2.0D, 0.0D).subtract(playerPos);
                    double angle = lookVec.normalize().dot(toMob.normalize());
                    double theta = Math.acos(angle);
                    double dist = toMob.length();
                    return theta * 2.0D + dist / range;
                }))
                .toList();

        int ticksBetweenHits = 4;
        List<Integer> taskIds = new ArrayList<>();
        AtomicInteger mobsHit = new AtomicInteger(0);
        ACTIVE_TASKS.put(uuid, taskIds);
        MOBS_HIT_MAP.put(uuid, mobsHit);

        for (int index = 0; index < sortedTargets.size(); index++) {
            Mob mob = sortedTargets.get(index);
            int delay = index * ticksBetweenHits;

            int taskId = EnderniumTickScheduler.schedule(() -> {
                if (!mob.isAlive() || mob.distanceTo(player) > range + 1.0D) {
                    return;
                }

                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.PORTAL,
                            player.getX(), player.getY() + 1.0D, player.getZ(),
                            32, 0.5D, 1.0D, 0.5D, 0.2D);
                    serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ENDERMAN_TELEPORT, player.getSoundSource(), 1.0F, 1.0F);
                }

                Vec3 mobCenter = mob.position().add(0.0D, mob.getBbHeight() / 2.0D, 0.0D);
                Vec3 toMob = mobCenter.subtract(player.position());
                Vec3 teleportOffset = toMob.normalize().scale(Math.min(1.5D, Math.max(1.0D, mob.getBbWidth() + 1.0D)));
                Vec3 teleportPos = new Vec3(
                        mobCenter.x - teleportOffset.x,
                        mob.position().y,
                        mobCenter.z - teleportOffset.z
                );

                if (!level.noCollision(player, player.getBoundingBox().move(teleportPos.subtract(player.position())))) {
                    return;
                }

                player.teleportTo(teleportPos.x, teleportPos.y, teleportPos.z);
                player.fallDistance = 0.0F;

                double dx = mob.getX() - player.getX();
                double dz = mob.getZ() - player.getZ();
                double dy = (mob.getY() + mob.getBbHeight() / 2.0D) - (player.getY() + player.getEyeHeight());
                float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
                float pitch = (float) Math.toDegrees(-Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));

                if (player instanceof ServerPlayer serverPlayer) {
                    EnderniumNetworking.sendCameraLerp(serverPlayer, yaw, pitch, 0);
                }

                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.PORTAL,
                            teleportPos.x, teleportPos.y + 1.0D, teleportPos.z,
                            32, 0.5D, 1.0D, 0.5D, 0.2D);
                    serverLevel.playSound(null, teleportPos.x, teleportPos.y, teleportPos.z,
                            SoundEvents.ENDERMAN_TELEPORT, player.getSoundSource(), 1.0F, 0.85F + 0.3F * level.getRandom().nextFloat());
                }

                player.setDeltaMovement(player.getDeltaMovement().x, 0.42D, player.getDeltaMovement().z);
                player.hurtMarked = true;

                var attackDamage = player.getAttribute(Attributes.ATTACK_DAMAGE);
                double baseAttack = attackDamage != null ? attackDamage.getValue() : 1.0D;
                mob.hurt(player.damageSources().playerAttack(player), (float) (baseAttack * 3.0D));
                player.swing(hand, true);
                mobsHit.incrementAndGet();

                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(EnderniumParticles.ENDERNIUM_SWEEP.get(),
                            mob.getX(), mob.getY() + mob.getBbHeight() / 2.0D, mob.getZ(),
                            1, 0.0D, 0.0D, 0.0D, 0.0D);
                    serverLevel.playSound(null, mob.getX(), mob.getY(), mob.getZ(),
                            SoundEvents.PLAYER_ATTACK_SWEEP, player.getSoundSource(), 1.0F, 1.0F);
                }
            }, delay);
            taskIds.add(taskId);
        }

        int totalDuration = sortedTargets.size() * ticksBetweenHits + 5;
        int endTaskId = EnderniumTickScheduler.schedule(() -> {
            addConfiguredCooldown(player, player.getItemInHand(hand), mobsHit.get());

            if (player instanceof ServerPlayer serverPlayer) {
                int hitCount = mobsHit.get();
                if (hitCount >= 15) {
                    EnderniumSwordSweepTrigger.INSTANCE.trigger(serverPlayer, hitCount);
                }
            }

            ACTIVE_TASKS.remove(uuid);
            MOBS_HIT_MAP.remove(uuid);
        }, totalDuration);
        taskIds.add(endTaskId);

        return InteractionResult.SUCCESS;
    }

    private static void addConfiguredCooldown(Player player, ItemStack stack, int mobsHit) {
        int cooldownTicks = EnderniumGameplayConfig.swordAbilityCooldownTicks(mobsHit);
        if (cooldownTicks > 0) {
            player.getCooldowns().addCooldown(stack, cooldownTicks);
        }
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        return super.mineBlock(stack, level, state, pos, entity);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay,
                                Consumer<Component> tooltipAdder, TooltipFlag flag) {
        if (EnderniumGameplayConfig.swordAbilityEnabled()) {
            tooltipAdder.accept(Component.translatable(
                    "endernium.tooltip.sword.activate",
                    EnderniumKeyBindings.abilityKeyName()
            ).withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltipAdder.accept(Component.translatable(
                    "endernium.tooltip.sword.cooldown",
                    EnderniumGameplayConfig.swordAbilityBaseCooldownSeconds(),
                    EnderniumGameplayConfig.swordAbilityPerMobCooldownSeconds()
            ).withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltipAdder.accept(Component.translatable("endernium.tooltip.sword.description").withStyle(ChatFormatting.GRAY));
        }
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
    }
}
