package org.example.cooking_recipe_bot.bot.handlers;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import lombok.extern.slf4j.Slf4j;
import org.example.cooking_recipe_bot.bot.ActionFactory;
import org.example.cooking_recipe_bot.bot.BotState;
import org.example.cooking_recipe_bot.bot.constants.BotMessageEnum;
import org.example.cooking_recipe_bot.bot.MessageTranslator;
import org.example.cooking_recipe_bot.db.dao.BotStateContextDAO;
import org.example.cooking_recipe_bot.db.dao.RecipeDAOManager;
import org.example.cooking_recipe_bot.db.dao.UserDAO;
import org.example.cooking_recipe_bot.db.entity.BotStateContext;
import org.example.cooking_recipe_bot.db.entity.MyMessageEntity;
import org.example.cooking_recipe_bot.db.entity.Recipe;
import org.example.cooking_recipe_bot.db.entity.User;
import org.example.cooking_recipe_bot.utils.MessageEntityMapper;
import org.example.cooking_recipe_bot.utils.RecipeParser;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.text.ParseException;
import java.util.*;

import static org.example.cooking_recipe_bot.bot.constants.BotMessageEnum.*;

@Slf4j
@Service
public class MessageHandler implements UpdateHandler {
    private final RecipeDAOManager recipeDAOManager;
    private final UserDAO userDAO;
    private final TelegramClient telegramClient;
    private final BotStateContextDAO botStateContextDAO;
    private final ActionFactory actionFactory;
    private final MessageTranslator messageTranslator;


    public MessageHandler(UserDAO userDAO, TelegramClient telegramClient, RecipeDAOManager recipeDAOManager, BotStateContextDAO botStateContextDAO, ActionFactory actionFactory, MessageTranslator messageTranslator) {
        this.userDAO = userDAO;
        this.telegramClient = telegramClient;
        this.recipeDAOManager = recipeDAOManager;
        this.botStateContextDAO = botStateContextDAO;
        this.actionFactory = actionFactory;
        this.messageTranslator = messageTranslator;
    }

    private void translateRecipeAndSave(Update update, String inputText, String sourceLanguage) {
        Translate translate = TranslateOptions.getDefaultInstance().getService();
        String targetLanguage = sourceLanguage.equals("en") ? "ru" : "en";
        Translation translated = translate.translate(inputText, Translate.TranslateOption.sourceLanguage(sourceLanguage),
                Translate.TranslateOption.targetLanguage(targetLanguage),
                Translate.TranslateOption.model("base"),
                Translate.TranslateOption.format("text"));
        String translatedText = translated.getTranslatedText();
        Recipe recipe;
        try {
            recipe = RecipeParser.parseRecipeFromString(translatedText, targetLanguage);
            recipe.setDateOfCreation(new Date());
            recipe.setDateOfLastEdit(new Date());
        } catch (ParseException e) {
            log.error(e.getMessage());
            log.error(Arrays.toString(e.getStackTrace()));
            return;
        }
        if (checkIsRecipeAlreadyExists(recipe, targetLanguage)) {
            log.error("Recipe already exists " + recipe.getName());
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

            recipeDAOManager.getRecipeDAO(targetLanguage).saveRecipe(recipe);

        }

    }

