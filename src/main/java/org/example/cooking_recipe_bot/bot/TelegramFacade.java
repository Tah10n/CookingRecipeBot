package org.example.cooking_recipe_bot.bot;

import lombok.extern.slf4j.Slf4j;
import org.example.cooking_recipe_bot.bot.handlers.CallbackQueryHandler;
import org.example.cooking_recipe_bot.bot.handlers.MessageHandler;
import org.example.cooking_recipe_bot.bot.handlers.UpdateHandler;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
public class TelegramFacade {
    final MessageHandler messageHandler;
    final CallbackQueryHandler callbackQueryHandler;
    UpdateHandler updateHandler;

    public TelegramFacade(MessageHandler messageHandler, CallbackQueryHandler callbackQueryHandler) {
        this.messageHandler = messageHandler;
        this.callbackQueryHandler = callbackQueryHandler;
    }


    public BotApiMethod<?> handleUpdate(Update update) {
        if (update.hasCallbackQuery()) {
            updateHandler = callbackQueryHandler;

        } else {
            updateHandler = messageHandler;
        }
        return updateHandler.handle(update);

    }
}
