package com.skittlq.endernium.client;

import com.skittlq.endernium.EnderniumConstants;
import com.skittlq.endernium.item.EnderniumItems;
import com.skittlq.endernium.item.armor.EnderniumArmorUtil;
import com.skittlq.endernium.item.tools.EnderniumSword;
import com.skittlq.endernium.particles.EnderniumParticles;
import com.skittlq.endernium.util.EnderniumTargeting;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public final class EnderniumClientBehavior {
    public static final Identifier ARMOR_COOLDOWN_ICON = Identifier.fromNamespaceAndPath(
            EnderniumConstants.MOD_ID,
            "textures/item/endernium_chestplate.png"
    );
    public static final int ARMOR_ICON_SIZE = 16;
    public static final int ARMOR_COOLDOWN_BACKGROUND_COLOR = 0x59FFFFFF;
    public static final int ARMOR_COOLDOWN_FOREGROUND_COLOR = -1;

    private EnderniumClientBehavior() {
    }

    public static void tickClient(Minecraft client) {
        CameraLerpHandler.clientTick(client);
        renderSwordPreviewParticles(client.player);
    }

    public static boolean shouldRenderArmorCooldown(Player player, boolean armorAbilityEnabled) {
        if (player == null || !armorAbilityEnabled) {
            return false;
        }

        ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
        return EnderniumArmorUtil.hasFullEnderniumSet(player) && chestStack.is(EnderniumItems.ENDERNIUM_CHESTPLATE.get());
    }

    public static float armorCooldownProgress(Player player, ItemStack chestStack) {
        return 1.0F - player.getCooldowns().getCooldownPercent(chestStack, 0.0F);
    }

    public static HudIconPosition armorCooldownPosition(int screenWidth, int screenHeight) {
        int hotbarLeft = screenWidth / 2 - 91;
        int hotbarTop = screenHeight - 22;
        int x = hotbarLeft - ARMOR_ICON_SIZE - 4;
        int y = hotbarTop + (22 - ARMOR_ICON_SIZE) / 2;
        return new HudIconPosition(x, y);
    }

    public static int cooldownFillHeight(float cooldownProgress) {
        return Math.max(0, Math.min(ARMOR_ICON_SIZE, Math.round(cooldownProgress * ARMOR_ICON_SIZE)));
    }

    private static void renderSwordPreviewParticles(Player player) {
        if (player == null) {
            return;
        }

        if (!(player.getMainHandItem().getItem() instanceof EnderniumSword)
                && !(player.getOffhandItem().getItem() instanceof EnderniumSword)) {
            return;
        }

        ItemStack stack = player.getMainHandItem().getItem() instanceof EnderniumSword
                ? player.getMainHandItem()
                : player.getOffhandItem();
        if (player.getCooldowns().isOnCooldown(stack)) {
            return;
        }

        for (Mob mob : EnderniumTargeting.findSwordTargets(player)) {
            AABB box = mob.getBoundingBox();
            for (int i = 0; i < 4; i++) {
                double px = box.minX + mob.level().getRandom().nextDouble() * (box.maxX - box.minX);
                double py = box.minY + mob.level().getRandom().nextDouble() * (box.maxY - box.minY);
                double pz = box.minZ + mob.level().getRandom().nextDouble() * (box.maxZ - box.minZ);
                player.level().addParticle(EnderniumParticles.REVERSE_ENDERNIUM_BIT.get(), px, py, pz, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    public record HudIconPosition(int x, int y) {
    }
}
