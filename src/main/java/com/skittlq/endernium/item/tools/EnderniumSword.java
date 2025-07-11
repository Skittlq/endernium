package com.skittlq.endernium.item.tools;

import com.skittlq.endernium.util.EnderniumTickScheduler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

public class EnderniumSword extends SwordItem {
    public EnderniumSword(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        InteractionHand hand = context.getHand();
        if (level.isClientSide || player == null) return InteractionResult.SUCCESS;

        boolean activated = activateSpecial(level, player, hand);
        return activated ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) {
            boolean activated = activateSpecial(level, player, hand);
            if (activated) {
                return InteractionResultHolder.success(player.getItemInHand(hand));
            }
        }
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (!player.level().isClientSide) {
            boolean activated = activateSpecial(player.level(), player, hand);
            if (activated) {
                return InteractionResult.SUCCESS;
            }
        }
        return super.interactLivingEntity(stack, player, target, hand);
    }

    /**
     * Returns true if the special ability was triggered, false if on cooldown.
     */
    private boolean activateSpecial(Level level, Player player, InteractionHand hand) {
        if (player.getCooldowns().isOnCooldown(player.getItemInHand(hand).getItem())) {
            return false;
        }

        double range = 10.0D;
        double arc = Math.PI / 1.5; // 120 degrees

        Vec3 lookVec = player.getLookAngle();
        Vec3 playerPos = player.position().add(0, player.getEyeHeight(), 0);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENCHANTMENT_TABLE_USE, player.getSoundSource(), 0.6F, 1F);

        List<Monster> monsters = level.getEntitiesOfClass(
                Monster.class,
                new AABB(
                        playerPos.x - range, playerPos.y - 2, playerPos.z - range,
                        playerPos.x + range, playerPos.y + 2, playerPos.z + range
                ),
                mob -> {
                    Vec3 toMob = mob.position().add(0, mob.getBbHeight() / 2, 0).subtract(playerPos);
                    double distance = toMob.length();
                    if (distance > range) return false;
                    double angle = lookVec.normalize().dot(toMob.normalize());
                    double theta = Math.acos(angle);
                    return theta < (arc / 2);
                }
        );

        List<Monster> sortedMonsters = monsters.stream()
                .sorted(Comparator.comparingDouble(
                        mob -> {
                            Vec3 toMob = mob.position().add(0, mob.getBbHeight() / 2, 0).subtract(playerPos);
                            double angle = lookVec.normalize().dot(toMob.normalize());
                            double theta = Math.acos(angle);
                            double dist = toMob.length();
                            return theta * 2 + dist / range;
                        }
                ))
                .toList();

        int ticksBetweenHits = 2;

        for (int i = 0; i < sortedMonsters.size(); i++) {
            Monster mob = sortedMonsters.get(i);
            int delay = i * ticksBetweenHits;

            EnderniumTickScheduler.schedule(() -> {
                if (!mob.isAlive() || mob.distanceTo(player) > range + 1.0) return;

                Vec3 mobCenter = mob.position().add(0, mob.getBbHeight() / 2, 0);
                Vec3 playerFeet = player.position();
                Vec3 dashVec = mobCenter.subtract(playerFeet);

                dashVec = new Vec3(dashVec.x, 0, dashVec.z);
                if (dashVec.lengthSqr() > 0.01) {
                    dashVec = dashVec.normalize().scale(0.4);
                    player.setDeltaMovement(dashVec.x, dashVec.y, dashVec.z);
                    player.hurtMarked = true;
                }

                Vec3 toMob = mobCenter.subtract(playerPos);
                double distance = toMob.length();
                double angleDot = lookVec.normalize().dot(toMob.normalize());
                double theta = Math.acos(angleDot);

                double angleMultiplier = 1.0 - (theta / (arc / 2));
                double distanceMultiplier = 1.0 - (distance / range);
                double baseDamage = 40.0;

                double damage = baseDamage * Math.max(0, angleMultiplier) * Math.max(0, distanceMultiplier);

                if (damage > 1.0) {
                    mob.hurt(player.damageSources().playerAttack(player), (float) damage);
                    player.swing(hand, true);
                    if (level instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(
                                ParticleTypes.SWEEP_ATTACK,
                                mob.getX(), mob.getY() + mob.getBbHeight() / 2, mob.getZ(),
                                1, 0, 0, 0, 0
                        );
                        serverLevel.playSound(null, mob.getX(), mob.getY(), mob.getZ(),
                                SoundEvents.PLAYER_ATTACK_SWEEP, player.getSoundSource(), 1.0F, 1.0F);
                    }
                }
            }, delay);
        }

        player.getCooldowns().addCooldown(player.getItemInHand(hand).getItem(), 1200);
        return true;
    }
}
