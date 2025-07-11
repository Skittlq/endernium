package com.skittlq.endernium.item.armor;

import com.skittlq.endernium.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EnderniumHelmet extends ArmorItem {
    public EnderniumHelmet(ArmorMaterial pMaterial, Type pType, Properties pProperties) {
        super(pMaterial, pType, pProperties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide && entity instanceof Player player) {
            if (hasFullSuitOfArmorOn(player) && Config.ENDERNIUM_ARMOR_ABILITY.get()) {
                evaluateArmorEffects(player, (ServerLevel) level);
            }
        }
        super.inventoryTick(stack, level, entity, slot, selected);
    }

    private void evaluateArmorEffects(Player player, ServerLevel level) {
        if (player.isSpectator()) return;

        if (hasPlayerCorrectArmorOn(player)) {
            long currentTime = player.level().getGameTime();
            long abilityLastUsed = player.getPersistentData().getLong("EnderniumArmorCooldown");

            long cooldownTicks = 20 * Config.ENDERNIUM_ARMOR_ABILITY_COOLDOWN.get();
            long elapsedTicks = currentTime - abilityLastUsed;
            float cooldownFraction = Math.min(elapsedTicks / (float) cooldownTicks, 1.0f);

            if (elapsedTicks < cooldownTicks) {
                int totalBars = 20;
                int filledBars = Math.round(cooldownFraction * totalBars);

                StringBuilder bar = new StringBuilder();
                bar.append(ChatFormatting.DARK_PURPLE).append("[");
                for (int i = 0; i < totalBars; i++) {
                    if (i < filledBars) {
                        bar.append(ChatFormatting.LIGHT_PURPLE).append("|");
                    } else {
                        bar.append(ChatFormatting.GRAY).append("|");
                    }
                }
                bar.append(ChatFormatting.DARK_PURPLE).append("] ");
                int percent = Math.round(cooldownFraction * 100f);
                bar.append(ChatFormatting.GRAY).append(percent).append("%");

                player.displayClientMessage(Component.literal(bar.toString()), true);
            }

            float health = player.getHealth();
            float maxHealth = player.getMaxHealth();
            int maxParticles = 20;

            if ((health < maxHealth) && (elapsedTicks < cooldownTicks)) {
                float healthFraction = health / maxHealth;
                int particleCount = Math.round((1.0F - healthFraction) * maxParticles);

                if (particleCount > 0) {
                    level.sendParticles(
                            ParticleTypes.PORTAL,
                            player.getX(), player.getY() + 1.0, player.getZ(),
                            particleCount,
                            0, 0, 0,
                            20
                    );
                }
            }

            if (player.getHealth() < Config.ENDERNIUM_ARMOR_ABILITY_THRESHOLD.get()
                    && (elapsedTicks > cooldownTicks)) {
                double radius = 8.0D;
                var hostiles = level.getEntitiesOfClass(
                        net.minecraft.world.entity.Mob.class,
                        player.getBoundingBox().inflate(radius),
                        mob -> mob.isAlive() && mob instanceof net.minecraft.world.entity.monster.Monster
                );

                for (var mob : hostiles) {
                    var direction = mob.position().subtract(player.position());
                    if (direction.lengthSqr() < 1.0E-5F) {
                        double angle = level.random.nextDouble() * 2 * Math.PI;
                        direction = new Vec3(Math.cos(angle), 0, Math.sin(angle));
                    } else {
                        direction = direction.normalize();
                    }

                    var pushVec = direction.scale(2.0D);
                    mob.push(pushVec.x, 1D, pushVec.z);

                }

                player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.REGENERATION,
                        200,
                        0
                ));

                player.getPersistentData().putLong("EnderniumArmorCooldown", currentTime);

                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.DRAGON_FIREBALL_EXPLODE, player.getSoundSource(), 1.0F, 1.0F);
                level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                        player.getX(), player.getY() + 1.0, player.getZ(),
                        2000, 0.0, 0.0, 0.0, 20.0);

            }
        }
    }

    private boolean hasPlayerCorrectArmorOn(Player player) {
        // Replace these with your actual armor item instances
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        ItemStack leggings = player.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);

        // Compare with your actual armor items
        return !boots.isEmpty() && boots.getItem() instanceof EnderniumBoots &&
                !leggings.isEmpty() && leggings.getItem() instanceof EnderniumLeggings &&
                !chestplate.isEmpty() && chestplate.getItem() instanceof EnderniumChestplate &&
                !helmet.isEmpty() && helmet.getItem() instanceof EnderniumHelmet;
    }

    private boolean hasFullSuitOfArmorOn(Player player) {
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        ItemStack leggings = player.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);

        return !boots.isEmpty() && !leggings.isEmpty() && !chestplate.isEmpty() && !helmet.isEmpty();
    }
}
