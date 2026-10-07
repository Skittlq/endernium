package com.skittlq.endernium.network.payloads;

import com.skittlq.endernium.EnderniumConstants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record SwordChargeSyncPayload(float storedDamage) implements CustomPacketPayload {
    public static final Type<SwordChargeSyncPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(
            EnderniumConstants.MOD_ID, "sword_charge_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SwordChargeSyncPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public SwordChargeSyncPayload decode(RegistryFriendlyByteBuf buffer) {
                    return new SwordChargeSyncPayload(buffer.readFloat());
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, SwordChargeSyncPayload payload) {
                    buffer.writeFloat(payload.storedDamage());
                }
            };

    public SwordChargeSyncPayload {
        storedDamage = Float.isFinite(storedDamage) ? Math.max(0.0F, storedDamage) : 0.0F;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
