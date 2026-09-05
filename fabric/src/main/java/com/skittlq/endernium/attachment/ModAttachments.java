package com.skittlq.endernium.attachment;

import com.mojang.serialization.Codec;
import com.skittlq.endernium.Endernium;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;

public final class ModAttachments {
    public static final AttachmentType<Long> ENDERNIUM_ARMOR_LAST_USED_TICK = AttachmentRegistry.create(
        Identifier.fromNamespaceAndPath(Endernium.MOD_ID, "endernium_armor_last_used_tick"),
        builder -> builder
            .persistent(Codec.LONG)
            .initializer(() -> 0L)
            .copyOnDeath()
    );

    public static final AttachmentType<Long> ENDERNIUM_SWORD_COOLDOWN_END_TICK = AttachmentRegistry.create(
        Identifier.fromNamespaceAndPath(Endernium.MOD_ID, "endernium_sword_cooldown_end_tick"),
        builder -> builder
            .persistent(Codec.LONG)
            .initializer(() -> 0L)
    );

    public static final AttachmentType<Integer> ENDERNIUM_SWORD_COOLDOWN_DURATION_TICKS = AttachmentRegistry.create(
        Identifier.fromNamespaceAndPath(Endernium.MOD_ID, "endernium_sword_cooldown_duration_ticks"),
        builder -> builder
            .persistent(Codec.INT)
            .initializer(() -> 0)
    );

    private ModAttachments() {
    }

    public static void initialize() {
        // Intentionally empty; calling this ensures the attachment class is loaded during mod init.
    }
}
