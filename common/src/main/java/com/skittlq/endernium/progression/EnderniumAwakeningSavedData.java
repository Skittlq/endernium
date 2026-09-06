package com.skittlq.endernium.progression;

import com.mojang.serialization.Codec;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Global queue for qualified players who have not yet received their visible blessing. */
public final class EnderniumAwakeningSavedData extends SavedData {
    private static final Codec<EnderniumAwakeningSavedData> CODEC = UUIDUtil.CODEC_SET
            .optionalFieldOf("pending_awakenings", Set.of())
            .xmap(EnderniumAwakeningSavedData::new, data -> Set.copyOf(data.pendingAwakenings))
            .codec();
    private static final SavedDataType<EnderniumAwakeningSavedData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("endernium", "pending_awakenings"),
            EnderniumAwakeningSavedData::new,
            CODEC,
            DataFixTypes.LEVEL
    );

    private final Set<UUID> pendingAwakenings;

    public EnderniumAwakeningSavedData() {
        this(Set.of());
    }

    private EnderniumAwakeningSavedData(Set<UUID> pendingAwakenings) {
        this.pendingAwakenings = new HashSet<>(pendingAwakenings);
    }

    public static EnderniumAwakeningSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean contains(UUID playerId) {
        return pendingAwakenings.contains(playerId);
    }

    public void addAll(Set<UUID> playerIds) {
        if (pendingAwakenings.addAll(playerIds)) {
            setDirty();
        }
    }

    public void remove(UUID playerId) {
        if (pendingAwakenings.remove(playerId)) {
            setDirty();
        }
    }
}
