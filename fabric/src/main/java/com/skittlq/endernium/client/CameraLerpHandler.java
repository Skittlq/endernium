package com.skittlq.endernium.client;

import com.skittlq.endernium.network.payloads.CameraLerpPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public final class CameraLerpHandler {
    private static boolean lerping = false;
    private static float startYaw;
    private static float startPitch;
    private static float targetYaw;
    private static float targetPitch;
    private static int ticksElapsed;
    private static int lerpDuration;

    private CameraLerpHandler() {
    }

    public static void onCameraLerpPacket(CameraLerpPayload payload) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }
        lerping = true;
        startYaw = client.player.getYRot();
        startPitch = client.player.getXRot();
        targetYaw = payload.targetYaw();
        targetPitch = payload.targetPitch();
        lerpDuration = Math.max(1, payload.durationTicks());
        ticksElapsed = 0;
    }

    public static void clientTick(Minecraft client) {
        if (!lerping || client.player == null) {
            return;
        }
        ticksElapsed++;
        float t = Math.min(1.0F, ticksElapsed / (float) lerpDuration);
        t = t * t * (3.0F - 2.0F * t);

        float currentYaw = lerpAngle(startYaw, targetYaw, t);
        float currentPitch = lerp(startPitch, targetPitch, t);

        Player player = client.player;
        player.setYRot(currentYaw);
        player.setXRot(currentPitch);
        player.yRotO = currentYaw;
        player.xRotO = currentPitch;

        if (t >= 1.0F) {
            lerping = false;
        }
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private static float lerpAngle(float a, float b, float t) {
        float diff = ((b - a + 540.0F) % 360.0F) - 180.0F;
        return a + diff * t;
    }
}
