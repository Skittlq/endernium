package com.skittlq.endernium.item.tools;

import com.skittlq.endernium.item.ModToolTiers;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class EnderniumSword extends Item {
    public EnderniumSword(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) {
            // Check if the item is still on cooldown
            if (player.getCooldowns().isOnCooldown(player.getItemInHand(hand))) {
                return InteractionResult.FAIL;
            }

            // Play the sound
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENCHANTMENT_TABLE_USE, player.getSoundSource(), 1.0F, 1.0F);

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.ENCHANT,
                        player.getX(), player.getY() + 2.0, player.getZ(),
                        1000, 0.0, 0.0, 0.0, 10.0);

                int circles = 3;
                double baseRadius = 3.0;
                double radiusStep = 2.0;
                int baseCount = 8;

                for (int r = 0; r < circles; r++) {
                    double radius = baseRadius + (r * radiusStep);
                    int fangCount = (int) Math.ceil(baseCount * (radius / baseRadius));

                    for (int i = 0; i < fangCount; i++) {
                        double angle = 2 * Math.PI * i / fangCount;
                        double x = player.getX() + radius * Math.cos(angle);
                        double z = player.getZ() + radius * Math.sin(angle);

                        // Start at player Y and search downward
                        double startY = player.getY() + 1.0;
                        double minY = level.getMinY(); // Just in case

                        double y = startY;
                        while (y > minY) {
                            if (!level.getBlockState(new BlockPos((int) x, (int) (y - 1), (int) z)).isAir()
                                    && level.getBlockState(new BlockPos((int) x, (int) y, (int) z)).isAir()) {
                                break; // Found the floor
                            }
                            y -= 1.0;
                        }

                        // Only spawn if valid floor was found
                        if (y > minY) {
                            EvokerFangs fang = new EvokerFangs(serverLevel, x, y, z, (float) angle, 0, player);
                            serverLevel.addFreshEntity(fang);
                        }
                    }
                }
            }

            // Set a cooldown (e.g., 40 ticks = 2 seconds)
            player.getCooldowns().addCooldown(player.getItemInHand(hand), 600);
        }

        return InteractionResult.SUCCESS;
    }


}
