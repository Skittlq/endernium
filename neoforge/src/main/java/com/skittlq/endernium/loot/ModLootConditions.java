package com.skittlq.endernium.loot;

import com.mojang.serialization.MapCodec;
import com.skittlq.endernium.Endernium;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModLootConditions {
    public static final DeferredRegister<MapCodec<? extends LootItemCondition>> LOOT_CONDITION_TYPES =
            DeferredRegister.create(BuiltInRegistries.LOOT_CONDITION_TYPE, Endernium.MODID);

    public static final DeferredHolder<MapCodec<? extends LootItemCondition>, MapCodec<DragonDefeatedLootCondition>> DRAGON_DEFEATED =
            registerDragonDefeated();

    private ModLootConditions() {
    }

    private static DeferredHolder<MapCodec<? extends LootItemCondition>, MapCodec<DragonDefeatedLootCondition>> registerDragonDefeated() {
        return LOOT_CONDITION_TYPES.register(
                EnderniumLootConditions.DRAGON_DEFEATED_ID,
                () -> DragonDefeatedLootCondition.CODEC
        );
    }

    public static void register(IEventBus eventBus) {
        LOOT_CONDITION_TYPES.register(eventBus);
    }
}