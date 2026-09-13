package com.skittlq.endernium.entity;

import com.skittlq.endernium.Endernium;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntities {
    private static final Identifier THROWN_SPEAR_ID =
            Identifier.fromNamespaceAndPath(Endernium.MOD_ID, "thrown_spear");
    public static final EntityType<EnderniumThrownSpear> THROWN_SPEAR = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            THROWN_SPEAR_ID,
            EntityType.Builder.<EnderniumThrownSpear>of(EnderniumThrownSpear::new, MobCategory.MISC)
                    .sized(0.35F, 0.35F)
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, THROWN_SPEAR_ID))
    );

    private ModEntities() {
    }

    public static void register() {
        EnderniumEntityTypes.bindThrownSpear(() -> THROWN_SPEAR);
    }
}
