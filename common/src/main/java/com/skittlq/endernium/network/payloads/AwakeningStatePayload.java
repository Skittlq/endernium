package com.skittlq.endernium.network.payloads;

import com.skittlq.endernium.EnderniumConstants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record AwakeningStatePayload(boolean awakened, boolean playReadyEffect) implements CustomPacketPayload {
    public static final Type<AwakeningStatePayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(EnderniumConstants.MOD_ID, "awakening_state"));
    public static final StreamCodec<RegistryFriendlyByteBuf, AwakeningStatePayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public AwakeningStatePayload decode(RegistryFriendlyByteBuf buffer) {
                    return new AwakeningStatePayload(buffer.readBoolean(), buffer.readBoolean());
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, AwakeningStatePayload payload) {
                    buffer.writeBoolean(payload.awakened());
                    buffer.writeBoolean(payload.playReadyEffect());
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
