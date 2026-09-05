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
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class EnderniumSword extends Item {
    private static final Map<UUID, List<Integer>> ACTIVE_TASKS = new HashMap<>();
    private static final Map<UUID, AtomicInteger> MOBS_HIT_MAP = new HashMap<>();

    private static CooldownStore cooldownStore = new CooldownStore() {
        @Override
        public long getCooldownEndGameTime(Player player) {
            return 0L;
        }

        @Override
        public void setCooldownEndGameTime(Player player, long gameTime) {
            // no-op until a platform-specific store is bound
        }

        @Override
        public int getCooldownDurationTicks(Player player) {
            return 0;
        }

        @Override
        public void setCooldownDurationTicks(Player player, int durationTicks) {
            // no-op until a platform-specific store is bound
        }
    };

    public static void bindCooldownStore(CooldownStore store) {
        cooldownStore = Objects.requireNonNull(store);
    }

    // Resends the persisted cooldown to the client on login, preserving the original duration so the HUD shows true elapsed progress instead of restarting.
    public static void syncCooldownOnLogin(ServerPlayer player) {
        long endGameTime = cooldownStore.getCooldownEndGameTime(player);
        if (endGameTime > player.level().getGameTime()) {
            int durationTicks = cooldownStore.getCooldownDurationTicks(player);
            EnderniumNetworking.sendSwordCooldownSync(player, endGameTime, durationTicks);
        }
    }

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
            addConfiguredCooldown(player, mobsHit);
            MOBS_HIT_MAP.remove(uuid);
            return InteractionResult.SUCCESS;
        }

        if (cooldownStore.getCooldownEndGameTime(player) > player.level().getGameTime()) {
            return InteractionResult.SUCCESS;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        double range = EnderniumTargeting.SWORD_RANGE;
        Vec3 lookVec = player.getLookAngle();
        Vec3 playerPos = EnderniumTargeting.eyePosition(player);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENCHANTMENT_TABLE_USE, player.getSoundSource(), 0.6F, 1.0F);

        List<LivingEntity> targets = EnderniumTargeting.findSwordTargets(serverPlayer);

        List<LivingEntity> sortedTargets = targets.stream()
                .sorted(Comparator.comparingDouble(target -> {
                    Vec3 toTarget = target.position().add(0.0D, target.getBbHeight() / 2.0D, 0.0D).subtract(playerPos);
                    double angle = lookVec.normalize().dot(toTarget.normalize());
                    double theta = Math.acos(angle);
                    double dist = toTarget.length();
                    return theta * 2.0D + dist / range;
                }))
                .toList();

        int ticksBetweenHits = 4;
        List<Integer> taskIds = new ArrayList<>();
        AtomicInteger mobsHit = new AtomicInteger(0);
        ACTIVE_TASKS.put(uuid, taskIds);
        MOBS_HIT_MAP.put(uuid, mobsHit);

        for (int index = 0; index < sortedTargets.size(); index++) {
            LivingEntity target = sortedTargets.get(index);
            int delay = index * ticksBetweenHits;

            int taskId = EnderniumTickScheduler.schedule(() -> {
                if (!EnderniumTargeting.isValidPlayerAbilityTarget(serverPlayer, target)
                        || target.distanceTo(player) > range + 1.0D) {
                    return;
                }

                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.PORTAL,
                            player.getX(), player.getY() + 1.0D, player.getZ(),
                            32, 0.5D, 1.0D, 0.5D, 0.2D);
                    serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ENDERMAN_TELEPORT, player.getSoundSource(), 1.0F, 1.0F);
                }

                Vec3 targetCenter = target.position().add(0.0D, target.getBbHeight() / 2.0D, 0.0D);
                Vec3 toTarget = targetCenter.subtract(player.position());
                Vec3 teleportOffset = toTarget.normalize().scale(Math.min(1.5D, Math.max(1.0D, target.getBbWidth() + 1.0D)));
                Vec3 teleportPos = new Vec3(
                        targetCenter.x - teleportOffset.x,
                        target.position().y,
                        targetCenter.z - teleportOffset.z
                );

                if (!level.noCollision(player, player.getBoundingBox().move(teleportPos.subtract(player.position())))) {
                    return;
                }

                player.teleportTo(teleportPos.x, teleportPos.y, teleportPos.z);
                player.fallDistance = 0.0F;

                double dx = target.getX() - player.getX();
                double dz = target.getZ() - player.getZ();
                double dy = (target.getY() + target.getBbHeight() / 2.0D) - (player.getY() + player.getEyeHeight());
                float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
                float pitch = (float) Math.toDegrees(-Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));

                EnderniumNetworking.sendCameraLerp(serverPlayer, yaw, pitch, 0);

                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.PORTAL,
                            teleportPos.x, teleportPos.y + 1.0D, teleportPos.z,
                            32, 0.5D, 1.0D, 0.5D, 0.2D);
                    serverLevel.playSound(null, teleportPos.x, teleportPos.y, teleportPos.z,
                            SoundEvents.ENDERMAN_TELEPORT, player.getSoundSource(), 1.0F, 0.85F + 0.3F * level.getRandom().nextFloat());
                }

                player.setDeltaMovement(player.getDeltaMovement().x, 0.42D, player.getDeltaMovement().z);
                player.hurtMarked = true;

                ItemStack weapon = player.getItemInHand(hand);
                DamageSource attackSource = weapon.getDamageSource(player);
                float baseAttackDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
                float normalAttackDamage = EnchantmentHelper.modifyDamage(
                        serverPlayer.level(),
                        weapon,
                        target,
                        attackSource,
                        baseAttackDamage
                );
                normalAttackDamage += weapon.getItem().getAttackDamageBonus(target, baseAttackDamage, attackSource);
                float abilityDamageMultiplier = target instanceof Player ? 2.0F : 3.0F;
                if (target.hurtOrSimulate(attackSource, normalAttackDamage * abilityDamageMultiplier)) {
                    EnchantmentHelper.doPostAttackEffectsWithItemSource(
                            serverPlayer.level(),
                            target,
                            attackSource,
                            weapon
                    );
                }
                player.swing(hand, true);
                mobsHit.incrementAndGet();

                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(EnderniumParticles.ENDERNIUM_SWEEP.get(),
                            target.getX(), target.getY() + target.getBbHeight() / 2.0D, target.getZ(),
                            1, 0.0D, 0.0D, 0.0D, 0.0D);
                    serverLevel.playSound(null, target.getX(), target.getY(), target.getZ(),
                            SoundEvents.PLAYER_ATTACK_SWEEP, player.getSoundSource(), 1.0F, 1.0F);
                }
            }, delay);
            taskIds.add(taskId);
        }

        int totalDuration = sortedTargets.size() * ticksBetweenHits + 5;
        int endTaskId = EnderniumTickScheduler.schedule(() -> {
            addConfiguredCooldown(player, mobsHit.get());

            int hitCount = mobsHit.get();
            if (hitCount >= 15) {
                EnderniumSwordSweepTrigger.INSTANCE.trigger(serverPlayer, hitCount);
            }

            ACTIVE_TASKS.remove(uuid);
            MOBS_HIT_MAP.remove(uuid);
        }, totalDuration);
        taskIds.add(endTaskId);

        return InteractionResult.SUCCESS;
    }

    private static void addConfiguredCooldown(Player player, int mobsHit) {
        int cooldownTicks = EnderniumGameplayConfig.swordAbilityCooldownTicks(mobsHit);
        if (cooldownTicks > 0 && player instanceof ServerPlayer serverPlayer) {
            long endGameTime = player.level().getGameTime() + cooldownTicks;
            cooldownStore.setCooldownEndGameTime(player, endGameTime);
            cooldownStore.setCooldownDurationTicks(player, cooldownTicks);
            EnderniumNetworking.sendSwordCooldownSync(serverPlayer, endGameTime, cooldownTicks);
        }
    }

    public interface CooldownStore {
        long getCooldownEndGameTime(Player player);

        void setCooldownEndGameTime(Player player, long gameTime);

        int getCooldownDurationTicks(Player player);

        void setCooldownDurationTicks(Player player, int durationTicks);
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
