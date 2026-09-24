package com.skittlq.endernium.loot;

import com.skittlq.endernium.item.ModItems;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

public final class ModLootModifiers {
    private static final Identifier END_CITY_TREASURE =
            Identifier.withDefaultNamespace("chests/end_city_treasure");
    private static boolean registered;

    private ModLootModifiers() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;

        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (!END_CITY_TREASURE.equals(key.identifier())) {
                return;
            }
            addPool(tableBuilder, ModItems.ENDERNIUM_DUST, 0.5F, 1, 4);
            addPool(tableBuilder, ModItems.ENDERNIUM_SHARD, 0.3F, 1, 1);
            addPool(tableBuilder, ModItems.ENDERNIUM_UPGRADE_SMITHING_TEMPLATE, 0.15F, 1, 1);
        });
    }

    private static void addPool(
            net.minecraft.world.level.storage.loot.LootTable.Builder tableBuilder,
            Item item,
            float chance,
            int minCount,
            int maxCount
    ) {
        LootPool.Builder pool = LootPool.lootPool()
                .setRolls(ContextIntProviders.exactly(1))
                .when(DragonDefeatedLootCondition.dragonDefeated())
                .when(LootItemRandomChanceCondition.randomChance(chance));
        var itemEntry = LootItem.lootTableItem(item).setWeight(1);
        if (minCount != 1 || maxCount != 1) {
            itemEntry.apply(SetItemCountFunction.setCount(
                    minCount == maxCount
                            ? ContextIntProviders.exactly(minCount)
                            : ContextIntProviders.between(minCount, maxCount)
            ));
        }
        tableBuilder.withPool(pool.add(itemEntry));
    }
}
