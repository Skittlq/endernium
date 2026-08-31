package com.skittlq.endernium.vfx;

import com.skittlq.endernium.network.EnderniumNetworking;
import com.skittlq.endernium.network.payloads.DragonDeathVfxPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Tracks the world's first dragon death without modifying the vanilla fight or reward flow. */
public final class DragonDeathVfxTracker {
    private static Session active;
    private static MinecraftServer activeServer;

    private DragonDeathVfxTracker() {
    }

    public static void tick(MinecraftServer server) {
        if (server != activeServer) {
            active = null;
            activeServer = server;
        }

        ServerLevel end = server.getLevel(Level.END);
        if (end == null) {
            active = null;
            return;
        }

        List<? extends EnderDragon> dragons = end.getEntities(EntityTypes.ENDER_DRAGON, dragon -> true);
        if (active == null) {
            var fight = end.getDragonFight();
            if (fight == null || fight.hasPreviouslyKilledDragon()) {
                return;
            }

            for (EnderDragon dragon : dragons) {
                if (dragon.dragonDeathTime > 0 && !dragon.isRemoved()) {
                    active = Session.start(dragon);
                    notifyNewPlayers(end, active);
                    return;
                }
            }
            return;
        }

        EnderDragon tracked = null;
        for (EnderDragon dragon : dragons) {
            if (dragon.getUUID().equals(active.dragonId)) {
                tracked = dragon;
                break;
            }
        }

        if (tracked != null && !tracked.isRemoved()) {
            active.entityId = tracked.getId();
            active.lastOrigin = tracked.position().add(0.0, 2.0, 0.0);
            active.deathTicks = Math.max(active.deathTicks, tracked.dragonDeathTime);
            notifyNewPlayers(end, active);
            return;
        }

        DragonDeathVfxPayload.Action action = active.deathTicks >= 199
                ? DragonDeathVfxPayload.Action.BURST
                : DragonDeathVfxPayload.Action.CANCEL;
        DragonDeathVfxPayload payload = active.payload(
                action,
                action == DragonDeathVfxPayload.Action.BURST ? 200 : active.deathTicks
        );
        for (ServerPlayer player : end.players()) {
            EnderniumNetworking.sendDragonDeathVfx(player, payload);
        }
        active = null;
    }

    public static void clear() {
        active = null;
        activeServer = null;
    }

    private static void notifyNewPlayers(ServerLevel end, Session session) {
        for (ServerPlayer player : end.players()) {
            if (session.notifiedPlayers.add(player.getUUID())) {
                EnderniumNetworking.sendDragonDeathVfx(player,
                        session.payload(DragonDeathVfxPayload.Action.START, session.deathTicks));
            }
        }
    }

    private static final class Session {
        private final UUID dragonId;
        private final long sequenceSeed;
        private final Set<UUID> notifiedPlayers = new HashSet<>();
        private int entityId;
        private Vec3 lastOrigin;
        private int deathTicks;

        private Session(UUID dragonId, int entityId, Vec3 lastOrigin, int deathTicks, long sequenceSeed) {
            this.dragonId = dragonId;
            this.entityId = entityId;
            this.lastOrigin = lastOrigin;
            this.deathTicks = deathTicks;
            this.sequenceSeed = sequenceSeed;
        }

        private static Session start(EnderDragon dragon) {
            UUID id = dragon.getUUID();
            long seed = id.getMostSignificantBits() ^ Long.rotateLeft(id.getLeastSignificantBits(), 23);
            return new Session(
                    id,
                    dragon.getId(),
                    dragon.position().add(0.0, 2.0, 0.0),
                    dragon.dragonDeathTime,
                    seed
            );
        }

        private DragonDeathVfxPayload payload(DragonDeathVfxPayload.Action action, int ticks) {
            return new DragonDeathVfxPayload(action, dragonId, entityId, lastOrigin, ticks, sequenceSeed);
        }
    }
}
