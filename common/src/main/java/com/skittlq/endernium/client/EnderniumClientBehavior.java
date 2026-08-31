package com.skittlq.endernium.client;

import com.skittlq.endernium.EnderniumConstants;
import com.skittlq.endernium.item.EnderniumItems;
import com.skittlq.endernium.item.armor.EnderniumArmorUtil;
import com.skittlq.endernium.item.tools.EnderniumSword;
import com.skittlq.endernium.particles.EnderniumParticles;
import com.skittlq.endernium.util.EnderniumTargeting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public final class EnderniumClientBehavior {
    public static final Identifier ARMOR_COOLDOWN_ICON = Identifier.fromNamespaceAndPath(
            EnderniumConstants.MOD_ID,
            "textures/item/endernium_chestplate_hud.png"
    );
    public static final Identifier ARMOR_COOLDOWN_READY_ICON = Identifier.fromNamespaceAndPath(
            EnderniumConstants.MOD_ID,
            "textures/item/endernium_chestplate_ready.png"
    );
    public static final int ARMOR_ICON_SIZE = 16;
    public static final int ARMOR_SPRITE_WIDTH = 14;
    public static final int ARMOR_SPRITE_HEIGHT = 13;
    public static final int ARMOR_SPRITE_OFFSET_X = 1;
    public static final int ARMOR_SPRITE_OFFSET_Y = 2;
    public static final int ARMOR_COOLDOWN_BACKGROUND_COLOR = 0x33FFFFFF;
    public static final int ARMOR_COOLDOWN_FOREGROUND_COLOR = -1;
    private static final long ARMOR_READY_FLASH_DURATION_NANOS = 200_000_000L;
    private static final long ARMOR_READY_HOLD_DURATION_NANOS = 1_000_000_000L;
    private static final long ARMOR_READY_FADE_DURATION_NANOS = 1_000_000_000L;
    private static final ArmorCooldownHudFrame HIDDEN_ARMOR_COOLDOWN_FRAME =
            new ArmorCooldownHudFrame(false, 0.0F, 0.0F, 0.0F, false);
    private static boolean armorCooldownWasActive;
    private static long armorCooldownReadyAtNanos = -1L;

    private EnderniumClientBehavior() {
    }

    public static void tickClient(Minecraft client) {
        CameraLerpHandler.clientTick(client);
        if (client.player == null) {
            EnderniumTargeting.clearClientCombatOpponents();
        }
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

    public static HudIconPosition armorCooldownPosition(int screenWidth, int screenHeight, HumanoidArm mainArm) {
        int hotbarLeft = screenWidth / 2 - 91;
        int hotbarRight = screenWidth / 2 + 91;
        int hotbarTop = screenHeight - 22;
        int x = mainArm == HumanoidArm.RIGHT
                ? hotbarRight + 4
                : hotbarLeft - ARMOR_ICON_SIZE - 4;
        int y = hotbarTop + (22 - ARMOR_ICON_SIZE) / 2;
        return new HudIconPosition(x, y);
    }

    public static int cooldownFillHeight(float cooldownProgress) {
        return Math.max(0, Math.min(ARMOR_SPRITE_HEIGHT, (int) (cooldownProgress * ARMOR_SPRITE_HEIGHT)));
    }

    public static ArmorCooldownHudFrame armorCooldownHudFrame(float cooldownRemaining) {
        long nowNanos = System.nanoTime();
        if (cooldownRemaining > 0.0F) {
            armorCooldownWasActive = true;
            armorCooldownReadyAtNanos = -1L;
            return new ArmorCooldownHudFrame(
                    true,
                    1.0F - cooldownRemaining,
                    1.0F,
                    0.0F,
                    false
            );
        }

        if (armorCooldownWasActive) {
            armorCooldownWasActive = false;
            armorCooldownReadyAtNanos = nowNanos;
            return new ArmorCooldownHudFrame(true, 1.0F, 1.0F, 1.0F, true);
        }

        if (armorCooldownReadyAtNanos < 0L) {
            return HIDDEN_ARMOR_COOLDOWN_FRAME;
        }

        long elapsedNanos = nowNanos - armorCooldownReadyAtNanos;
        if (elapsedNanos < ARMOR_READY_FLASH_DURATION_NANOS) {
            float flashOpacity = 1.0F - (float) elapsedNanos / ARMOR_READY_FLASH_DURATION_NANOS;
            return new ArmorCooldownHudFrame(true, 1.0F, 1.0F, flashOpacity, false);
        }

        long holdElapsedNanos = elapsedNanos - ARMOR_READY_FLASH_DURATION_NANOS;
        if (holdElapsedNanos < ARMOR_READY_HOLD_DURATION_NANOS) {
            return new ArmorCooldownHudFrame(true, 1.0F, 1.0F, 0.0F, false);
        }

        long fadeElapsedNanos = holdElapsedNanos - ARMOR_READY_HOLD_DURATION_NANOS;
        if (fadeElapsedNanos < ARMOR_READY_FADE_DURATION_NANOS) {
            float opacity = 1.0F - (float) fadeElapsedNanos / ARMOR_READY_FADE_DURATION_NANOS;
            return new ArmorCooldownHudFrame(true, 1.0F, opacity, 0.0F, false);
        }

        armorCooldownReadyAtNanos = -1L;
        return HIDDEN_ARMOR_COOLDOWN_FRAME;
    }

    public static void resetArmorCooldownHud() {
        armorCooldownWasActive = false;
        armorCooldownReadyAtNanos = -1L;
    }

    public static void playArmorCooldownReadySound(Minecraft client) {
        client.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ENDER_EYE_DEATH, 2.0F));
    }

    public static int hudColorWithOpacity(int color, float opacity) {
        int sourceAlpha = color >>> 24;
        int adjustedAlpha = Math.round(sourceAlpha * Math.max(0.0F, Math.min(1.0F, opacity)));
        return color & 0x00FFFFFF | adjustedAlpha << 24;
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

        for (LivingEntity target : EnderniumTargeting.findSwordPreviewTargets(player)) {
            AABB box = target.getBoundingBox();
            for (int i = 0; i < 4; i++) {
                double px = box.minX + target.level().getRandom().nextDouble() * (box.maxX - box.minX);
                double py = box.minY + target.level().getRandom().nextDouble() * (box.maxY - box.minY);
                double pz = box.minZ + target.level().getRandom().nextDouble() * (box.maxZ - box.minZ);
                player.level().addParticle(EnderniumParticles.REVERSE_ENDERNIUM_BIT.get(), px, py, pz, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    public record HudIconPosition(int x, int y) {
    }

    public record ArmorCooldownHudFrame(
            boolean visible,
            float cooldownProgress,
            float opacity,
            float flashOpacity,
            boolean playReadySound
    ) {
    }
}
