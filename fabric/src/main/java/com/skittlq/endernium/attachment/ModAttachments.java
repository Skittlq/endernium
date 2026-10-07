package com.skittlq.endernium.attachment;

import com.mojang.serialization.Codec;
import com.skittlq.endernium.Endernium;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;

public final class ModAttachments {
    public static final AttachmentType<Float> ENDERNIUM_ARMOR_STORED_DAMAGE = AttachmentRegistry.create(
        Identifier.fromNamespaceAndPath(Endernium.MOD_ID, "endernium_armor_stored_damage"),
        builder -> builder
            .persistent(Codec.FLOAT)
            .initializer(() -> 0.0F)
    );

    public static final AttachmentType<Float> ENDERNIUM_SWORD_STORED_DAMAGE = AttachmentRegistry.create(
        Identifier.fromNamespaceAndPath(Endernium.MOD_ID, "endernium_sword_stored_damage"),
        builder -> builder
            .persistent(Codec.FLOAT)
            .initializer(() -> 0.0F)
    );

    public static final AttachmentType<Long> ENDERNIUM_SPEAR_STRAIN_END_TICK = AttachmentRegistry.create(
        Identifier.fromNamespaceAndPath(Endernium.MOD_ID, "endernium_spear_strain_end_tick"),
        builder -> builder
            .persistent(Codec.LONG)
            .initializer(() -> 0L)
    );

    public static final AttachmentType<Boolean> ENDERNIUM_ABILITIES_BLESSED = AttachmentRegistry.create(
        Identifier.fromNamespaceAndPath(Endernium.MOD_ID, "endernium_abilities_awakened"),
        builder -> builder
            .persistent(Codec.BOOL)
            .initializer(() -> false)
            .copyOnDeath()
    );

    private ModAttachments() {
    }

    public static void initialize() {
        // Intentionally empty; calling this ensures the attachment class is loaded during mod init.
    }
}
