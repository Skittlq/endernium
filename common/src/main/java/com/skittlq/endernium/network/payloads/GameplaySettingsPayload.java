package com.skittlq.endernium.network.payloads;

import com.skittlq.endernium.EnderniumConstants;
import com.skittlq.endernium.config.EnderniumGameplayConfig;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record GameplaySettingsPayload(
        boolean swordEnabled,
        int swordBaseCooldownSeconds,
        int swordPerMobCooldownSeconds,
        boolean veinMiningEnabled,
        boolean armorEnabled,
        int armorThreshold,
        long armorCooldownSeconds
) implements CustomPacketPayload {
    private static final int VERSION = 1;
    public static final Type<GameplaySettingsPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(
            EnderniumConstants.MOD_ID, "gameplay_settings"));
    public static final StreamCodec<RegistryFriendlyByteBuf, GameplaySettingsPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public GameplaySettingsPayload decode(RegistryFriendlyByteBuf buffer) {
                    int version = buffer.readVarInt();
                    if (version != VERSION) {
                        throw new IllegalArgumentException("Unsupported Endernium settings version: " + version);
                    }
                    return new GameplaySettingsPayload(buffer.readBoolean(), buffer.readVarInt(),
                            buffer.readVarInt(), buffer.readBoolean(), buffer.readBoolean(),
                            buffer.readVarInt(), buffer.readVarLong());
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, GameplaySettingsPayload payload) {
                    buffer.writeVarInt(VERSION);
                    buffer.writeBoolean(payload.swordEnabled());
                    buffer.writeVarInt(payload.swordBaseCooldownSeconds());
                    buffer.writeVarInt(payload.swordPerMobCooldownSeconds());
                    buffer.writeBoolean(payload.veinMiningEnabled());
                    buffer.writeBoolean(payload.armorEnabled());
                    buffer.writeVarInt(payload.armorThreshold());
                    buffer.writeVarLong(payload.armorCooldownSeconds());
                }
            };

    public GameplaySettingsPayload {
        swordBaseCooldownSeconds = clampCooldown(swordBaseCooldownSeconds);
        swordPerMobCooldownSeconds = clampCooldown(swordPerMobCooldownSeconds);
        armorThreshold = Math.max(1, Math.min(2048, armorThreshold));
        armorCooldownSeconds = Math.max(1L, Math.min(
                EnderniumGameplayConfig.MAX_COOLDOWN_SECONDS, armorCooldownSeconds));
    }

    public static GameplaySettingsPayload current() {
        EnderniumGameplayConfig.Snapshot settings = EnderniumGameplayConfig.Snapshot.current();
        return new GameplaySettingsPayload(settings.swordAbilityEnabled(),
                settings.swordBaseCooldownSeconds(), settings.swordPerMobCooldownSeconds(),
                settings.toolsVeinMiningEnabled(), settings.armorAbilityEnabled(),
                settings.armorThreshold(), settings.armorCooldownSeconds());
    }

    private static int clampCooldown(int seconds) {
        return Math.max(0, Math.min(EnderniumGameplayConfig.MAX_COOLDOWN_SECONDS, seconds));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
