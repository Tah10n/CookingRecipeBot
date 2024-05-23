package org.example.cooking_recipe_bot.bot;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@Component
public class MessageTranslator {

    private final Map<String, JSONObject> translations = new HashMap<>();


    @PostConstruct
    public void loadTranslations() throws IOException {
        try (Stream<Path> enStream = Files.find(Paths.get(""), 5, (path, basicFileAttributes) -> basicFileAttributes.isRegularFile() && path.toString().endsWith("en.json"))) {
            Path enPath = enStream.findFirst()
                    .orElseThrow(() -> new IOException("en.json not found"));
            translations.put("en", readJsonFile(enPath));
        }

        try (Stream<Path> ruStream = Files.find(Paths.get(""), 5, (path, basicFileAttributes) -> basicFileAttributes.isRegularFile() && path.toString().endsWith("ru.json"))) {
            Path ruPath = ruStream.findFirst()
                    .orElseThrow(() -> new IOException("ru.json not found"));
            translations.put("ru", readJsonFile(ruPath));
        }
    }

    private JSONObject readJsonFile(Path path) throws IOException {
        byte[] fileBytes = Files.readAllBytes(path);
        return new JSONObject(new String(fileBytes));
    }

    public String getTranslation(String languageCode, String key) {
        JSONObject translation = translations.getOrDefault(languageCode, translations.get("en"));
        return translation.optString(key, "No translation found for key: " + key);
    }

    public String getMessage(String messageEnumName, String languageCode) {
        String message = getTranslation(languageCode, messageEnumName);
        if (message == null) {
            message = getTranslation("en", messageEnumName);
        }

        return message;
    }
}
