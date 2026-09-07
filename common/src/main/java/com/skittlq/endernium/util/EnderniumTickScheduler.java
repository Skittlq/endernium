package com.skittlq.endernium.util;

import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.UUID;

public final class EnderniumTickScheduler {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<MinecraftServer, ServerTasks> TASKS_BY_SERVER = new HashMap<>();

    private EnderniumTickScheduler() {
    }

    public static int schedule(MinecraftServer server, UUID owner, Runnable action, int delayTicks) {
        ServerTasks tasks = TASKS_BY_SERVER.computeIfAbsent(server, ignored -> new ServerTasks());
        int id = tasks.nextId();
        long currentTick = server.overworld().getGameTime();
        long delay = Math.max(1, delayTicks);
        long dueTick = currentTick > Long.MAX_VALUE - delay ? Long.MAX_VALUE : currentTick + delay;
        ScheduledTask task = new ScheduledTask(id, owner, dueTick, action);
        tasks.queue.add(task);
        tasks.byId.put(id, task);
        return id;
    }

    public static void cancel(MinecraftServer server, int id) {
        ServerTasks tasks = TASKS_BY_SERVER.get(server);
        if (tasks == null) {
            return;
        }
        ScheduledTask task = tasks.byId.remove(id);
        if (task != null) {
            task.cancelled = true;
        }
    }

    public static void cancelOwner(MinecraftServer server, UUID owner) {
        ServerTasks tasks = TASKS_BY_SERVER.get(server);
        if (tasks == null) {
            return;
        }
        for (ScheduledTask task : new ArrayList<>(tasks.byId.values())) {
            if (task.owner.equals(owner)) {
                task.cancelled = true;
                tasks.byId.remove(task.id);
            }
        }
    }

    public static void tick(MinecraftServer server) {
        ServerTasks tasks = TASKS_BY_SERVER.get(server);
        if (tasks == null) {
            return;
        }
        long currentTick = server.overworld().getGameTime();
        while (!tasks.queue.isEmpty() && tasks.queue.peek().dueTick <= currentTick) {
            ScheduledTask task = tasks.queue.poll();
            if (task.cancelled || tasks.byId.remove(task.id) == null) {
                continue;
            }
            try {
                task.action.run();
            } catch (RuntimeException exception) {
                LOGGER.error("Scheduled Endernium task {} failed", task.id, exception);
            }
        }
        if (tasks.byId.isEmpty()) {
            TASKS_BY_SERVER.remove(server);
        }
    }

    public static void clear(MinecraftServer server) {
        TASKS_BY_SERVER.remove(server);
    }

    private static final class ServerTasks {
        private final PriorityQueue<ScheduledTask> queue = new PriorityQueue<>(
                Comparator.comparingLong((ScheduledTask task) -> task.dueTick).thenComparingInt(task -> task.id));
        private final Map<Integer, ScheduledTask> byId = new HashMap<>();
        private int nextId = 1;

        private int nextId() {
            while (byId.containsKey(nextId)) {
                nextId = nextId == Integer.MAX_VALUE ? 1 : nextId + 1;
            }
            int result = nextId;
            nextId = nextId == Integer.MAX_VALUE ? 1 : nextId + 1;
            return result;
        }
    }

    private static final class ScheduledTask {
        private final int id;
        private final UUID owner;
        private final long dueTick;
        private final Runnable action;
        private boolean cancelled;

        private ScheduledTask(int id, UUID owner, long dueTick, Runnable action) {
            this.id = id;
            this.owner = owner;
            this.dueTick = dueTick;
            this.action = action;
        }
    }
}
