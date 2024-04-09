package org.example.cooking_recipe_bot.appconfig;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.example.cooking_recipe_bot.CookBookTelegramBot;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "telegrambot")
public class BotConfig {

    private String webHookPath;
    private String botName;
    private String botToken;

    @Bean
    public SetWebhook setWebhookInstance() {
        return SetWebhook.builder().url(webHookPath).build();
    }

    @Bean
    public CookBookTelegramBot springWebhookBot(SetWebhook setWebhook) {

        CookBookTelegramBot bot = new CookBookTelegramBot(setWebhook, botToken);
        bot.setBotName(botName);
        bot.setWebHookPath(webHookPath);

        return bot;
    }

}
