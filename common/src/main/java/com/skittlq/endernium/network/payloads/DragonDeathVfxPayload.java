package com.skittlq.endernium.network.payloads;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public record DragonDeathVfxPayload(
        Action action,
        UUID dragonId,
        int entityId,
        Vec3 origin,
        int deathTicks,
        long sequenceSeed
) implements CustomPacketPayload {
    public static final Type<DragonDeathVfxPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath("endernium", "dragon_death_vfx")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, DragonDeathVfxPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT.map(Action::byId, Action::ordinal), DragonDeathVfxPayload::action,
            UUIDUtil.STREAM_CODEC, DragonDeathVfxPayload::dragonId,
            ByteBufCodecs.VAR_INT, DragonDeathVfxPayload::entityId,
            Vec3.STREAM_CODEC, DragonDeathVfxPayload::origin,
            ByteBufCodecs.VAR_INT, DragonDeathVfxPayload::deathTicks,
            ByteBufCodecs.LONG, DragonDeathVfxPayload::sequenceSeed,
            DragonDeathVfxPayload::new
    );

    public DragonDeathVfxPayload {
        deathTicks = Math.max(0, Math.min(200, deathTicks));
        if (!Double.isFinite(origin.x) || !Double.isFinite(origin.y) || !Double.isFinite(origin.z)) {
            origin = Vec3.ZERO;
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public enum Action {
        START,
        BURST,
        CANCEL;

        private static Action byId(int id) {
            return id >= 0 && id < values().length ? values()[id] : CANCEL;
        }
    }
}
