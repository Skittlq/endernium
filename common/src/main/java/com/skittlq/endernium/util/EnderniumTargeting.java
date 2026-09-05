package com.skittlq.endernium.util;

import com.skittlq.endernium.combat.EnderniumCombatTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class EnderniumTargeting {
    public static final double SWORD_RANGE = 10.0D;
    public static final double SWORD_ARC = Math.PI / 1.5D;
    public static final double SWORD_INNER_RADIUS = 2.0D;
    public static final int MOB_RETALIATION_TICKS = 200;

    private static final Set<Identifier> EXTRA_HOSTILES = new HashSet<>(Set.of(
            Identifier.withDefaultNamespace("phantom"),
            Identifier.withDefaultNamespace("shulker"),
            Identifier.withDefaultNamespace("vex"),
            Identifier.withDefaultNamespace("ender_dragon"),
            Identifier.withDefaultNamespace("wither"),
            Identifier.withDefaultNamespace("warden"),
            Identifier.withDefaultNamespace("elder_guardian"),
            Identifier.withDefaultNamespace("ghast"),
            Identifier.withDefaultNamespace("piglin"),
            Identifier.withDefaultNamespace("piglin_brute"),
            Identifier.withDefaultNamespace("slime"),
            Identifier.withDefaultNamespace("magma_cube")
    ));
    private static Set<UUID> clientCombatOpponents = Set.of();

    private EnderniumTargeting() {
    }

    public static List<LivingEntity> findSwordTargets(ServerPlayer player) {
        Vec3 lookVector = player.getLookAngle();
        Vec3 playerPosition = eyePosition(player);
        return player.level().getEntitiesOfClass(
                LivingEntity.class,
                swordSearchBox(playerPosition, SWORD_RANGE),
                target -> isValidSwordTarget(
                        player,
                        target,
                        playerPosition,
                        lookVector,
                        SWORD_RANGE,
                        SWORD_ARC
                )
        );
    }

    public static List<LivingEntity> findSwordPreviewTargets(Player player) {
        Vec3 lookVector = player.getLookAngle();
        Vec3 playerPosition = eyePosition(player);
        return player.level().getEntitiesOfClass(
                LivingEntity.class,
                swordSearchBox(playerPosition, SWORD_RANGE),
                target -> isValidSwordPreviewTarget(
                        player,
                        target,
                        playerPosition,
                        lookVector,
                        SWORD_RANGE,
                        SWORD_ARC
                )
        );
    }

    public static boolean isValidSwordTarget(ServerPlayer player, LivingEntity target, Vec3 playerPosition,
                                             Vec3 lookVector, double range, double arc) {
        return isValidPlayerAbilityTarget(player, target)
                && isWithinSwordTargetingArea(player, target, playerPosition, lookVector, range, arc);
    }

    public static boolean isValidSwordPreviewTarget(Player player, LivingEntity target, Vec3 playerPosition,
                                                    Vec3 lookVector, double range, double arc) {
        return isValidClientPreviewTarget(player, target)
                && isWithinSwordTargetingArea(player, target, playerPosition, lookVector, range, arc);
    }

    public static boolean isValidPlayerAbilityTarget(ServerPlayer player, LivingEntity target) {
        if (target == player || !target.isAlive() || isTamed(target)) {
            return false;
        }

        if (target instanceof ServerPlayer otherPlayer) {
            return !otherPlayer.isCreative()
                    && !otherPlayer.isSpectator()
                    && player.level().isPvpAllowed()
                    && player.canHarmPlayer(otherPlayer)
                    && EnderniumCombatTags.canTarget(player, otherPlayer);
        }

        if (player.isAlliedTo(target)) {
            return false;
        }

        if (target instanceof Mob mob) {
            return isHostile(mob) || isNeutralMobAngryAt(mob, player, player.level());
        }

        return false;
    }

    public static boolean isValidMobArmorTarget(Mob wearer, LivingEntity target, ServerLevel level) {
        if (target == wearer || !target.isAlive() || wearer.isAlliedTo(target) || isTamed(target)) {
            return false;
        }

        if (target instanceof Mob mob) {
            if (wearer.getType() == mob.getType()) {
                return false;
            }
            return isHostile(mob) || isNeutralMobAngryAt(mob, wearer, level);
        }

        if (target instanceof ServerPlayer player) {
            if (player.isCreative() || player.isSpectator()) {
                return false;
            }
            return wearer.getTarget() == player || isRecentAttacker(wearer, player);
        }

        return false;
    }

    public static Vec3 eyePosition(Player player) {
        return player.position().add(0.0D, player.getEyeHeight(), 0.0D);
    }

    public static boolean isExtraHostile(Mob mob) {
        return EXTRA_HOSTILES.contains(BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()));
    }

    public static boolean isHostile(Mob mob) {
        return !(mob instanceof NeutralMob) && (mob instanceof Monster || isExtraHostile(mob));
    }

    public static void replaceClientCombatOpponents(List<UUID> opponentIds) {
        clientCombatOpponents = Set.copyOf(opponentIds);
    }

    public static void clearClientCombatOpponents() {
        clientCombatOpponents = Set.of();
    }

    private static boolean isValidClientPreviewTarget(Player player, LivingEntity target) {
        if (target == player || !target.isAlive() || isTamed(target)) {
            return false;
        }

        if (target instanceof Player otherPlayer) {
            return !otherPlayer.isCreative()
                    && !otherPlayer.isSpectator()
                    && player.canHarmPlayer(otherPlayer)
                    && clientCombatOpponents.contains(otherPlayer.getUUID());
        }

        if (player.isAlliedTo(target)) {
            return false;
        }

        if (target instanceof Mob mob) {
            return isHostile(mob) || mob instanceof NeutralMob && mob.getTarget() == player;
        }

        return false;
    }

    private static boolean isWithinSwordTargetingArea(Player player, LivingEntity target, Vec3 playerPosition,
                                                      Vec3 lookVector, double range, double arc) {
        if (player.distanceTo(target) <= SWORD_INNER_RADIUS) {
            return true;
        }

        Vec3 toTarget = target.position()
                .add(0.0D, target.getBbHeight() / 2.0D, 0.0D)
                .subtract(playerPosition);
        double distance = toTarget.length();
        if (distance > range || distance <= 1.0E-5D) {
            return false;
        }

        double angle = lookVector.normalize().dot(toTarget.normalize());
        double theta = Math.acos(Math.max(-1.0D, Math.min(1.0D, angle)));
        return theta < (arc / 2.0D);
    }

    private static boolean isNeutralMobAngryAt(Mob mob, LivingEntity target, ServerLevel level) {
        return mob instanceof NeutralMob neutralMob && neutralMob.isAngryAt(target, level);
    }

    private static boolean isTamed(LivingEntity entity) {
        return entity instanceof OwnableEntity ownableEntity && ownableEntity.getOwnerReference() != null;
    }

    private static boolean isRecentAttacker(Mob wearer, ServerPlayer player) {
        if (wearer.getLastHurtByMob() != player) {
            return false;
        }

        int elapsedTicks = wearer.tickCount - wearer.getLastHurtByMobTimestamp();
        return elapsedTicks >= 0 && elapsedTicks <= MOB_RETALIATION_TICKS;
    }

    private static AABB swordSearchBox(Vec3 playerPosition, double range) {
        return new AABB(
                playerPosition.x - range,
                playerPosition.y - 2.0D,
                playerPosition.z - range,
                playerPosition.x + range,
                playerPosition.y + 2.0D,
                playerPosition.z + range
        );
    }
}
