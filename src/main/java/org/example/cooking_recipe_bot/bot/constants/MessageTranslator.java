package org.example.cooking_recipe_bot.bot.constants;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class MessageTranslator {

    private final Map<String, JSONObject> translations = new HashMap<>();

    public MessageTranslator() {
        try {
            loadTranslations();
        } catch (IOException e) {
            log.error("Failed to load translations", e);
        }
    }

    public void loadTranslations() throws IOException {
        translations.put("en", new JSONObject(new String(Files.readAllBytes(Paths.get("src/main/resources/languages/en.json")))));
        translations.put("ru", new JSONObject(new String(Files.readAllBytes(Paths.get("src/main/resources/languages/ru.json")))));
    }

    public String getTranslation(String languageCode, String key) {
        JSONObject translation = translations.getOrDefault(languageCode, translations.get("en"));
        return translation.optString(key,"No translation found for key: " + key);
    }

    public String getMessage(String messageEnumName, String languageCode) {
        String message = getTranslation(languageCode,messageEnumName);
        if (message == null) {
            message = getTranslation("en", messageEnumName);
        }

        return message;
    }
}
