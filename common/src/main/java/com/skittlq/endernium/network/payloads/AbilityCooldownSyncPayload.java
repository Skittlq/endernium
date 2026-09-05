package com.skittlq.endernium.network.payloads;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record AbilityCooldownSyncPayload(Ability ability, long endGameTime, int durationTicks) implements CustomPacketPayload {
    public static final Type<AbilityCooldownSyncPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("endernium", "ability_cooldown_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AbilityCooldownSyncPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public AbilityCooldownSyncPayload decode(RegistryFriendlyByteBuf buffer) {
                    Ability ability = Ability.values()[buffer.readVarInt()];
                    long endGameTime = buffer.readLong();
                    int durationTicks = buffer.readVarInt();
                    return new AbilityCooldownSyncPayload(ability, endGameTime, durationTicks);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, AbilityCooldownSyncPayload payload) {
                    buffer.writeVarInt(payload.ability().ordinal());
                    buffer.writeLong(payload.endGameTime());
                    buffer.writeVarInt(payload.durationTicks());
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public enum Ability {
        ARMOR,
        SWORD
    }
}
