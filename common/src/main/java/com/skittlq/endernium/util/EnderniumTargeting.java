package com.skittlq.endernium.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class EnderniumTargeting {
    public static final double SWORD_RANGE = 10.0D;
    public static final double SWORD_ARC = Math.PI / 1.5D;

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

    private EnderniumTargeting() {
    }

    public static List<Mob> findSwordTargets(Player player) {
        Vec3 lookVec = player.getLookAngle();
        Vec3 playerPos = eyePosition(player);
        double range = SWORD_RANGE;
        return player.level().getEntitiesOfClass(
                Mob.class,
                new AABB(
                        playerPos.x - range,
                        playerPos.y - 2.0D,
                        playerPos.z - range,
                        playerPos.x + range,
                        playerPos.y + 2.0D,
                        playerPos.z + range
                ),
                mob -> isValidSwordTarget(mob, playerPos, lookVec, SWORD_RANGE, SWORD_ARC)
        );
    }

    public static Vec3 eyePosition(Player player) {
        return player.position().add(0.0D, player.getEyeHeight(), 0.0D);
    }

    public static boolean isExtraHostile(Mob mob) {
        return EXTRA_HOSTILES.contains(BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()));
    }

    public static boolean isHostile(Mob mob) {
        return mob instanceof Monster || isExtraHostile(mob);
    }

    public static boolean isValidSwordTarget(Mob mob, Vec3 playerPos, Vec3 lookVec, double range, double arc) {
        if (!isHostile(mob)) {
            return false;
        }

        Vec3 toMob = mob.position().add(0.0D, mob.getBbHeight() / 2.0D, 0.0D).subtract(playerPos);
        double distance = toMob.length();
        if (distance > range || distance <= 1.0E-5D) {
            return false;
        }

        double angle = lookVec.normalize().dot(toMob.normalize());
        double theta = Math.acos(Math.max(-1.0D, Math.min(1.0D, angle)));
        return theta < (arc / 2.0D);
    }
}
