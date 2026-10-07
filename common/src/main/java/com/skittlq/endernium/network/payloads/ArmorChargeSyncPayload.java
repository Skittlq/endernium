package com.skittlq.endernium.network.payloads;

import com.skittlq.endernium.EnderniumConstants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ArmorChargeSyncPayload(float storedDamage) implements CustomPacketPayload {
    public static final Type<ArmorChargeSyncPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(
            EnderniumConstants.MOD_ID, "armor_charge_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ArmorChargeSyncPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public ArmorChargeSyncPayload decode(RegistryFriendlyByteBuf buffer) {
                    return new ArmorChargeSyncPayload(buffer.readFloat());
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, ArmorChargeSyncPayload payload) {
                    buffer.writeFloat(payload.storedDamage());
                }
            };

    public ArmorChargeSyncPayload {
        storedDamage = Float.isFinite(storedDamage) ? Math.max(0.0F, storedDamage) : 0.0F;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
