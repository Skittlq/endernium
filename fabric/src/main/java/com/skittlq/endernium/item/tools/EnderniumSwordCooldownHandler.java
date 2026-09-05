package com.skittlq.endernium.item.tools;

import com.skittlq.endernium.attachment.ModAttachments;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.world.entity.player.Player;

public final class EnderniumSwordCooldownHandler {
    private static boolean registered;

    private EnderniumSwordCooldownHandler() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        EnderniumSword.bindCooldownStore(new EnderniumSword.CooldownStore() {
            @Override
            public long getCooldownEndGameTime(Player player) {
                return player.getAttachedOrCreate(ModAttachments.ENDERNIUM_SWORD_COOLDOWN_END_TICK);
            }

            @Override
            public void setCooldownEndGameTime(Player player, long gameTime) {
                player.setAttached(ModAttachments.ENDERNIUM_SWORD_COOLDOWN_END_TICK, gameTime);
            }

            @Override
            public int getCooldownDurationTicks(Player player) {
                return player.getAttachedOrCreate(ModAttachments.ENDERNIUM_SWORD_COOLDOWN_DURATION_TICKS);
            }

            @Override
            public void setCooldownDurationTicks(Player player, int durationTicks) {
                player.setAttached(ModAttachments.ENDERNIUM_SWORD_COOLDOWN_DURATION_TICKS, durationTicks);
            }
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                EnderniumSword.syncCooldownOnLogin(handler.getPlayer()));
    }
}
