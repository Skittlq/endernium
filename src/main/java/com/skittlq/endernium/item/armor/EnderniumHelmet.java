package com.skittlq.endernium.item.armor;

import com.skittlq.endernium.Config;
import com.skittlq.endernium.particles.ModParticles;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class EnderniumHelmet extends Item {
    public EnderniumHelmet(Properties properties) {
        super(properties.humanoidArmor(ModArmorMaterial.ENDERNIUM_ARMOR_MATERIAL, ArmorType.HELMET).fireResistant());
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if(entity instanceof Player player && hasFullSuitOfArmorOn(player) && (Config.ENDERNIUM_ARMOR_ABILITY.getAsBoolean())) {
            evaluateArmorEffects(player, level);
        }
    }

    private void evaluateArmorEffects(Player player, ServerLevel level) {
        ArmorMaterial mapArmorMaterial = ModArmorMaterial.ENDERNIUM_ARMOR_MATERIAL;

        if (player.isSpectator()) return;

        if (hasPlayerCorrectArmorOn(mapArmorMaterial, player)) {
            long currentTime = player.level().getGameTime();
            long abilityLastUsed = player.getPersistentData().getLong("EnderniumArmorCooldown").orElse(0L);

            long cooldownTicks = 20 * Config.ENDERNIUM_ARMOR_ABILITY_COOLDOWN.getAsLong();
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
                            ModParticles.ENDERNIUM_BIT.get(),
                            player.getX(), player.getY() + 1.0, player.getZ(),
                            particleCount,
                            0, 0, 0,
                            20
                    );
                }
            }

            if (player.getHealth() < Config.ENDERNIUM_ARMOR_ABILITY_THRESHOLD.getAsInt()
                    && (elapsedTicks > cooldownTicks)) {
                // Push all mobs of monster class away
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
                level.sendParticles(ModParticles.REVERSE_ENDERNIUM_BIT.get(),
                        player.getX(), player.getY() + 1.0, player.getZ(),
                        2000, 0.0, 0.0, 0.0, 20.0);

            }
        }
    }

    private boolean hasPlayerCorrectArmorOn(ArmorMaterial mapArmorMaterial, Player player) {
        Equippable equippableComponentBoots = player.getItemBySlot(EquipmentSlot.FEET).getComponents().get(DataComponents.EQUIPPABLE);
        Equippable equippableComponentLeggings = player.getItemBySlot(EquipmentSlot.LEGS).getComponents().get(DataComponents.EQUIPPABLE);
        Equippable equippableComponentBreastplate = player.getItemBySlot(EquipmentSlot.CHEST).getComponents().get(DataComponents.EQUIPPABLE);
        Equippable equippableComponentHelmet = player.getItemBySlot(EquipmentSlot.HEAD).getComponents().get(DataComponents.EQUIPPABLE);

        return equippableComponentBoots.assetId().get().equals(mapArmorMaterial.assetId()) &&
                equippableComponentLeggings.assetId().get().equals(mapArmorMaterial.assetId()) &&
                equippableComponentBreastplate.assetId().get().equals(mapArmorMaterial.assetId()) &&
                equippableComponentHelmet.assetId().get().equals(mapArmorMaterial.assetId());
    }

    private boolean hasFullSuitOfArmorOn(Player player) {
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        ItemStack leggings = player.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);

        return !boots.isEmpty() && !leggings.isEmpty() && !chestplate.isEmpty() && !helmet.isEmpty();
    }
}
