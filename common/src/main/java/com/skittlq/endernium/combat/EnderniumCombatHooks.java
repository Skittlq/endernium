package com.skittlq.endernium.combat;

import com.skittlq.endernium.progression.DragonBlessingTracker;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;

public final class EnderniumCombatHooks {
    private EnderniumCombatHooks() {
    }

    public static void onDamage(LivingEntity entity, DamageSource source, float inflictedDamage, boolean blocked) {
        if (entity instanceof EnderDragon dragon
                && source.getEntity() instanceof ServerPlayer attacker
                && shouldRecordDragonDamage(inflictedDamage, true)) {
            DragonBlessingTracker.recordDragonDamage(attacker, dragon);
        }
        if (entity instanceof ServerPlayer victim
                && source.getEntity() instanceof ServerPlayer attacker
                && shouldRecordPvpHit(inflictedDamage, blocked, true)) {
            EnderniumCombatTags.recordSuccessfulHit(attacker, victim);
        }
    }

    public static void onDeath(LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            EnderniumCombatTags.playerDied(player);
        }
    }

    public static void onPlayerJoin(ServerPlayer player) {
        EnderniumCombatTags.playerJoined(player);
    }

    public static void onPlayerDisconnect(ServerPlayer player) {
        EnderniumCombatTags.playerDisconnected(player);
    }

    public static void onServerTick(MinecraftServer server) {
        EnderniumCombatTags.tick(server);
    }

    public static void onServerStopped(MinecraftServer server) {
        EnderniumCombatTags.clear(server);
    }

    static boolean shouldRecordDragonDamage(float inflictedDamage, boolean playerAttacker) {
        return inflictedDamage > 0.0F && playerAttacker;
    }

    static boolean shouldRecordPvpHit(float inflictedDamage, boolean blocked, boolean playerAttacker) {
        return inflictedDamage > 0.0F && !blocked && playerAttacker;
    }
}
