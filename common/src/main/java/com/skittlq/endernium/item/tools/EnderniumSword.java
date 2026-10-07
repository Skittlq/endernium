package com.skittlq.endernium.item.tools;

import com.skittlq.endernium.advancement.EnderniumSwordSweepTrigger;
import com.skittlq.endernium.client.EnderniumKeyBindings;
import com.skittlq.endernium.client.EnderniumClientGameplaySettings;
import com.skittlq.endernium.config.EnderniumGameplayConfig;
import com.skittlq.endernium.item.EnderniumTooltipProvider;
import com.skittlq.endernium.item.EnderniumTooltipNumbers;
import com.skittlq.endernium.item.ModToolTiers;
import com.skittlq.endernium.network.EnderniumNetworking;
import com.skittlq.endernium.particles.EnderniumParticles;
import com.skittlq.endernium.progression.EnderniumBlessing;
import com.skittlq.endernium.util.EnderniumTickScheduler;
import com.skittlq.endernium.util.EnderniumTargeting;
import com.skittlq.endernium.util.EnderniumAbilityMath;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public class EnderniumSword extends Item implements EnderniumTooltipProvider {
    public static final float BASE_ATTACK_DAMAGE = 9.0F;
    private static final int TARGET_BUFFER_TICKS = 10;
    private static final Map<MinecraftServer, Map<UUID, AbilitySequence>> ACTIVE_SEQUENCES = new HashMap<>();
    private static final ThreadLocal<UUID> ABILITY_DAMAGE_ATTACKER = new ThreadLocal<>();

    private static ChargeStore chargeStore = new ChargeStore() {
        @Override
        public float getStoredDamage(Player player) {
            return 0.0F;
        }

        @Override
        public void setStoredDamage(Player player, float storedDamage) {
            // no-op until a platform-specific store is bound
        }
    };

    public static void bindChargeStore(ChargeStore store) {
        chargeStore = Objects.requireNonNull(store);
    }

    public static void syncCharge(ServerPlayer player) {
        EnderniumNetworking.sendSwordChargeSync(player, normalizedStoredDamage(player));
    }

    public static float strikeCost() {
        return (float) (BASE_ATTACK_DAMAGE * EnderniumGameplayConfig.swordDamagePerStrikeMultiplier());
    }

    public static int storedStrikes(Player player) {
        return storedStrikes(normalizedStoredDamage(player), strikeCost(), EnderniumGameplayConfig.swordMaxStrikes());
    }

    public static int storedStrikes(float storedDamage, float cost, int maximum) {
        return EnderniumAbilityMath.storedStrikes(storedDamage, cost, maximum);
    }

    public static float partialStrikeProgress(float storedDamage, float cost, int maximum) {
        return EnderniumAbilityMath.partialStrikeProgress(storedDamage, cost, maximum);
    }

    public static void recordNormalAttackDamage(ServerPlayer player, float inflictedDamage) {
        if (inflictedDamage <= 0.0F || !EnderniumGameplayConfig.swordAbilityEnabled()
                || !EnderniumBlessing.isBlessed(player) || isAbilityDamage(player)) {
            return;
        }
        float cost = strikeCost();
        float next = EnderniumAbilityMath.accumulateSwordCharge(normalizedStoredDamage(player),
                inflictedDamage, cost, EnderniumGameplayConfig.swordMaxStrikes());
        setStoredDamage(player, next);
    }

    public static boolean isAbilityDamage(Player player) {
        return player.getUUID().equals(ABILITY_DAMAGE_ATTACKER.get());
    }

    public static void clearCharge(ServerPlayer player) {
        setStoredDamage(player, 0.0F);
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

        if (!(player instanceof ServerPlayer serverPlayer) || !(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }

        MinecraftServer server = serverPlayer.level().getServer();
        UUID uuid = player.getUUID();
        Map<UUID, AbilitySequence> serverSequences =
                ACTIVE_SEQUENCES.computeIfAbsent(server, ignored -> new HashMap<>());
        AbilitySequence activeSequence = serverSequences.remove(uuid);
        if (activeSequence != null) {
            for (int taskId : activeSequence.taskIds) {
                EnderniumTickScheduler.cancel(server, taskId);
            }
            if (serverSequences.isEmpty()) {
                ACTIVE_SEQUENCES.remove(server);
            }
            return InteractionResult.SUCCESS;
        }

        int availableStrikes = storedStrikes(player);
        if (availableStrikes <= 0) {
            return InteractionResult.SUCCESS;
        }

        double range = EnderniumTargeting.SWORD_RANGE;
        List<LivingEntity> targets = EnderniumTargeting.findSwordTargets(serverPlayer);

        List<LivingEntity> sortedTargets = EnderniumTargeting.sortSwordTargets(player, targets).stream()
                .limit(availableStrikes)
                .toList();

        int ticksBetweenHits = 4;
        ItemStack activatedWeapon = player.getItemInHand(hand);
        AbilitySequence sequence = new AbilitySequence();
        serverSequences.put(uuid, sequence);
        if (sortedTargets.isEmpty()) {
            scheduleTargetScan(serverPlayer, serverLevel, hand, activatedWeapon,
                    serverSequences, sequence, TARGET_BUFFER_TICKS);
            return InteractionResult.SUCCESS;
        }

        setStoredDamage(serverPlayer, EnderniumAbilityMath.chargeAfterBarrageAttempt(
                normalizedStoredDamage(serverPlayer), true));
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENCHANTMENT_TABLE_USE, player.getSoundSource(), 0.6F, 1.0F);

        for (int index = 0; index < sortedTargets.size(); index++) {
            LivingEntity target = sortedTargets.get(index);
            int delay = index * ticksBetweenHits;

            int taskId = EnderniumTickScheduler.schedule(server, uuid, () -> {
                if (!canContinueSequence(serverPlayer, serverLevel, hand, activatedWeapon)) {
                    cancelSequence(serverPlayer, true);
                    return;
                }
                if (!EnderniumTargeting.isValidPlayerAbilityTarget(serverPlayer, target)
                        || target.distanceTo(player) > range + 1.0D) {
                    return;
                }

                if (level instanceof ServerLevel effectsLevel) {
                    effectsLevel.sendParticles(EnderniumParticles.REVERSE_ENDERNIUM_BIT.get(),
                            player.getX(), player.getY() + 1.0D, player.getZ(),
                            32, 0.5D, 1.0D, 0.5D, 0.2D);
                    effectsLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
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

                BlockPos teleportBlock = BlockPos.containing(teleportPos);
                if (!serverLevel.getChunkSource().hasChunk(
                        SectionPos.blockToSectionCoord(teleportBlock.getX()),
                        SectionPos.blockToSectionCoord(teleportBlock.getZ())
                )
                        || !serverLevel.getWorldBorder().isWithinBounds(teleportBlock)
                        || !serverLevel.noCollision(player,
                        player.getBoundingBox().move(teleportPos.subtract(player.position())))) {
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

                if (level instanceof ServerLevel effectsLevel) {
                    effectsLevel.sendParticles(EnderniumParticles.REVERSE_ENDERNIUM_BIT.get(),
                            teleportPos.x, teleportPos.y + 1.0D, teleportPos.z,
                            32, 0.5D, 1.0D, 0.5D, 0.2D);
                    effectsLevel.playSound(null, teleportPos.x, teleportPos.y, teleportPos.z,
                            SoundEvents.ENDERMAN_TELEPORT, player.getSoundSource(), 1.0F, 0.85F + 0.3F * level.getRandom().nextFloat());
                }

                player.setDeltaMovement(player.getDeltaMovement().x, 0.42D, player.getDeltaMovement().z);
                serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));

                DamageSource attackSource = activatedWeapon.getDamageSource(player);
                float baseAttackDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
                float normalAttackDamage = EnchantmentHelper.modifyDamage(
                        serverPlayer.level(),
                        activatedWeapon,
                        target,
                        attackSource,
                        baseAttackDamage
                );
                normalAttackDamage += activatedWeapon.getItem().getAttackDamageBonus(
                        target, baseAttackDamage, attackSource);
                boolean hurt;
                ABILITY_DAMAGE_ATTACKER.set(serverPlayer.getUUID());
                try {
                    hurt = target.hurtServer(
                            serverPlayer.level(),
                            attackSource,
                            EnderniumAbilityMath.swordBarrageDamage(normalAttackDamage)
                    );
                } finally {
                    ABILITY_DAMAGE_ATTACKER.remove();
                }
                if (hurt) {
                    EnchantmentHelper.doPostAttackEffectsWithItemSource(
                            serverPlayer.level(),
                            target,
                            attackSource,
                            activatedWeapon
                    );
                    activatedWeapon.hurtAndBreak(2, serverPlayer, hand);
                    sequence.mobsHit++;
                }
                player.swing(hand, SwingAnimation.DEFAULT, true);

                if (level instanceof ServerLevel effectsLevel) {
                    effectsLevel.sendParticles(EnderniumParticles.ENDERNIUM_SWEEP.get(),
                            target.getX(), target.getY() + target.getBbHeight() / 2.0D, target.getZ(),
                            1, 0.0D, 0.0D, 0.0D, 0.0D);
                    effectsLevel.playSound(null, target.getX(), target.getY(), target.getZ(),
                            SoundEvents.PLAYER_ATTACK_SWEEP, player.getSoundSource(), 1.0F, 1.0F);
                }
            }, delay);
            sequence.taskIds.add(taskId);
        }

        int totalDuration = sortedTargets.size() * ticksBetweenHits + 5;
        int endTaskId = EnderniumTickScheduler.schedule(server, uuid, () -> {
            if (serverSequences.remove(uuid) != sequence) {
                return;
            }
            int hitCount = sequence.mobsHit;
            if (hitCount >= 15) {
                EnderniumSwordSweepTrigger.INSTANCE.trigger(serverPlayer, hitCount);
            }

            if (serverSequences.isEmpty()) {
                ACTIVE_SEQUENCES.remove(server);
            }
        }, totalDuration);
        sequence.taskIds.add(endTaskId);

        return InteractionResult.SUCCESS;
    }

    private void scheduleTargetScan(ServerPlayer player, ServerLevel originLevel,
                                    InteractionHand hand, ItemStack activatedWeapon,
                                    Map<UUID, AbilitySequence> serverSequences,
                                    AbilitySequence sequence, int ticksRemaining) {
        MinecraftServer server = player.level().getServer();
        UUID playerId = player.getUUID();
        int taskId = EnderniumTickScheduler.schedule(server, playerId, () -> {
            if (serverSequences.get(playerId) != sequence) {
                return;
            }
            if (!canContinueSequence(player, originLevel, hand, activatedWeapon)) {
                removeSequence(server, serverSequences, playerId, sequence);
                return;
            }
            if (!EnderniumTargeting.findSwordTargets(player).isEmpty()) {
                removeSequence(server, serverSequences, playerId, sequence);
                activateAbility(originLevel, player, hand);
                return;
            }
            if (ticksRemaining <= 1) {
                removeSequence(server, serverSequences, playerId, sequence);
                return;
            }
            scheduleTargetScan(player, originLevel, hand, activatedWeapon,
                    serverSequences, sequence, ticksRemaining - 1);
        }, 1);
        sequence.taskIds.add(taskId);
    }

    private static void removeSequence(MinecraftServer server,
                                       Map<UUID, AbilitySequence> serverSequences,
                                       UUID playerId, AbilitySequence expected) {
        if (serverSequences.remove(playerId, expected) && serverSequences.isEmpty()) {
            ACTIVE_SEQUENCES.remove(server);
        }
    }

    private static boolean canContinueSequence(ServerPlayer player, ServerLevel originLevel,
                                               InteractionHand hand, ItemStack activatedWeapon) {
        return player.isAlive()
                && !player.isRemoved()
                && !player.isSpectator()
                && player.level() == originLevel
                && player.getItemInHand(hand) == activatedWeapon
                && activatedWeapon.getItem() instanceof EnderniumSword
                && EnderniumBlessing.isBlessed(player)
                && EnderniumGameplayConfig.swordAbilityEnabled();
    }

    public static void cancelSequence(ServerPlayer player, boolean applyCooldown) {
        MinecraftServer server = player.level().getServer();
        Map<UUID, AbilitySequence> serverSequences = ACTIVE_SEQUENCES.get(server);
        if (serverSequences == null) {
            return;
        }
        AbilitySequence sequence = serverSequences.remove(player.getUUID());
        if (sequence == null) {
            return;
        }
        for (int taskId : sequence.taskIds) {
            EnderniumTickScheduler.cancel(server, taskId);
        }
        if (serverSequences.isEmpty()) {
            ACTIVE_SEQUENCES.remove(server);
        }
    }

    public static void clearServerState(MinecraftServer server) {
        ACTIVE_SEQUENCES.remove(server);
    }

    private static float normalizedStoredDamage(Player player) {
        float stored = chargeStore.getStoredDamage(player);
        if (!Float.isFinite(stored)) {
            return 0.0F;
        }
        return Math.max(0.0F, Math.min(strikeCost() * EnderniumGameplayConfig.swordMaxStrikes(), stored));
    }

    private static void setStoredDamage(Player player, float storedDamage) {
        float normalized = Float.isFinite(storedDamage) ? Math.max(0.0F, storedDamage) : 0.0F;
        chargeStore.setStoredDamage(player, normalized);
        if (player instanceof ServerPlayer serverPlayer) {
            EnderniumNetworking.sendSwordChargeSync(serverPlayer, normalized);
        }
    }

    private static final class AbilitySequence {
        private final List<Integer> taskIds = new ArrayList<>();
        private int mobsHit;
    }

    public interface ChargeStore {
        float getStoredDamage(Player player);

        void setStoredDamage(Player player, float storedDamage);
    }

    @Override
    public void appendEnderniumTooltip(ItemStack stack, Consumer<Component> tooltipAdder) {
        EnderniumGameplayConfig.Snapshot clientSettings = EnderniumClientGameplaySettings.get();
        if (clientSettings.swordAbilityEnabled()) {
            if (!EnderniumBlessing.isClientBlessed()) {
                tooltipAdder.accept(Component.translatable("endernium.tooltip.ability.locked")
                        .withStyle(ChatFormatting.GRAY));
            } else {
                tooltipAdder.accept(Component.translatable("endernium.tooltip.sword.title")
                        .withStyle(ChatFormatting.LIGHT_PURPLE));
                tooltipAdder.accept(Component.empty());
                tooltipAdder.accept(Component.translatable(
                        "endernium.tooltip.sword.activate",
                        EnderniumKeyBindings.abilityKeyName()
                ).withStyle(ChatFormatting.LIGHT_PURPLE));
                tooltipAdder.accept(Component.translatable(
                        "endernium.tooltip.sword.charge",
                        EnderniumTooltipNumbers.compact(BASE_ATTACK_DAMAGE
                                * clientSettings.swordDamagePerStrikeMultiplier()),
                        clientSettings.swordMaxStrikes()
                ).withStyle(ChatFormatting.LIGHT_PURPLE));
                tooltipAdder.accept(Component.translatable("endernium.tooltip.sword.description").withStyle(ChatFormatting.GRAY));
            }
        }
    }
}
