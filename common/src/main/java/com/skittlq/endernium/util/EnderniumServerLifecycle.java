package com.skittlq.endernium.util;

import com.skittlq.endernium.item.EnderniumAbilityHandler;
import com.skittlq.endernium.item.armor.EnderniumHorseArmorAbility;
import com.skittlq.endernium.item.tools.EnderniumSword;
import com.skittlq.endernium.progression.DragonBlessingTracker;
import com.skittlq.endernium.vfx.DragonDeathVfxTracker;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class EnderniumServerLifecycle {
    private EnderniumServerLifecycle() {
    }

    public static void onEndTick(MinecraftServer server) {
        EnderniumTickScheduler.tick(server);
        DragonDeathVfxTracker.tick(server);
        DragonBlessingTracker.tick(server);
        EnderniumHorseArmorAbility.tick(server);
    }

    public static void onServerStopped(MinecraftServer server) {
        EnderniumTickScheduler.clear(server);
        EnderniumUtils.clearServerState(server);
        EnderniumSword.clearServerState(server);
        com.skittlq.endernium.item.tools.EnderniumSpear.clearServerState(server);
        EnderniumAbilityHandler.clearServerState(server);
        DragonDeathVfxTracker.clear();
        DragonBlessingTracker.clear();
    }

    public static void cancelPlayerState(ServerPlayer player) {
        MinecraftServer server = player.level().getServer();
        EnderniumSword.cancelSequence(player, false);
        EnderniumUtils.cancelAllVeinMiningOperations(player);
        EnderniumAbilityHandler.clearPlayerState(server, player.getUUID());
        EnderniumTickScheduler.cancelOwner(server, player.getUUID());
    }
}
