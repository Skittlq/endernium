package com.skittlq.endernium.item.tools;

import com.skittlq.endernium.network.ModNetworking;
import com.skittlq.endernium.network.payloads.CameraLerpPayload;
import com.skittlq.endernium.particles.ModParticles;
import com.skittlq.endernium.util.EnderniumTickScheduler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class EnderniumSword extends SwordItem {
    private static final Map<UUID, List<Integer>> activeTasks = new HashMap<>();
    private static final Map<UUID, AtomicInteger> mobsHitMap = new HashMap<>();

    private static final Set<EntityType<?>> EXTRA_HOSTILES = new HashSet<>(Set.of(
            EntityType.PHANTOM,
            EntityType.SHULKER,
            EntityType.VEX,
            EntityType.ENDER_DRAGON,
            EntityType.WITHER,
            EntityType.WARDEN,
            EntityType.ELDER_GUARDIAN,
            EntityType.GHAST,
            EntityType.PIGLIN,
            EntityType.PIGLIN_BRUTE,
            EntityType.SLIME,
            EntityType.MAGMA_CUBE
    ));

    public EnderniumSword(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);

        if (!level.isClientSide) return;
        if (!(entity instanceof Player player)) return;
        if (!player.getMainHandItem().is(this) && !player.getOffhandItem().is(this)) return;

    }

    private void showTargetParticles(Level level, Player player) {
        double range = 10.0D;
        double arc = Math.PI / 1.5;

        Vec3 lookVec = player.getLookAngle();
        Vec3 playerPos = player.position().add(0, player.getEyeHeight(), 0);

        List<Mob> targets = level.getEntitiesOfClass(
                Mob.class,
                new AABB(
                        playerPos.x - range, playerPos.y - 2, playerPos.z - range,
                        playerPos.x + range, playerPos.y + 2, playerPos.z + range
                ),
                mob -> {
                    if (!(mob instanceof Monster) && !EXTRA_HOSTILES.contains(mob.getType())) {
                        return false;
                    }
                    Vec3 toMob = mob.position().add(0, mob.getBbHeight() / 2, 0).subtract(playerPos);
                    double distance = toMob.length();
                    if (distance > range) return false;
                    double angle = lookVec.normalize().dot(toMob.normalize());
                    double theta = Math.acos(angle);
                    return theta < (arc / 2);
                }
        );

        for (Mob mob : targets) {
            double x = mob.getX();
            double y = mob.getY() + mob.getBbHeight() / 2.0;
            double z = mob.getZ();

            level.addParticle(
                    ModParticles.ENDERNIUM_SWEEP.get(),
                    x, y, z,
                    0, 0, 0
            );
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) return InteractionResultHolder.success(stack);

        UUID uuid = player.getUUID();

        if (activeTasks.containsKey(uuid) && !activeTasks.get(uuid).isEmpty()) {
            for (int taskId : activeTasks.get(uuid)) {
                EnderniumTickScheduler.cancel(taskId);
            }
            activeTasks.get(uuid).clear();

            int mobsHit = mobsHitMap.getOrDefault(uuid, new AtomicInteger(0)).get();
            int baseCooldown = 100;
            int perMobCooldown = 100;
            int cooldown = baseCooldown + perMobCooldown * mobsHit;
            player.getCooldowns().addCooldown(player.getItemInHand(hand).getItem(), cooldown);

            mobsHitMap.remove(uuid);

            return InteractionResultHolder.success(stack);
        }

        double range = 10.0D;
        double arc = Math.PI / 1.5;

        Vec3 lookVec = player.getLookAngle();
        Vec3 playerPos = player.position().add(0, player.getEyeHeight(), 0);

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENCHANTMENT_TABLE_USE, player.getSoundSource(), 0.6F, 1F);

        List<Mob> targets = level.getEntitiesOfClass(
                Mob.class,
                new AABB(
                        playerPos.x - range, playerPos.y - 2, playerPos.z - range,
                        playerPos.x + range, playerPos.y + 2, playerPos.z + range
                ),
                mob -> {
                    if (!(mob instanceof Monster) && !EXTRA_HOSTILES.contains(mob.getType())) {
                        return false;
                    }
                    Vec3 toMob = mob.position().add(0, mob.getBbHeight() / 2, 0).subtract(playerPos);
                    double distance = toMob.length();
                    if (distance > range) return false;
                    double angle = lookVec.normalize().dot(toMob.normalize());
                    double theta = Math.acos(angle);
                    return theta < (arc / 2);
                }
        );

        List<Mob> sortedTargets = targets.stream()
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
        int ticksBetweenHits = 3;

        List<Integer> taskIds = new ArrayList<>();
        AtomicInteger mobsHit = new AtomicInteger(0);
        activeTasks.put(uuid, taskIds);
        mobsHitMap.put(uuid, mobsHit);

        for (int i = 0; i < sortedTargets.size(); i++) {
            Mob mob = sortedTargets.get(i);
            int delay = i * ticksBetweenHits;

            int taskId = EnderniumTickScheduler.schedule(() -> {
                if (!mob.isAlive() || mob.distanceTo(player) > range + 1.0) return;

                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                            ParticleTypes.PORTAL,
                            player.getX(), player.getY() + 1, player.getZ(),
                            32, 0.5, 1, 0.5, 0.2
                    );
                    serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ENDERMAN_TELEPORT, player.getSoundSource(), 1.0F, 1.0F);
                }

                Vec3 mobCenter = mob.position().add(0, mob.getBbHeight() / 2, 0);
                Vec3 toMob = mobCenter.subtract(player.position());
                Vec3 teleportOffset = toMob.normalize().scale(Math.min(1.5, Math.max(1.0, mob.getBbWidth() + 1.0)));
                Vec3 teleportPos = new Vec3(
                        mobCenter.x - teleportOffset.x,
                        mob.position().y,
                        mobCenter.z - teleportOffset.z
                );

                if (!level.noCollision(player, player.getBoundingBox().move(teleportPos.subtract(player.position())))) {
                    return;
                }

                player.teleportTo(teleportPos.x, teleportPos.y, teleportPos.z);
                player.fallDistance = 0.0F;

                double dx = mob.getX() - player.getX();
                double dz = mob.getZ() - player.getZ();
                double dy = (mob.getY() + mob.getBbHeight() / 2.0) - (player.getY() + player.getEyeHeight());
                float yaw = (float) (Math.toDegrees(Math.atan2(-dx, dz)));
                float pitch = (float) (Math.toDegrees(-Math.atan2(dy, Math.sqrt(dx * dx + dz * dz))));

                if (player instanceof ServerPlayer serverPlayer) {
                    int lerpDuration = 0;
                    ModNetworking.CHANNEL.send(
                            PacketDistributor.PLAYER.with(() -> serverPlayer),
                            new CameraLerpPayload(yaw, pitch, lerpDuration)
                    );
                }


                if (level instanceof ServerLevel serverLevel2) {
                    serverLevel2.sendParticles(
                            ParticleTypes.PORTAL,
                            teleportPos.x, teleportPos.y + 1, teleportPos.z,
                            32, 0.5, 1, 0.5, 0.2
                    );
                    serverLevel2.playSound(null, teleportPos.x, teleportPos.y, teleportPos.z,
                            SoundEvents.ENDERMAN_TELEPORT, player.getSoundSource(), 1.0F, 0.85F + 0.3F * level.random.nextFloat());
                }

                player.setDeltaMovement(player.getDeltaMovement().x, 0.42D, player.getDeltaMovement().z);
                player.hurtMarked = true;

                double baseAttack = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE).getValue();
                double totalDamage = baseAttack * 3.0;
                mob.hurt(player.damageSources().playerAttack(player), (float) totalDamage);
                player.swing(hand, true);

                mobsHit.incrementAndGet();

                if (level instanceof ServerLevel serverLevel3) {
                    serverLevel3.sendParticles(
                            ModParticles.ENDERNIUM_SWEEP.get(),
                            mob.getX(), mob.getY() + mob.getBbHeight() / 2, mob.getZ(),
                            1, 0, 0, 0, 0
                    );
                    serverLevel3.playSound(null, mob.getX(), mob.getY(), mob.getZ(),
                            SoundEvents.PLAYER_ATTACK_SWEEP, player.getSoundSource(), 1.0F, 1.0F);
                }
            }, delay);
            taskIds.add(taskId);
        }

        int totalDuration = sortedTargets.size() * ticksBetweenHits + 5;
        int endTaskId = EnderniumTickScheduler.schedule(() -> {
            int baseCooldown = 200;
            int perMobCooldown = 100;
            int cooldown = baseCooldown + perMobCooldown * mobsHit.get();
            player.getCooldowns().addCooldown(player.getItemInHand(hand).getItem(), cooldown);
//            player.getCooldowns().addCooldown(player.getItemInHand(hand), 1);

            activeTasks.remove(uuid);
            mobsHitMap.remove(uuid);

        }, totalDuration);
        taskIds.add(endTaskId);

        return InteractionResultHolder.success(stack);
    }
}
