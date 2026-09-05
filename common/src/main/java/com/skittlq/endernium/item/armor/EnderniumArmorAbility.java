package com.skittlq.endernium.item.armor;

import com.skittlq.endernium.network.EnderniumNetworking;
import com.skittlq.endernium.particles.EnderniumParticles;
import com.skittlq.endernium.util.EnderniumTargeting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class EnderniumArmorAbility {
    private EnderniumArmorAbility() {
    }

    public static void tickPlayers(List<ServerPlayer> players, Settings settings, CooldownStore cooldownStore) {
        for (ServerPlayer player : players) {
            tickPlayer(player, settings, cooldownStore);
        }
    }

    public static void tickPlayer(ServerPlayer player, Settings settings, CooldownStore cooldownStore) {
        if (!settings.enabled() || player.isSpectator() || !EnderniumArmorUtil.hasFullEnderniumSet(player)) {
            return;
        }

        tickWearer(player, settings, cooldownStore);
    }

    public static void tickMobs(Iterable<ServerLevel> levels, Settings settings, CooldownStore cooldownStore) {
        if (!settings.enabled()) {
            return;
        }

        for (ServerLevel level : levels) {
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof Mob mob) {
                    tickMob(mob, settings, cooldownStore);
                }
            }
        }
    }

    public static void tickMob(Mob mob, Settings settings, CooldownStore cooldownStore) {
        if (!settings.enabled() || !mob.isAlive() || !EnderniumArmorUtil.hasFullEnderniumSet(mob)) {
            return;
        }

        tickWearer(mob, settings, cooldownStore);
    }

    private static void tickWearer(LivingEntity wearer, Settings settings, CooldownStore cooldownStore) {
        ServerLevel level = (ServerLevel) wearer.level();
        long currentTime = level.getGameTime();
        long cooldownTicks = 20L * settings.cooldownSeconds();
        long lastUsed = cooldownStore.getLastUsedTick(wearer);
        long elapsedTicks = currentTime - lastUsed;

        float health = wearer.getHealth();
        float maxHealth = wearer.getMaxHealth();
        int maxParticles = 20;

        if (health < maxHealth && elapsedTicks < cooldownTicks) {
            float healthFraction = health / maxHealth;
            int particleCount = Math.round((1.0F - healthFraction) * maxParticles);
            if (particleCount > 0) {
                level.sendParticles(EnderniumParticles.ENDERNIUM_BIT.get(),
                        wearer.getX(), wearer.getY() + 1.5D, wearer.getZ(),
                        particleCount, 0.0D, 0.0D, 0.0D, 20.0D);
            }
        }

        if (health < settings.threshold() && elapsedTicks > cooldownTicks) {
            triggerAbility(wearer, level, cooldownStore, cooldownTicks, currentTime);
        }
    }

    private static void triggerAbility(LivingEntity wearer, ServerLevel level, CooldownStore cooldownStore,
                                       long cooldownTicks, long currentTime) {
        double radius = 8.0D;
        List<? extends LivingEntity> targets = findTargets(wearer, level, radius);

        for (LivingEntity target : targets) {
            Vec3 direction = target.position().subtract(wearer.position());
            if (direction.lengthSqr() < 1.0E-5D) {
                double angle = level.getRandom().nextDouble() * 2.0D * Math.PI;
                direction = new Vec3(Math.cos(angle), 0.0D, Math.sin(angle));
            } else {
                direction = direction.normalize();
            }
            Vec3 pushVec = direction.scale(2.0D);
            target.push(pushVec.x, 1.0D, pushVec.z);
            target.hurtMarked = true;
        }

        wearer.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
        cooldownStore.setLastUsedTick(wearer, currentTime);
        if (wearer instanceof ServerPlayer player) {
            long endGameTime = currentTime + cooldownTicks;
            int cooldownTicksInt = (int) Math.min(Integer.MAX_VALUE, cooldownTicks);
            EnderniumNetworking.sendArmorCooldownSync(player, endGameTime, cooldownTicksInt);
        }

        level.playSound(null, wearer.getX(), wearer.getY(), wearer.getZ(),
                SoundEvents.DRAGON_FIREBALL_EXPLODE, wearer.getSoundSource(), 1.0F, 1.0F);
        level.sendParticles(EnderniumParticles.REVERSE_ENDERNIUM_BIT.get(),
                wearer.getX(), wearer.getY() + 1.5D, wearer.getZ(),
                2000, 0.0D, 0.0D, 0.0D, 1.0D);
    }

    private static List<? extends LivingEntity> findTargets(LivingEntity wearer, ServerLevel level, double radius) {
        return level.getEntitiesOfClass(
                LivingEntity.class,
                wearer.getBoundingBox().inflate(radius),
                target -> wearer instanceof ServerPlayer player
                        ? EnderniumTargeting.isValidPlayerAbilityTarget(player, target)
                        : wearer instanceof Mob mob
                        && EnderniumTargeting.isValidMobArmorTarget(mob, target, level)
        );
    }

    // Resends the persisted cooldown to the client on login, preserving the original duration so the HUD shows true elapsed progress instead of restarting.
    public static void syncCooldownOnLogin(ServerPlayer player, Settings settings, CooldownStore cooldownStore) {
        if (!settings.enabled()) {
            return;
        }

        long cooldownTicks = 20L * settings.cooldownSeconds();
        long currentTime = player.level().getGameTime();
        long endGameTime = cooldownStore.getLastUsedTick(player) + cooldownTicks;
        if (endGameTime > currentTime) {
            int cooldownTicksInt = (int) Math.min(Integer.MAX_VALUE, cooldownTicks);
            EnderniumNetworking.sendArmorCooldownSync(player, endGameTime, cooldownTicksInt);
        }
    }

    public interface Settings {
        boolean enabled();

        int threshold();

        long cooldownSeconds();
    }

    public interface CooldownStore {
        long getLastUsedTick(LivingEntity entity);

        void setLastUsedTick(LivingEntity entity, long tick);
    }
}
