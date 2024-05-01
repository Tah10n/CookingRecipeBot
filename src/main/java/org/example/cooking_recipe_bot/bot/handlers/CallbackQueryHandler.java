package org.example.cooking_recipe_bot.bot.handlers;

import lombok.extern.slf4j.Slf4j;
import org.example.cooking_recipe_bot.bot.BotState;
import org.example.cooking_recipe_bot.bot.keyboards.InlineKeyboardMaker;
import org.example.cooking_recipe_bot.bot.keyboards.ReplyKeyboardMaker;
import org.example.cooking_recipe_bot.db.dao.BotStateContextDAO;
import org.example.cooking_recipe_bot.db.dao.RecipeDAO;
import org.example.cooking_recipe_bot.db.dao.UserDAO;
import org.example.cooking_recipe_bot.db.entity.BotStateContext;
import org.example.cooking_recipe_bot.db.entity.Recipe;
import org.example.cooking_recipe_bot.db.entity.User;
import org.example.cooking_recipe_bot.utils.UserParser;
import org.example.cooking_recipe_bot.utils.constants.BotMessageEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ForceReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Slf4j
@Service
public class CallbackQueryHandler implements UpdateHandler {
    private final UserDAO userDAO;
    InlineKeyboardMaker inlineKeyboardMaker;
    ReplyKeyboardMaker replyKeyboardMaker;
    TelegramClient telegramClient;
    RecipeDAO recipeDAO;
    BotStateContextDAO botStateContextDAO;

    public CallbackQueryHandler(InlineKeyboardMaker inlineKeyboardMaker, TelegramClient telegramClient, RecipeDAO recipeDAO, BotStateContextDAO botStateContextDAO, UserDAO userDAO) {

        this.inlineKeyboardMaker = inlineKeyboardMaker;
        this.telegramClient = telegramClient;
        this.recipeDAO = recipeDAO;
        this.botStateContextDAO = botStateContextDAO;
        this.userDAO = userDAO;
    }

