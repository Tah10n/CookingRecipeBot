package org.example.cooking_recipe_bot.bot.handlers;

import lombok.extern.slf4j.Slf4j;
import org.example.cooking_recipe_bot.bot.ActionFactory;
import org.example.cooking_recipe_bot.bot.BotState;
import org.example.cooking_recipe_bot.bot.constants.BotMessageEnum;
import org.example.cooking_recipe_bot.bot.constants.MessageTranslator;
import org.example.cooking_recipe_bot.bot.keyboards.InlineKeyboardMaker;
import org.example.cooking_recipe_bot.bot.keyboards.ReplyKeyboardMaker;
import org.example.cooking_recipe_bot.db.dao.BotStateContextDAO;
import org.example.cooking_recipe_bot.db.dao.RecipeDAOManager;
import org.example.cooking_recipe_bot.db.dao.UserDAO;
import org.example.cooking_recipe_bot.db.entity.BotStateContext;
import org.example.cooking_recipe_bot.db.entity.Recipe;
import org.example.cooking_recipe_bot.db.entity.User;
import org.example.cooking_recipe_bot.utils.MessageEntityMapper;
import org.example.cooking_recipe_bot.utils.UserParser;
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
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaAnimation;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaVideo;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class CallbackQueryHandler implements UpdateHandler {
    private final UserDAO userDAO;
    private final ActionFactory actionFactory;
    private final InlineKeyboardMaker inlineKeyboardMaker;
    private final TelegramClient telegramClient;
    private final RecipeDAOManager recipeDAOManager;
    private final BotStateContextDAO botStateContextDAO;
    private final MessageTranslator messageTranslator;
    private final ReplyKeyboardMaker replyKeyboardMaker;

    public CallbackQueryHandler(InlineKeyboardMaker inlineKeyboardMaker, TelegramClient telegramClient, RecipeDAOManager recipeDAOManager, BotStateContextDAO botStateContextDAO, UserDAO userDAO, ActionFactory actionFactory, MessageTranslator messageTranslator, ReplyKeyboardMaker replyKeyboardMaker) {

        this.inlineKeyboardMaker = inlineKeyboardMaker;
        this.telegramClient = telegramClient;
        this.recipeDAOManager = recipeDAOManager;
        this.botStateContextDAO = botStateContextDAO;
        this.userDAO = userDAO;
        this.actionFactory = actionFactory;
        this.messageTranslator = messageTranslator;
        this.replyKeyboardMaker = replyKeyboardMaker;
    }

    @Override
    public BotApiMethod<?> handle(Update update) {
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
        User user = userDAO.getUserById(userId);

        switch (data) {
            case ("delete_user_button"):
                deleteUser(callbackQuery, chatId, messageId);
                break;
            case ("set_admin_button"):
                setAdmin(callbackQuery, chatId, messageId);
                break;
            case ("unset_admin_button"):
                unsetAdmin(callbackQuery, chatId, messageId);
                break;
            case ("delete_recipe_button"):
                deleteRecipe(callbackQuery, chatId, user);
                break;
            case ("yes_for_delete_recipe_button"):
                yesForDeleteRecipe(callbackQuery, chatId, messageId, user);
                break;
            case ("no_for_delete_recipe_button"):
                noForDeleteRecipe(chatId, messageId);
                break;
            case ("open_recipe_button"):
                openAndCloseRecipe(callbackQuery, chatId, messageId);
                break;
            case ("change_photo_button"):
                changePhoto(callbackQuery, chatId);
                break;
            case ("change_video_button"):
                changeVideo(callbackQuery, chatId);
                break;
            case ("more_recipes_button"):
                moreRecipes(userId, chatId, messageId);
                break;
            case ("cancel_button"):
                cancelAction(chatId, messageId, userId);
                break;
            case ("rate_button"):
                rateButtonAction(callbackQuery, chatId, messageId);
                break;
            case ("rate_1"):
                rateRecipe(callbackQuery, userId, chatId, messageId, 1);
                break;
            case ("rate_2"):
                rateRecipe(callbackQuery, userId, chatId, messageId, 2);
                break;
            case ("rate_3"):
                rateRecipe(callbackQuery, userId, chatId, messageId, 3);
                break;
            case ("rate_4"):
                rateRecipe(callbackQuery, userId, chatId, messageId, 4);
                break;
            case ("rate_5"):
                rateRecipe(callbackQuery, userId, chatId, messageId, 5);
                break;
            case ("language_ru_button"):
                switchToLanguage(callbackQuery, chatId, messageId, "ru");
                break;
            case ("language_en_button"):
                switchToLanguage(callbackQuery, chatId, messageId, "en");
                break;
            default:
                log.error(this.getClass().getName() + " Unexpected value in switch: " + data);
                botStateContextDAO.changeBotState(userId, BotState.DEFAULT);
                break;
        }

        return null;
    }

    private void switchToLanguage(CallbackQuery callbackQuery, long chatId, int messageId, String languageCode) {
        User user = userDAO.getUserById(callbackQuery.getFrom().getId());
        user.setLanguage(languageCode);
        user = userDAO.saveUser(user);
        DeleteMessage deleteMessage = DeleteMessage.builder().chatId(chatId).messageId(messageId).build();
        SendMessage sendMessage = SendMessage.builder().chatId(chatId)
                .text(messageTranslator.getMessage(BotMessageEnum.LANGUAGE_WAS_CHANGED_MESSAGE.name(), user.getLanguage())).build();
        sendMessage.setReplyMarkup(replyKeyboardMaker.getMainMenuKeyboard(user));
        try {
            telegramClient.execute(deleteMessage);
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            log.error(Arrays.toString(e.getStackTrace()));
        }
    }

    private void rateButtonAction(CallbackQuery callbackQuery, long chatId, int messageId) {
        String recipeId = callbackQuery.getData().substring(callbackQuery.getData().lastIndexOf(":") + 1);
        Message message = (Message) callbackQuery.getMessage();
        InlineKeyboardMarkup inlineKeyboardMarkup = message.getReplyMarkup();
        InlineKeyboardMarkup inlineKeyboardMarkupChanged = changeToRatingButtons(inlineKeyboardMarkup, recipeId);
        EditMessageReplyMarkup editMessageReplyMarkup = EditMessageReplyMarkup.builder()
                .chatId(chatId).messageId(messageId).replyMarkup(inlineKeyboardMarkupChanged).build();
        try {
            telegramClient.execute(editMessageReplyMarkup);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            log.error(Arrays.toString(e.getStackTrace()));
        }
    }

    private void cancelAction(long chatId, int messageId, long userId) {
        DeleteMessage deleteMessage = DeleteMessage.builder().chatId(chatId).messageId(messageId).build();
        try {
            telegramClient.execute(deleteMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            log.error(Arrays.toString(e.getStackTrace()));
        }
        botStateContextDAO.changeBotState(userId, BotState.DEFAULT);
    }

    private void moreRecipes(long userId, long chatId, int messageId) {
        BotStateContext botStateContext = botStateContextDAO.findBotStateContextById(userId);
        List<Recipe> recipes = botStateContext.getRecipeList();
        DeleteMessage deleteMessage = DeleteMessage.builder().chatId(chatId).messageId(messageId).build();
        try {
            telegramClient.execute(deleteMessage);
            actionFactory.sendRecipesList(userId, chatId, recipes);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            log.error(Arrays.toString(e.getStackTrace()));
        }
    }

    private void changeVideo(CallbackQuery callbackQuery, long chatId) {
        long userId = callbackQuery.getFrom().getId();
        User user = userDAO.getUserById(userId);
        String recipeId = callbackQuery.getData().substring(callbackQuery.getData().lastIndexOf(":") + 1);
        String message = messageTranslator.getMessage(BotMessageEnum.SEND_ME_VIDEO_MESSAGE.name(), user.getLanguage());
        SendMessage sendMessage = SendMessage.builder().chatId(chatId).text(message)
                .replyMarkup(inlineKeyboardMaker.getCancelKeyboard(user)).build();
        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            log.error(Arrays.toString(e.getStackTrace()));
        }
        botStateContextDAO.changeBotState(userId, BotState.WAITING_FOR_VIDEO, recipeId);
    }

    private void changePhoto(CallbackQuery callbackQuery, long chatId) {
        long userId = callbackQuery.getFrom().getId();
        User user = userDAO.getUserById(userId);
        String recipeId = callbackQuery.getData().substring(callbackQuery.getData().lastIndexOf(":") + 1);
        String message = messageTranslator.getMessage(BotMessageEnum.SEND_ME_PHOTO_MESSAGE.name(), user.getLanguage());
        SendMessage sendMessage = SendMessage.builder().chatId(chatId).text(message)
                .replyMarkup(inlineKeyboardMaker.getCancelKeyboard(user)).build();
        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            log.error(Arrays.toString(e.getStackTrace()));
        }

        botStateContextDAO.changeBotState(userId, BotState.WAITING_FOR_PHOTO, recipeId);
    }

    private void noForDeleteRecipe(long chatId, int messageId) {
        DeleteMessage deleteMessage2 = DeleteMessage.builder().chatId(chatId).messageId(messageId).build();
        try {
            telegramClient.execute(deleteMessage2);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            log.error(Arrays.toString(e.getStackTrace()));
        }
    }

    private void yesForDeleteRecipe(CallbackQuery callbackQuery, long chatId, int messageId, User user) {
        String recipeId = callbackQuery.getData().substring(callbackQuery.getData().indexOf(":") + 1);
        recipeDAOManager.getRecipeDAO(user.getLanguage()).deleteRecipe(recipeId);

        DeleteMessage deleteMessage1 = DeleteMessage.builder().chatId(chatId).messageId(messageId).build();

        SendMessage sendMessage = SendMessage.builder().chatId(chatId).text("Рецепт удален").build();
        try {
            telegramClient.execute(deleteMessage1);
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            log.error(Arrays.toString(e.getStackTrace()));
        }
    }

    private void deleteRecipe(CallbackQuery callbackQuery, long chatId, User user) {
        String recipeId = callbackQuery.getData().substring(callbackQuery.getData().indexOf(":") + 1);
        String message = String.format(messageTranslator.getMessage(BotMessageEnum.DELETE_RECIPE_QUESTION_MESSAGE.name(), user.getLanguage()), recipeDAOManager.getRecipeDAO(user.getLanguage()).findRecipeById(recipeId).getName());
        SendMessage questionMessage = SendMessage.builder().chatId(chatId)
                .text(message)
                .replyMarkup(inlineKeyboardMaker.getYesOrNoForDeleteRecipeKeyboard(user, recipeId)).build();
        try {
            telegramClient.execute(questionMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            log.error(Arrays.toString(e.getStackTrace()));
        }
    }

    private void openAndCloseRecipe(CallbackQuery callbackQuery, long chatId, int messageId) {
        int opened = Integer.parseInt(callbackQuery.getData().substring(callbackQuery.getData().indexOf(":") + 1, callbackQuery.getData().lastIndexOf(":")));
        Long userId = callbackQuery.getFrom().getId();
        User user = userDAO.getUserById(userId);
        String recipeId = callbackQuery.getData().substring(callbackQuery.getData().lastIndexOf(":") + 1);
        Recipe recipe = recipeDAOManager.getRecipeDAO(user.getLanguage()).findRecipeById(recipeId);
        if (recipe == null) {
            SendMessage sendMessage = SendMessage.builder().chatId(chatId).text(messageTranslator.getMessage(BotMessageEnum.RECIPE_NOT_FOUND_MESSAGE.name(), user.getLanguage())).build();
            try {
                telegramClient.execute(sendMessage);
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
                log.error(Arrays.toString(e.getStackTrace()));
            }
            return;
        }

        EditMessageText editMessageTextFromOpenButton = null;
        List<MessageEntity> messageEntities = new ArrayList<>();
        if (recipe.getMessageEntities() != null) {
            messageEntities = MessageEntityMapper.mapToMessageEntities(recipe.getMessageEntities());
        }
        if (opened == 0) {
            if (recipe.getPhotoId() != null && !recipe.getPhotoId().isEmpty()) {
                if (recipe.getAnimationId() != null && !recipe.getAnimationId().isEmpty()) {
                    EditMessageMedia editMessageMedia = EditMessageMedia.builder().chatId(chatId).messageId(messageId - 1)
                            .media(new InputMediaAnimation(recipe.getAnimationId())).build();
                    try {
                        telegramClient.execute(editMessageMedia);
                    } catch (TelegramApiException e) {
                        log.error(e.getMessage());
                        log.error(Arrays.toString(e.getStackTrace()));
                    }

                } else if (recipe.getVideoId() != null && !recipe.getVideoId().isEmpty()) {
                    EditMessageMedia editMessageMedia = EditMessageMedia.builder().chatId(chatId).messageId(messageId - 1)
                            .media(new InputMediaVideo(recipe.getVideoId())).build();
                    try {
                        telegramClient.execute(editMessageMedia);
                    } catch (TelegramApiException e) {
                        log.error(e.getMessage());
                        log.error(Arrays.toString(e.getStackTrace()));
                    }

                }
            } else {
                if (recipe.getAnimationId() != null && !recipe.getAnimationId().isEmpty()) {
                    DeleteMessage deleteMessage = DeleteMessage.builder().chatId(chatId).messageId(messageId).build();
                    SendAnimation sendAnimation = SendAnimation.builder().chatId(chatId)
                            .animation(new InputFile(recipe.getAnimationId())).build();

                    try {
                        telegramClient.execute(deleteMessage);
                        telegramClient.execute(sendAnimation);

                    } catch (TelegramApiException e) {
                        log.error(e.getMessage());
                        log.error(Arrays.toString(e.getStackTrace()));
                    }

                } else if (recipe.getVideoId() != null && !recipe.getVideoId().isEmpty()) {
                    DeleteMessage deleteMessage = DeleteMessage.builder().chatId(chatId).messageId(messageId).build();
                    SendVideo sendVideo = SendVideo.builder().chatId(chatId)
                            .video(new InputFile(recipe.getVideoId())).build();

                    try {
                        telegramClient.execute(deleteMessage);
                        telegramClient.execute(sendVideo);
                    } catch (TelegramApiException e) {
                        log.error(e.getMessage());
                        log.error(Arrays.toString(e.getStackTrace()));
                    }

                } else {
                    editMessageTextFromOpenButton = EditMessageText.builder().chatId(chatId).messageId(messageId).text(recipe.toString())
                            .entities(messageEntities).build();
                    editMessageTextFromOpenButton.setReplyMarkup(getReplyMarkup(recipe, 1, userId));
                    try {
                        telegramClient.execute(editMessageTextFromOpenButton);
                    } catch (TelegramApiException e) {
                        log.error(e.getMessage());
                        log.error(Arrays.toString(e.getStackTrace()));
                    }
                    return;
                }
                SendMessage sendMessage = SendMessage.builder().chatId(chatId).text(recipe.toString())
                        .entities(messageEntities).replyMarkup(getReplyMarkup(recipe, 1, userId)).build();
                try {
                    telegramClient.execute(sendMessage);
                } catch (TelegramApiException e) {
                    log.error(e.getMessage());
                    log.error(Arrays.toString(e.getStackTrace()));
                }
                return;
            }
            editMessageTextFromOpenButton = EditMessageText.builder().chatId(chatId).messageId(messageId).text(recipe.toString())
                    .entities(messageEntities).build();
            editMessageTextFromOpenButton.setReplyMarkup(getReplyMarkup(recipe, 1, userId));
        } else if (opened == 1) {
            editMessageTextFromOpenButton = getEditMessageTextFromOpenButton(recipe, chatId, messageId, userId);

        }
        try {
            telegramClient.execute(editMessageTextFromOpenButton);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            log.error(Arrays.toString(e.getStackTrace()));
        }

    }

    private void unsetAdmin(CallbackQuery callbackQuery, long chatId, int messageId) {
        Long userRequesterId = callbackQuery.getFrom().getId();
        Long userIdFromMessage = getUserIdFromMessage((Message) callbackQuery.getMessage());
        User userRequester = userDAO.getUserById(userRequesterId);
        User userFromMessage = userDAO.unsetAdmin(userIdFromMessage);

        String message = String.format(messageTranslator.getMessage(BotMessageEnum.USER_WAS_UNSET_ADMIN_MESSAGE.name(), userRequester.getLanguage()), userFromMessage.getUserName());
        SendMessage sendMessage = SendMessage.builder().chatId(chatId).text(message).build();

        try {
            telegramClient.execute(sendMessage);

        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            log.error(Arrays.toString(e.getStackTrace()));
        }
        EditMessageText editMessageText = EditMessageText.builder().chatId(chatId).messageId(messageId)
                .text(userFromMessage.toString()).replyMarkup(inlineKeyboardMaker.getUserKeyboard(userRequester, userFromMessage)).build();
        try {
            telegramClient.execute(editMessageText);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            log.error(Arrays.toString(e.getStackTrace()));
        }
    }

    private void setAdmin(CallbackQuery callbackQuery, long chatId, int messageId) {
        Long userRequesterId = callbackQuery.getFrom().getId();
        Long userIdFromMessage = getUserIdFromMessage((Message) callbackQuery.getMessage());
        User userRequester = userDAO.getUserById(userRequesterId);
        User userFromMessage = userDAO.setAdmin(userIdFromMessage);

        String message = String.format(messageTranslator.getMessage(BotMessageEnum.USER_WAS_SET_ADMIN_MESSAGE.name(), userRequester.getLanguage()), userFromMessage.getUserName());

        SendMessage sendMessage = SendMessage.builder().chatId(chatId).text(message).build();

        try {
            telegramClient.execute(sendMessage);

        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            log.error(Arrays.toString(e.getStackTrace()));
        }
        EditMessageText editMessageText = EditMessageText.builder().chatId(chatId).messageId(messageId)
                .text(userFromMessage.toString()).replyMarkup(inlineKeyboardMaker.getUserKeyboard(userRequester, userFromMessage)).build();
        try {
            telegramClient.execute(editMessageText);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            log.error(Arrays.toString(e.getStackTrace()));
        }
    }

    private void deleteUser(CallbackQuery callbackQuery, long chatId, int messageId) {
        long userIdFromMessage = getUserIdFromMessage((Message) callbackQuery.getMessage());
        User user = userDAO.getUserById(userIdFromMessage);
        String message = String.format(messageTranslator.getMessage(BotMessageEnum.USER_WAS_DELETED_MESSAGE.name(), user.getLanguage()), user.getUserName());
        SendMessage sendMessage = SendMessage.builder().chatId(chatId).text(message).build();
        DeleteMessage deleteMessage = DeleteMessage.builder().chatId(chatId).messageId(messageId).build();
        try {
            userDAO.deleteUser(userIdFromMessage);
            telegramClient.execute(sendMessage);
            telegramClient.execute(deleteMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            log.error(Arrays.toString(e.getStackTrace()));
        }

    }

    private EditMessageText getEditMessageTextFromOpenButton(Recipe recipe, long chatId, int messageId, long userId) {
        DeleteMessage deleteMessage;
        EditMessageText editMessageTextFromOpenButton;
        if ((recipe.getAnimationId() != null && !recipe.getAnimationId().isEmpty()) || (recipe.getVideoId() != null && !recipe.getVideoId().isEmpty())) {
            if (recipe.getPhotoId() != null && !recipe.getPhotoId().isEmpty()) {
                EditMessageMedia editMessageMedia = EditMessageMedia.builder().chatId(chatId).messageId(messageId - 1)
                        .media(new InputMediaPhoto(recipe.getPhotoId())).build();

                try {
                    telegramClient.execute(editMessageMedia);
                } catch (TelegramApiException e) {
                    log.error(e.getMessage());
                    log.error(Arrays.toString(e.getStackTrace()));
                }
            } else {
                deleteMessage = DeleteMessage.builder().chatId(chatId).messageId(messageId - 1).build();
                try {
                    telegramClient.execute(deleteMessage);
                } catch (TelegramApiException e) {
                    log.error(e.getMessage());
                    log.error(Arrays.toString(e.getStackTrace()));
                }
            }

        }
        Double rating = recipe.getRating() == null ? 0 : recipe.getRating();

        editMessageTextFromOpenButton = EditMessageText.builder().chatId(chatId).messageId(messageId)
                .text("<b>" + recipe.getName().toUpperCase() + "</b> \nРейтинг: " + String.format("%.2f", rating)).parseMode(ParseMode.HTML).build();
        editMessageTextFromOpenButton.setReplyMarkup(getReplyMarkup(recipe, 0, userId));
        return editMessageTextFromOpenButton;
    }

    private void rateRecipe(CallbackQuery callbackQuery, long userId, long chatId, int messageId, int ratingFromUser) {
        EditMessageText editMessageText;
        User user = userDAO.getUserById(userId);
        String recipeId = callbackQuery.getData().substring(callbackQuery.getData().lastIndexOf(":") + 1);

        Recipe recipe;
        recipe = recipeDAOManager.getRecipeDAO(user.getLanguage()).findRecipeById(recipeId);
        Double rating = recipe.getRating();
        if (rating == null) {
            rating = (double) ratingFromUser;
        }
        rating = (rating + ratingFromUser) / 2;
        recipe.setRating(rating);
        List<Long> votedUsersIds = recipe.getVotedUsersIds();
        if (votedUsersIds == null) {
            votedUsersIds = new ArrayList<>();
        }
        votedUsersIds.add(userId);
        recipe.setVotedUsersIds(votedUsersIds);
        recipeDAOManager.getRecipeDAO(user.getLanguage()).saveRecipe(recipe);
        editMessageText = getEditMessageTextFromOpenButton(recipe, chatId, messageId, userId);
        try {
            telegramClient.execute(editMessageText);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            log.error(Arrays.toString(e.getStackTrace()));
        }
    }

    private InlineKeyboardMarkup changeToRatingButtons(InlineKeyboardMarkup inlineKeyboardMarkup, String recipeId) {
        List<InlineKeyboardRow> rows = new ArrayList<>(inlineKeyboardMarkup.getKeyboard());
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
        return actionFactory.getRecipeKeyboardWithRateButton(recipe, user, inlineKeyboardMaker.getRecipeKeyboard(recipe, state, user));
    }

}
