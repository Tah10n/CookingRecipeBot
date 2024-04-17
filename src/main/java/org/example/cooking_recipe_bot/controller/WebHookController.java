package org.example.cooking_recipe_bot.controller;

import lombok.AllArgsConstructor;
import org.example.cooking_recipe_bot.bot.CookBookTelegramBot;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@AllArgsConstructor
@RestController
public class WebHookController {
    private final CookBookTelegramBot bot;


    @PostMapping("/")
    public BotApiMethod<?> onUpdateReceived(@RequestBody Update update) {
        return bot.consumeUpdate(update);
    }
}
