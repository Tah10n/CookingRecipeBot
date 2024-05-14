package org.example.cooking_recipe_bot.bot.handlers;

import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface UpdateHandler {

    BotApiMethod<?> handle(Update update) throws TelegramApiException;
}
