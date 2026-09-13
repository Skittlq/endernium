package com.skittlq.endernium.client;

import com.skittlq.endernium.EnderniumConstants;
import com.skittlq.endernium.client.vfx.EnderniumVfxManager;
import com.skittlq.endernium.config.EnderniumGameplayConfig;
import com.skittlq.endernium.item.EnderniumItems;
import com.skittlq.endernium.item.armor.EnderniumArmorUtil;
import com.skittlq.endernium.item.armor.EnderniumHorseArmorAbility;
import com.skittlq.endernium.item.armor.EnderniumNautilusArmorAbility;
import com.skittlq.endernium.item.tools.EnderniumSword;
import com.skittlq.endernium.item.tools.EnderniumSpear;
import com.skittlq.endernium.item.tools.EnderniumPickaxe;
import com.skittlq.endernium.item.tools.EnderniumShovel;
import com.skittlq.endernium.item.tools.EnderniumAxe;
import com.skittlq.endernium.item.tools.EnderniumHoe;
import com.skittlq.endernium.item.tools.EnderniumVeinMiningToolHelper;
import com.skittlq.endernium.particles.EnderniumParticles;
import com.skittlq.endernium.progression.EnderniumBlessing;
import com.skittlq.endernium.util.EnderniumTargeting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public final class EnderniumClientBehavior {
    public static final Identifier ARMOR_COOLDOWN_ICON = Identifier.fromNamespaceAndPath(
            EnderniumConstants.MOD_ID,
            "textures/item/endernium_armor_hud.png"
    );
    public static final Identifier ARMOR_COOLDOWN_READY_ICON = Identifier.fromNamespaceAndPath(
            EnderniumConstants.MOD_ID,
            "textures/item/endernium_armor_ready.png"
    );
    public static final Identifier SWORD_COOLDOWN_ICON = Identifier.fromNamespaceAndPath(
            EnderniumConstants.MOD_ID,
            "textures/item/endernium_sword_hud.png"
    );
    public static final Identifier SWORD_COOLDOWN_READY_ICON = Identifier.fromNamespaceAndPath(
            EnderniumConstants.MOD_ID,
            "textures/item/endernium_sword_ready.png"
    );
    public static final Identifier HORSE_COOLDOWN_ICON = Identifier.fromNamespaceAndPath(
            EnderniumConstants.MOD_ID,
            "textures/item/endernium_horse_armor.png"
    );
    public static final Identifier NAUTILUS_COOLDOWN_ICON = Identifier.fromNamespaceAndPath(
            EnderniumConstants.MOD_ID,
            "textures/item/endernium_nautilus_armor.png"
    );
    public static final Identifier SPEAR_COOLDOWN_ICON = Identifier.fromNamespaceAndPath(
            EnderniumConstants.MOD_ID,
            "textures/item/endernium_spear.png"
    );
    public static final Identifier PICKAXE_VEIN_MINING_ICON = Identifier.fromNamespaceAndPath(
            EnderniumConstants.MOD_ID,
            "textures/item/endernium_pickaxe.png"
    );
    public static final Identifier SHOVEL_VEIN_MINING_ICON = Identifier.fromNamespaceAndPath(
            EnderniumConstants.MOD_ID,
            "textures/item/endernium_shovel.png"
    );
    public static final Identifier AXE_VEIN_MINING_ICON = Identifier.fromNamespaceAndPath(
            EnderniumConstants.MOD_ID,
            "textures/item/endernium_axe.png"
    );
    public static final Identifier HOE_VEIN_MINING_ICON = Identifier.fromNamespaceAndPath(
            EnderniumConstants.MOD_ID,
            "textures/item/endernium_hoe.png"
    );
    public static final int COOLDOWN_ICON_SIZE = 16;
    public static final int COOLDOWN_SPRITE_WIDTH = 14;
    public static final int COOLDOWN_SPRITE_HEIGHT = 13;
    public static final int COOLDOWN_SPRITE_OFFSET_X = 1;
    public static final int COOLDOWN_SPRITE_OFFSET_Y = 2;
    public static final int COOLDOWN_BACKGROUND_COLOR = 0x33FFFFFF;
    public static final int COOLDOWN_FOREGROUND_COLOR = -1;
    private static final long READY_FLASH_DURATION_NANOS = 200_000_000L;
    private static final long READY_HOLD_DURATION_NANOS = 1_000_000_000L;
    private static final long READY_FADE_DURATION_NANOS = 1_000_000_000L;
    private static final CooldownHudFrame HIDDEN_COOLDOWN_FRAME =
            new CooldownHudFrame(false, 0.0F, 0.0F, 0.0F, false);
    private static final CooldownHudTracker ARMOR_HUD_TRACKER = new CooldownHudTracker();
    private static final CooldownHudTracker SWORD_HUD_TRACKER = new CooldownHudTracker();
    private static final CooldownHudTracker HORSE_HUD_TRACKER = new CooldownHudTracker();
    private static final CooldownHudTracker NAUTILUS_HUD_TRACKER = new CooldownHudTracker();
    private static final CooldownHudTracker SPEAR_HUD_TRACKER = new CooldownHudTracker();
    private static final VeinMiningHudTracker VEIN_MINING_HUD_TRACKER = new VeinMiningHudTracker();

    private EnderniumClientBehavior() {
    }

    public static void tickClient(Minecraft client) {
        CameraLerpHandler.clientTick(client);
        EnderniumVfxManager.tick(client);
        if (client.player == null) {
            resetSessionState();
            return;
        }
        EnderniumClientEquipmentState.setFullEnderniumSetEquipped(
                EnderniumArmorUtil.hasFullEnderniumSet(client.player));
        renderSwordPreviewParticles(client.player);
    }

    public static void resetSessionState() {
        EnderniumTargeting.clearClientCombatOpponents();
        EnderniumBlessing.clearClientState();
        EnderniumClientCooldowns.clear();
        EnderniumClientGameplaySettings.reset();
        EnderniumClientEquipmentState.reset();
        CameraLerpHandler.reset();
        ARMOR_HUD_TRACKER.reset();
        SWORD_HUD_TRACKER.reset();
        HORSE_HUD_TRACKER.reset();
        NAUTILUS_HUD_TRACKER.reset();
        SPEAR_HUD_TRACKER.reset();
        VEIN_MINING_HUD_TRACKER.reset();
        EnderniumClientVeinMiningState.reset();
        EnderniumVfxManager.clear();
    }

    public static void playLockedAbilityCue(Minecraft client) {
        if (client.player == null || !hasEnabledAbilityItem(client.player)) {
            return;
        }
        client.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.SHIELD_BLOCK.value(), 0.0F, 0.08F));
    }

    private static boolean hasEnabledAbilityItem(Player player) {
        EnderniumGameplayConfig.Snapshot settings = EnderniumClientGameplaySettings.get();
        return EnderniumHorseArmorAbility.hasArmoredHorseMount(player)
                || EnderniumNautilusArmorAbility.hasArmoredNautilusMount(player)
                || hasEnabledAbilityItem(player.getMainHandItem(), settings)
                || hasEnabledAbilityItem(player.getOffhandItem(), settings);
    }

    private static boolean hasEnabledAbilityItem(ItemStack stack, EnderniumGameplayConfig.Snapshot settings) {
        if (stack.getItem() instanceof EnderniumSword) {
            return settings.swordAbilityEnabled();
        }
        if (stack.getItem() instanceof EnderniumSpear) {
            return true;
        }
        return settings.toolsVeinMiningEnabled()
                && (stack.getItem() instanceof EnderniumPickaxe
                || stack.getItem() instanceof EnderniumShovel
                || stack.getItem() instanceof EnderniumAxe
                || stack.getItem() instanceof EnderniumHoe);
    }

    public static boolean shouldRenderArmorCooldown(Player player, boolean armorAbilityEnabled) {
        if (player == null || !armorAbilityEnabled || !EnderniumBlessing.isBlessed(player)) {
            return false;
        }

        ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
        return EnderniumArmorUtil.hasFullEnderniumSet(player) && chestStack.is(EnderniumItems.ENDERNIUM_CHESTPLATE.get());
    }

    public static boolean shouldRenderSwordCooldown(Player player, boolean swordAbilityEnabled) {
        if (player == null || !swordAbilityEnabled || !EnderniumBlessing.isBlessed(player)) {
            return false;
        }

        return player.getMainHandItem().getItem() instanceof EnderniumSword
                || player.getOffhandItem().getItem() instanceof EnderniumSword;
    }

    public static boolean shouldRenderHorseCooldown(Player player) {
        return player != null
                && EnderniumBlessing.isBlessed(player)
                && EnderniumHorseArmorAbility.hasArmoredHorseMount(player);
    }

    public static boolean shouldRenderNautilusCooldown(Player player) {
        return player != null
                && EnderniumBlessing.isBlessed(player)
                && EnderniumNautilusArmorAbility.hasArmoredNautilusMount(player);
    }

    public static boolean shouldRenderSpearCooldown(Player player) {
        return player != null
                && EnderniumBlessing.isBlessed(player)
                && EnderniumSpear.isHeldBy(player);
    }

    public static ItemStack heldVeinMiningTool(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        if (isVeinMiningTool(mainHand)) {
            return mainHand;
        }
        ItemStack offhand = player.getOffhandItem();
        return isVeinMiningTool(offhand) ? offhand : ItemStack.EMPTY;
    }

    private static boolean isVeinMiningTool(ItemStack stack) {
        return stack.getItem() instanceof EnderniumPickaxe
                || stack.getItem() instanceof EnderniumShovel
                || stack.getItem() instanceof EnderniumAxe
                || stack.getItem() instanceof EnderniumHoe;
    }

    private static Identifier veinMiningIcon(ItemStack stack) {
        if (stack.getItem() instanceof EnderniumPickaxe) {
            return PICKAXE_VEIN_MINING_ICON;
        }
        if (stack.getItem() instanceof EnderniumShovel) {
            return SHOVEL_VEIN_MINING_ICON;
        }
        if (stack.getItem() instanceof EnderniumAxe) {
            return AXE_VEIN_MINING_ICON;
        }
        return HOE_VEIN_MINING_ICON;
    }

    // Slot 0 sits right next to the hotbar; each further slot stacks outward, so a lone icon closes the gap.
    public static HudIconPosition cooldownPosition(int screenWidth, int screenHeight, HumanoidArm mainArm, int slotIndex) {
        int hotbarLeft = screenWidth / 2 - 91;
        int hotbarRight = screenWidth / 2 + 91;
        int hotbarTop = screenHeight - 22;
        int gap = 4;
        int offset = slotIndex * (COOLDOWN_ICON_SIZE + gap);
        int x = mainArm == HumanoidArm.RIGHT
                ? hotbarRight + 4 + offset
                : hotbarLeft - COOLDOWN_ICON_SIZE - 4 - offset;
        int y = hotbarTop + (22 - COOLDOWN_ICON_SIZE) / 2;
        return new HudIconPosition(x, y);
    }

    public static int cooldownFillHeight(float cooldownProgress) {
        return Math.max(0, Math.min(COOLDOWN_SPRITE_HEIGHT, (int) (cooldownProgress * COOLDOWN_SPRITE_HEIGHT)));
    }

    public static CooldownHudFrame armorCooldownHudFrame(float cooldownRemaining) {
        return ARMOR_HUD_TRACKER.frame(cooldownRemaining);
    }

    public static CooldownHudFrame swordCooldownHudFrame(float cooldownRemaining) {
        return SWORD_HUD_TRACKER.frame(cooldownRemaining);
    }

    public static CooldownHudFrame horseCooldownHudFrame(float cooldownRemaining) {
        return HORSE_HUD_TRACKER.frame(cooldownRemaining);
    }

    public static CooldownHudFrame nautilusCooldownHudFrame(float cooldownRemaining) {
        return NAUTILUS_HUD_TRACKER.frame(cooldownRemaining);
    }

    public static CooldownHudFrame spearCooldownHudFrame(float cooldownRemaining) {
        return SPEAR_HUD_TRACKER.frame(cooldownRemaining);
    }

    public static void resetArmorCooldownHud() {
        ARMOR_HUD_TRACKER.reset();
    }

    public static void resetSwordCooldownHud() {
        SWORD_HUD_TRACKER.reset();
    }

    public static void resetHorseCooldownHud() {
        HORSE_HUD_TRACKER.reset();
    }

    public static void resetNautilusCooldownHud() {
        NAUTILUS_HUD_TRACKER.reset();
    }

    public static void resetSpearCooldownHud() {
        SPEAR_HUD_TRACKER.reset();
    }

    public static void triggerBlessingReadyHud() {
        ARMOR_HUD_TRACKER.triggerReady();
        SWORD_HUD_TRACKER.triggerReady();
        HORSE_HUD_TRACKER.triggerReady();
        SPEAR_HUD_TRACKER.triggerReady();
    }

    public static void playCooldownReadySound(Minecraft client) {
        client.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ENDER_EYE_DEATH, 2.0F));
    }

    public static int hudColorWithOpacity(int color, float opacity) {
        int sourceAlpha = color >>> 24;
        int adjustedAlpha = Math.round(sourceAlpha * Math.max(0.0F, Math.min(1.0F, opacity)));
        return color & 0x00FFFFFF | adjustedAlpha << 24;
    }

    // Renders both ability cooldown icons, dynamically stacking so a lone visible icon sits next to the hotbar.
    public static void renderCooldownHuds(
            Minecraft client,
            GuiGraphicsExtractor gui,
            boolean armorAbilityEnabled,
            boolean swordAbilityEnabled
    ) {
        Player player = client.player;
        if (player == null || !EnderniumBlessing.isBlessed(player)) {
            return;
        }

        int slot = 0;

        ItemStack veinMiningTool = heldVeinMiningTool(player);
        if (!veinMiningTool.isEmpty()
                && EnderniumClientGameplaySettings.get().toolsVeinMiningEnabled()) {
            boolean enabled = EnderniumVeinMiningToolHelper.isVeinMiningPreferenceEnabled(
                    veinMiningTool);
            boolean active = enabled && EnderniumClientVeinMiningState.isActive();
            CooldownHudFrame frame = VEIN_MINING_HUD_TRACKER.frame(
                    veinMiningTool,
                    enabled,
                    active,
                    EnderniumClientVeinMiningState.blockProgress(player.level().getGameTime())
            );
            HudIconPosition position = cooldownPosition(
                    gui.guiWidth(), gui.guiHeight(), player.getMainArm(), slot++);
            Identifier icon = veinMiningIcon(veinMiningTool);
            renderCooldownIcon(gui, icon, icon, position, frame);
        } else {
            VEIN_MINING_HUD_TRACKER.reset();
        }

        if (shouldRenderArmorCooldown(player, armorAbilityEnabled)) {
            float cooldownRemaining = EnderniumClientCooldowns.armorCooldownRemainingFraction(player.level().getGameTime());
            CooldownHudFrame frame = armorCooldownHudFrame(cooldownRemaining);
            if (frame.visible()) {
                if (frame.playReadySound()) {
                    playCooldownReadySound(client);
                }
                HudIconPosition position = cooldownPosition(gui.guiWidth(), gui.guiHeight(), player.getMainArm(), slot++);
                renderCooldownIcon(gui, ARMOR_COOLDOWN_ICON, ARMOR_COOLDOWN_READY_ICON, position, frame);
            }
        } else {
            resetArmorCooldownHud();
        }

        if (shouldRenderHorseCooldown(player)) {
            float cooldownRemaining = EnderniumClientCooldowns.horseCooldownRemainingFraction(
                    player.level().getGameTime());
            CooldownHudFrame frame = horseCooldownHudFrame(cooldownRemaining);
            if (frame.visible()) {
                if (frame.playReadySound()) {
                    playCooldownReadySound(client);
                }
                HudIconPosition position = cooldownPosition(
                        gui.guiWidth(), gui.guiHeight(), player.getMainArm(), slot++);
                renderCooldownIcon(gui, HORSE_COOLDOWN_ICON, HORSE_COOLDOWN_ICON, position, frame);
            }
        } else {
            resetHorseCooldownHud();
        }

        if (shouldRenderNautilusCooldown(player)) {
            float cooldownRemaining = EnderniumClientCooldowns.nautilusCooldownRemainingFraction(
                    player.level().getGameTime());
            CooldownHudFrame frame = nautilusCooldownHudFrame(cooldownRemaining);
            if (frame.visible()) {
                if (frame.playReadySound()) {
                    playCooldownReadySound(client);
                }
                HudIconPosition position = cooldownPosition(
                        gui.guiWidth(), gui.guiHeight(), player.getMainArm(), slot++);
                renderCooldownIcon(gui, NAUTILUS_COOLDOWN_ICON, NAUTILUS_COOLDOWN_ICON, position, frame);
            }
        } else {
            resetNautilusCooldownHud();
        }

        if (shouldRenderSpearCooldown(player)) {
            float cooldownRemaining = EnderniumClientCooldowns.spearCooldownRemainingFraction(
                    player.level().getGameTime());
            CooldownHudFrame frame = spearCooldownHudFrame(cooldownRemaining);
            if (frame.visible()) {
                if (frame.playReadySound()) {
                    playCooldownReadySound(client);
                }
                HudIconPosition position = cooldownPosition(
                        gui.guiWidth(), gui.guiHeight(), player.getMainArm(), slot++);
                renderCooldownIcon(gui, SPEAR_COOLDOWN_ICON, SPEAR_COOLDOWN_ICON, position, frame);
            }
        } else {
            resetSpearCooldownHud();
        }

        if (shouldRenderSwordCooldown(player, swordAbilityEnabled)) {
            float cooldownRemaining = EnderniumClientCooldowns.swordCooldownRemainingFraction(player.level().getGameTime());
            CooldownHudFrame frame = swordCooldownHudFrame(cooldownRemaining);
            if (frame.visible()) {
                if (frame.playReadySound()) {
                    playCooldownReadySound(client);
                }
                HudIconPosition position = cooldownPosition(gui.guiWidth(), gui.guiHeight(), player.getMainArm(), slot++);
                renderCooldownIcon(gui, SWORD_COOLDOWN_ICON, SWORD_COOLDOWN_READY_ICON, position, frame);
            }
        } else {
            resetSwordCooldownHud();
        }
    }

    private static void renderCooldownIcon(
            GuiGraphicsExtractor gui,
            Identifier icon,
            Identifier readyIcon,
            HudIconPosition position,
            CooldownHudFrame frame
    ) {
        int iconX = position.x() + COOLDOWN_SPRITE_OFFSET_X;
        int iconY = position.y() + COOLDOWN_SPRITE_OFFSET_Y;
        int backgroundColor = hudColorWithOpacity(COOLDOWN_BACKGROUND_COLOR, frame.opacity());
        int foregroundColor = hudColorWithOpacity(COOLDOWN_FOREGROUND_COLOR, frame.opacity());

        gui.blit(RenderPipelines.GUI_TEXTURED,
                icon,
                iconX,
                iconY,
                0.0F,
                0.0F,
                COOLDOWN_SPRITE_WIDTH,
                COOLDOWN_SPRITE_HEIGHT,
                COOLDOWN_SPRITE_WIDTH,
                COOLDOWN_SPRITE_HEIGHT,
                backgroundColor);

        int fillHeight = cooldownFillHeight(frame.cooldownProgress());
        if (fillHeight > 0) {
            int sourceY = COOLDOWN_SPRITE_HEIGHT - fillHeight;
            gui.blit(RenderPipelines.GUI_TEXTURED,
                    icon,
                    iconX,
                    iconY + sourceY,
                    0.0F,
                    sourceY,
                    COOLDOWN_SPRITE_WIDTH,
                    fillHeight,
                    COOLDOWN_SPRITE_WIDTH,
                    COOLDOWN_SPRITE_HEIGHT,
                    foregroundColor);
        }

        int flashColor = hudColorWithOpacity(-1, frame.flashOpacity());
        if (frame.flashOpacity() > 0.0F) {
            gui.blit(RenderPipelines.GUI_TEXTURED,
                    readyIcon,
                    iconX,
                    iconY,
                    0.0F,
                    0.0F,
                    COOLDOWN_SPRITE_WIDTH,
                    COOLDOWN_SPRITE_HEIGHT,
                    COOLDOWN_SPRITE_WIDTH,
                    COOLDOWN_SPRITE_HEIGHT,
                    flashColor);
        }
    }

    private static void renderSwordPreviewParticles(Player player) {
        if (player == null || !EnderniumBlessing.isClientBlessed()
                || !EnderniumClientGameplaySettings.get().swordAbilityEnabled()) {
            return;
        }

        if (!(player.getMainHandItem().getItem() instanceof EnderniumSword)
                && !(player.getOffhandItem().getItem() instanceof EnderniumSword)) {
            return;
        }

        if (EnderniumClientCooldowns.isSwordOnCooldown(player.level().getGameTime())) {
            return;
        }

        for (LivingEntity target : EnderniumTargeting.findSwordPreviewTargets(player)) {
            AABB box = target.getBoundingBox();
            for (int i = 0; i < 2; i++) {
                double px = box.minX + target.level().getRandom().nextDouble() * (box.maxX - box.minX);
                double py = box.minY + target.level().getRandom().nextDouble() * (box.maxY - box.minY);
                double pz = box.minZ + target.level().getRandom().nextDouble() * (box.maxZ - box.minZ);
                player.level().addParticle(EnderniumParticles.REVERSE_ENDERNIUM_BIT.get(), px, py, pz, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    public record HudIconPosition(int x, int y) {
    }

    public record CooldownHudFrame(
            boolean visible,
            float cooldownProgress,
            float opacity,
            float flashOpacity,
            boolean playReadySound
    ) {
    }

    private static final class CooldownHudTracker {
        private boolean wasActive;
        private long readyAtNanos = -1L;

        CooldownHudFrame frame(float cooldownRemaining) {
            long nowNanos = System.nanoTime();
            if (cooldownRemaining > 0.0F) {
                wasActive = true;
                readyAtNanos = -1L;
                return new CooldownHudFrame(true, 1.0F - cooldownRemaining, 1.0F, 0.0F, false);
            }

            if (wasActive) {
                wasActive = false;
                readyAtNanos = nowNanos;
                return new CooldownHudFrame(true, 1.0F, 1.0F, 1.0F, true);
            }

            if (readyAtNanos < 0L) {
                return HIDDEN_COOLDOWN_FRAME;
            }

            long elapsedNanos = nowNanos - readyAtNanos;
            if (elapsedNanos < READY_FLASH_DURATION_NANOS) {
                float flashOpacity = 1.0F - (float) elapsedNanos / READY_FLASH_DURATION_NANOS;
                return new CooldownHudFrame(true, 1.0F, 1.0F, flashOpacity, false);
            }

            long holdElapsedNanos = elapsedNanos - READY_FLASH_DURATION_NANOS;
            if (holdElapsedNanos < READY_HOLD_DURATION_NANOS) {
                return new CooldownHudFrame(true, 1.0F, 1.0F, 0.0F, false);
            }

            long fadeElapsedNanos = holdElapsedNanos - READY_HOLD_DURATION_NANOS;
            if (fadeElapsedNanos < READY_FADE_DURATION_NANOS) {
                float opacity = 1.0F - (float) fadeElapsedNanos / READY_FADE_DURATION_NANOS;
                return new CooldownHudFrame(true, 1.0F, opacity, 0.0F, false);
            }

            readyAtNanos = -1L;
            return HIDDEN_COOLDOWN_FRAME;
        }

        void reset() {
            wasActive = false;
            readyAtNanos = -1L;
        }

        void triggerReady() {
            wasActive = false;
            readyAtNanos = System.nanoTime();
        }
    }

    private static final class VeinMiningHudTracker {
        private Item trackedItem;
        private boolean initialized;
        private boolean wasEnabled;
        private long enabledAtNanos = -1L;

        CooldownHudFrame frame(
                ItemStack stack,
                boolean enabled,
                boolean active,
                float blockProgress
        ) {
            long nowNanos = System.nanoTime();
            if (trackedItem != stack.getItem()) {
                trackedItem = stack.getItem();
                initialized = true;
                wasEnabled = enabled;
                enabledAtNanos = -1L;
            } else if (!initialized) {
                initialized = true;
                wasEnabled = enabled;
            }

            if (enabled && !wasEnabled) {
                enabledAtNanos = nowNanos;
            }
            wasEnabled = enabled;

            float progress;
            if (!enabled) {
                progress = 0.0F;
            } else if (active) {
                progress = Math.max(0.0F, Math.min(1.0F, blockProgress));
            } else {
                progress = 1.0F;
            }

            float flashOpacity = 0.0F;
            if (enabledAtNanos >= 0L) {
                long elapsed = nowNanos - enabledAtNanos;
                if (elapsed < READY_FLASH_DURATION_NANOS) {
                    flashOpacity = 1.0F - (float) elapsed / READY_FLASH_DURATION_NANOS;
                } else {
                    enabledAtNanos = -1L;
                }
            }
            return new CooldownHudFrame(true, progress, 1.0F, flashOpacity, false);
        }

        void reset() {
            trackedItem = null;
            initialized = false;
            wasEnabled = false;
            enabledAtNanos = -1L;
        }
    }
}

