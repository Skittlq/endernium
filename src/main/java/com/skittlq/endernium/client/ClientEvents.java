package com.skittlq.endernium.client;

import com.skittlq.endernium.Config;
import com.skittlq.endernium.Endernium;
import com.skittlq.endernium.item.ModItems;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import com.skittlq.endernium.item.tools.EnderniumSword;
import com.skittlq.endernium.particles.ModParticles;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientEvents {
    private static final Identifier ARMOR_COOLDOWN_ICON = Identifier.fromNamespaceAndPath(
            Endernium.MODID,
            "textures/item/endernium_chestplate.png"
    );
    private static final int ARMOR_ICON_SIZE = 16;
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
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        CameraLerpHandler.clientTick(mc);

        EnderniumSword sword = null;
        if (player.getMainHandItem().getItem() instanceof EnderniumSword)
            sword = (EnderniumSword) player.getMainHandItem().getItem();
        else if (player.getOffhandItem().getItem() instanceof EnderniumSword)
            sword = (EnderniumSword) player.getOffhandItem().getItem();

        if (sword == null) return;

        ItemStack stack = player.getMainHandItem().getItem() instanceof EnderniumSword
                ? player.getMainHandItem() : player.getOffhandItem();
        if (player.getCooldowns().isOnCooldown(stack)) return;

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
                double px = bb.minX + mob.level().getRandom().nextDouble() * (bb.maxX - bb.minX);
                double py = bb.minY + mob.level().getRandom().nextDouble() * (bb.maxY - bb.minY);
                double pz = bb.minZ + mob.level().getRandom().nextDouble() * (bb.maxZ - bb.minZ);

                player.level().addParticle(
                        ModParticles.REVERSE_ENDERNIUM_BIT.get(),
                        px, py, pz,
                        0, 0, 0
                );
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || !Config.ENDERNIUM_ARMOR_ABILITY.getAsBoolean()) return;

        ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!isWearingFullEnderniumSet(player) || !chestStack.is(ModItems.ENDERNIUM_CHESTPLATE.get())) return;

        float cooldownRemaining = player.getCooldowns().getCooldownPercent(chestStack, 0.0F);
        if (cooldownRemaining <= 0.0F) return;

        float cooldownProgress = 1.0F - cooldownRemaining;
        int screenWidth = event.getGuiGraphics().guiWidth();
        int screenHeight = event.getGuiGraphics().guiHeight();

        int hotbarLeft = screenWidth / 2 - 91;
        int hotbarTop = screenHeight - 22;
        int x = hotbarLeft - ARMOR_ICON_SIZE - 4;
        int y = hotbarTop + (22 - ARMOR_ICON_SIZE) / 2;

        var gui = event.getGuiGraphics();
        gui.blit(
                RenderPipelines.GUI_TEXTURED,
                ARMOR_COOLDOWN_ICON,
                x,
                y,
                0,
                0,
                ARMOR_ICON_SIZE,
                ARMOR_ICON_SIZE,
                ARMOR_ICON_SIZE,
                ARMOR_ICON_SIZE,
                ARGB.white(0.5F)
        );

        int fillHeight = Math.max(0, Math.min(ARMOR_ICON_SIZE, Math.round(cooldownProgress * ARMOR_ICON_SIZE)));
        if (fillHeight > 0) {
            int sourceY = ARMOR_ICON_SIZE - fillHeight;
            gui.blit(
                    RenderPipelines.GUI_TEXTURED,
                    ARMOR_COOLDOWN_ICON,
                    x,
                    y + sourceY,
                    0,
                    sourceY,
                    ARMOR_ICON_SIZE,
                    fillHeight,
                    ARMOR_ICON_SIZE,
                    ARMOR_ICON_SIZE,
                    -1
            );
        }
    }

    private static boolean isWearingFullEnderniumSet(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.ENDERNIUM_HELMET.get())
                && player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.ENDERNIUM_CHESTPLATE.get())
                && player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.ENDERNIUM_LEGGINGS.get())
                && player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.ENDERNIUM_BOOTS.get());
    }

}
