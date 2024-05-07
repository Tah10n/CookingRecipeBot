package org.example.cooking_recipe_bot.config;

import lombok.AllArgsConstructor;
import org.example.cooking_recipe_bot.bot.CookBookTelegramBot;
import org.example.cooking_recipe_bot.bot.TelegramFacade;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.generics.TelegramClient;

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
    public CookBookTelegramBot springWebhookBot(TelegramFacade telegramFacade) {

        CookBookTelegramBot bot = new CookBookTelegramBot(botConfig.getBotPath(), telegramFacade::handleUpdate, botConfig::setWebhook, botConfig::deleteWebhook);


        HttpResponse response = botConfig.setWebhook();
        int responseOnSetWebhookCode = response.statusCode();
        if (responseOnSetWebhookCode != 200) {
            throw new RuntimeException("Can't set webhook: " + response.body().toString());
        }
        return bot;
    }

    @Bean
    public TelegramClient getTelegramClient(BotConfig botConfig) {
        return new OkHttpTelegramClient(botConfig.getBotToken());
    }

}
