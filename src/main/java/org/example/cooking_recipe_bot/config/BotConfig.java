package org.example.cooking_recipe_bot.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
@Getter
@Component
public class BotConfig {

    @Value("${telegrambot.webhookPath}")
    private String botPath;
    @Value("${telegrambot.botName}")
    private String botName;
    @Value("${telegrambot.botToken}")
    private String botToken;
    @Value("${telegrambot.botOwner}")
    private String botOwner;




    public HttpResponse<String> setWebhook() {
        var telegramUrl = "https://api.telegram.org/bot" + getBotToken();
        var url = telegramUrl + "/setWebhook?url=" + getBotPath();
        final HttpClient client = HttpClient.newBuilder().build();
        HttpRequest request = null;
        try {
            request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return response;
    }

    public HttpResponse<String> deleteWebhook() {
        var telegramUrl = "https://api.telegram.org/bot" + getBotToken();
        var url = telegramUrl + "/deleteWebhook?url=" + getBotPath();
        final HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage(), e);
        }
        return response;
    }
}
