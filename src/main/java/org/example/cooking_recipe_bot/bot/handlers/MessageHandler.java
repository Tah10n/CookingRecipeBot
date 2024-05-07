package org.example.cooking_recipe_bot.bot.handlers;

import lombok.extern.slf4j.Slf4j;
import org.example.cooking_recipe_bot.bot.BotState;
import org.example.cooking_recipe_bot.db.dao.BotStateContextDAO;
import org.example.cooking_recipe_bot.db.entity.BotStateContext;
import org.example.cooking_recipe_bot.bot.keyboards.InlineKeyboardMaker;
import org.example.cooking_recipe_bot.bot.keyboards.ReplyKeyboardMaker;
import org.example.cooking_recipe_bot.utils.constants.BotMessageEnum;
import org.example.cooking_recipe_bot.utils.constants.ButtonNameEnum;
import org.example.cooking_recipe_bot.db.dao.RecipeDAO;
import org.example.cooking_recipe_bot.db.dao.UserDAO;
import org.example.cooking_recipe_bot.db.entity.Recipe;
import org.example.cooking_recipe_bot.db.entity.User;
import org.example.cooking_recipe_bot.utils.RecipeParser;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.text.ParseException;
import java.util.Comparator;
import java.util.HashMap;
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


    public MessageHandler(ReplyKeyboardMaker replyKeyboardMaker, InlineKeyboardMaker inlineKeyboardMaker, UserDAO userDAO, TelegramClient telegramClient, RecipeDAO recipeDAO, BotStateContextDAO botStateContextDAO) {
        this.replyKeyboardMaker = replyKeyboardMaker;
        this.inlineKeyboardMaker = inlineKeyboardMaker;
        this.userDAO = userDAO;
        this.telegramClient = telegramClient;
        this.recipeDAO = recipeDAO;
        this.botStateContextDAO = botStateContextDAO;
    }

    @Override
    public SendMessage handle(Update update) {
        Message message = update.getMessage();

        User user = getUserFromUpdate(update);
        BotStateContext botStateContext = botStateContextDAO.findBotStateContextByUserName(user.getUserName());
        if (botStateContext == null) {
            botStateContext = new BotStateContext();
            botStateContext.setUserName(user.getUserName());
            botStateContext.setCurrentBotState(BotState.DEFAULT);
            botStateContextDAO.saveBotStateContext(botStateContext);
        }

        long chatId = message.getChatId();

//todo refactor to new class
        Map<String, Runnable> buttonActions = new HashMap<>();
        buttonActions.put("/start", getStartAction(update, user));
        buttonActions.put(ButtonNameEnum.HELP_BUTTON.getButtonName().toLowerCase(), getHelpAction(update, user));
        buttonActions.put(ButtonNameEnum.FIND_RANDOM_RECIPE_BUTTON.getButtonName().toLowerCase(), getFindRandomRecipeAction(update));
        buttonActions.put(ButtonNameEnum.ADD_RECIPE_BUTTON.getButtonName().toLowerCase(), getAddRecipeAction(update));
        buttonActions.put(ButtonNameEnum.USERS_BUTTON.getButtonName().toLowerCase(), getGetUsersAction(update));
        buttonActions.put(ButtonNameEnum.SEND_NOTIFICATION.getButtonName().toLowerCase(), getSendNotificationAction(update));

        SendMessage sendMessage = SendMessage.builder().chatId(chatId).text("").build();

        if (message.hasText()) {

            String inputText = message.getText().toLowerCase();
            if (buttonActions.containsKey(inputText)) {
                buttonActions.get(inputText).run();
            } else {
                if (botStateContext.getCurrentBotState().equals(BotState.DEFAULT)) {
                    List<Recipe> recipes = recipeDAO.findRecipesByString(inputText);

                    try {
                        sendRecipesList(update, recipes);
                    } catch (TelegramApiException e) {
                        log.error(e.getMessage());
                        e.printStackTrace();
                    }
                } else if (botStateContext.getCurrentBotState().equals(BotState.ADDING_RECIPE)) {
                    sendMessage = addNewRecipe(update, sendMessage, user);

                } else if (botStateContext.getCurrentBotState().equals(BotState.WAITING_FOR_EDITED_RECIPE)) {

                    sendMessage = updateRecipe(update, sendMessage, user);
                } else if (botStateContext.getCurrentBotState().equals(BotState.WAITING_FOR_PHOTO) && inputText.equals("delete")) {
                    String recipeId = botStateContextDAO.findBotStateContextByUserName(user.getUserName()).getAdditionalData();
                    Recipe recipe = recipeDAO.findRecipeById(recipeId);
                    recipe.setPhotoId(null);
                    recipeDAO.saveRecipe(recipe);
                    sendMessage.setText("Фото удалено");
                    botStateContextDAO.changeBotState(user.getUserName(), BotState.DEFAULT);
                } else if (botStateContext.getCurrentBotState().equals(BotState.WAITING_FOR_VIDEO) && inputText.equals("delete")) {
                    String recipeId = botStateContextDAO.findBotStateContextByUserName(user.getUserName()).getAdditionalData();
                    Recipe recipe = recipeDAO.findRecipeById(recipeId);
                    recipe.setVideoId(null);
                    recipe.setAnimationId(null);
                    recipeDAO.saveRecipe(recipe);
                    sendMessage.setText("Видео удалено");
                    botStateContextDAO.changeBotState(user.getUserName(), BotState.DEFAULT);
                } else if (botStateContext.getCurrentBotState().equals(BotState.WAITING_FOR_NOTIFICATION)) {
                    sendMessage = sendNotificationToUsers(update, user);

                }
            }

        } else  {

            if (botStateContext.getCurrentBotState().equals(BotState.DEFAULT)) {
                sendMessage.setText("Отправьте ключевое слово или ингредиент для поиска рецептов");
            } else if (botStateContext.getCurrentBotState().equals(BotState.ADDING_RECIPE)) {
                sendMessage = addNewRecipe(update, sendMessage, user);
            } else if (botStateContext.getCurrentBotState().equals(BotState.WAITING_FOR_PHOTO)) {
                sendMessage = addPhoto(update, sendMessage, user);
            } else if (botStateContext.getCurrentBotState().equals(BotState.WAITING_FOR_VIDEO)) {
                sendMessage = addVideo(update, sendMessage, user);
            }

        }

        return sendMessage;
    }

    private SendMessage sendNotificationToUsers(Update update, User user) {
        String notificationText = update.getMessage().getText();
        List<User> users = userDAO.findAllUsers();
        for (User u : users) {
            SendMessage sendMessage = SendMessage.builder().chatId(u.getChatId()).text(notificationText).build();
            try {
                telegramClient.execute(sendMessage);
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
                e.printStackTrace();
            }
        }
        botStateContextDAO.changeBotState(user.getUserName(), BotState.DEFAULT);
        return SendMessage.builder().chatId(update.getMessage().getChatId()).text("Уведомления отправлены").build();
    }

    private @NotNull Runnable getSendNotificationAction(Update update) {
        return () -> {
            Long chatId = update.getMessage().getChatId();
            SendMessage sendMessage = SendMessage.builder().chatId(chatId).text(BotMessageEnum.INSERT_NOTIFICATION_MESSAGE.getMessage()).build();
            try {
                telegramClient.execute(sendMessage);
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
                e.printStackTrace();
            }

            botStateContextDAO.changeBotState(update.getMessage().getFrom().getUserName(), BotState.WAITING_FOR_NOTIFICATION);
        };
    }

    private SendMessage addVideo(Update update, SendMessage sendMessage, User user) {
        String recipeId = botStateContextDAO.findBotStateContextByUserName(user.getUserName()).getAdditionalData();
        Recipe recipe = recipeDAO.findRecipeById(recipeId);
        String file_id;
        if(update.getMessage().getVideo() != null){
            file_id = update.getMessage().getVideo().getFileId();
            recipe.setVideoId(file_id);
        } else if (update.getMessage().getAnimation() != null) {
            file_id = update.getMessage().getAnimation().getFileId();
            recipe.setAnimationId(file_id);
        } else {
            sendMessage.setText("There is no video in this message");
            return sendMessage;
        }

        recipeDAO.updateRecipe(recipe);
        sendMessage.setText("Видео обновлено");

        botStateContextDAO.changeBotState(user.getUserName(), BotState.DEFAULT);
        return sendMessage;
    }

    private SendMessage updateRecipe(Update update, SendMessage sendMessage, User user) {
        Recipe recipe = null;
        String inputText = update.getMessage().getText();
        String[] split = inputText.split("//");
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
            sendRecipesList(update, List.of(recipeToEdit));
        } catch (ParseException e) {
            sendMessage.setText("Не удалось распарсить рецепт");
            e.printStackTrace();
            log.error(e.getMessage());

        } catch (TelegramApiException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }

        botStateContextDAO.changeBotState(user.getUserName(), BotState.DEFAULT);
        return sendMessage;
    }

    private SendMessage addPhoto(Update update, SendMessage sendMessage, User user) {
        String recipeId = botStateContextDAO.findBotStateContextByUserName(user.getUserName()).getAdditionalData();
        Recipe recipe = recipeDAO.findRecipeById(recipeId);
        String f_id = update.getMessage().hasPhoto() ? update.getMessage().getPhoto().stream()
                .max(Comparator.comparing(PhotoSize::getFileSize))
                .map(PhotoSize::getFileId)
                .orElse("") : null;
        recipe.setPhotoId(f_id);
        recipeDAO.updateRecipe(recipe);
        sendMessage.setText("Фото обновлено");

        botStateContextDAO.changeBotState(user.getUserName(), BotState.DEFAULT);
        return sendMessage;
    }

    private SendMessage addNewRecipe(Update update, SendMessage sendMessage, User user) {
        if (!update.hasMessage()) {

            botStateContextDAO.changeBotState(user.getUserName(), BotState.DEFAULT);
            log.error("No message in update");
            return sendMessage;
        }

        Recipe recipe;
        try {

            String inputText = update.getMessage().hasText() ? update.getMessage().getText() : update.getMessage().getCaption();
            recipe = RecipeParser.parseRecipeFromString(inputText);
        } catch (ParseException e) {
            log.error(e.getMessage());
            e.printStackTrace();
            sendMessage.setText(BotMessageEnum.RECIPE_PARSING_ERROR.getMessage());
            return sendMessage;
        }

        if (checkIsRecipeAlreadyExists(recipe)) {
            sendMessage.setText(BotMessageEnum.RECIPE_ALREADY_EXISTS.getMessage());
        } else {
            String f_id = update.getMessage().hasPhoto() ? update.getMessage().getPhoto().stream()
                    .max(Comparator.comparing(PhotoSize::getFileSize))
                    .map(PhotoSize::getFileId)
                    .orElse("") : null;
            recipe.setPhotoId(f_id);

            if(update.getMessage().hasAnimation()) {
                String animationId = update.getMessage().getAnimation().getFileId();
                recipe.setAnimationId(animationId);
            } else if(update.getMessage().hasVideo()) {
                String videoId = update.getMessage().getVideo().getFileId();
                recipe.setVideoId(videoId);
            }

            Recipe savedRecipe = recipeDAO.saveRecipe(recipe);

            try {
                sendRecipesList(update, List.of(savedRecipe));
                sendMessage.setText(BotMessageEnum.RECIPE_ADDED.getMessage());
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
                e.printStackTrace();
                sendMessage.setText(BotMessageEnum.RECIPE_SENDING_ERROR.getMessage());
            }
        }

        botStateContextDAO.changeBotState(user.getUserName(), BotState.DEFAULT);
        return sendMessage;
    }

    private @NotNull Runnable getGetUsersAction(Update update) {
        return () -> {
            try {
                sendUsersList(update);
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
                e.printStackTrace();
            }
        };
    }

    private @NotNull Runnable getAddRecipeAction(Update update) {
        return () -> {
            Long chatId = update.getMessage().getChatId();
            SendMessage sendMessage = SendMessage.builder().chatId(chatId).text(BotMessageEnum.INSERT_RECIPE_MESSAGE.getMessage()).build();
            try {
                telegramClient.execute(sendMessage);
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
                e.printStackTrace();
            }

            botStateContextDAO.changeBotState(update.getMessage().getFrom().getUserName(), BotState.ADDING_RECIPE);
        };
    }

    private @NotNull Runnable getFindRandomRecipeAction(Update update) {
        return () -> {

            botStateContextDAO.changeBotState(update.getMessage().getFrom().getUserName(), BotState.DEFAULT);

            Recipe randomRecipe = recipeDAO.getRandomRecipe();
            if (randomRecipe != null) {
                try {
                    sendRecipesList(update, List.of(randomRecipe));
                } catch (TelegramApiException e) {
                    log.error(e.getMessage());
                    e.printStackTrace();
                }
            } else {
                SendMessage sendMessage = SendMessage.builder().chatId(update.getMessage().getChatId()).text(BotMessageEnum.RECIPE_NOT_FOUND.getMessage()).build();
                try {
                    telegramClient.execute(sendMessage);
                } catch (TelegramApiException e) {
                    log.error(e.getMessage());
                    e.printStackTrace();
                }
            }
        };
    }

    private @NotNull Runnable getHelpAction(Update update, User user) {
        return () -> {
            long chatId = update.getMessage().getChatId();

            try {

                telegramClient.execute(SendMessage.builder().chatId(chatId).text(BotMessageEnum.HELP_MESSAGE.getMessage()).replyMarkup(replyKeyboardMaker.getMainMenuKeyboard(user)).build());
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
                e.printStackTrace();
            }
            SendMessage.builder().chatId(chatId).text(BotMessageEnum.HELP_MESSAGE.getMessage()).replyMarkup(replyKeyboardMaker.getMainMenuKeyboard(user)).build();
        };
    }

    private @NotNull Runnable getStartAction(Update update, User user) {
        return () -> {
            botStateContextDAO.changeBotState(user.getUserName(), BotState.DEFAULT);
            long chatId = update.getMessage().getChatId();
            try {
                telegramClient.execute(SendMessage.builder().chatId(chatId).text("Привет, " + user.getUserName() + "\n\n" + BotMessageEnum.HELP_MESSAGE.getMessage()).replyMarkup(replyKeyboardMaker.getMainMenuKeyboard(user)).build());
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
                e.printStackTrace();
            }
        };
    }

    private boolean checkIsRecipeAlreadyExists(Recipe recipe) {
        String recipeName = recipe.getName().toLowerCase();
        return recipeDAO.findRecipeByNameEqualsIgnoreCase(recipeName) != null;
    }

    private void sendRecipesList(Update update, List<Recipe> recipeList) throws TelegramApiException {
        Long userId = update.getMessage().getFrom().getId();
        User user = userDAO.findById(userId).isPresent() ? userDAO.findById(userId).get() : null;
        if (user == null) {
            throw new TelegramApiException("User not found by id: " + userId);
        }
        //TODO: add pagination
        if (recipeList.isEmpty()) {
            SendMessage sendMessage = SendMessage.builder().chatId(update.getMessage().getChatId()).text(BotMessageEnum.RECIPE_NOT_FOUND.getMessage()).build();
            telegramClient.execute(sendMessage);
            return;
        }
        for (Recipe recipe : recipeList) {
            if (recipe.getAnimationId() != null && !recipe.getAnimationId().isEmpty()) {
                SendAnimation sendAnimation = SendAnimation.builder().chatId(update.getMessage().getChatId()).animation(new InputFile(recipe.getAnimationId())).build();
                if (user.getIsAdmin().equals(Boolean.TRUE)) {
                    sendAnimation.setReplyMarkup(inlineKeyboardMaker.getRecipeAdminKeyboard(recipe, 0));
                } else {
                    sendAnimation.setReplyMarkup(inlineKeyboardMaker.getRecipeKeyboard(recipe, 0));
                }
                telegramClient.execute(sendAnimation);
            } else if (recipe.getVideoId() != null && !recipe.getVideoId().isEmpty()) {
                SendVideo sendVideo = SendVideo.builder().chatId(update.getMessage().getChatId())
                        .video(new InputFile(recipe.getVideoId())).build();
                if (user.getIsAdmin().equals(Boolean.TRUE)) {
                    sendVideo.setReplyMarkup(inlineKeyboardMaker.getRecipeAdminKeyboard(recipe, 0));
                } else {
                    sendVideo.setReplyMarkup(inlineKeyboardMaker.getRecipeKeyboard(recipe, 0));
                }
                telegramClient.execute(sendVideo);
            } else if (recipe.getPhotoId() != null && !recipe.getPhotoId().isEmpty()) {
                SendPhoto sendPhoto = SendPhoto.builder().chatId(update.getMessage().getChatId())
                        .caption("").photo(new InputFile(recipe.getPhotoId())).build();

                if (user.getIsAdmin().equals(Boolean.TRUE)) {
                    sendPhoto.setReplyMarkup(inlineKeyboardMaker.getRecipeAdminKeyboard(recipe, 0));
                } else {
                    sendPhoto.setReplyMarkup(inlineKeyboardMaker.getRecipeKeyboard(recipe, 0));
                }
                telegramClient.execute(sendPhoto);

            } else {

                SendMessage sendMessage = SendMessage.builder().chatId(update.getMessage().getChatId()).text("Рецепт:").build();

                if (user.getIsAdmin().equals(Boolean.TRUE)) {
                    sendMessage.setReplyMarkup(inlineKeyboardMaker.getRecipeAdminKeyboard(recipe, 0));
                } else {
                    sendMessage.setReplyMarkup(inlineKeyboardMaker.getRecipeKeyboard(recipe, 0));
                }
                telegramClient.execute(sendMessage);
            }

        }
    }

    private void sendUsersList(Update update) throws TelegramApiException {
        List<User> allUsers = userDAO.findAllUsers();

        for (User user : allUsers) {
            if (user.getUserName().equals(update.getMessage().getFrom().getUserName()) || userDAO.isFirstAdmin(user))
                continue;
            SendMessage sendMessage = SendMessage.builder().chatId(update.getMessage().getChatId()).text(user.toString()).build();
            if (user.getIsAdmin().equals(Boolean.TRUE)) {
                sendMessage.setReplyMarkup(inlineKeyboardMaker.getUserAdminKeyboard(user.getId()));
            } else {
                sendMessage.setReplyMarkup(inlineKeyboardMaker.getUserKeyboard(user.getId()));
            }
            telegramClient.execute(sendMessage);
        }

    }

    private User getUserFromUpdate(Update update) {
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
        user.setUserName(update.getMessage().getFrom().getUserName());
        user.setIsAdmin(userDAO.isFirstAdmin(user));
        user.setChatId(update.getMessage().getChatId());
        userDAO.saveUser(user);
        return user;
    }


}
