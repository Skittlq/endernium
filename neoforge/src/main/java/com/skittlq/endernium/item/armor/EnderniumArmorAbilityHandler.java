package com.skittlq.endernium.item.armor;

import com.skittlq.endernium.Config;
import com.skittlq.endernium.particles.ModParticles;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class EnderniumArmorAbilityHandler {
    private static final Set<Identifier> EXTRA_AFFECTED_MOBS = new HashSet<>(Set.of(
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
    private static boolean registered;

    private EnderniumArmorAbilityHandler() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        NeoForge.EVENT_BUS.addListener(EnderniumArmorAbilityHandler::onServerTick);
    }

    private static void onServerTick(ServerTickEvent.Post event) {
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            tickPlayer(player);
        }
    }

    private static void tickPlayer(ServerPlayer player) {
        if (!Config.ENDERNIUM_ARMOR_ABILITY.getAsBoolean()
                || player.isSpectator()
                || !EnderniumArmorUtil.hasFullEnderniumSet(player)) {
            return;
        }

        ServerLevel level = (ServerLevel) player.level();
        long currentTime = level.getGameTime();
        long cooldownTicks = 20L * Config.ENDERNIUM_ARMOR_ABILITY_COOLDOWN.getAsLong();
        long lastUsed = player.getPersistentData().getLong("EnderniumArmorCooldown").orElse(0L);
        long elapsedTicks = currentTime - lastUsed;
        syncCooldownIndicator(player, cooldownTicks, elapsedTicks);

        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();
        int maxParticles = 20;

        if (health < maxHealth && elapsedTicks < cooldownTicks) {
            float healthFraction = health / maxHealth;
            int particleCount = Math.round((1.0F - healthFraction) * maxParticles);
            if (particleCount > 0) {
                level.sendParticles(
                        ModParticles.ENDERNIUM_BIT.get(),
                        player.getX(), player.getY() + 1.0D, player.getZ(),
                        particleCount,
                        0.0D, 0.0D, 0.0D,
                        20.0D
                );
            }
        }

        if (health < Config.ENDERNIUM_ARMOR_ABILITY_THRESHOLD.getAsInt() && elapsedTicks > cooldownTicks) {
            triggerAbility(player, level, cooldownTicks, currentTime);
        }
    }

    private static void triggerAbility(ServerPlayer player, ServerLevel level, long cooldownTicks, long currentTime) {
        double radius = 8.0D;
        List<Mob> hostiles = level.getEntitiesOfClass(
                Mob.class,
                player.getBoundingBox().inflate(radius),
                mob -> mob.isAlive()
                        && (mob instanceof Monster
                        || EXTRA_AFFECTED_MOBS.contains(BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType())))
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
        player.getPersistentData().putLong("EnderniumArmorCooldown", currentTime);
        int cooldownTicksInt = (int) Math.min(Integer.MAX_VALUE, cooldownTicks);
        player.getCooldowns().addCooldown(player.getItemBySlot(EquipmentSlot.CHEST), cooldownTicksInt);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.DRAGON_FIREBALL_EXPLODE, player.getSoundSource(), 1.0F, 1.0F);
        level.sendParticles(ModParticles.REVERSE_ENDERNIUM_BIT.get(),
                player.getX(), player.getY() + 1.0D, player.getZ(),
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
}