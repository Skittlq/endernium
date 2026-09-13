package com.skittlq.endernium.network.payloads;

import com.skittlq.endernium.EnderniumConstants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record AbilityCooldownSyncPayload(Ability ability, long endGameTime, int durationTicks) implements CustomPacketPayload {
    public static final Type<AbilityCooldownSyncPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(EnderniumConstants.MOD_ID, "ability_cooldown_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AbilityCooldownSyncPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public AbilityCooldownSyncPayload decode(RegistryFriendlyByteBuf buffer) {
                    Ability ability = Ability.fromId(buffer.readVarInt());
                    long endGameTime = Math.max(0L, buffer.readLong());
                    int durationTicks = Math.max(0, buffer.readVarInt());
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
        SWORD,
        HORSE,
        SPEAR,
        NAUTILUS;

        private static Ability fromId(int id) {
            if (id < 0 || id >= values().length) {
                throw new IllegalArgumentException("Invalid Endernium ability id: " + id);
            }
            return values()[id];
        }
    }
}
