package org.example.cooking_recipe_bot.bot.handlers;

import lombok.extern.slf4j.Slf4j;
import org.example.cooking_recipe_bot.bot.keyboards.InlineKeyboardMaker;
import org.example.cooking_recipe_bot.db.dao.UserDAO;
import org.example.cooking_recipe_bot.db.entity.User;
import org.example.cooking_recipe_bot.utils.UserParser;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Component
public class CallbackQueryHandler implements UpdateHandler {
    UserDAO userDAO;
    InlineKeyboardMaker inlineKeyboardMaker;
    TelegramClient telegramClient;

    public CallbackQueryHandler(UserDAO userDAO, InlineKeyboardMaker inlineKeyboardMaker, TelegramClient telegramClient) {
        this.userDAO = userDAO;
        this.inlineKeyboardMaker = inlineKeyboardMaker;
        this.telegramClient = telegramClient;
    }

    @Override
    public SendMessage handle(Update update) throws TelegramApiException {

        final CallbackQuery callbackQuery = update.getCallbackQuery();

        String data = callbackQuery.getData();
        long chatId = callbackQuery.getMessage().getChatId();
        long userId = getUserIdFromMessage((Message) callbackQuery.getMessage());
        int messageId = callbackQuery.getMessage().getMessageId();
        SendMessage sendMessage;
        switch (data) {
            case ("delete_user_button"):
                sendMessage = SendMessage.builder().chatId(chatId).text("Пользователь " + userDAO.getUserById(userId).getUserName() + " удален").build();
                DeleteMessage deleteMessage = DeleteMessage.builder().chatId(chatId).messageId(messageId).build();
                telegramClient.execute(sendMessage);
                telegramClient.execute(deleteMessage);
                userDAO.deleteUser(userId);
                break;
            case ("set_admin_button"):
                userDAO.setAdmin(userId);
                sendMessage = SendMessage.builder().chatId(chatId).text("Пользователь " + userDAO.getUserById(userId).getUserName() + " стал администратором").build();
                telegramClient.execute(sendMessage);
                EditMessageText editMessageText = EditMessageText.builder().chatId(chatId).messageId(messageId).text(userDAO.getUserById(userId).toString()).replyMarkup(inlineKeyboardMaker.getUserAdminKeyboard()).build();
                telegramClient.execute(editMessageText);
                break;
            case ("unset_admin_button"):
                userDAO.unsetAdmin(userId);
                sendMessage = SendMessage.builder().chatId(chatId).text("Пользователь " + userDAO.getUserById(userId).getUserName() + " больше не администратор").build();
                telegramClient.execute(sendMessage);
                editMessageText = EditMessageText.builder().chatId(chatId).messageId(messageId).text(userDAO.getUserById(userId).toString()).replyMarkup(inlineKeyboardMaker.getUserKeyboard()).build();
                telegramClient.execute(editMessageText);
                break;
        }

        return null;
    }

    private long getUserIdFromMessage(Message message) {
        String text = message.getText();
        User user = UserParser.parseUserFromString(text);
        return user.getId();

    }


}
