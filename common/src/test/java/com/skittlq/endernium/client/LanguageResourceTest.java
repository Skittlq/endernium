package com.skittlq.endernium.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LanguageResourceTest {
    private static final String[] LOCALES = {
            "ar_sa", "de_de", "es_es", "fr_fr", "hi_in",
            "ja_jp", "ko_kr", "pt_br", "ru_ru", "zh_cn"
    };

    @Test
    void everyLocaleMatchesTheEnglishKeySet() throws IOException {
        Set<String> englishKeys = readLanguage("en_us").keySet();
        for (String locale : LOCALES) {
            assertEquals(englishKeys, readLanguage(locale).keySet(),
                    () -> locale + " must contain exactly the en_us translation keys");
        }
    }

    private static JsonObject readLanguage(String locale) throws IOException {
        String resource = "/assets/endernium/lang/" + locale + ".json";
        try (InputStream stream = LanguageResourceTest.class.getResourceAsStream(resource)) {
            assertNotNull(stream, "Missing language resource " + resource);
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                    .getAsJsonObject();
        }
    }
}
