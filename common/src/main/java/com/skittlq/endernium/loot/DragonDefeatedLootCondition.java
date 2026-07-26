package com.skittlq.endernium.loot;

import com.mojang.serialization.MapCodec;
import com.skittlq.endernium.util.EnderniumDragonState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public final class DragonDefeatedLootCondition implements LootItemCondition {
    public static final DragonDefeatedLootCondition INSTANCE = new DragonDefeatedLootCondition();
    public static final MapCodec<DragonDefeatedLootCondition> CODEC = MapCodec.unit(INSTANCE);

    private DragonDefeatedLootCondition() {
    }

    public static LootItemCondition.Builder dragonDefeated() {
        return () -> INSTANCE;
    }

    @Override
    public boolean test(LootContext lootContext) {
        return EnderniumDragonState.hasDragonBeenDefeated(lootContext.getLevel());
    }

    @Override
    public MapCodec<? extends LootItemCondition> codec() {
        return CODEC;
    }
}