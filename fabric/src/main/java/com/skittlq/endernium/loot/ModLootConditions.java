package com.skittlq.endernium.loot;

import com.mojang.serialization.MapCodec;
import com.skittlq.endernium.Endernium;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public final class ModLootConditions {
    public static final MapCodec<DragonDefeatedLootCondition> DRAGON_DEFEATED = registerDragonDefeated();

    private ModLootConditions() {
    }

    private static MapCodec<DragonDefeatedLootCondition> registerDragonDefeated() {
        return Registry.register(
                BuiltInRegistries.LOOT_CONDITION_TYPE,
                Identifier.fromNamespaceAndPath(Endernium.MOD_ID, EnderniumLootConditions.DRAGON_DEFEATED_ID),
                DragonDefeatedLootCondition.CODEC
        );
    }

    public static void register() {
        MapCodec<? extends LootItemCondition> ignored = DRAGON_DEFEATED;
    }
}