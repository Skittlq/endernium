package com.skittlq.endernium.item.tools;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skittlq.endernium.entity.EnderniumThrownSpear;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Spear throws canceled by a dedicated-server disconnect, persisted until they can be returned. */
public final class EnderniumSpearPendingReturns extends SavedData {
    private static final Codec<PendingReturn> RETURN_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("owner").forGetter(PendingReturn::owner),
            ItemStack.CODEC.fieldOf("stack").forGetter(PendingReturn::stack),
            Codec.BOOL.fieldOf("off_hand").forGetter(PendingReturn::offHand)
    ).apply(instance, PendingReturn::new));
    private static final Codec<EnderniumSpearPendingReturns> CODEC = RETURN_CODEC.listOf()
            .optionalFieldOf("returns", List.of())
            .xmap(EnderniumSpearPendingReturns::new, data -> List.copyOf(data.pending))
            .codec();
    private static final SavedDataType<EnderniumSpearPendingReturns> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("endernium", "pending_spear_returns"),
            EnderniumSpearPendingReturns::new,
            CODEC,
            DataFixTypes.LEVEL
    );

    private final List<PendingReturn> pending;

    public EnderniumSpearPendingReturns() {
        this(List.of());
    }

    private EnderniumSpearPendingReturns(List<PendingReturn> pending) {
        this.pending = new ArrayList<>(pending);
    }

    private static EnderniumSpearPendingReturns get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public static void onDisconnect(ServerPlayer player) {
        MinecraftServer server = player.level().getServer();
        if (!server.isDedicatedServer()) {
            return;
        }

        EnderniumSpearPendingReturns returns = get(server);
        for (ServerLevel level : server.getAllLevels()) {
            List<EnderniumThrownSpear> spears = new ArrayList<>();
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof EnderniumThrownSpear spear && spear.belongsTo(player.getUUID())) {
                    spears.add(spear);
                }
            }
            for (EnderniumThrownSpear spear : spears) {
                InteractionHand hand = spear.returnHand();
                ItemStack stack = spear.takeForDisconnect(player);
                if (!stack.isEmpty()) {
                    returns.pending.add(new PendingReturn(player.getUUID(), stack,
                            hand == InteractionHand.OFF_HAND));
                    returns.setDirty();
                }
            }
        }
    }

    public static void onLogin(ServerPlayer player) {
        get(player.level().getServer()).deliver(player);
    }

    public static void onServerTick(MinecraftServer server) {
        EnderniumSpearPendingReturns returns = get(server);
        if (returns.pending.isEmpty()) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.connection.isAcceptingMessages()) {
                returns.deliver(player);
            }
        }
    }

    private void deliver(ServerPlayer player) {
        if (!player.isAlive()) {
            return;
        }
        boolean changed = false;
        for (int i = 0; i < pending.size(); ) {
            PendingReturn entry = pending.get(i);
            if (!entry.owner().equals(player.getUUID())) {
                i++;
                continue;
            }

            InteractionHand hand = entry.offHand() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            ItemStack stack = entry.stack().copy();
            if (player.getItemInHand(hand).isEmpty()) {
                player.setItemInHand(hand, stack);
            } else if (!player.getInventory().add(stack)) {
                // Keep the return queued rather than dropping a valuable item.
                i++;
                continue;
            }
            pending.remove(i);
            changed = true;
            EnderniumSpear.beginReturnCooldown(player);
        }
        if (changed) {
            setDirty();
        }
    }

    private record PendingReturn(UUID owner, ItemStack stack, boolean offHand) {
    }
}
