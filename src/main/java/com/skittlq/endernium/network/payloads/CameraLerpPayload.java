package com.skittlq.endernium.network.payloads;

import com.skittlq.endernium.Endernium;
import com.skittlq.endernium.client.CameraLerpHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CameraLerpPayload {
    public static final ResourceLocation ID = new ResourceLocation(Endernium.MODID, "camera_lerp");

    public final float targetYaw;
    public final float targetPitch;
    public final int durationTicks;

    public CameraLerpPayload(float targetYaw, float targetPitch, int durationTicks) {
        this.targetYaw = targetYaw;
        this.targetPitch = targetPitch;
        this.durationTicks = durationTicks;
    }

    public static CameraLerpPayload decode(FriendlyByteBuf buf) {
        float yaw = buf.readFloat();
        float pitch = buf.readFloat();
        int duration = buf.readVarInt();
        return new CameraLerpPayload(yaw, pitch, duration);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeFloat(targetYaw);
        buf.writeFloat(targetPitch);
        buf.writeVarInt(durationTicks);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            CameraLerpHandler.onCameraLerpPacket(this);
        });
        ctx.get().setPacketHandled(true);
    }

}
