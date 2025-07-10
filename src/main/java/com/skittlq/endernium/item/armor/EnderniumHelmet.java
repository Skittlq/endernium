package com.skittlq.endernium.item.armor;

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
        if(entity instanceof Player player && hasFullSuitOfArmorOn(player)) {
            evaluateArmorEffects(player, stack, level, entity);
        }
    }

    private void evaluateArmorEffects(Player player, ItemStack stack, ServerLevel level, Entity entity) {
        ArmorMaterial mapArmorMaterial = ModArmorMaterial.ENDERNIUM_ARMOR_MATERIAL;

        if(hasPlayerCorrectArmorOn(mapArmorMaterial, player)) {
            long currentTime = player.level().getGameTime();
            long lastMsgTime = player.getPersistentData().getLong("EnderniumArmorCooldown").orElse(0L);

            float health = player.getHealth();
            float maxHealth = player.getMaxHealth();

            int maxParticles = 20;

            if (health < maxHealth) {
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

            if (player.getHealth() < 4.0F && (currentTime - lastMsgTime > 20 * 120)) {
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
                level.sendParticles(ParticleTypes.REVERSE_PORTAL,
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
