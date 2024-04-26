package org.example.cooking_recipe_bot.bot.handlers;

import org.example.cooking_recipe_bot.bot.BotState;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

public interface UpdateHandler {

    BotApiMethod<?> handle(Update update) throws TelegramApiException;
}
