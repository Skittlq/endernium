package com.skittlq.endernium.loot;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockLootResourceTest {
    @Test
    void blockLootTablesUseTwentySixThreeConditionAndModifierSchema() throws Exception {
        JsonObject block = load("endernium_block");
        JsonObject blockPool = block.getAsJsonArray("pools").get(0).getAsJsonObject();
        assertFalse(blockPool.has("conditions"));
        assertEquals("minecraft:survives_explosion",
                blockPool.getAsJsonObject("condition").get("type").getAsString());

        JsonObject ore = load("endernium_ore");
        JsonArray children = ore.getAsJsonArray("pools")
                .get(0).getAsJsonObject()
                .getAsJsonArray("entries")
                .get(0).getAsJsonObject()
                .getAsJsonArray("children");

        JsonObject silkTouchEntry = children.get(0).getAsJsonObject();
        assertFalse(silkTouchEntry.has("conditions"));
        assertEquals("minecraft:match_tool",
                silkTouchEntry.getAsJsonObject("condition").get("type").getAsString());

        JsonObject dustEntry = children.get(1).getAsJsonObject();
        assertFalse(dustEntry.has("functions"));
        JsonArray modifiers = dustEntry.getAsJsonArray("modifier");
        assertEquals(3, modifiers.size());
        assertTrue(modifiers.asList().stream()
                .map(element -> element.getAsJsonObject())
                .allMatch(modifier -> modifier.has("type")));
    }

    private static JsonObject load(String name) throws Exception {
        String path = "/data/endernium/loot_table/blocks/" + name + ".json";
        var stream = BlockLootResourceTest.class.getResourceAsStream(path);
        assertNotNull(stream, path);
        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }
}
