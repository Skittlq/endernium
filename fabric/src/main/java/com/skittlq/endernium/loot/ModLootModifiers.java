package com.skittlq.endernium.loot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.skittlq.endernium.Endernium;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ModLootModifiers {
    private static final Gson GSON = new GsonBuilder().create();
    private static final String GLOBAL_LOOT_MODIFIERS_INDEX = "data/neoforge/loot_modifiers/global_loot_modifiers.json";
    private static final String LOOT_MODIFIER_PATH_TEMPLATE = "data/%s/loot_modifiers/%s.json";
    private static final Map<Identifier, List<LootModifierDefinition>> MODIFIERS_BY_TABLE = new HashMap<>();
    private static boolean registered;

    private ModLootModifiers() {
    }

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;

        reloadModifiers();
        LootTableEvents.MODIFY.register(ModLootModifiers::modifyLootTable);
    }

    private static void reloadModifiers() {
        MODIFIERS_BY_TABLE.clear();
        try (InputStream indexStream = ModLootModifiers.class.getClassLoader().getResourceAsStream(GLOBAL_LOOT_MODIFIERS_INDEX)) {
            if (indexStream == null) {
                Endernium.LOGGER.warn("Could not find {}", GLOBAL_LOOT_MODIFIERS_INDEX);
                return;
            }

            JsonObject indexJson = GSON.fromJson(new InputStreamReader(indexStream, StandardCharsets.UTF_8), JsonObject.class);
            JsonArray entries = GsonHelper.getAsJsonArray(indexJson, "entries", new JsonArray());
            for (JsonElement element : entries) {
                if (!element.isJsonPrimitive()) {
                    continue;
                }

                Identifier modifierId = Identifier.parse(element.getAsString());
                String resourcePath = LOOT_MODIFIER_PATH_TEMPLATE.formatted(modifierId.getNamespace(), modifierId.getPath());
                try (InputStream modifierStream = ModLootModifiers.class.getClassLoader().getResourceAsStream(resourcePath)) {
                    if (modifierStream == null) {
                        Endernium.LOGGER.warn("Could not find loot modifier resource {}", resourcePath);
                        continue;
                    }

                    JsonObject json = GSON.fromJson(new InputStreamReader(modifierStream, StandardCharsets.UTF_8), JsonObject.class);
                    LootModifierDefinition definition = parse(modifierId, json);
                    if (definition != null) {
                        MODIFIERS_BY_TABLE.computeIfAbsent(definition.lootTableId(), ignored -> new ArrayList<>()).add(definition);
                    }
                } catch (Exception exception) {
                    Endernium.LOGGER.error("Failed to load loot modifier {}", modifierId, exception);
                }
            }
        } catch (Exception exception) {
            Endernium.LOGGER.error("Failed to load Fabric loot modifier index", exception);
        }

        Endernium.LOGGER.info("Loaded {} Endernium loot modifier target sets", MODIFIERS_BY_TABLE.size());
    }

    private static void modifyLootTable(net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> key,
                                        net.minecraft.world.level.storage.loot.LootTable.Builder tableBuilder,
                                        net.fabricmc.fabric.api.loot.v3.LootTableSource source,
                                        HolderLookup.Provider registries) {
        List<LootModifierDefinition> definitions = MODIFIERS_BY_TABLE.getOrDefault(key.identifier(), Collections.emptyList());
        for (LootModifierDefinition definition : definitions) {
            LootPool.Builder pool = LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F));
            if (definition.chance() < 1.0F) {
                pool.when(LootItemRandomChanceCondition.randomChance(definition.chance()));
            }

            var itemBuilder = LootItem.lootTableItem(definition.item()).setWeight(1);
            if (definition.minCount() == definition.maxCount()) {
                if (definition.minCount() != 1) {
                    itemBuilder.apply(SetItemCountFunction.setCount(ConstantValue.exactly(definition.minCount())));
                }
            } else {
                itemBuilder.apply(SetItemCountFunction.setCount(UniformGenerator.between(definition.minCount(), definition.maxCount())));
            }

            pool.add(itemBuilder);
            tableBuilder.withPool(pool);
        }
    }

    private static LootModifierDefinition parse(Identifier resourceId, JsonObject json) {
        String type = GsonHelper.getAsString(json, "type", "");
        if (!"endernium:add_item".equals(type)) {
            return null;
        }

        Identifier lootTableId = null;
        float chance = 1.0F;
        JsonArray conditions = GsonHelper.getAsJsonArray(json, "conditions", new JsonArray());
        for (JsonElement element : conditions) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject condition = element.getAsJsonObject();
            String conditionType = GsonHelper.getAsString(condition, "condition", "");
            if ("neoforge:loot_table_id".equals(conditionType)) {
                lootTableId = Identifier.parse(GsonHelper.getAsString(condition, "loot_table_id"));
            } else if ("minecraft:random_chance".equals(conditionType)) {
                chance = GsonHelper.getAsFloat(condition, "chance", 1.0F);
            }
        }

        if (lootTableId == null) {
            Endernium.LOGGER.warn("Skipping loot modifier {} because it has no supported loot table target", resourceId);
            return null;
        }

        Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(GsonHelper.getAsString(json, "item")));
        if (item == Items.AIR) {
            Endernium.LOGGER.warn("Skipping loot modifier {} because item {} could not be resolved", resourceId, GsonHelper.getAsString(json, "item"));
            return null;
        }

        int minCount = 1;
        int maxCount = 1;
        JsonArray functions = GsonHelper.getAsJsonArray(json, "functions", new JsonArray());
        for (JsonElement element : functions) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject function = element.getAsJsonObject();
            if (!"minecraft:set_count".equals(GsonHelper.getAsString(function, "function", ""))) {
                continue;
            }
            JsonObject count = GsonHelper.getAsJsonObject(function, "count");
            minCount = GsonHelper.getAsInt(count, "min", 1);
            maxCount = GsonHelper.getAsInt(count, "max", minCount);
        }

        return new LootModifierDefinition(lootTableId, item, chance, minCount, maxCount);
    }

    private record LootModifierDefinition(Identifier lootTableId, Item item, float chance, int minCount, int maxCount) {
    }
}
