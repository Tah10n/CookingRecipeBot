package org.example.cooking_recipe_bot.bot.handlers;

import lombok.extern.slf4j.Slf4j;
import org.example.cooking_recipe_bot.bot.BotState;
import org.example.cooking_recipe_bot.bot.keyboards.InlineKeyboardMaker;
import org.example.cooking_recipe_bot.db.dao.BotStateContextDAO;
import org.example.cooking_recipe_bot.db.dao.RecipeDAO;
import org.example.cooking_recipe_bot.db.dao.UserDAO;
import org.example.cooking_recipe_bot.db.entity.BotStateContext;
import org.example.cooking_recipe_bot.db.entity.User;
import org.example.cooking_recipe_bot.utils.UserParser;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
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
    RecipeDAO recipeDAO;
    BotStateContextDAO botStateContextDAO;

    public CallbackQueryHandler(UserDAO userDAO, InlineKeyboardMaker inlineKeyboardMaker, TelegramClient telegramClient, RecipeDAO recipeDAO, BotStateContextDAO botStateContextDAO) {
        this.userDAO = userDAO;
        this.inlineKeyboardMaker = inlineKeyboardMaker;
        this.telegramClient = telegramClient;
        this.recipeDAO = recipeDAO;
        this.botStateContextDAO = botStateContextDAO;
    }

    @Override
    public SendMessage handle(Update update) throws TelegramApiException {

        final CallbackQuery callbackQuery = update.getCallbackQuery();

        String data = callbackQuery.getData().substring(0, callbackQuery.getData().indexOf(":"));
        long chatId = callbackQuery.getMessage().getChatId();
        int messageId = callbackQuery.getMessage().getMessageId();
        SendMessage sendMessage;
        switch (data) {
            case ("delete_user_button"):
                //todo get userId from button
                long userId = getUserIdFromMessage((Message) callbackQuery.getMessage());
                sendMessage = SendMessage.builder().chatId(chatId).text("Пользователь " + userDAO.getUserById(userId).getUserName() + " удален").build();
                DeleteMessage deleteMessage = DeleteMessage.builder().chatId(chatId).messageId(messageId).build();
                telegramClient.execute(sendMessage);
                telegramClient.execute(deleteMessage);
                userDAO.deleteUser(userId);
                break;
            case ("set_admin_button"):
                userId = getUserIdFromMessage((Message) callbackQuery.getMessage());
                userDAO.setAdmin(userId);
                sendMessage = SendMessage.builder().chatId(chatId).text("Пользователь " + userDAO.getUserById(userId).getUserName() + " стал администратором").build();
                telegramClient.execute(sendMessage);
                EditMessageText editMessageText = EditMessageText.builder().chatId(chatId).messageId(messageId).text(userDAO.getUserById(userId).toString()).replyMarkup(inlineKeyboardMaker.getUserAdminKeyboard(userId)).build();
                telegramClient.execute(editMessageText);
                break;
            case ("unset_admin_button"):
                userId = getUserIdFromMessage((Message) callbackQuery.getMessage());
                userDAO.unsetAdmin(userId);
                sendMessage = SendMessage.builder().chatId(chatId).text("Пользователь " + userDAO.getUserById(userId).getUserName() + " больше не администратор").build();
                telegramClient.execute(sendMessage);
                editMessageText = EditMessageText.builder().chatId(chatId).messageId(messageId).text(userDAO.getUserById(userId).toString()).replyMarkup(inlineKeyboardMaker.getUserKeyboard(userId)).build();
                telegramClient.execute(editMessageText);
                break;
            case ("delete_recipe_button"):
                String recipeId = callbackQuery.getData().substring(callbackQuery.getData().indexOf(":") + 1);
                recipeDAO.deleteRecipe(recipeId);
                sendMessage = SendMessage.builder().chatId(chatId).text("Рецепт удален").build();
                telegramClient.execute(sendMessage);
                DeleteMessage deleteMessage1 = DeleteMessage.builder().chatId(chatId).messageId(messageId).build();
                telegramClient.execute(deleteMessage1);
                break;
            case ("change_photo_button"):
                //todo
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
