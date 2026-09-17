package com.skittlq.endernium.network.payloads;

import com.skittlq.endernium.EnderniumConstants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record CameraLerpPayload(float targetYaw, float targetPitch, int durationTicks) implements CustomPacketPayload {
    private static final int MAX_DURATION_TICKS = 200;
    public static final Type<CameraLerpPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(
            EnderniumConstants.MOD_ID, "camera_lerp"));

    @SuppressWarnings("null")
    public static final StreamCodec<RegistryFriendlyByteBuf, CameraLerpPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, CameraLerpPayload::targetYaw,
            ByteBufCodecs.FLOAT, CameraLerpPayload::targetPitch,
            ByteBufCodecs.VAR_INT, CameraLerpPayload::durationTicks,
            CameraLerpPayload::new
    );

    public CameraLerpPayload {
        targetYaw = Float.isFinite(targetYaw) ? targetYaw : 0.0F;
        targetPitch = Float.isFinite(targetPitch) ? Math.max(-90.0F, Math.min(90.0F, targetPitch)) : 0.0F;
        durationTicks = Math.max(0, Math.min(MAX_DURATION_TICKS, durationTicks));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
