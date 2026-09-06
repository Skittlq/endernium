package com.skittlq.endernium.network.payloads;

import com.skittlq.endernium.EnderniumConstants;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public record BlessingVfxPayload(
        UUID recipientId,
        int entityId,
        Vec3 origin,
        long seed,
        int recipientIndex,
        int recipientCount
) implements CustomPacketPayload {
    public static final Type<BlessingVfxPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(EnderniumConstants.MOD_ID, "blessing_vfx"));
    public static final StreamCodec<RegistryFriendlyByteBuf, BlessingVfxPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public BlessingVfxPayload decode(RegistryFriendlyByteBuf buffer) {
            UUID recipientId = UUIDUtil.STREAM_CODEC.decode(buffer);
            int entityId = buffer.readVarInt();
            Vec3 origin = Vec3.STREAM_CODEC.decode(buffer);
            long seed = buffer.readLong();
            int recipientIndex = buffer.readVarInt();
            int recipientCount = buffer.readVarInt();
            return new BlessingVfxPayload(recipientId, entityId, origin, seed, recipientIndex, recipientCount);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, BlessingVfxPayload payload) {
            UUIDUtil.STREAM_CODEC.encode(buffer, payload.recipientId());
            buffer.writeVarInt(payload.entityId());
            Vec3.STREAM_CODEC.encode(buffer, payload.origin());
            buffer.writeLong(payload.seed());
            buffer.writeVarInt(payload.recipientIndex());
            buffer.writeVarInt(payload.recipientCount());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
