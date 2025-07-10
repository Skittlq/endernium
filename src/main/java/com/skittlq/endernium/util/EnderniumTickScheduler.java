package com.skittlq.endernium.util;

import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

@EventBusSubscriber
public class EnderniumTickScheduler {
    private static final Queue<ScheduledTask> TASKS = new LinkedList<>();

    public static void schedule(Runnable action, int delayTicks) {
        TASKS.add(new ScheduledTask(action, delayTicks));
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        Iterator<ScheduledTask> it = TASKS.iterator();
        while (it.hasNext()) {
            ScheduledTask task = it.next();
            task.ticksLeft--;
            if (task.ticksLeft <= 0) {
                try { task.action.run(); } catch (Exception e) { e.printStackTrace(); }
                it.remove();
            }
        }
    }

    private static class ScheduledTask {
        final Runnable action;
        int ticksLeft;

        ScheduledTask(Runnable action, int ticks) {
            this.action = action;
            this.ticksLeft = ticks;
        }
    }
}
