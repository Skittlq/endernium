package com.skittlq.endernium.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public final class EnderniumTickSchedulerEvents {
    private static boolean registered;

    private EnderniumTickSchedulerEvents() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        ServerTickEvents.END_SERVER_TICK.register(server -> EnderniumTickScheduler.tickNow());
    }
}