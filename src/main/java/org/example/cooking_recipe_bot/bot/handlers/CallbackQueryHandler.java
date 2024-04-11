package org.example.cooking_recipe_bot.bot.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
public class CallbackQueryHandler implements UpdateHandler {
    @Override
    public SendMessage handle(Update update) {

        final CallbackQuery callbackQuery = update.getCallbackQuery();
        final long chatId = callbackQuery.getMessage().getChatId();
        final long userId = callbackQuery.getFrom().getId();

        SendMessage callBackAnswer = null;

        String data = callbackQuery.getData();

        switch (data) {
            case ("buttonFindRecipe"):
                callBackAnswer = new SendMessage(String.valueOf(chatId), "Введите ключевое слово для поиска рецептов");

                break;
        }
        return callBackAnswer;
    }
}
