package com.skittlq.endernium.util;

import com.skittlq.endernium.Endernium;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = Endernium.MODID)
public final class EnderniumTickSchedulerEvents {
    private EnderniumTickSchedulerEvents() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        EnderniumTickScheduler.tickNow();
    }
}