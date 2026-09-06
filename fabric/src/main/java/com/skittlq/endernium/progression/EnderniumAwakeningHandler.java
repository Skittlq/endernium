package com.skittlq.endernium.progression;

import com.skittlq.endernium.attachment.ModAttachments;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public final class EnderniumAwakeningHandler {
    private static boolean registered;

    private EnderniumAwakeningHandler() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        EnderniumAwakening.bindUnlockStore(new EnderniumAwakening.UnlockStore() {
            @Override
            public boolean isAwakened(net.minecraft.server.level.ServerPlayer player) {
                return player.getAttachedOrCreate(ModAttachments.ENDERNIUM_ABILITIES_AWAKENED);
            }

            @Override
            public void setAwakened(net.minecraft.server.level.ServerPlayer player, boolean awakened) {
                player.setAttached(ModAttachments.ENDERNIUM_ABILITIES_AWAKENED, awakened);
            }
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                EnderniumAwakening.syncOnLogin(handler.getPlayer()));
    }
}
