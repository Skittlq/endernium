package com.skittlq.endernium.network;

import com.skittlq.endernium.network.payloads.AbilityCooldownSyncPayload;
import com.skittlq.endernium.network.payloads.ArmorChargeSyncPayload;
import com.skittlq.endernium.network.payloads.BlessingStatePayload;
import com.skittlq.endernium.network.payloads.BlessingVfxPayload;
import com.skittlq.endernium.network.payloads.DragonDeathVfxPayload;
import com.skittlq.endernium.network.payloads.GameplaySettingsPayload;
import com.skittlq.endernium.network.payloads.VeinMiningStatePayload;
import com.skittlq.endernium.network.payloads.SwordChargeSyncPayload;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

public final class EnderniumNetworking {
    private static CameraLerpSender cameraLerpSender = (player, targetYaw, targetPitch, durationTicks) -> {
        throw new IllegalStateException("Endernium camera lerp sender has not been bound to a loader network API yet");
    };
    private static CombatOpponentsSender combatOpponentsSender = (player, opponentIds) -> {
        throw new IllegalStateException("Endernium combat opponent sender has not been bound to a loader network API yet");
    };
    private static DragonDeathVfxSender dragonDeathVfxSender = (player, payload) -> {
        throw new IllegalStateException("Endernium dragon VFX sender has not been bound to a loader network API yet");
    };
    private static AbilityCooldownSyncSender abilityCooldownSyncSender = (player, payload) -> {
        throw new IllegalStateException("Endernium ability cooldown sync sender has not been bound to a loader network API yet");
    };
    private static SwordChargeSyncSender swordChargeSyncSender = (player, payload) -> {
        throw new IllegalStateException("Endernium sword charge sync sender has not been bound yet");
    };
    private static ArmorChargeSyncSender armorChargeSyncSender = (player, payload) -> {
        throw new IllegalStateException("Endernium armor charge sync sender has not been bound yet");
    };
    private static BlessingStateSender blessingStateSender = (player, payload) -> {
        throw new IllegalStateException("Endernium blessing state sender has not been bound to a loader network API yet");
    };
    private static BlessingVfxSender blessingVfxSender = (player, payload) -> {
        throw new IllegalStateException("Endernium blessing VFX sender has not been bound to a loader network API yet");
    };
    private static GameplaySettingsSender gameplaySettingsSender = (player, payload) -> {
        throw new IllegalStateException("Endernium gameplay settings sender has not been bound yet");
    };
    private static VeinMiningStateSender veinMiningStateSender = (player, payload) -> {
        throw new IllegalStateException("Endernium vein mining state sender has not been bound yet");
    };

    private EnderniumNetworking() {
    }

    public static void bindCameraLerpSender(CameraLerpSender sender) {
        cameraLerpSender = Objects.requireNonNull(sender);
    }

    public static void sendCameraLerp(ServerPlayer player, float targetYaw, float targetPitch, int durationTicks) {
        cameraLerpSender.send(player, targetYaw, targetPitch, durationTicks);
    }

    public static void bindCombatOpponentsSender(CombatOpponentsSender sender) {
        combatOpponentsSender = Objects.requireNonNull(sender);
    }

    public static void sendCombatOpponents(ServerPlayer player, Collection<UUID> opponentIds) {
        combatOpponentsSender.send(player, opponentIds);
    }

    public static void bindDragonDeathVfxSender(DragonDeathVfxSender sender) {
        dragonDeathVfxSender = Objects.requireNonNull(sender);
    }

    public static void sendDragonDeathVfx(ServerPlayer player, DragonDeathVfxPayload payload) {
        dragonDeathVfxSender.send(player, payload);
    }

    public static void bindAbilityCooldownSyncSender(AbilityCooldownSyncSender sender) {
        abilityCooldownSyncSender = Objects.requireNonNull(sender);
    }

    public static void bindSwordChargeSyncSender(SwordChargeSyncSender sender) {
        swordChargeSyncSender = Objects.requireNonNull(sender);
    }

    public static void sendSwordChargeSync(ServerPlayer player, float storedDamage) {
        swordChargeSyncSender.send(player, new SwordChargeSyncPayload(storedDamage));
    }

