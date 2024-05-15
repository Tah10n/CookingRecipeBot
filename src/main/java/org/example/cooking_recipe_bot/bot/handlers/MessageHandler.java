package org.example.cooking_recipe_bot.bot.handlers;

import lombok.extern.slf4j.Slf4j;
import org.example.cooking_recipe_bot.bot.ActionFactory;
import org.example.cooking_recipe_bot.bot.BotState;
import org.example.cooking_recipe_bot.bot.constants.BotMessageEnum;
import org.example.cooking_recipe_bot.bot.keyboards.InlineKeyboardMaker;
import org.example.cooking_recipe_bot.bot.keyboards.ReplyKeyboardMaker;
import org.example.cooking_recipe_bot.db.dao.BotStateContextDAO;
import org.example.cooking_recipe_bot.db.dao.RecipeDAO;
import org.example.cooking_recipe_bot.db.dao.UserDAO;
import org.example.cooking_recipe_bot.db.entity.BotStateContext;
import org.example.cooking_recipe_bot.db.entity.Recipe;
import org.example.cooking_recipe_bot.db.entity.User;
import org.example.cooking_recipe_bot.utils.RecipeParser;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MessageHandler implements UpdateHandler {
    private final RecipeDAO recipeDAO;
    ReplyKeyboardMaker replyKeyboardMaker;
    InlineKeyboardMaker inlineKeyboardMaker;
    UserDAO userDAO;
    TelegramClient telegramClient;
    BotStateContextDAO botStateContextDAO;
    ActionFactory actionFactory;


    public MessageHandler(ReplyKeyboardMaker replyKeyboardMaker, InlineKeyboardMaker inlineKeyboardMaker, UserDAO userDAO, TelegramClient telegramClient, RecipeDAO recipeDAO, BotStateContextDAO botStateContextDAO, ActionFactory actionFactory) {
        this.replyKeyboardMaker = replyKeyboardMaker;
        this.inlineKeyboardMaker = inlineKeyboardMaker;
        this.userDAO = userDAO;
        this.telegramClient = telegramClient;
        this.recipeDAO = recipeDAO;
        this.botStateContextDAO = botStateContextDAO;
        this.actionFactory = actionFactory;
    }

    @Override
    public SendMessage handle(Update update) {
        if (!update.hasMessage()) {
            log.error(this.getClass().getName() + " No message in update");
            log.error(update.toString());
            return null;
        }
        Message message = update.getMessage();
        User user = getOrCreateUserFromUpdate(update);
        if(user == null) {
            log.error(this.getClass().getName() + " No user in update");
            log.error(update.toString());
            return null;
        }
        BotStateContext botStateContext = getOrCreateBotStateContext(user);
        long chatId = message.getChatId();
        SendMessage sendMessage = SendMessage.builder().chatId(chatId).text("").build();

        Map<String, Runnable> buttonActions = actionFactory.createButtonActions(update, user);

        if (message.hasText()) {
            String inputText = message.getText().toLowerCase();

            if (buttonActions.containsKey(inputText)) {
                buttonActions.get(inputText).run();
            } else {
                switch (botStateContext.getCurrentBotState()) {
                    case DEFAULT:
                        handleDefaultState(update, inputText);
                        break;
                    case ADDING_RECIPE:
                        return addNewRecipe(update, sendMessage, user);
                    case WAITING_FOR_EDITED_RECIPE:
                        return updateRecipe(update, sendMessage, user);
                    case WAITING_FOR_PHOTO:
                        return handleWaitingForPhotoState(sendMessage, inputText, user);
                    case WAITING_FOR_VIDEO:
                        return handleWaitingForVideoState(sendMessage, inputText, user);
                    case WAITING_FOR_NOTIFICATION:
                        return sendNotificationToUsers(update, user);
                }
            }
        } else {
            sendMessage = handleNoTextState(botStateContext, sendMessage, update, user);
        }

        return sendMessage;
    }

    private void handleDefaultState(Update update, String inputText) {
        List<Recipe> recipes = recipeDAO.findRecipesByString(inputText);
        try {
            actionFactory.sendRecipesList(update, recipes);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            log.error(Arrays.toString(e.getStackTrace()));
        }
    }

    private SendMessage handleWaitingForPhotoState(SendMessage sendMessage, String inputText, User user) {
        if ("delete".equals(inputText)) {
            String recipeId = botStateContextDAO.findBotStateContextById(user.getId()).getAdditionalData();
            Recipe recipe = recipeDAO.findRecipeById(recipeId);
            recipe.setPhotoId(null);
            recipeDAO.saveRecipe(recipe);
            sendMessage.setText("Фото удалено");
            botStateContextDAO.changeBotState(user.getId(), BotState.DEFAULT);
        } else {
            sendMessage.setText("Отправьте фото или напишите delete для его удаления");
        }
        return sendMessage;
    }

    private SendMessage handleWaitingForVideoState(SendMessage sendMessage, String inputText, User user) {
        if ("delete".equals(inputText)) {
            String recipeId = botStateContextDAO.findBotStateContextById(user.getId()).getAdditionalData();
            Recipe recipe = recipeDAO.findRecipeById(recipeId);
            recipe.setVideoId(null);
            recipe.setAnimationId(null);
            recipeDAO.saveRecipe(recipe);
            sendMessage.setText("Видео удалено");
            botStateContextDAO.changeBotState(user.getId(), BotState.DEFAULT);
        } else {
            sendMessage.setText("Отправьте видео или напишите delete для его удаления");
        }
        return sendMessage;
    }

    private SendMessage handleNoTextState(BotStateContext botStateContext, SendMessage sendMessage, Update update, User user) {
        switch (botStateContext.getCurrentBotState()) {
            case DEFAULT:
                sendMessage.setText("Отправьте ключевое слово или ингредиент для поиска рецептов");
                break;
            case ADDING_RECIPE:
                return addNewRecipe(update, sendMessage, user);
            case WAITING_FOR_PHOTO:
                return addPhoto(update, sendMessage, user);
            case WAITING_FOR_VIDEO:
                return addVideo(update, sendMessage, user);
            default:
                log.debug(this.getClass().getName() + "switch default: botStateContext=" + botStateContext.getCurrentBotState());
                break;
        }
        return sendMessage;
    }

    private @NotNull BotStateContext getOrCreateBotStateContext(User user) {
        BotStateContext botStateContext = botStateContextDAO.findBotStateContextById(user.getId());
        if (botStateContext == null) {
            botStateContext = new BotStateContext();
            botStateContext.setId(String.valueOf(user.getId()));
            botStateContext.setCurrentBotState(BotState.DEFAULT);
            botStateContextDAO.saveBotStateContext(botStateContext);
        }
        return botStateContext;
    }


    private SendMessage sendNotificationToUsers(Update update, User user) {
        String notificationText = update.getMessage().getText();
        List<User> users = userDAO.findAllUsers();
        for (User u : users) {
            SendMessage sendMessage = SendMessage.builder().chatId(u.getChatId()).text(notificationText).build();
            log.info("Sending message to users: " + notificationText);
            try {
                telegramClient.execute(sendMessage);
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
                log.error(Arrays.toString(e.getStackTrace()));
            } finally {
                botStateContextDAO.changeBotState(user.getId(), BotState.DEFAULT);
            }
        }

        return SendMessage.builder().chatId(update.getMessage().getChatId()).text("Уведомление отправлено всем пользователям").build();
    }


    private SendMessage addVideo(Update update, SendMessage sendMessage, User user) {
        String recipeId = botStateContextDAO.findBotStateContextById(user.getId()).getAdditionalData();
        Recipe recipe = recipeDAO.findRecipeById(recipeId);
        String fileId;
        if (update.getMessage().getVideo() != null) {
            fileId = update.getMessage().getVideo().getFileId();
            recipe.setVideoId(fileId);
        } else if (update.getMessage().getAnimation() != null) {
            fileId = update.getMessage().getAnimation().getFileId();
            recipe.setAnimationId(fileId);
        } else {
            sendMessage.setText("There is no video in this message");
            return sendMessage;
        }

        recipeDAO.updateRecipe(recipe);
        sendMessage.setText("Видео обновлено");

        botStateContextDAO.changeBotState(user.getId(), BotState.DEFAULT);
        return sendMessage;
    }

    private SendMessage updateRecipe(Update update, SendMessage sendMessage, User user) {
        Recipe recipe;

        String inputText = update.getMessage().getText();
        String[] split = inputText.split("//");
        if (split.length != 3) {
            sendMessage.setText("Неверный формат: не стирайте служебную строку /edit_recipe//...//");
            log.debug(this.getClass().getName() + " split.length=" + split.length);
            botStateContextDAO.changeBotState(user.getId(), BotState.DEFAULT);
            return sendMessage;
        }
        String recipeId = split[1];
        String recipeString = split[2];
        try {
            recipe = RecipeParser.parseRecipeFromString(recipeString);
            Recipe recipeToEdit = recipeDAO.findRecipeById(recipeId);
            recipeToEdit.setName(recipe.getName());
            recipeToEdit.setIngredients(recipe.getIngredients());
            recipeToEdit.setInstructions(recipe.getInstructions());
            recipeToEdit.setHashtags(recipe.getHashtags());

            recipeDAO.updateRecipe(recipeToEdit);
            sendMessage.setText("Рецепт изменен");
            actionFactory.sendRecipesList(update, List.of(recipeToEdit));
        } catch (ParseException e) {
            sendMessage.setText(BotMessageEnum.RECIPE_PARSING_ERROR.getMessage() + e.getMessage());
            log.error(e.getMessage());
            log.error(Arrays.toString(e.getStackTrace()));

        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            log.error(Arrays.toString(e.getStackTrace()));
        } finally {
            botStateContextDAO.changeBotState(user.getId(), BotState.DEFAULT);
        }

        return sendMessage;
    }

    private SendMessage addPhoto(Update update, SendMessage sendMessage, User user) {
        String recipeId = botStateContextDAO.findBotStateContextById(user.getId()).getAdditionalData();
        Recipe recipe = recipeDAO.findRecipeById(recipeId);
        String fileId = update.getMessage().hasPhoto() ? update.getMessage().getPhoto().stream()
                .max(Comparator.comparing(PhotoSize::getFileSize))
                .map(PhotoSize::getFileId)
                .orElse("") : null;
        String thumbnailId = update.getMessage().hasPhoto() ? update.getMessage().getPhoto().stream()
                .min(Comparator.comparing(PhotoSize::getFileSize))
                .map(PhotoSize::getFileId)
                .orElse("") : null;
        recipe.setPhotoId(fileId);
        recipe.setThumbnailId(thumbnailId);
        recipeDAO.updateRecipe(recipe);
        sendMessage.setText("Фото обновлено");

        botStateContextDAO.changeBotState(user.getId(), BotState.DEFAULT);
        return sendMessage;
    }

    private SendMessage addNewRecipe(Update update, SendMessage sendMessage, User user) {

        Recipe recipe;
        try {

            String inputText = update.getMessage().hasText() ? update.getMessage().getText() : update.getMessage().getCaption();
            recipe = RecipeParser.parseRecipeFromString(inputText);
        } catch (ParseException e) {
            log.error(e.getMessage());
            log.error(Arrays.toString(e.getStackTrace()));
            sendMessage.setText(BotMessageEnum.RECIPE_PARSING_ERROR.getMessage() + e.getMessage());
            return sendMessage;
        }

        if (checkIsRecipeAlreadyExists(recipe)) {
            sendMessage.setText(BotMessageEnum.RECIPE_ALREADY_EXISTS.getMessage());
        } else {

            if (update.getMessage().hasPhoto()) {
                String photoId = update.getMessage().getPhoto().stream()
                        .max(Comparator.comparing(PhotoSize::getFileSize))
                        .map(PhotoSize::getFileId).orElse(null);
                recipe.setPhotoId(photoId);
            } else if (update.getMessage().hasAnimation()) {
                String animationId = update.getMessage().getAnimation().getFileId();
                recipe.setAnimationId(animationId);
            } else if (update.getMessage().hasVideo()) {
                String videoId = update.getMessage().getVideo().getFileId();
                recipe.setVideoId(videoId);
            }

            Recipe savedRecipe = recipeDAO.saveRecipe(recipe);

            try {
                actionFactory.sendRecipesList(update, List.of(savedRecipe));
                sendMessage.setText(BotMessageEnum.RECIPE_ADDED.getMessage());
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
                log.error(Arrays.toString(e.getStackTrace()));
                sendMessage.setText(BotMessageEnum.RECIPE_SENDING_ERROR.getMessage());
            } finally {
                botStateContextDAO.changeBotState(user.getId(), BotState.DEFAULT);
            }
        }

        return sendMessage;
    }


    private boolean checkIsRecipeAlreadyExists(Recipe recipe) {
        String recipeName = recipe.getName().toLowerCase();
        return recipeDAO.findRecipeByNameEqualsIgnoreCase(recipeName) != null;
    }


    private User getOrCreateUserFromUpdate(Update update) {
        if (update.getMessage() == null) {
            return null;
        }
        Long userId = update.getMessage().getFrom().getId();
        if (userDAO.findById(userId).isPresent()) {
            return userDAO.findById(userId).get();
        } else {
            return createUser(update);
        }
    }

    private User createUser(Update update) {
        User user = new User();
        user.setId(update.getMessage().getFrom().getId());
        user.setFirstName(update.getMessage().getFrom().getFirstName());
        user.setLastName(update.getMessage().getFrom().getLastName());
        String userName = update.getMessage().getFrom().getUserName();
        user.setUserName(userName);
        user.setIsAdmin(userDAO.isFirstAdmin(userName));
        user.setChatId(update.getMessage().getChatId());
        userDAO.saveUser(user);
        return user;
    }
}
