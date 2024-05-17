package org.example.cooking_recipe_bot.bot;

import lombok.extern.slf4j.Slf4j;
import org.example.cooking_recipe_bot.bot.constants.BotMessageEnum;
import org.example.cooking_recipe_bot.bot.handlers.CallbackQueryHandler;
import org.example.cooking_recipe_bot.bot.handlers.InlineQueryHandler;
import org.example.cooking_recipe_bot.bot.handlers.MessageHandler;
import org.example.cooking_recipe_bot.bot.handlers.UpdateHandler;
import org.example.cooking_recipe_bot.db.dao.UserDAO;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;

@Slf4j
@Component
public class TelegramFacade {
    private final MessageHandler messageHandler;
    private final CallbackQueryHandler callbackQueryHandler;
    private final UserDAO userDAO;
    private UpdateHandler updateHandler;
    private final InlineQueryHandler inlineQueryHandler;


    public TelegramFacade(MessageHandler messageHandler, CallbackQueryHandler callbackQueryHandler, InlineQueryHandler inlineQueryHandler, UserDAO userDAO) {
        this.messageHandler = messageHandler;
        this.callbackQueryHandler = callbackQueryHandler;
        this.inlineQueryHandler = inlineQueryHandler;
        this.userDAO = userDAO;
    }


    public BotApiMethod<?> handleUpdate(Update update) {
        Long chatId = 0L;
        if (update.hasInlineQuery()) {
            updateHandler = inlineQueryHandler;
            chatId = update.getInlineQuery().getFrom().getId();
        } else if (update.hasCallbackQuery()) {
            updateHandler = callbackQueryHandler;
            chatId = update.getCallbackQuery().getMessage().getChatId();
        } else if (update.hasMessage()) {
            updateHandler = messageHandler;
            chatId = update.getMessage().getChatId();
        } else if (update.hasMyChatMember()) {
            Long userId = update.getMyChatMember().getFrom().getId();
            if (update.getMyChatMember().getNewChatMember().getStatus().equals("kicked")) {
                    userDAO.deleteUser(userId);
            }
        } else {
            log.info("No inline, callback or message in handleUpdate");
            log.info(update.toString());
        }
        try {
            return updateHandler.handle(update);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            log.error(Arrays.toString(e.getStackTrace()));
            return SendMessage.builder().text(BotMessageEnum.EXCEPTION_UPDATE_HANDLE.getMessage()).chatId(chatId).build();
        }

    }
}
