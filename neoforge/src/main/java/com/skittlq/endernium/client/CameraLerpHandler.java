package com.skittlq.endernium.client;

import com.skittlq.endernium.network.payloads.CameraLerpPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class CameraLerpHandler {
    private static boolean lerping = false;
    private static float startYaw, startPitch;
    private static float targetYaw, targetPitch;
    private static int ticksElapsed, lerpDuration;

    public static void onCameraLerpPacket(CameraLerpPayload payload) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        lerping = true;
        startYaw = mc.player.getYRot();
        startPitch = mc.player.getXRot();
        targetYaw = payload.targetYaw();
        targetPitch = payload.targetPitch();
        lerpDuration = payload.durationTicks();
        ticksElapsed = 0;
    }

    public static void clientTick(Minecraft mc) {
        if (!lerping || mc.player == null) return;
        ticksElapsed++;
        float t = Math.min(1.0f, ticksElapsed / (float) lerpDuration);
        t = t * t * (3 - 2 * t);

        float currentYaw = lerpAngle(startYaw, targetYaw, t);
        float currentPitch = lerp(startPitch, targetPitch, t);

        Player player = mc.player;
        player.setYRot(currentYaw);
        player.setXRot(currentPitch);
        player.yRotO = currentYaw;
        player.xRotO = currentPitch;

        if (t >= 1.0f) {
            lerping = false;
        }
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private static float lerpAngle(float a, float b, float t) {
        float diff = ((b - a + 540) % 360) - 180;
        return a + diff * t;
    }
}
