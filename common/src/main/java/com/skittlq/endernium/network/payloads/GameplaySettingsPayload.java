package com.skittlq.endernium.network.payloads;

import com.skittlq.endernium.EnderniumConstants;
import com.skittlq.endernium.config.EnderniumGameplayConfig;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record GameplaySettingsPayload(
        boolean swordEnabled,
        double swordDamagePerStrikeMultiplier,
        int swordMaxStrikes,
        boolean veinMiningEnabled,
        boolean armorEnabled,
        int armorThreshold,
        double armorMaxStoredDamage,
        int spearStrainDurationSeconds,
        double spearMaximumHealthCostPercent
) implements CustomPacketPayload {
    private static final int VERSION = 2;
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
                    return new GameplaySettingsPayload(buffer.readBoolean(), buffer.readDouble(),
                            buffer.readVarInt(), buffer.readBoolean(), buffer.readBoolean(),
                            buffer.readVarInt(), buffer.readDouble(), buffer.readVarInt(),
                            buffer.readDouble());
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, GameplaySettingsPayload payload) {
                    buffer.writeVarInt(VERSION);
                    buffer.writeBoolean(payload.swordEnabled());
                    buffer.writeDouble(payload.swordDamagePerStrikeMultiplier());
                    buffer.writeVarInt(payload.swordMaxStrikes());
                    buffer.writeBoolean(payload.veinMiningEnabled());
                    buffer.writeBoolean(payload.armorEnabled());
                    buffer.writeVarInt(payload.armorThreshold());
                    buffer.writeDouble(payload.armorMaxStoredDamage());
                    buffer.writeVarInt(payload.spearStrainDurationSeconds());
                    buffer.writeDouble(payload.spearMaximumHealthCostPercent());
                }
            };

    public GameplaySettingsPayload {
        swordDamagePerStrikeMultiplier = finiteClamp(swordDamagePerStrikeMultiplier,
                EnderniumGameplayConfig.MIN_SWORD_DAMAGE_PER_STRIKE_MULTIPLIER,
                EnderniumGameplayConfig.MAX_SWORD_DAMAGE_PER_STRIKE_MULTIPLIER,
                EnderniumGameplayConfig.DEFAULT_SWORD_DAMAGE_PER_STRIKE_MULTIPLIER);
        swordMaxStrikes = Math.max(1, Math.min(EnderniumGameplayConfig.MAX_SWORD_STRIKES,
                swordMaxStrikes));
        armorThreshold = Math.max(1, Math.min(2048, armorThreshold));
        armorMaxStoredDamage = finiteClamp(armorMaxStoredDamage, 1.0D,
                EnderniumGameplayConfig.MAX_ARMOR_STORED_DAMAGE,
                EnderniumGameplayConfig.DEFAULT_ARMOR_MAX_STORED_DAMAGE);
        spearStrainDurationSeconds = Math.max(1,
                Math.min(EnderniumGameplayConfig.MAX_SPEAR_STRAIN_DURATION_SECONDS,
                        spearStrainDurationSeconds));
        spearMaximumHealthCostPercent = finiteClamp(spearMaximumHealthCostPercent,
                0.0D, 100.0D,
                EnderniumGameplayConfig.DEFAULT_SPEAR_MAXIMUM_HEALTH_COST_PERCENT);
    }

    public static GameplaySettingsPayload current() {
        EnderniumGameplayConfig.Snapshot settings = EnderniumGameplayConfig.Snapshot.current();
        return new GameplaySettingsPayload(settings.swordAbilityEnabled(),
                settings.swordDamagePerStrikeMultiplier(), settings.swordMaxStrikes(),
                settings.toolsVeinMiningEnabled(), settings.armorAbilityEnabled(),
                settings.armorThreshold(), settings.armorMaxStoredDamage(),
                settings.spearStrainDurationSeconds(), settings.spearMaximumHealthCostPercent());
    }

    private static double finiteClamp(double value, double minimum, double maximum, double fallback) {
        return Double.isFinite(value) ? Math.max(minimum, Math.min(maximum, value)) : fallback;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
