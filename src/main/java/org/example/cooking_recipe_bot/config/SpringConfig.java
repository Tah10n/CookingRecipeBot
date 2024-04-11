package org.example.cooking_recipe_bot.config;

import lombok.AllArgsConstructor;
import org.example.cooking_recipe_bot.bot.CookBookTelegramBot;
import org.example.cooking_recipe_bot.bot.TelegramFacade;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Configuration
@AllArgsConstructor
public class SpringConfig {
    private final BotConfig botConfig;

    @Bean
    public SetWebhook setWebhookInstance() {
        return SetWebhook.builder().url(botConfig.getWebHookPath()).build();
    }

    @Bean
    public CookBookTelegramBot springWebhookBot(SetWebhook setWebhook, TelegramFacade telegramFacade) {

        CookBookTelegramBot bot = new CookBookTelegramBot(setWebhook, botConfig.getBotToken(), telegramFacade);
        bot.setBotName(botConfig.getBotName());
        bot.setWebHookPath(botConfig.getWebHookPath());

        HttpResponse response = sendSetWebHookRequest();
        int responseOnSetWebhookCode = response.statusCode();
        if(responseOnSetWebhookCode != 200) {
            throw new RuntimeException("Can't set webhook: " + response.body().toString());
        }
        return bot;
    }

    private HttpResponse sendSetWebHookRequest() {
        var telegramUrl = "https://api.telegram.org/bot" + botConfig.getBotToken();
        var url = telegramUrl + "/setWebhook?url=" + botConfig.getWebHookPath();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
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
}
