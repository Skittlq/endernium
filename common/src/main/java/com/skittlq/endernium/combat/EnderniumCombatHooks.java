package com.skittlq.endernium.combat;

import com.skittlq.endernium.progression.DragonBlessingTracker;
import com.skittlq.endernium.item.EnderniumItems;
import com.skittlq.endernium.item.armor.EnderniumArmorAbility;
import com.skittlq.endernium.item.tools.EnderniumSword;
import com.skittlq.endernium.item.tools.EnderniumSpear;
import com.skittlq.endernium.util.EnderniumAbilityMath;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;

import java.util.Map;
import java.util.WeakHashMap;

public final class EnderniumCombatHooks {
    private static final Map<LivingEntity, Float> HEALTH_BEFORE_DAMAGE = new WeakHashMap<>();

    private EnderniumCombatHooks() {
    }

    public static void beforeDamage(LivingEntity entity) {
        HEALTH_BEFORE_DAMAGE.put(entity, entity.getHealth());
    }

    public static void onDamage(LivingEntity entity, DamageSource source, float inflictedDamage, boolean blocked) {
        float healthDamage = actualHealthDamage(entity, inflictedDamage);
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
        if (source.getEntity() instanceof ServerPlayer attacker
                && source.getDirectEntity() == attacker
                && attacker.getMainHandItem().is(EnderniumItems.ENDERNIUM_SWORD.get())
                && healthDamage > 0.0F
                && !EnderniumSword.isAbilityDamage(attacker)) {
            EnderniumSword.recordNormalAttackDamage(attacker, healthDamage);
        }
        if (inflictedDamage > 0.0F && !blocked
                && source.getEntity() instanceof LivingEntity attacker
                && attacker != entity
                && !EnderniumDamageTypes.isSpearStrain(source)) {
            EnderniumArmorAbility.recordQualifyingDamage(entity, inflictedDamage);
        }
    }

    static float actualHealthDamage(LivingEntity entity, float reportedDamage) {
        Float previousHealth = HEALTH_BEFORE_DAMAGE.remove(entity);
        if (previousHealth == null || !Float.isFinite(previousHealth)) {
            return Math.max(0.0F, reportedDamage);
        }
        return EnderniumAbilityMath.actualHealthDamage(previousHealth, entity.getHealth(), reportedDamage);
    }

    public static void onDeath(LivingEntity entity) {
        if (entity instanceof ServerPlayer player) {
            EnderniumCombatTags.playerDied(player);
            EnderniumSword.clearCharge(player);
            EnderniumArmorAbility.clearCharge(player);
            EnderniumSpear.clearStrain(player);
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
        EnderniumSpear.tickStrainExpiry(server);
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
