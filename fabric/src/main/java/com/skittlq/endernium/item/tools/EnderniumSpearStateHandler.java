package com.skittlq.endernium.item.tools;

import com.skittlq.endernium.attachment.ModAttachments;
import net.minecraft.world.entity.player.Player;

public final class EnderniumSpearStateHandler {
    private static boolean registered;

    private EnderniumSpearStateHandler() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        EnderniumSpear.bindStrainStore(new EnderniumSpear.StrainStore() {
            @Override
            public long getEndGameTime(Player player) {
                return player.getAttachedOrCreate(ModAttachments.ENDERNIUM_SPEAR_STRAIN_END_TICK);
            }

            @Override
            public void setEndGameTime(Player player, long gameTime) {
                player.setAttached(ModAttachments.ENDERNIUM_SPEAR_STRAIN_END_TICK, gameTime);
            }
        });
    }
}
