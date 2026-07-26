package com.skittlq.endernium.network.payloads;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record EnderniumAbilityPayload() implements CustomPacketPayload {
    public static final EnderniumAbilityPayload INSTANCE = new EnderniumAbilityPayload();
    public static final Type<EnderniumAbilityPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("endernium", "activate_ability"));
    public static final StreamCodec<RegistryFriendlyByteBuf, EnderniumAbilityPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
