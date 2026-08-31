package com.skittlq.endernium.util;

import com.skittlq.endernium.Endernium;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import com.skittlq.endernium.vfx.DragonDeathVfxTracker;

@EventBusSubscriber(modid = Endernium.MODID)
public final class EnderniumTickSchedulerEvents {
    private EnderniumTickSchedulerEvents() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        EnderniumTickScheduler.tickNow();
        DragonDeathVfxTracker.tick(event.getServer());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        DragonDeathVfxTracker.clear();
    }
}
