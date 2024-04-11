package org.example.cooking_recipe_bot.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Getter
@Component
public class BotConfig {

    @Value("${telegrambot.webhookPath}")
    private String webHookPath;
    @Value("${telegrambot.botName}")
    private String botName;
    @Value("${telegrambot.botToken}")
    private String botToken;

}
