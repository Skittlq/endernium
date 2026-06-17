package com.skittlq.endernium.client;

import com.skittlq.endernium.Endernium;
import com.skittlq.endernium.config.EnderniumConfigManager;
import com.skittlq.endernium.item.ModItems;
import com.skittlq.endernium.item.tools.EnderniumSword;
import com.skittlq.endernium.particles.ModParticles;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ClientEvents {
    private static final Identifier ARMOR_COOLDOWN_ICON = Identifier.fromNamespaceAndPath(
            Endernium.MOD_ID,
            "textures/item/endernium_chestplate.png"
    );
    private static final Identifier ARMOR_COOLDOWN_HUD = Identifier.fromNamespaceAndPath(Endernium.MOD_ID, "armor_cooldown_hud");
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
    private static boolean registered;

    private ClientEvents() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        ClientTickEvents.END_CLIENT_TICK.register(CameraLerpHandler::clientTick);
        ClientTickEvents.END_CLIENT_TICK.register(ClientEvents::onClientTick);
        HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR, ARMOR_COOLDOWN_HUD, ClientEvents::renderArmorCooldownHud);
    }

    private static void onClientTick(Minecraft client) {
        Player player = client.player;
        if (player == null) {
            return;
        }

        EnderniumSword sword = null;
        if (player.getMainHandItem().getItem() instanceof EnderniumSword mainHandSword) {
            sword = mainHandSword;
        } else if (player.getOffhandItem().getItem() instanceof EnderniumSword offHandSword) {
            sword = offHandSword;
        }

        if (sword == null) {
            return;
        }

        ItemStack stack = player.getMainHandItem().getItem() instanceof EnderniumSword
                ? player.getMainHandItem()
                : player.getOffhandItem();
        if (player.getCooldowns().isOnCooldown(stack)) {
            return;
        }

        double range = 10.0D;
        double arc = Math.PI / 1.5D;
        Vec3 lookVec = player.getLookAngle();
        Vec3 playerPos = player.position().add(0.0D, player.getEyeHeight(), 0.0D);

        List<Mob> targets = player.level().getEntitiesOfClass(
                Mob.class,
                new AABB(
                        playerPos.x - range, playerPos.y - 2.0D, playerPos.z - range,
                        playerPos.x + range, playerPos.y + 2.0D, playerPos.z + range
                ),
                mob -> {
                    if (!(mob instanceof Monster) && !EXTRA_HOSTILES.contains(mob.getType())) {
                        return false;
                    }
                    Vec3 toMob = mob.position().add(0.0D, mob.getBbHeight() / 2.0D, 0.0D).subtract(playerPos);
                    double distance = toMob.length();
                    if (distance > range) {
                        return false;
                    }
                    double angle = lookVec.normalize().dot(toMob.normalize());
                    double theta = Math.acos(angle);
                    return theta < (arc / 2.0D);
                }
        );

        for (Mob mob : targets) {
            AABB box = mob.getBoundingBox();
            for (int i = 0; i < 4; i++) {
                double px = box.minX + mob.level().getRandom().nextDouble() * (box.maxX - box.minX);
                double py = box.minY + mob.level().getRandom().nextDouble() * (box.maxY - box.minY);
                double pz = box.minZ + mob.level().getRandom().nextDouble() * (box.maxZ - box.minZ);
                player.level().addParticle(ModParticles.REVERSE_ENDERNIUM_BIT, px, py, pz, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    private static void renderArmorCooldownHud(GuiGraphicsExtractor gui, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        Player player = client.player;
        if (player == null || !EnderniumConfigManager.getConfig().enderniumArmorAbility) {
            return;
        }

        ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!isWearingFullEnderniumSet(player) || !chestStack.is(ModItems.ENDERNIUM_CHESTPLATE)) {
            return;
        }

        float cooldownRemaining = player.getCooldowns().getCooldownPercent(chestStack, 0.0F);
        if (cooldownRemaining <= 0.0F) {
            return;
        }

        float cooldownProgress = 1.0F - cooldownRemaining;
        int screenWidth = gui.guiWidth();
        int screenHeight = gui.guiHeight();
        int hotbarLeft = screenWidth / 2 - 91;
        int hotbarTop = screenHeight - 22;
        int x = hotbarLeft - ARMOR_ICON_SIZE - 4;
        int y = hotbarTop + (22 - ARMOR_ICON_SIZE) / 2;

        gui.fill(x, y, x + ARMOR_ICON_SIZE, y + ARMOR_ICON_SIZE, 0x26000000);

        int fillHeight = Math.max(0, Math.min(ARMOR_ICON_SIZE, Math.round(cooldownProgress * ARMOR_ICON_SIZE)));
        if (fillHeight > 0) {
            int sourceY = ARMOR_ICON_SIZE - fillHeight;
            gui.blit(RenderPipelines.GUI_TEXTURED,
                    ARMOR_COOLDOWN_ICON,
                    x,
                    y + sourceY,
                    0.0F,
                    sourceY,
                    ARMOR_ICON_SIZE,
                    fillHeight,
                    ARMOR_ICON_SIZE,
                    ARMOR_ICON_SIZE);
        }
    }

    private static boolean isWearingFullEnderniumSet(Player player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.ENDERNIUM_HELMET)
                && player.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.ENDERNIUM_CHESTPLATE)
                && player.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.ENDERNIUM_LEGGINGS)
                && player.getItemBySlot(EquipmentSlot.FEET).is(ModItems.ENDERNIUM_BOOTS);
    }
}

