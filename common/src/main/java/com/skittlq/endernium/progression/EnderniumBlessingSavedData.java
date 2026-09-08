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
public final class EnderniumBlessingSavedData extends SavedData {
    private static final Codec<EnderniumBlessingSavedData> CODEC = UUIDUtil.CODEC_SET
            .optionalFieldOf("pending_awakenings", Set.of())
            .xmap(EnderniumBlessingSavedData::new, data -> Set.copyOf(data.pendingBlessings))
            .codec();
    private static final SavedDataType<EnderniumBlessingSavedData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("endernium", "pending_awakenings"),
            EnderniumBlessingSavedData::new,
            CODEC,
            DataFixTypes.LEVEL
    );

    private final Set<UUID> pendingBlessings;

    public EnderniumBlessingSavedData() {
        this(Set.of());
    }

    private EnderniumBlessingSavedData(Set<UUID> pendingBlessings) {
        this.pendingBlessings = new HashSet<>(pendingBlessings);
    }

    public static EnderniumBlessingSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean contains(UUID playerId) {
        return pendingBlessings.contains(playerId);
    }

    public void addAll(Set<UUID> playerIds) {
        if (pendingBlessings.addAll(playerIds)) {
            setDirty();
        }
    }

    public void remove(UUID playerId) {
        if (pendingBlessings.remove(playerId)) {
            setDirty();
        }
    }
}
