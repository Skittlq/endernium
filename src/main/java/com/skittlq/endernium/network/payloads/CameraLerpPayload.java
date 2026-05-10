package com.skittlq.endernium.network.payloads;

import com.skittlq.endernium.Endernium;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record CameraLerpPayload(float targetYaw, float targetPitch, int durationTicks) implements CustomPacketPayload {

    public static final Type<CameraLerpPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Endernium.MODID, "camera_lerp"));

    public static final StreamCodec<FriendlyByteBuf, CameraLerpPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, CameraLerpPayload::targetYaw,
            ByteBufCodecs.FLOAT, CameraLerpPayload::targetPitch,
            ByteBufCodecs.VAR_INT, CameraLerpPayload::durationTicks,
            CameraLerpPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