    @Override
    public BotApiMethod handle(Update update) throws TelegramApiException {
        final CallbackQuery callbackQuery = update.getCallbackQuery();

        String data = callbackQuery.getData().substring(0, callbackQuery.getData().indexOf(":"));
        long chatId = callbackQuery.getMessage().getChatId();
        int messageId = callbackQuery.getMessage().getMessageId();
        Long userId = update.getCallbackQuery().getFrom().getId();
        SendMessage sendMessage;
        switch (data) {
            case ("delete_user_button"):
                long userIdFromMessage = getUserIdFromMessage((Message) callbackQuery.getMessage());
                sendMessage = SendMessage.builder().chatId(chatId).text("Пользователь " + userDAO.getUserById(userIdFromMessage).getUserName() + " удален").build();
                DeleteMessage deleteMessage = DeleteMessage.builder().chatId(chatId).messageId(messageId).build();
                telegramClient.execute(sendMessage);
                telegramClient.execute(deleteMessage);
                userDAO.deleteUser(userIdFromMessage);
                break;
            case ("set_admin_button"):
                userIdFromMessage = getUserIdFromMessage((Message) callbackQuery.getMessage());
                userDAO.setAdmin(userIdFromMessage);
                sendMessage = SendMessage.builder().chatId(chatId).text("Пользователь " + userDAO.getUserById(userIdFromMessage).getUserName() + " стал администратором").build();
                telegramClient.execute(sendMessage);
                EditMessageText editMessageText = EditMessageText.builder().chatId(chatId).messageId(messageId).text(userDAO.getUserById(userIdFromMessage).toString()).replyMarkup(inlineKeyboardMaker.getUserAdminKeyboard(userIdFromMessage)).build();
                telegramClient.execute(editMessageText);
                break;
            case ("unset_admin_button"):
                userIdFromMessage = getUserIdFromMessage((Message) callbackQuery.getMessage());
                userDAO.unsetAdmin(userIdFromMessage);
                sendMessage = SendMessage.builder().chatId(chatId).text("Пользователь " + userDAO.getUserById(userIdFromMessage).getUserName() + " больше не администратор").build();
                telegramClient.execute(sendMessage);
                editMessageText = EditMessageText.builder().chatId(chatId).messageId(messageId).text(userDAO.getUserById(userIdFromMessage).toString()).replyMarkup(inlineKeyboardMaker.getUserKeyboard(userIdFromMessage)).build();
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
            case ("open_recipe_button"):
                int opened = Integer.parseInt(callbackQuery.getData().substring(callbackQuery.getData().indexOf(":") + 1, callbackQuery.getData().lastIndexOf(":")));
                recipeId = callbackQuery.getData().substring(callbackQuery.getData().lastIndexOf(":") + 1);
                Recipe recipe = recipeDAO.findRecipeById(recipeId);
                if (recipe == null) {
                    return SendMessage.builder().chatId(chatId).text(BotMessageEnum.RECIPE_NOT_FOUND.getMessage()).build();
                }
                Message message = (Message) callbackQuery.getMessage();

                EditMessageText editMessageTextFromOpenButton = null;
                if (message.hasText()) {
                    if (opened == 0) {
                        editMessageTextFromOpenButton = EditMessageText.builder().chatId(chatId).messageId(messageId).text(recipe.toString()).build();
                        editMessageTextFromOpenButton.setReplyMarkup(getReplyMarkup(recipe, 1, userId));
                    } else if (opened == 1) {
                        editMessageTextFromOpenButton = EditMessageText.builder().chatId(chatId).messageId(messageId).text("Рецепт:").build();
                        editMessageTextFromOpenButton.setReplyMarkup(getReplyMarkup(recipe, 0, userId));
                        if (recipe.getVideoId() != null || recipe.getAnimationId() != null || recipe.getPhotoId() != null) {
                            deleteMessage = DeleteMessage.builder().chatId(chatId).messageId(messageId).build();
                            telegramClient.execute(deleteMessage);
                            EditMessageCaption editMessageCaption = EditMessageCaption.builder().chatId(chatId).messageId(messageId - 1).caption("").build();
                            editMessageCaption.setReplyMarkup(getReplyMarkup(recipe, 0, userId));
                            telegramClient.execute(editMessageCaption);
                            break;
                        }
                    }
                    telegramClient.execute(editMessageTextFromOpenButton);
                } else {
                    EditMessageCaption editMessageCaption = EditMessageCaption.builder().chatId(chatId).messageId(messageId).caption("").build();
                    if (opened == 0) {
                        if (recipe.toString().length() > 1024) {
                            deleteMessage = DeleteMessage.builder().chatId(chatId).messageId(messageId).build();
                            telegramClient.execute(deleteMessage);
                            if (recipe.getAnimationId() != null && !recipe.getAnimationId().isEmpty()) {
                                SendAnimation sendAnimation = SendAnimation.builder().chatId(chatId).animation(new InputFile(recipe.getAnimationId())).build();
                                telegramClient.execute(sendAnimation);
                            } else if (recipe.getVideoId() != null && !recipe.getVideoId().isEmpty()) {
                                SendVideo sendVideo = SendVideo.builder().chatId(chatId).video(new InputFile(recipe.getVideoId())).build();
                                telegramClient.execute(sendVideo);

                            } else if (recipe.getPhotoId() != null && !recipe.getPhotoId().isEmpty()) {
                                SendPhoto sendPhoto = SendPhoto.builder().chatId(chatId).photo(new InputFile(recipe.getPhotoId())).build();
                                telegramClient.execute(sendPhoto);

                            }
                            sendMessage = SendMessage.builder().chatId(chatId).text(recipe.toString()).build();
                            sendMessage.setReplyMarkup(getReplyMarkup(recipe, 1, userId));
                            telegramClient.execute(sendMessage);
                            break;
                        } else {
                            editMessageCaption.setCaption(recipe.toString());
                            editMessageCaption.setReplyMarkup(getReplyMarkup(recipe, 1, userId));
                        }

                    } else if (opened == 1) {
                        editMessageCaption.setCaption("");
                        editMessageCaption.setReplyMarkup(getReplyMarkup(recipe, 0, userId));
                    }

                    telegramClient.execute(editMessageCaption);
                }
                break;
            case ("change_photo_button"):
                String userName = update.getCallbackQuery().getFrom().getUserName();
                recipeId = callbackQuery.getData().substring(callbackQuery.getData().lastIndexOf(":") + 1);
                sendMessage = SendMessage.builder().chatId(chatId).text("Отправьте новое фото или напишите delete если хотите удалить фото").build();
                telegramClient.execute(sendMessage);

                botStateContextDAO.changeBotState(userName, BotState.WAITING_FOR_PHOTO, recipeId);

                break;
            case ("change_video_button"):
                userName = update.getCallbackQuery().getFrom().getUserName();
                recipeId = callbackQuery.getData().substring(callbackQuery.getData().lastIndexOf(":") + 1);
                sendMessage = SendMessage.builder().chatId(chatId).text("Отправьте новое видео или напишите delete если хотите удалить видео").build();
                telegramClient.execute(sendMessage);

                botStateContextDAO.changeBotState(userName, BotState.WAITING_FOR_VIDEO, recipeId);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + data);
        }

        return null;
    }



    private long getUserIdFromMessage(Message message) {
        String text = message.getText();
        User user = UserParser.parseUserFromString(text);
        return user.getId();

    }

    private InlineKeyboardMarkup getReplyMarkup(Recipe recipe, int state, long userId) {
        if (userDAO.getUserById(userId).getIsAdmin()) {
            return inlineKeyboardMaker.getRecipeAdminKeyboard(recipe, state);
        } else {
            return inlineKeyboardMaker.getRecipeKeyboard(recipe, state);
        }
    }

    private void setCaptionMessage(EditMessageCaption editMessageCaption, Recipe recipe, int state, long userId) {

    }

}
