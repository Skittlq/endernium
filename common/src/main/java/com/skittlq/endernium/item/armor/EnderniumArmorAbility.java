package com.skittlq.endernium.item.armor;

import com.skittlq.endernium.network.EnderniumNetworking;
import com.skittlq.endernium.particles.EnderniumParticles;
import com.skittlq.endernium.progression.EnderniumBlessing;
import com.skittlq.endernium.util.EnderniumTargeting;
import com.skittlq.endernium.util.EnderniumAbilityMath;
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
import java.util.Objects;

public final class EnderniumArmorAbility {
    private static Settings boundSettings;
    private static ChargeStore boundChargeStore;

    private EnderniumArmorAbility() {
    }

    public static void tickPlayers(List<ServerPlayer> players, Settings settings, ChargeStore chargeStore) {
        for (ServerPlayer player : players) {
            tickPlayer(player, settings, chargeStore);
        }
    }

    public static void tickPlayer(ServerPlayer player, Settings settings, ChargeStore chargeStore) {
        if (!settings.enabled()
                || player.isSpectator()
                || !EnderniumBlessing.isBlessed(player)
                || !EnderniumArmorUtil.hasFullEnderniumSet(player)) {
            clearCharge(player, chargeStore);
            return;
        }

        tickWearer(player, settings, chargeStore);
    }

    public static void tickMob(Mob mob, Settings settings, ChargeStore chargeStore) {
        if (!settings.enabled() || !mob.isAlive() || !EnderniumArmorUtil.hasFullEnderniumSet(mob)) {
            clearCharge(mob, chargeStore);
            return;
        }

        tickWearer(mob, settings, chargeStore);
    }

    public static void bind(Settings settings, ChargeStore chargeStore) {
        boundSettings = Objects.requireNonNull(settings);
        boundChargeStore = Objects.requireNonNull(chargeStore);
    }

    public static void tickEquippedMob(Entity entity) {
        if (entity instanceof Mob mob && boundSettings != null && boundChargeStore != null) {
            tickMob(mob, boundSettings, boundChargeStore);
        }
    }

    public static void recordQualifyingDamage(LivingEntity wearer, float inflictedDamage) {
        if (boundSettings == null || boundChargeStore == null) {
            return;
        }
        if (!boundSettings.enabled() || !EnderniumArmorUtil.hasFullEnderniumSet(wearer)
                || wearer instanceof ServerPlayer player && !EnderniumBlessing.isBlessed(player)) {
            clearCharge(wearer, boundChargeStore);
            return;
        }
        if (inflictedDamage <= 0.0F) {
            return;
        }
        float capacity = (float) boundSettings.maxStoredDamage();
        float next = EnderniumAbilityMath.accumulateArmorCharge(
                normalizedCharge(wearer, boundChargeStore, capacity), inflictedDamage, capacity);
        setCharge(wearer, boundChargeStore, next);
        if (wearer.level() instanceof ServerLevel level) {
            int particles = Math.max(1, Math.round(4.0F + 20.0F * (next / capacity)));
            level.sendParticles(EnderniumParticles.ENDERNIUM_BIT.get(),
                    wearer.getX(), wearer.getY() + 1.0D, wearer.getZ(),
                    particles, 0.35D, 0.6D, 0.35D, 0.06D);
        }
    }

    public static void clearCharge(LivingEntity wearer) {
        if (boundChargeStore != null) {
            clearCharge(wearer, boundChargeStore);
        }
    }

    public static void syncCharge(ServerPlayer player, Settings settings, ChargeStore chargeStore) {
        float capacity = (float) settings.maxStoredDamage();
        EnderniumNetworking.sendArmorChargeSync(player, normalizedCharge(player, chargeStore, capacity));
    }

    private static void tickWearer(LivingEntity wearer, Settings settings, ChargeStore chargeStore) {
        ServerLevel level = (ServerLevel) wearer.level();
        float capacity = (float) settings.maxStoredDamage();
        float storedDamage = normalizedCharge(wearer, chargeStore, capacity);
        float health = wearer.getHealth();
        if (storedDamage > 0.0F && level.getGameTime() % 10L == 0L) {
            float strength = storedDamage / capacity;
            level.sendParticles(EnderniumParticles.ENDERNIUM_BIT.get(),
                    wearer.getX(), wearer.getY() + 1.25D, wearer.getZ(),
                    Math.max(1, Math.round(4.0F * strength)),
                    0.25D, 0.5D, 0.25D, 0.02D);
        }

        if (health < settings.threshold() && storedDamage > 0.0F) {
            setCharge(wearer, chargeStore, 0.0F);
            triggerAbility(wearer, level, storedDamage / capacity);
        }
    }

    private static void triggerAbility(LivingEntity wearer, ServerLevel level, float strength) {
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
            Vec3 pushVec = direction.scale(2.0D * strength);
            target.push(pushVec.x, strength, pushVec.z);
            target.push(0.0D, 0.0D, 0.0D);
        }

        int regenerationTicks = Math.round(200.0F * strength);
        if (regenerationTicks > 0) {
            wearer.addEffect(new MobEffectInstance(MobEffects.REGENERATION, regenerationTicks, 1));
        }

        level.playSound(null, wearer.getX(), wearer.getY(), wearer.getZ(),
                SoundEvents.DRAGON_FIREBALL_EXPLODE, wearer.getSoundSource(), strength, 1.0F);
        level.sendParticles(EnderniumParticles.REVERSE_ENDERNIUM_BIT.get(),
                wearer.getX(), wearer.getY() + 1.5D, wearer.getZ(),
                Math.max(1, Math.round(256.0F * strength)), 0.0D, 0.0D, 0.0D, strength);
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

    public static float strength(float storedDamage, float capacity) {
        return EnderniumAbilityMath.armorStrength(storedDamage, capacity);
    }

    private static float normalizedCharge(LivingEntity wearer, ChargeStore chargeStore, float capacity) {
        float stored = chargeStore.getStoredDamage(wearer);
        if (!Float.isFinite(stored)) {
            return 0.0F;
        }
        return Math.max(0.0F, Math.min(capacity, stored));
    }

    private static void clearCharge(LivingEntity wearer, ChargeStore chargeStore) {
        if (chargeStore.getStoredDamage(wearer) != 0.0F) {
            setCharge(wearer, chargeStore, 0.0F);
        }
    }

    private static void setCharge(LivingEntity wearer, ChargeStore chargeStore, float storedDamage) {
        chargeStore.setStoredDamage(wearer, storedDamage);
        if (wearer instanceof ServerPlayer player) {
            EnderniumNetworking.sendArmorChargeSync(player, storedDamage);
        }
    }

    public interface Settings {
        boolean enabled();

        int threshold();

        double maxStoredDamage();
    }

    public interface ChargeStore {
        float getStoredDamage(LivingEntity entity);

        void setStoredDamage(LivingEntity entity, float storedDamage);
    }
}
