package com.skittlq.endernium.network.payloads;

import com.skittlq.endernium.EnderniumConstants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record VeinMiningStatePayload(
        boolean active,
        long blockEndGameTime,
        int blockDurationTicks
) implements CustomPacketPayload {
    public static final Type<VeinMiningStatePayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(EnderniumConstants.MOD_ID, "vein_mining_state"));
    public static final StreamCodec<RegistryFriendlyByteBuf, VeinMiningStatePayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public VeinMiningStatePayload decode(RegistryFriendlyByteBuf buffer) {
                    return new VeinMiningStatePayload(
                            buffer.readBoolean(),
                            buffer.readLong(),
                            buffer.readVarInt()
                    );
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, VeinMiningStatePayload payload) {
                    buffer.writeBoolean(payload.active());
                    buffer.writeLong(payload.blockEndGameTime());
                    buffer.writeVarInt(payload.blockDurationTicks());
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
