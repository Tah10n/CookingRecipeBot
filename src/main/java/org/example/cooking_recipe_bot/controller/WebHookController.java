package org.example.cooking_recipe_bot.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cooking_recipe_bot.bot.CookBookTelegramBot;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@AllArgsConstructor
@RestController
public class WebHookController {
    private final CookBookTelegramBot bot;


    @PostMapping("/")
    public BotApiMethod<?> onUpdateReceived(@RequestBody Update update) {
        return bot.consumeUpdate(update);
    }
}