    @Override
    public SendMessage handle(Update update) {
        if (!update.hasMessage()) {
            log.error("{} No message in update", this.getClass().getName());
            log.error(update.toString());
            return null;
        }
        Message message = update.getMessage();
        User user = getOrCreateUserFromUpdate(update);
        if (user == null) {
            log.error("{} No user in update", this.getClass().getName());
            log.error(update.toString());
            return null;
        }
        BotStateContext botStateContext = getOrCreateBotStateContext(user);
        long chatId = message.getChatId();
        SendMessage sendMessage = SendMessage.builder().chatId(chatId).text("").build();

        Map<String, Runnable> buttonActions = actionFactory.createButtonActions(user, chatId);

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
                        return sendNotificationToUsers(update);
                }
            }
        } else {
            sendMessage = handleNoTextState(botStateContext, sendMessage, update, user);
        }

        return sendMessage;
    }

    private void handleDefaultState(Update update, String inputText) {
        long userId = update.getMessage().getFrom().getId();
        long chatId = update.getMessage().getChatId();
        User user = userDAO.getUserById(userId);
        List<Recipe> recipes = recipeDAOManager.getRecipeDAO(user.getLanguage()).findRecipesByString(inputText);

        actionFactory.sendRecipesList(userId, chatId, recipes);
    }

    private SendMessage handleWaitingForPhotoState(SendMessage sendMessage, String inputText, User user) {
        if ("delete".equals(inputText)) {
            String recipeId = botStateContextDAO.findBotStateContextById(user.getId()).getAdditionalData();
            Recipe recipe = recipeDAOManager.getRecipeDAO(user.getLanguage()).findRecipeById(recipeId);
            recipe.setPhotoId(null);
            recipeDAOManager.getRecipeDAO(user.getLanguage()).saveRecipe(recipe);
            sendMessage.setText(messageTranslator.getMessage(PHOTO_WAS_DELETED_MESSAGE.name(), user.getLanguage()));
            botStateContextDAO.changeBotState(user.getId(), BotState.DEFAULT);
        } else {
            sendMessage.setText(messageTranslator.getMessage(SEND_ME_PHOTO_MESSAGE.name(), user.getLanguage()));
        }
        return sendMessage;
    }

    private SendMessage handleWaitingForVideoState(SendMessage sendMessage, String inputText, User user) {
        if ("delete".equals(inputText)) {
            String recipeId = botStateContextDAO.findBotStateContextById(user.getId()).getAdditionalData();
            Recipe recipe = recipeDAOManager.getRecipeDAO(user.getLanguage()).findRecipeById(recipeId);
            recipe.setVideoId(null);
            recipe.setAnimationId(null);
            recipeDAOManager.getRecipeDAO(user.getLanguage()).saveRecipe(recipe);
            sendMessage.setText(messageTranslator.getMessage(VIDEO_WAS_DELETED_MESSAGE.name(), user.getLanguage()));
            botStateContextDAO.changeBotState(user.getId(), BotState.DEFAULT);
        } else {
            sendMessage.setText(messageTranslator.getMessage(SEND_ME_VIDEO_MESSAGE.name(), user.getLanguage()));
        }
        return sendMessage;
    }

    private SendMessage handleNoTextState(BotStateContext botStateContext, SendMessage sendMessage, Update update, User user) {
        switch (botStateContext.getCurrentBotState()) {
            case DEFAULT:
                sendMessage.setText(messageTranslator.getMessage(INSERT_KEYWORD_MESSAGE.name(), user.getLanguage()));
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

    private SendMessage sendNotificationToUsers(Update update) {
        List<MessageEntity> messageEntities = update.getMessage().getEntities();
        String notificationText = update.getMessage().getText();
        List<User> users = userDAO.findAllUsers();
        for (User usr : users) {
            SendMessage sendMessage = SendMessage.builder().chatId(usr.getChatId()).text(notificationText).entities(messageEntities).build();

            try {
                log.info("Sending message to user {}", usr);
                telegramClient.execute(sendMessage);
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
                log.error(Arrays.toString(e.getStackTrace()));
            } finally {
                botStateContextDAO.changeBotState(usr.getId(), BotState.DEFAULT);
            }
        }

        return SendMessage.builder().chatId(update.getMessage().getChatId()).text(messageTranslator.getMessage(BotMessageEnum.NOTIFICATION_WAS_SENT_MESSAGE.name(), update.getMessage().getFrom().getLanguageCode())).build();
    }

    private SendMessage addVideo(Update update, SendMessage sendMessage, User user) {
        String recipeId = botStateContextDAO.findBotStateContextById(user.getId()).getAdditionalData();
        Recipe recipe = recipeDAOManager.getRecipeDAO(user.getLanguage()).findRecipeById(recipeId);
        String fileId;
        if (update.getMessage().getVideo() != null) {
            fileId = update.getMessage().getVideo().getFileId();
            recipe.setVideoId(fileId);
        } else if (update.getMessage().getAnimation() != null) {
            fileId = update.getMessage().getAnimation().getFileId();
            recipe.setAnimationId(fileId);
        } else {
            sendMessage.setText(messageTranslator.getMessage(VIDEO_NOT_FOUND_MESSAGE.name(), user.getLanguage()));
            return sendMessage;
        }

        recipe.setDateOfLastEdit(new Date());
        recipeDAOManager.getRecipeDAO(user.getLanguage()).saveRecipe(recipe);
        sendMessage.setText(messageTranslator.getMessage(VIDEO_UPDATED_MESSAGE.name(), user.getLanguage()));

        botStateContextDAO.changeBotState(user.getId(), BotState.DEFAULT);
        return sendMessage;
    }

    private SendMessage updateRecipe(Update update, SendMessage sendMessage, User user) {
        Recipe recipe;
        String inputText = update.getMessage().getText();
        List<MyMessageEntity> messageEntities = new ArrayList<>();
        int offset = inputText.lastIndexOf("///") + 3;
        if (update.getMessage().getEntities() != null) {
            messageEntities = MessageEntityMapper.mapToMyMessageEntities(update.getMessage().getEntities(), offset);
        }

        String[] split = inputText.split("///");
        if (split.length != 3) {
            sendMessage.setText(messageTranslator.getMessage(RECIPE_UPDATING_FORMAT_ERROR.name(), user.getLanguage()));
            log.debug(this.getClass().getName() + " split.length=" + split.length);
            botStateContextDAO.changeBotState(user.getId(), BotState.DEFAULT);
            return sendMessage;
        }
        String recipeId = split[1];
        String recipeString = split[2];
        try {
            Long userId = user.getId();
            Long chatId = update.getMessage().getChatId();
            recipe = RecipeParser.parseRecipeFromString(recipeString, user.getLanguage());
            Recipe recipeToEdit = recipeDAOManager.getRecipeDAO(user.getLanguage()).findRecipeById(recipeId);
            recipeToEdit.setText(recipe.getText());
            recipeToEdit.setName(recipe.getName());
            recipeToEdit.setIngredients(recipe.getIngredients());
            recipeToEdit.setInstructions(recipe.getInstructions());
            recipeToEdit.setHashtags(recipe.getHashtags());
            recipeToEdit.setMessageEntities(messageEntities);
            recipeToEdit.setDateOfLastEdit(new Date());

            recipeDAOManager.getRecipeDAO(user.getLanguage()).saveRecipe(recipeToEdit);
            sendMessage.setText(messageTranslator.getMessage(RECIPE_UPDATED_MESSAGE.name(), user.getLanguage()));
            actionFactory.sendRecipesList(userId, chatId, List.of(recipeToEdit));
        } catch (ParseException e) {
            sendMessage.setText(messageTranslator.getMessage(RECIPE_PARSING_ERROR.name(), user.getLanguage()) + e.getMessage());
            log.error(e.getMessage());
            log.error(Arrays.toString(e.getStackTrace()));

        } finally {
            botStateContextDAO.changeBotState(user.getId(), BotState.DEFAULT);
        }

        return sendMessage;
    }

    private SendMessage addPhoto(Update update, SendMessage sendMessage, User user) {
        String recipeId = botStateContextDAO.findBotStateContextById(user.getId()).getAdditionalData();
        Recipe recipe = recipeDAOManager.getRecipeDAO(user.getLanguage()).findRecipeById(recipeId);
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
        recipe.setDateOfLastEdit(new Date());
        recipeDAOManager.getRecipeDAO(user.getLanguage()).saveRecipe(recipe);
        sendMessage.setText(messageTranslator.getMessage(PHOTO_UPDATED_MESSAGE.name(), user.getLanguage()));
        actionFactory.sendRecipesList(user.getId(), update.getMessage().getChatId(), List.of(recipe));
        botStateContextDAO.changeBotState(user.getId(), BotState.DEFAULT);
        return sendMessage;
    }

    private SendMessage addNewRecipe(Update update, SendMessage sendMessage, User user) {
        List<MyMessageEntity> messageEntities = new ArrayList<>();
        String languageCode = user.getLanguage();
        if (update.getMessage().getEntities() != null) {
            messageEntities = MessageEntityMapper.mapToMyMessageEntities(update.getMessage().getEntities(), 0);
        }
        String inputText = update.getMessage().hasText() ? update.getMessage().getText() : update.getMessage().getCaption();


        Recipe recipe;
        try {
            recipe = RecipeParser.parseRecipeFromString(inputText, languageCode);
            recipe.setDateOfCreation(new Date());
            recipe.setDateOfLastEdit(new Date());
        } catch (ParseException e) {
            log.error(e.getMessage());
            log.error(Arrays.toString(e.getStackTrace()));
            sendMessage.setText(messageTranslator.getMessage(RECIPE_PARSING_ERROR.name(), languageCode) + e.getMessage());
            return sendMessage;
        }

        if (checkIsRecipeAlreadyExists(recipe, languageCode)) {
            sendMessage.setText(messageTranslator.getMessage(RECIPE_ALREADY_EXISTS_MESSAGE.name(), languageCode));
        } else {
            translateRecipeAndSave(update, inputText, languageCode);
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

            recipe.setMessageEntities(messageEntities);
            Recipe savedRecipe = recipeDAOManager.getRecipeDAO(languageCode).saveRecipe(recipe);

            Long userId = user.getId();
            Long chatId = update.getMessage().getChatId();
            actionFactory.sendRecipesList(userId, chatId, List.of(savedRecipe));
            sendMessage.setText(messageTranslator.getMessage(RECIPE_ADDED_MESSAGE.name(), languageCode));

            botStateContextDAO.changeBotState(userId, BotState.DEFAULT);
        }

        return sendMessage;
    }

    private boolean checkIsRecipeAlreadyExists(Recipe recipe, String language) {
        String recipeName = recipe.getName().toLowerCase();
        return recipeDAOManager.getRecipeDAO(language).findRecipeByNameEqualsIgnoreCase(recipeName) != null;
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
        String languageCode = update.getMessage().getFrom().getLanguageCode() == null ? "en" : update.getMessage().getFrom().getLanguageCode();
        String userName = update.getMessage().getFrom().getUserName();
        user.setId(update.getMessage().getFrom().getId());
        user.setFirstName(update.getMessage().getFrom().getFirstName());
        user.setLastName(update.getMessage().getFrom().getLastName());
        user.setUserName(userName);
        user.setIsAdmin(userDAO.isFirstAdmin(userName));
        user.setChatId(update.getMessage().getChatId());
        user.setLanguage(languageCode);
        userDAO.saveUser(user);
        return user;
    }
}
