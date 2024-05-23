package org.example.cooking_recipe_bot.bot;

import lombok.extern.slf4j.Slf4j;
import org.example.cooking_recipe_bot.bot.handlers.CallbackQueryHandler;
import org.example.cooking_recipe_bot.bot.handlers.InlineQueryHandler;
import org.example.cooking_recipe_bot.bot.handlers.MessageHandler;
import org.example.cooking_recipe_bot.bot.handlers.UpdateHandler;
import org.example.cooking_recipe_bot.db.dao.UserDAO;
import org.example.cooking_recipe_bot.db.entity.User;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;
import java.util.Optional;

@Slf4j
@Component
public class TelegramFacade {
    private final MessageHandler messageHandler;
    private final CallbackQueryHandler callbackQueryHandler;
    private final UserDAO userDAO;
    private final InlineQueryHandler inlineQueryHandler;


    public TelegramFacade(MessageHandler messageHandler, CallbackQueryHandler callbackQueryHandler, InlineQueryHandler inlineQueryHandler, UserDAO userDAO) {
        this.messageHandler = messageHandler;
        this.callbackQueryHandler = callbackQueryHandler;
        this.inlineQueryHandler = inlineQueryHandler;
        this.userDAO = userDAO;
    }


    public BotApiMethod<?> handleUpdate(Update update) {
        UpdateHandler updateHandler;
        if (update.hasInlineQuery()) {
            updateHandler = inlineQueryHandler;
        } else if (update.hasCallbackQuery()) {
            updateHandler = callbackQueryHandler;
        } else if (update.hasMessage()) {
            updateHandler = messageHandler;
        } else if (update.hasMyChatMember()) {
            Long userId = update.getMyChatMember().getFrom().getId();
            if (update.getMyChatMember().getNewChatMember().getStatus().equals("kicked")) {
                Optional<User> user = userDAO.findById(userId);
                if (user.isPresent()) {
                    user.get().setIsUnsubscribed(true);
                    userDAO.saveUser(user.get());
                }

            }
            return null;
        } else {
            log.info("No inline, callback or message in handleUpdate");
            log.info(update.toString());
            return null;
        }
        try {
            return updateHandler.handle(update);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            log.error(Arrays.toString(e.getStackTrace()));
            return null;
        }

    }
}
