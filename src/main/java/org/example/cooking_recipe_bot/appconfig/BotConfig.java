package org.example.cooking_recipe_bot.appconfig;

import lombok.Getter;
import lombok.Setter;
import org.example.cooking_recipe_bot.CookBookTelegramBot;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "telegrambot")
public class BotConfig {

    private String webHookPath;
    private String botName;
    private String botToken;

    @Bean
    public CookBookTelegramBot bot() {


        CookBookTelegramBot bot = new CookBookTelegramBot(botToken);
        bot.setBotName(botName);
        bot.setWebHookPath(webHookPath);

        setWebHook();


        return bot;
    }

    private void setWebHook() {
        var telegramUrl = "https://api.telegram.org/bot" + botToken;
        var url = telegramUrl + "/setWebhook?url=" + webHookPath;
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        //System.out.println("response body: " + response.body());
    }

}
