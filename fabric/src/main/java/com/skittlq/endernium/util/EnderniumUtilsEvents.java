package com.skittlq.endernium.util;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;

public final class EnderniumUtilsEvents {
    private static boolean registered;

    private EnderniumUtilsEvents() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) ->
                EnderniumUtils.onAutoCollectToolBlockBreak(level, player, pos, state, true));
    }
}