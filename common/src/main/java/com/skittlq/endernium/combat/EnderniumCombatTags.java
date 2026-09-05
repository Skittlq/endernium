package com.skittlq.endernium.combat;

import com.skittlq.endernium.network.EnderniumNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

public final class EnderniumCombatTags {
    public static final int DURATION_TICKS = 200;

    private static final Map<MinecraftServer, Map<TargetingPermission, Long>> TAGS_BY_SERVER = new WeakHashMap<>();

    private EnderniumCombatTags() {
    }

    public static void recordSuccessfulHit(ServerPlayer attacker, ServerPlayer victim) {
        if (attacker == victim || attacker.level().getServer() != victim.level().getServer()) {
            return;
        }

        MinecraftServer server = attacker.level().getServer();
        long expiresAtTick = currentTick(server) + DURATION_TICKS;
        tags(server).put(new TargetingPermission(victim.getUUID(), attacker.getUUID()), expiresAtTick);
        sync(victim);
    }

    public static boolean canTarget(ServerPlayer player, ServerPlayer attacker) {
        if (player == attacker || player.level().getServer() != attacker.level().getServer()) {
            return false;
        }

        MinecraftServer server = player.level().getServer();
        Long expiresAtTick = tags(server).get(new TargetingPermission(player.getUUID(), attacker.getUUID()));
        return expiresAtTick != null && expiresAtTick > currentTick(server);
    }

    public static void playerJoined(ServerPlayer player) {
        sync(player);
    }

    public static void playerDied(ServerPlayer player) {
        removePlayer(player, true);
    }

    public static void playerDisconnected(ServerPlayer player) {
        removePlayer(player, false);
    }

    private static void removePlayer(ServerPlayer player, boolean syncRemovedPlayer) {
        MinecraftServer server = player.level().getServer();
        Map<TargetingPermission, Long> serverTags = TAGS_BY_SERVER.get(server);
        if (serverTags == null) {
            if (syncRemovedPlayer) {
                EnderniumNetworking.sendCombatOpponents(player, Set.of());
            }
            return;
        }

        Set<UUID> affectedPlayers = new HashSet<>();
        UUID playerId = player.getUUID();
        serverTags.keySet().removeIf(permission -> {
            if (!permission.contains(playerId)) {
                return false;
            }
            if (!permission.player().equals(playerId)) {
                affectedPlayers.add(permission.player());
            }
            return true;
        });

        if (syncRemovedPlayer) {
            EnderniumNetworking.sendCombatOpponents(player, Set.of());
        }
        syncOnlinePlayers(server, affectedPlayers);
    }

    public static void tick(MinecraftServer server) {
        Map<TargetingPermission, Long> serverTags = TAGS_BY_SERVER.get(server);
        if (serverTags == null || serverTags.isEmpty()) {
            return;
        }

        long currentTick = currentTick(server);
        Set<UUID> affectedPlayers = new HashSet<>();
        serverTags.entrySet().removeIf(entry -> {
            if (entry.getValue() > currentTick) {
                return false;
            }
            affectedPlayers.add(entry.getKey().player());
            return true;
        });
        syncOnlinePlayers(server, affectedPlayers);
    }

    public static void clear(MinecraftServer server) {
        TAGS_BY_SERVER.remove(server);
    }

    private static void sync(ServerPlayer player) {
        MinecraftServer server = player.level().getServer();
        Map<TargetingPermission, Long> serverTags = TAGS_BY_SERVER.get(server);
        if (serverTags == null || serverTags.isEmpty()) {
            EnderniumNetworking.sendCombatOpponents(player, Set.of());
            return;
        }

        long currentTick = currentTick(server);
        UUID playerId = player.getUUID();
        Set<UUID> opponents = new HashSet<>();
        for (Map.Entry<TargetingPermission, Long> entry : serverTags.entrySet()) {
            TargetingPermission permission = entry.getKey();
            if (entry.getValue() > currentTick && permission.player().equals(playerId)) {
                opponents.add(permission.attacker());
            }
        }
        EnderniumNetworking.sendCombatOpponents(player, opponents);
    }

    private static void syncOnlinePlayers(MinecraftServer server, Set<UUID> playerIds) {
        for (UUID playerId : playerIds) {
            ServerPlayer player = server.getPlayerList().getPlayer(playerId);
            if (player != null) {
                sync(player);
            }
        }
    }

    private static Map<TargetingPermission, Long> tags(MinecraftServer server) {
        return TAGS_BY_SERVER.computeIfAbsent(server, ignored -> new HashMap<>());
    }

    private static long currentTick(MinecraftServer server) {
        return Integer.toUnsignedLong(server.getTickCount());
    }

    private record TargetingPermission(UUID player, UUID attacker) {
        private boolean contains(UUID playerId) {
            return player.equals(playerId) || attacker.equals(playerId);
        }
    }
}
