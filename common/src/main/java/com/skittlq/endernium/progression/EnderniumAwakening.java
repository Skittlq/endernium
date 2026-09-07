package com.skittlq.endernium.progression;

import com.skittlq.endernium.network.EnderniumNetworking;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;

/** Persistent per-player permission for Endernium's supernatural abilities. */
public final class EnderniumAwakening {
    private static final Identifier FREE_THE_END = Identifier.withDefaultNamespace("end/kill_dragon");
    private static UnlockStore unlockStore = new UnlockStore() {
        @Override
        public boolean isAwakened(ServerPlayer player) {
            throw new IllegalStateException("Endernium awakening storage has not been bound to a loader yet");
        }

        @Override
        public void setAwakened(ServerPlayer player, boolean awakened) {
            throw new IllegalStateException("Endernium awakening storage has not been bound to a loader yet");
        }
    };
    private static boolean clientAwakened;

    private EnderniumAwakening() {
    }

    public static void bindUnlockStore(UnlockStore store) {
        unlockStore = Objects.requireNonNull(store);
    }

    public static boolean isAwakened(Player player) {
        if (player.level().isClientSide()) {
            return clientAwakened;
        }
        return player instanceof ServerPlayer serverPlayer && unlockStore.isAwakened(serverPlayer);
    }

    public static boolean isAwakened(ServerPlayer player) {
        return unlockStore.isAwakened(player);
    }

    public static void awaken(ServerPlayer player, boolean playReadyEffect) {
        setAwakened(player, true, playReadyEffect);
    }

    public static void setAwakened(ServerPlayer player, boolean awakened, boolean playReadyEffect) {
        unlockStore.setAwakened(player, awakened);
        EnderniumNetworking.sendAwakeningState(player, awakened, playReadyEffect && awakened);
    }

    public static void syncOnLogin(ServerPlayer player) {
        EnderniumNetworking.sendGameplaySettings(player);
        boolean awakened = unlockStore.isAwakened(player);
        boolean pending = EnderniumAwakeningSavedData.get(player.level().getServer()).contains(player.getUUID());
        if (!awakened && !pending && hasVanillaDragonAdvancement(player)) {
            unlockStore.setAwakened(player, true);
            awakened = true;
        }
        EnderniumNetworking.sendAwakeningState(player, awakened, false);
    }

    public static void setClientAwakened(boolean awakened) {
        clientAwakened = awakened;
    }

    public static boolean isClientAwakened() {
        return clientAwakened;
    }

    public static void clearClientState() {
        clientAwakened = false;
    }

    private static boolean hasVanillaDragonAdvancement(ServerPlayer player) {
        AdvancementHolder advancement = player.level().getServer().getAdvancements().get(FREE_THE_END);
        return advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone();
    }

    public interface UnlockStore {
        boolean isAwakened(ServerPlayer player);

        void setAwakened(ServerPlayer player, boolean awakened);
    }
}
