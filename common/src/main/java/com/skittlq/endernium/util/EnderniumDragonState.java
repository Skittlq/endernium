package com.skittlq.endernium.util;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public final class EnderniumDragonState {
    private EnderniumDragonState() {
    }

    public static boolean hasDragonBeenDefeated(ServerLevel level) {
        return hasDragonBeenDefeated(level.getServer());
    }

    public static boolean hasDragonBeenDefeated(MinecraftServer server) {
        ServerLevel endLevel = server.getLevel(Level.END);
        if (endLevel == null) {
            return false;
        }

        var dragonFight = endLevel.getDragonFight();
        return dragonFight != null && dragonFight.hasPreviouslyKilledDragon();
    }
}
