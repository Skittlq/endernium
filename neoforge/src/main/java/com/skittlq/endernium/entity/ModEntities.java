package com.skittlq.endernium.entity;

import com.skittlq.endernium.Endernium;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    public static final DeferredRegister.Entities ENTITY_TYPES =
            DeferredRegister.createEntities(Endernium.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<EnderniumThrownSpear>> THROWN_SPEAR =
            ENTITY_TYPES.registerEntityType(
                    "thrown_spear",
                    EnderniumThrownSpear::new,
                    MobCategory.MISC,
                    builder -> builder.sized(0.35F, 0.35F)
                            .clientTrackingRange(8)
                            .updateInterval(1)
            );

    private ModEntities() {
    }

    public static void register(IEventBus eventBus) {
        EnderniumEntityTypes.bindThrownSpear(THROWN_SPEAR);
        ENTITY_TYPES.register(eventBus);
    }
}
