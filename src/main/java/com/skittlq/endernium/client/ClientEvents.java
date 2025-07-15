package com.skittlq.endernium.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import com.skittlq.endernium.item.tools.EnderniumSword;
import com.skittlq.endernium.particles.ModParticles;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ClientEvents {
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

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (event.phase != TickEvent.Phase.END || player == null) return;

        CameraLerpHandler.clientTick(mc);

        EnderniumSword sword = null;
        if (player.getMainHandItem().getItem() instanceof EnderniumSword)
            sword = (EnderniumSword) player.getMainHandItem().getItem();
        else if (player.getOffhandItem().getItem() instanceof EnderniumSword)
            sword = (EnderniumSword) player.getOffhandItem().getItem();

        if (sword == null) return;

        ItemStack stack = player.getMainHandItem().getItem() instanceof EnderniumSword
                ? player.getMainHandItem() : player.getOffhandItem();
        if (player.getCooldowns().isOnCooldown(stack.getItem())) return;

        double range = 10.0D;
        double arc = Math.PI / 1.5;

        Vec3 lookVec = player.getLookAngle();
        Vec3 playerPos = player.position().add(0, player.getEyeHeight(), 0);

        List<Mob> targets = player.level().getEntitiesOfClass(
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
            int count = 4;
            AABB bb = mob.getBoundingBox();
            for (int i = 0; i < count; i++) {
                double px = bb.minX + mob.level().random.nextDouble() * (bb.maxX - bb.minX);
                double py = bb.minY + mob.level().random.nextDouble() * (bb.maxY - bb.minY);
                double pz = bb.minZ + mob.level().random.nextDouble() * (bb.maxZ - bb.minZ);

                player.level().addParticle(
                        ModParticles.REVERSE_ENDERNIUM_BIT.get(),
                        px, py, pz,
                        0, 0, 0
                );
            }
        }
    }

}
