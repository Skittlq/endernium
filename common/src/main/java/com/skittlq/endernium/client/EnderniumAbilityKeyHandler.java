package com.skittlq.endernium.client;

import com.skittlq.endernium.progression.EnderniumBlessing;
import com.skittlq.endernium.item.armor.EnderniumHorseArmorAbility;
import com.skittlq.endernium.item.armor.EnderniumNautilusArmorAbility;
import com.skittlq.endernium.item.tools.EnderniumSpear;
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
                    boolean horseBlocked = EnderniumHorseArmorAbility.hasArmoredHorseMount(client.player)
                            && EnderniumClientCooldowns.isHorseOnCooldown(client.player.level().getGameTime());
                    boolean spearBlocked = EnderniumSpear.isHeldBy(client.player)
                            && EnderniumClientCooldowns.isSpearOnCooldown(client.player.level().getGameTime());
                    boolean nautilusBlocked = EnderniumNautilusArmorAbility.hasArmoredNautilusMount(client.player)
                            && EnderniumClientCooldowns.isNautilusOnCooldown(client.player.level().getGameTime());
                    if (!horseBlocked && !spearBlocked && !nautilusBlocked) {
                        sendActivation.run();
                    }
                } else {
                    EnderniumClientBehavior.playLockedAbilityCue(client);
                }
                handledForCurrentHold = true;
            }
        }
    }
}
