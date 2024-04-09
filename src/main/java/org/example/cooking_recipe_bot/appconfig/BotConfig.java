package org.example.cooking_recipe_bot.appconfig;

import lombok.Getter;
import lombok.Setter;
import org.example.cooking_recipe_bot.CookBookTelegramBot;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
        CookBookTelegramBot bot = new CookBookTelegramBot();
        bot.setBotToken(botToken);
        bot.setBotName(botName);
        bot.setWebHookPath(webHookPath);
        return bot;
    }

}
