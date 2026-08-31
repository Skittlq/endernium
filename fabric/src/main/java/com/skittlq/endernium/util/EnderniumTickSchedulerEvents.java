package com.skittlq.endernium.util;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import com.skittlq.endernium.vfx.DragonDeathVfxTracker;

public final class EnderniumTickSchedulerEvents {
    private static boolean registered;

    private EnderniumTickSchedulerEvents() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            EnderniumTickScheduler.tickNow();
            DragonDeathVfxTracker.tick(server);
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> DragonDeathVfxTracker.clear());
    }
}
