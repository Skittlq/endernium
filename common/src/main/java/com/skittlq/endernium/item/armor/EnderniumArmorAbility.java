package com.skittlq.endernium.item.armor;

import com.skittlq.endernium.particles.EnderniumParticles;
import com.skittlq.endernium.util.EnderniumTargeting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
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

        ServerLevel level = (ServerLevel) player.level();
        long currentTime = level.getGameTime();
        long cooldownTicks = 20L * settings.cooldownSeconds();
        long lastUsed = cooldownStore.getLastUsedTick(player);
        long elapsedTicks = currentTime - lastUsed;
        syncCooldownIndicator(player, cooldownTicks, elapsedTicks);

        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();
        int maxParticles = 20;

        if (health < maxHealth && elapsedTicks < cooldownTicks) {
            float healthFraction = health / maxHealth;
            int particleCount = Math.round((1.0F - healthFraction) * maxParticles);
            if (particleCount > 0) {
                level.sendParticles(EnderniumParticles.ENDERNIUM_BIT.get(),
                        player.getX(), player.getY() + 1.5D, player.getZ(),
                        particleCount, 0.0D, 0.0D, 0.0D, 20.0D);
            }
        }

        if (health < settings.threshold() && elapsedTicks > cooldownTicks) {
            triggerAbility(player, level, cooldownStore, cooldownTicks, currentTime);
        }
    }

    private static void triggerAbility(ServerPlayer player, ServerLevel level, CooldownStore cooldownStore,
                                       long cooldownTicks, long currentTime) {
        double radius = 8.0D;
        List<Mob> hostiles = level.getEntitiesOfClass(
                Mob.class,
                player.getBoundingBox().inflate(radius),
                mob -> mob.isAlive() && EnderniumTargeting.isHostile(mob)
        );

        for (Mob mob : hostiles) {
            Vec3 direction = mob.position().subtract(player.position());
            if (direction.lengthSqr() < 1.0E-5D) {
                double angle = level.getRandom().nextDouble() * 2.0D * Math.PI;
                direction = new Vec3(Math.cos(angle), 0.0D, Math.sin(angle));
            } else {
                direction = direction.normalize();
            }
            Vec3 pushVec = direction.scale(2.0D);
            mob.push(pushVec.x, 1.0D, pushVec.z);
        }

        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0));
        cooldownStore.setLastUsedTick(player, currentTime);
        int cooldownTicksInt = (int) Math.min(Integer.MAX_VALUE, cooldownTicks);
        player.getCooldowns().addCooldown(player.getItemBySlot(EquipmentSlot.CHEST), cooldownTicksInt);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.DRAGON_FIREBALL_EXPLODE, player.getSoundSource(), 1.0F, 1.0F);
        level.sendParticles(EnderniumParticles.REVERSE_ENDERNIUM_BIT.get(),
                player.getX(), player.getY() + 1.5D, player.getZ(),
                2000, 0.0D, 0.0D, 0.0D, 1.0D);
    }

    private static void syncCooldownIndicator(ServerPlayer player, long cooldownTicks, long elapsedTicks) {
        if (elapsedTicks < 0L || elapsedTicks >= cooldownTicks) {
            return;
        }

        ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chestStack.isEmpty() || player.getCooldowns().isOnCooldown(chestStack)) {
            return;
        }

        long remainingTicks = cooldownTicks - elapsedTicks;
        int remainingTicksInt = (int) Math.min(Integer.MAX_VALUE, remainingTicks);
        if (remainingTicksInt > 0) {
            player.getCooldowns().addCooldown(chestStack, remainingTicksInt);
        }
    }

    public interface Settings {
        boolean enabled();

        int threshold();

        long cooldownSeconds();
    }

    public interface CooldownStore {
        long getLastUsedTick(ServerPlayer player);

        void setLastUsedTick(ServerPlayer player, long tick);
    }
}
