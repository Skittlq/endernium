package com.skittlq.endernium.progression;

import com.skittlq.endernium.attachment.ModAttachments;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;

public final class EnderniumBlessingHandler {
    private static boolean registered;

    private EnderniumBlessingHandler() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        EnderniumBlessing.bindUnlockStore(new EnderniumBlessing.UnlockStore() {
            @Override
            public boolean isBlessed(net.minecraft.server.level.ServerPlayer player) {
                return player.getAttachedOrCreate(ModAttachments.ENDERNIUM_ABILITIES_BLESSED);
            }

            @Override
            public void setBlessed(net.minecraft.server.level.ServerPlayer player, boolean blessed) {
                player.setAttached(ModAttachments.ENDERNIUM_ABILITIES_BLESSED, blessed);
            }
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                EnderniumBlessing.syncOnLogin(handler.getPlayer()));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) ->
                EnderniumBlessing.syncOnLogin(newPlayer));
    }
}
