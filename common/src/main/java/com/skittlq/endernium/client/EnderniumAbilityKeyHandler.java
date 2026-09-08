package com.skittlq.endernium.client;

import com.skittlq.endernium.progression.EnderniumBlessing;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import java.util.Objects;

public final class EnderniumAbilityKeyHandler {
    private static boolean handledForCurrentHold;

    private EnderniumAbilityKeyHandler() {
    }

    public static void tick(Minecraft client, KeyMapping abilityKey, Runnable sendActivation) {
        Objects.requireNonNull(client);
        Objects.requireNonNull(abilityKey);
        Objects.requireNonNull(sendActivation);
        if (!abilityKey.isDown()) {
            handledForCurrentHold = false;
        }
        while (abilityKey.consumeClick()) {
            if (!handledForCurrentHold && client.player != null && client.getConnection() != null) {
                if (EnderniumBlessing.isClientBlessed()) {
                    sendActivation.run();
                } else {
                    EnderniumClientBehavior.playLockedAbilityCue(client);
                }
                handledForCurrentHold = true;
            }
        }
    }
}
