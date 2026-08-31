package com.skittlq.endernium.network.payloads;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record CombatOpponentsPayload(List<UUID> opponentIds) implements CustomPacketPayload {
    private static final int MAX_OPPONENTS = 1024;

    public static final Type<CombatOpponentsPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("endernium", "combat_opponents"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CombatOpponentsPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public CombatOpponentsPayload decode(RegistryFriendlyByteBuf buffer) {
                    int size = buffer.readVarInt();
                    if (size < 0 || size > MAX_OPPONENTS) {
                        throw new IllegalArgumentException("Invalid Endernium combat opponent count: " + size);
                    }

                    List<UUID> opponentIds = new ArrayList<>(size);
                    for (int index = 0; index < size; index++) {
                        opponentIds.add(buffer.readUUID());
                    }
                    return new CombatOpponentsPayload(opponentIds);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, CombatOpponentsPayload payload) {
                    buffer.writeVarInt(payload.opponentIds().size());
                    for (UUID opponentId : payload.opponentIds()) {
                        buffer.writeUUID(opponentId);
                    }
                }
            };

    public CombatOpponentsPayload {
        opponentIds = List.copyOf(opponentIds);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
