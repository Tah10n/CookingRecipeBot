package org.example.cooking_recipe_bot.bot.handlers;

import lombok.extern.slf4j.Slf4j;
import org.example.cooking_recipe_bot.bot.ActionFactory;
import org.example.cooking_recipe_bot.bot.BotState;
import org.example.cooking_recipe_bot.bot.constants.BotMessageEnum;
import org.example.cooking_recipe_bot.bot.keyboards.InlineKeyboardMaker;
import org.example.cooking_recipe_bot.db.dao.BotStateContextDAO;
import org.example.cooking_recipe_bot.db.dao.RecipeDAO;
import org.example.cooking_recipe_bot.db.dao.UserDAO;
import org.example.cooking_recipe_bot.db.entity.BotStateContext;
import org.example.cooking_recipe_bot.db.entity.Recipe;
import org.example.cooking_recipe_bot.db.entity.User;
import org.example.cooking_recipe_bot.utils.UserParser;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaAnimation;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaVideo;
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CallbackQueryHandler implements UpdateHandler {
    private final UserDAO userDAO;
    private final ActionFactory actionFactory;
    InlineKeyboardMaker inlineKeyboardMaker;
    TelegramClient telegramClient;
    RecipeDAO recipeDAO;
    BotStateContextDAO botStateContextDAO;

    public CallbackQueryHandler(InlineKeyboardMaker inlineKeyboardMaker, TelegramClient telegramClient, RecipeDAO recipeDAO, BotStateContextDAO botStateContextDAO, UserDAO userDAO, ActionFactory actionFactory) {

        this.inlineKeyboardMaker = inlineKeyboardMaker;
        this.telegramClient = telegramClient;
        this.recipeDAO = recipeDAO;
        this.botStateContextDAO = botStateContextDAO;
        this.userDAO = userDAO;
        this.actionFactory = actionFactory;
    }

    @Override
    public BotApiMethod<?> handle(Update update) throws TelegramApiException {
        final CallbackQuery callbackQuery = update.getCallbackQuery();
        if (callbackQuery == null) {
            log.error(this.getClass().getName() + " No callback query in update");
            log.error(update.toString());
            return null;
        }
        String data = callbackQuery.getData().substring(0, callbackQuery.getData().indexOf(":"));
        long chatId = callbackQuery.getMessage().getChatId();
        int messageId = callbackQuery.getMessage().getMessageId();
        long userId = update.getCallbackQuery().getFrom().getId();
        String recipeId = callbackQuery.getData().substring(callbackQuery.getData().indexOf(":") + 1);

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
                EditMessageText editMessageText = EditMessageText.builder().chatId(chatId).messageId(messageId)
                        .text(userDAO.getUserById(userIdFromMessage).toString()).replyMarkup(inlineKeyboardMaker.getUserKeyboard(userIdFromMessage, true)).build();
                telegramClient.execute(editMessageText);
                break;
            case ("unset_admin_button"):
                userIdFromMessage = getUserIdFromMessage((Message) callbackQuery.getMessage());
                userDAO.unsetAdmin(userIdFromMessage);
                sendMessage = SendMessage.builder().chatId(chatId).text("Пользователь " + userDAO.getUserById(userIdFromMessage).getUserName() + " больше не администратор").build();
                telegramClient.execute(sendMessage);
                editMessageText = EditMessageText.builder().chatId(chatId).messageId(messageId)
                        .text(userDAO.getUserById(userIdFromMessage).toString()).replyMarkup(inlineKeyboardMaker.getUserKeyboard(userIdFromMessage, false)).build();
                telegramClient.execute(editMessageText);
                break;
            case ("delete_recipe_button"):

                SendMessage questionMessage = SendMessage.builder().chatId(chatId)
                        .text("Вы уверены, что хотите удалить рецепт " + recipeDAO.findRecipeById(recipeId).getName() + " ?")
                        .replyMarkup(inlineKeyboardMaker.getYesOrNoForDeleteRecipeKeyboard(recipeId)).build();
                telegramClient.execute(questionMessage);
                break;
            case ("yes_for_delete_recipe_button"):
                recipeId = callbackQuery.getData().substring(callbackQuery.getData().indexOf(":") + 1);
                recipeDAO.deleteRecipe(recipeId);

                DeleteMessage deleteMessage1 = DeleteMessage.builder().chatId(chatId).messageId(messageId).build();
                telegramClient.execute(deleteMessage1);

                sendMessage = SendMessage.builder().chatId(chatId).text("Рецепт удален").build();
                telegramClient.execute(sendMessage);
                break;
            case ("no_for_delete_recipe_button"):
                DeleteMessage deleteMessage2 = DeleteMessage.builder().chatId(chatId).messageId(messageId).build();
                telegramClient.execute(deleteMessage2);
                break;
            case ("open_recipe_button"):
                int opened = Integer.parseInt(callbackQuery.getData().substring(callbackQuery.getData().indexOf(":") + 1, callbackQuery.getData().lastIndexOf(":")));
                recipeId = callbackQuery.getData().substring(callbackQuery.getData().lastIndexOf(":") + 1);
                Recipe recipe = recipeDAO.findRecipeById(recipeId);
                if (recipe == null) {
                    return SendMessage.builder().chatId(chatId).text(BotMessageEnum.RECIPE_NOT_FOUND.getMessage()).build();
                }

                EditMessageText editMessageTextFromOpenButton = null;
                if (opened == 0) {
                    if (recipe.getPhotoId() != null && !recipe.getPhotoId().isEmpty()) {
                        if (recipe.getAnimationId() != null && !recipe.getAnimationId().isEmpty()) {
                            EditMessageMedia editMessageMedia = EditMessageMedia.builder().chatId(chatId).messageId(messageId - 1)
                                    .media(new InputMediaAnimation(recipe.getAnimationId())).build();
                            telegramClient.execute(editMessageMedia);
                        } else if (recipe.getVideoId() != null && !recipe.getVideoId().isEmpty()) {
                            EditMessageMedia editMessageMedia = EditMessageMedia.builder().chatId(chatId).messageId(messageId - 1)
                                    .media(new InputMediaVideo(recipe.getVideoId())).build();
                            telegramClient.execute(editMessageMedia);
                        }
                    } else {
                        if (recipe.getAnimationId() != null && !recipe.getAnimationId().isEmpty()) {
                            deleteMessage = DeleteMessage.builder().chatId(chatId).messageId(messageId).build();
                            telegramClient.execute(deleteMessage);
                            SendAnimation sendAnimation = SendAnimation.builder().chatId(chatId)
                                    .animation(new InputFile(recipe.getAnimationId())).build();
                            sendMessage = SendMessage.builder().chatId(chatId).text(recipe.toString()).replyMarkup(getReplyMarkup(recipe, 1, userId)).build();
                            telegramClient.execute(sendAnimation);
                            telegramClient.execute(sendMessage);
                            break;
                        } else if (recipe.getVideoId() != null && !recipe.getVideoId().isEmpty()) {
                            deleteMessage = DeleteMessage.builder().chatId(chatId).messageId(messageId).build();
                            telegramClient.execute(deleteMessage);
                            SendVideo sendVideo = SendVideo.builder().chatId(chatId)
                                    .video(new InputFile(recipe.getVideoId())).build();
                            sendMessage = SendMessage.builder().chatId(chatId).text(recipe.toString()).replyMarkup(getReplyMarkup(recipe, 1, userId)).build();
                            telegramClient.execute(sendVideo);
                            telegramClient.execute(sendMessage);
                            break;
                        }
                    }
                    editMessageTextFromOpenButton = EditMessageText.builder().chatId(chatId).messageId(messageId).text(recipe.toString()).build();
                    editMessageTextFromOpenButton.setReplyMarkup(getReplyMarkup(recipe, 1, userId));
                } else if (opened == 1) {
                    editMessageTextFromOpenButton = getEditMessageTextFromOpenButton(recipe, chatId, messageId, userId);

                }
                telegramClient.execute(editMessageTextFromOpenButton);

                break;
            case ("change_photo_button"):
                userId = update.getCallbackQuery().getFrom().getId();
                recipeId = callbackQuery.getData().substring(callbackQuery.getData().lastIndexOf(":") + 1);
                sendMessage = SendMessage.builder().chatId(chatId).text("Отправьте новое фото или напишите delete если хотите удалить фото").build();
                telegramClient.execute(sendMessage);

                botStateContextDAO.changeBotState(userId, BotState.WAITING_FOR_PHOTO, recipeId);

                break;
            case ("change_video_button"):
                userId = update.getCallbackQuery().getFrom().getId();
                recipeId = callbackQuery.getData().substring(callbackQuery.getData().lastIndexOf(":") + 1);
                sendMessage = SendMessage.builder().chatId(chatId).text("Отправьте новое видео или напишите delete если хотите удалить видео").build();
                telegramClient.execute(sendMessage);

                botStateContextDAO.changeBotState(userId, BotState.WAITING_FOR_VIDEO, recipeId);
                break;
            case ("more_recipes_button"):
                BotStateContext botStateContext = botStateContextDAO.findBotStateContextById(userId);
                List<Recipe> recipes = botStateContext.getRecipeList();
                deleteMessage = DeleteMessage.builder().chatId(chatId).messageId(messageId).build();
                telegramClient.execute(deleteMessage);
                actionFactory.sendRecipesList(userId, chatId, recipes);
                break;
            case ("cancel_button"):
                deleteMessage = DeleteMessage.builder().chatId(chatId).messageId(messageId).build();
                telegramClient.execute(deleteMessage);
                botStateContextDAO.changeBotState(userId, BotState.DEFAULT);
                break;
            case ("rate_button"):
                recipeId = callbackQuery.getData().substring(callbackQuery.getData().lastIndexOf(":") + 1);
                Message message = (Message) callbackQuery.getMessage();
                InlineKeyboardMarkup inlineKeyboardMarkup = message.getReplyMarkup();
                InlineKeyboardMarkup inlineKeyboardMarkupChanged = changeToRatingButtons(inlineKeyboardMarkup, recipeId);
                EditMessageReplyMarkup editMessageReplyMarkup = EditMessageReplyMarkup.builder()
                        .chatId(chatId).messageId(messageId).replyMarkup(inlineKeyboardMarkupChanged).build();
                telegramClient.execute(editMessageReplyMarkup);
                break;
            case ("rate_1"):
                rateRecipe(recipeId, userId, chatId, messageId, 1);
                break;
            case ("rate_2"):
                rateRecipe(recipeId, userId, chatId, messageId, 2);
                break;
            case ("rate_3"):
                rateRecipe(recipeId, userId, chatId, messageId, 3);
                break;
            case ("rate_4"):
                rateRecipe(recipeId, userId, chatId, messageId, 4);
                break;
            case ("rate_5"):
                rateRecipe(recipeId, userId, chatId, messageId, 5);
                break;
            default:
                log.error(this.getClass().getName() + " Unexpected value in switch: " + data);
                botStateContextDAO.changeBotState(userId, BotState.DEFAULT);
                break;
        }

        return null;
    }

    private @NotNull EditMessageText getEditMessageTextFromOpenButton(Recipe recipe, long chatId, int messageId, long userId) throws TelegramApiException {
        DeleteMessage deleteMessage;
        EditMessageText editMessageTextFromOpenButton;
        if ((recipe.getAnimationId() != null && !recipe.getAnimationId().isEmpty()) || (recipe.getVideoId() != null && !recipe.getVideoId().isEmpty())) {
            if (recipe.getPhotoId() != null && !recipe.getPhotoId().isEmpty()) {
                EditMessageMedia editMessageMedia = EditMessageMedia.builder().chatId(chatId).messageId(messageId - 1)
                        .media(new InputMediaPhoto(recipe.getPhotoId())).build();

                telegramClient.execute(editMessageMedia);
            } else {
                deleteMessage = DeleteMessage.builder().chatId(chatId).messageId(messageId - 1).build();
                telegramClient.execute(deleteMessage);
            }

        }
        Double rating = recipe.getRating() == null ? 0 : recipe.getRating();

        editMessageTextFromOpenButton = EditMessageText.builder().chatId(chatId).messageId(messageId)
                .text("*" + recipe.getName().toUpperCase() + "* \nРейтинг: " + String.format("%.2f", rating)).parseMode(ParseMode.MARKDOWN).build();
        editMessageTextFromOpenButton.setReplyMarkup(getReplyMarkup(recipe, 0, userId));
        return editMessageTextFromOpenButton;
    }

    private void rateRecipe(String recipeId, long userId, long chatId, int messageId, int ratingFromUser) throws TelegramApiException {
        EditMessageText editMessageText;
        Recipe recipe;
        recipe = recipeDAO.findRecipeById(recipeId);
        Double rating = recipe.getRating();
        if (rating == null) {
            rating = Double.valueOf(ratingFromUser);
        }
        rating = (rating + ratingFromUser) / 2;
        recipe.setRating(rating);
        List<Long> votedUsersIds = recipe.getVotedUsersIds();
        if (votedUsersIds == null) {
            votedUsersIds = new ArrayList<>();
        }
        votedUsersIds.add(userId);
        recipe.setVotedUsersIds(votedUsersIds);
        recipeDAO.saveRecipe(recipe);
        editMessageText = getEditMessageTextFromOpenButton(recipe, chatId, messageId, userId);
        telegramClient.execute(editMessageText);
    }

    private InlineKeyboardMarkup changeToRatingButtons(InlineKeyboardMarkup inlineKeyboardMarkup, String recipeId) {
        List<InlineKeyboardRow> rows = new ArrayList<>();
        rows.addAll(inlineKeyboardMarkup.getKeyboard());
        rows.remove(rows.size() - 1);
        rows.add(inlineKeyboardMaker.getRatingButtons(recipeId));
        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }


    private long getUserIdFromMessage(Message message) {
        String text = message.getText();
        User user = UserParser.parseUserFromString(text);
        return user.getId();

    }

    private InlineKeyboardMarkup getReplyMarkup(Recipe recipe, int state, long userId) {
        User user = userDAO.getUserById(userId);
        InlineKeyboardMarkup recipeKeyboardWithRateButton = actionFactory.getRecipeKeyboardWithRateButton(recipe, user, inlineKeyboardMaker.getRecipeKeyboard(recipe, state, user.getIsAdmin()));

        return recipeKeyboardWithRateButton;

    }

}
