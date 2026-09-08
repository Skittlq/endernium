package com.skittlq.endernium.progression;

import com.skittlq.endernium.network.EnderniumNetworking;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;

/** Persistent per-player permission for Endernium's supernatural abilities. */
public final class EnderniumBlessing {
    private static final Identifier FREE_THE_END = Identifier.withDefaultNamespace("end/kill_dragon");
    private static UnlockStore unlockStore = new UnlockStore() {
        @Override
        public boolean isBlessed(ServerPlayer player) {
            throw new IllegalStateException("Endernium blessing storage has not been bound to a loader yet");
        }

        @Override
        public void setBlessed(ServerPlayer player, boolean blessed) {
            throw new IllegalStateException("Endernium blessing storage has not been bound to a loader yet");
        }
    };
    private static boolean clientBlessed;

    private EnderniumBlessing() {
    }

    public static void bindUnlockStore(UnlockStore store) {
        unlockStore = Objects.requireNonNull(store);
    }

    public static boolean isBlessed(Player player) {
        if (player.level().isClientSide()) {
            return clientBlessed;
        }
        return player instanceof ServerPlayer serverPlayer && unlockStore.isBlessed(serverPlayer);
    }

    public static boolean isBlessed(ServerPlayer player) {
        return unlockStore.isBlessed(player);
    }

    public static void bless(ServerPlayer player, boolean playReadyEffect) {
        setBlessed(player, true, playReadyEffect);
    }

    public static void setBlessed(ServerPlayer player, boolean blessed, boolean playReadyEffect) {
        unlockStore.setBlessed(player, blessed);
        EnderniumNetworking.sendBlessingState(player, blessed, playReadyEffect && blessed);
    }

    public static void syncOnLogin(ServerPlayer player) {
        EnderniumNetworking.sendGameplaySettings(player);
        boolean blessed = unlockStore.isBlessed(player);
        boolean pending = EnderniumBlessingSavedData.get(player.level().getServer()).contains(player.getUUID());
        if (!blessed && !pending && hasVanillaDragonAdvancement(player)) {
            unlockStore.setBlessed(player, true);
            blessed = true;
        }
        EnderniumNetworking.sendBlessingState(player, blessed, false);
    }

    public static void setClientBlessed(boolean blessed) {
        clientBlessed = blessed;
    }

    public static boolean isClientBlessed() {
        return clientBlessed;
    }

    public static void clearClientState() {
        clientBlessed = false;
    }

    private static boolean hasVanillaDragonAdvancement(ServerPlayer player) {
        AdvancementHolder advancement = player.level().getServer().getAdvancements().get(FREE_THE_END);
        return advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone();
    }

    public interface UnlockStore {
        boolean isBlessed(ServerPlayer player);

        void setBlessed(ServerPlayer player, boolean blessed);
    }
}
