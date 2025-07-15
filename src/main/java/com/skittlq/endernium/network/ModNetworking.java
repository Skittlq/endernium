package com.skittlq.endernium.network;

import com.skittlq.endernium.Endernium;
import com.skittlq.endernium.network.payloads.CameraLerpPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetworking {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Endernium.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    private static int packetId = 0;

    public static void register() {
        CHANNEL.registerMessage(
                packetId++,
                CameraLerpPayload.class,
                CameraLerpPayload::encode,
                CameraLerpPayload::decode,
                CameraLerpPayload::handle
        );
        // Register other packets here as needed
    }
}
