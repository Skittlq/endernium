package com.skittlq.endernium.util;

import com.skittlq.endernium.Endernium;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public final class EnderniumTickScheduler {
    private static final Queue<ScheduledTask> TASKS = new LinkedList<>();
    private static final AtomicInteger NEXT_ID = new AtomicInteger(1);
    private static final Map<Integer, ScheduledTask> TASK_MAP = new HashMap<>();
    private static boolean registered;

    private EnderniumTickScheduler() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        ServerTickEvents.END_SERVER_TICK.register(server -> onServerTick());
    }

    public static int schedule(Runnable action, int delayTicks) {
        int id = NEXT_ID.getAndIncrement();
        ScheduledTask task = new ScheduledTask(id, action, delayTicks);
        TASKS.add(task);
        TASK_MAP.put(id, task);
        return id;
    }

    public static void cancel(int id) {
        ScheduledTask task = TASK_MAP.remove(id);
        if (task != null) {
            TASKS.remove(task);
        }
    }

    private static void onServerTick() {
        ScheduledTask[] tasksSnapshot = TASKS.toArray(new ScheduledTask[0]);
        for (ScheduledTask task : tasksSnapshot) {
            task.ticksLeft--;
            if (task.ticksLeft <= 0) {
                try {
                    task.action.run();
                } catch (Exception exception) {
                    Endernium.LOGGER.error("Scheduled Endernium task {} failed", task.id, exception);
                }
                TASKS.remove(task);
                TASK_MAP.remove(task.id);
            }
        }
    }

    private static final class ScheduledTask {
        private final int id;
        private final Runnable action;
        private int ticksLeft;

        private ScheduledTask(int id, Runnable action, int ticksLeft) {
            this.id = id;
            this.action = action;
            this.ticksLeft = ticksLeft;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof ScheduledTask other)) {
                return false;
            }
            return id == other.id;
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(id);
        }
    }
}