    public static void bindArmorChargeSyncSender(ArmorChargeSyncSender sender) {
        armorChargeSyncSender = Objects.requireNonNull(sender);
    }

    public static void sendArmorChargeSync(ServerPlayer player, float storedDamage) {
        armorChargeSyncSender.send(player, new ArmorChargeSyncPayload(storedDamage));
    }

    public static void sendHorseCooldownSync(ServerPlayer player, long endGameTime, int durationTicks) {
        abilityCooldownSyncSender.send(player,
                new AbilityCooldownSyncPayload(AbilityCooldownSyncPayload.Ability.HORSE, endGameTime, durationTicks));
    }

    public static void sendSpearCooldownSync(ServerPlayer player, long endGameTime, int durationTicks) {
        abilityCooldownSyncSender.send(player,
                new AbilityCooldownSyncPayload(AbilityCooldownSyncPayload.Ability.SPEAR, endGameTime, durationTicks));
    }

    public static void sendNautilusCooldownSync(ServerPlayer player, long endGameTime, int durationTicks) {
        abilityCooldownSyncSender.send(player,
                new AbilityCooldownSyncPayload(AbilityCooldownSyncPayload.Ability.NAUTILUS, endGameTime, durationTicks));
    }

    public static void bindBlessingStateSender(BlessingStateSender sender) {
        blessingStateSender = Objects.requireNonNull(sender);
    }

    public static void sendBlessingState(ServerPlayer player, boolean blessed, boolean playReadyEffect) {
        blessingStateSender.send(player, new BlessingStatePayload(blessed, playReadyEffect));
    }

    public static void bindBlessingVfxSender(BlessingVfxSender sender) {
        blessingVfxSender = Objects.requireNonNull(sender);
    }

    public static void sendBlessingVfx(ServerPlayer player, BlessingVfxPayload payload) {
        blessingVfxSender.send(player, payload);
    }

    public static void bindGameplaySettingsSender(GameplaySettingsSender sender) {
        gameplaySettingsSender = Objects.requireNonNull(sender);
    }

    public static void sendGameplaySettings(ServerPlayer player) {
        gameplaySettingsSender.send(player, GameplaySettingsPayload.current());
    }

    public static void bindVeinMiningStateSender(VeinMiningStateSender sender) {
        veinMiningStateSender = Objects.requireNonNull(sender);
    }

    public static void sendVeinMiningState(
            ServerPlayer player,
            boolean active,
            long blockEndGameTime,
            int blockDurationTicks
    ) {
        veinMiningStateSender.send(
                player,
                new VeinMiningStatePayload(active, blockEndGameTime, blockDurationTicks)
        );
    }

    @FunctionalInterface
    public interface CameraLerpSender {
        void send(ServerPlayer player, float targetYaw, float targetPitch, int durationTicks);
    }

    @FunctionalInterface
    public interface CombatOpponentsSender {
        void send(ServerPlayer player, Collection<UUID> opponentIds);
    }

    @FunctionalInterface
    public interface DragonDeathVfxSender {
        void send(ServerPlayer player, DragonDeathVfxPayload payload);
    }

    @FunctionalInterface
    public interface AbilityCooldownSyncSender {
        void send(ServerPlayer player, AbilityCooldownSyncPayload payload);
    }

    @FunctionalInterface
    public interface SwordChargeSyncSender {
        void send(ServerPlayer player, SwordChargeSyncPayload payload);
    }

    @FunctionalInterface
    public interface ArmorChargeSyncSender {
        void send(ServerPlayer player, ArmorChargeSyncPayload payload);
    }

    @FunctionalInterface
    public interface BlessingStateSender {
        void send(ServerPlayer player, BlessingStatePayload payload);
    }

    @FunctionalInterface
    public interface BlessingVfxSender {
        void send(ServerPlayer player, BlessingVfxPayload payload);
    }

    @FunctionalInterface
    public interface GameplaySettingsSender {
        void send(ServerPlayer player, GameplaySettingsPayload payload);
    }

    @FunctionalInterface
    public interface VeinMiningStateSender {
        void send(ServerPlayer player, VeinMiningStatePayload payload);
    }
}
