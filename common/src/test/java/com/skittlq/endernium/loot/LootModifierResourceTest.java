package com.skittlq.endernium.loot;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LootModifierResourceTest {
    private static final List<String> MODIFIERS = List.of(
            "endernium_dust_in_end_city",
            "endernium_shard_in_end_city",
            "endernium_upgrade_in_end_city"
    );

    @Test
    void endCityModifiersUseNeoForgeTwentySixThreeConditionSchema() throws Exception {
        for (String modifier : MODIFIERS) {
            String path = "/data/endernium/loot_modifiers/" + modifier + ".json";
            var stream = LootModifierResourceTest.class.getResourceAsStream(path);
            assertNotNull(stream, path);

            JsonObject root;
            try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                root = JsonParser.parseReader(reader).getAsJsonObject();
            }

            assertTrue(root.has("condition"), modifier);
            JsonObject condition = root.getAsJsonObject("condition");
            assertEquals("minecraft:all_of", condition.get("type").getAsString(), modifier);
            JsonArray terms = condition.getAsJsonArray("terms");
            assertEquals(3, terms.size(), modifier);
            for (var term : terms) {
                assertTrue(term.getAsJsonObject().has("type"), modifier);
            }
            assertTrue(terms.asList().stream()
                    .map(element -> element.getAsJsonObject())
                    .anyMatch(term -> "neoforge:loot_table_id".equals(term.get("type").getAsString())
                            && "minecraft:chests/end_city_treasure".equals(term.get("loot_table_id").getAsString())),
                    modifier);
        }
    }
}
