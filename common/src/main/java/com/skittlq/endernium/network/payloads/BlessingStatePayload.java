package com.skittlq.endernium.network.payloads;

import com.skittlq.endernium.EnderniumConstants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record BlessingStatePayload(boolean blessed, boolean playReadyEffect) implements CustomPacketPayload {
    public static final Type<BlessingStatePayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(EnderniumConstants.MOD_ID, "blessing_state"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BlessingStatePayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public BlessingStatePayload decode(RegistryFriendlyByteBuf buffer) {
                    return new BlessingStatePayload(buffer.readBoolean(), buffer.readBoolean());
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, BlessingStatePayload payload) {
                    buffer.writeBoolean(payload.blessed());
                    buffer.writeBoolean(payload.playReadyEffect());
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
