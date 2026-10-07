package com.skittlq.endernium.item.tools;

import com.skittlq.endernium.attachment.ModAttachments;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
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
        EnderniumSword.bindChargeStore(new EnderniumSword.ChargeStore() {
            @Override
            public float getStoredDamage(Player player) {
                return player.getAttachedOrCreate(ModAttachments.ENDERNIUM_SWORD_STORED_DAMAGE);
            }

            @Override
            public void setStoredDamage(Player player, float storedDamage) {
                player.setAttached(ModAttachments.ENDERNIUM_SWORD_STORED_DAMAGE, storedDamage);
            }
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                EnderniumSword.syncCharge(handler.getPlayer()));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) ->
                EnderniumSword.syncCharge(newPlayer));
    }
}
