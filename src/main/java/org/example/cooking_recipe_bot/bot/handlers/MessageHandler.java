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
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
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
@Component

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


        Map<String, Runnable> buttonActions = new HashMap<>();
        buttonActions.put("/start", getStartAction(update, user));
        buttonActions.put(ButtonNameEnum.HELP_BUTTON.getButtonName().toLowerCase(), getHelpAction(update, user));
        buttonActions.put(ButtonNameEnum.FIND_RANDOM_RECIPE_BUTTON.getButtonName().toLowerCase(), getFindRandomRecipeAction(update));
        buttonActions.put(ButtonNameEnum.ADD_RECIPE_BUTTON.getButtonName().toLowerCase(), getAddRecipeAction(update));
        buttonActions.put(ButtonNameEnum.USERS_BUTTON.getButtonName().toLowerCase(), getGetUsersAction(update));

        SendMessage sendMessage = SendMessage.builder().chatId(chatId).text("").build();

        if (message.hasText()) {
            String inputText = message.getText().toLowerCase();
            if (buttonActions.containsKey(inputText)) {
                buttonActions.get(inputText).run();
            } else {
                if (botStateContext.getCurrentBotState().equals(BotState.DEFAULT)) {
                    //todo change search
                    List<Recipe> recipesByHashtagsContains = recipeDAO.findRecipesByHashtagsContains(inputText);

                    try {
                        sendRecipesList(update, recipesByHashtagsContains);
                    } catch (TelegramApiException e) {
                        log.error(e.getMessage());
                        e.printStackTrace();
                    }
                } else if (botStateContext.getCurrentBotState().equals(BotState.ADDING_RECIPE)) {
                    sendMessage = addNewRecipe(update, sendMessage, user);

                } else if (botStateContext.getCurrentBotState().equals(BotState.WAITING_FOR_EDITED_RECIPE)) {

                    sendMessage = updateRecipe(update, sendMessage, user);
                }
            }

        } else if (message.hasPhoto()) {

            if (botStateContext.getCurrentBotState().equals(BotState.DEFAULT)) {
                sendMessage.setText("Отправьте ключевое слово или ингредиент для поиска рецептов");
            } else if (botStateContext.getCurrentBotState().equals(BotState.ADDING_RECIPE)) {
                sendMessage = addNewRecipe(update, sendMessage, user);
            } else if (botStateContext.getCurrentBotState().equals(BotState.WAITING_FOR_PHOTO)) {
                sendMessage = addPhoto(update, sendMessage, user);
            }

        }

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
        if (!update.getMessage().hasText() && !update.getMessage().hasPhoto()) {

            botStateContextDAO.changeBotState(user.getUserName(), BotState.DEFAULT);
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
                throw new RuntimeException(e);
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
                throw new RuntimeException(e);
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
                throw new RuntimeException(e);
            }
        };
    }

    private boolean checkIsRecipeAlreadyExists(Recipe recipe) {
        String recipeName = recipe.getName();
        if (recipeDAO.findRecipeByNameEqualsIgnoreCase(recipeName) != null) {
            return true;
        }
        return false;
    }

    private void sendRecipesList(Update update, List<Recipe> recipeList) throws TelegramApiException {
        Long userId = update.getMessage().getFrom().getId();
        //TODO: add pagination
        if(recipeList.isEmpty()) {
            SendMessage sendMessage = SendMessage.builder().chatId(update.getMessage().getChatId()).text(BotMessageEnum.RECIPE_NOT_FOUND.getMessage()).build();
            telegramClient.execute(sendMessage);
            return;
        }
        for (Recipe recipe : recipeList) {
            if (recipe.getPhotoId() == null) {
                SendMessage sendMessage = SendMessage.builder().chatId(update.getMessage().getChatId()).text("Рецепт:").build();

                if (userDAO.findById(userId).get().getIsAdmin()) {
                    sendMessage.setReplyMarkup(inlineKeyboardMaker.getRecipeAdminKeyboard(recipe,0));
                } else {
                    sendMessage.setReplyMarkup(inlineKeyboardMaker.getRecipeKeyboard(recipe,0));
                }
                telegramClient.execute(sendMessage);
            } else {
                SendPhoto sendPhoto = SendPhoto.builder().chatId(update.getMessage().getChatId())
                        .caption("").photo(new InputFile(recipe.getPhotoId())).build();

                if (userDAO.findById(userId).get().getIsAdmin()) {
                    sendPhoto.setReplyMarkup(inlineKeyboardMaker.getRecipeAdminKeyboard(recipe,0));
                } else {
                    sendPhoto.setReplyMarkup(inlineKeyboardMaker.getRecipeKeyboard(recipe,0));
                }
                telegramClient.execute(sendPhoto);
            }

        }
    }

    private void sendUsersList(Update update) throws TelegramApiException {
        List<User> allUsers = userDAO.findAllUsers();

        for (User user1 : allUsers) {
            if (user1.getUserName().equals(update.getMessage().getFrom().getUserName()) || userDAO.isFirstAdmin(user1))
                continue;
            SendMessage sendMessage = SendMessage.builder().chatId(update.getMessage().getChatId()).text(user1.toString()).build();
            if (user1.getIsAdmin()) {
                sendMessage.setReplyMarkup(inlineKeyboardMaker.getUserAdminKeyboard(user1.getId()));
            } else {
                sendMessage.setReplyMarkup(inlineKeyboardMaker.getUserKeyboard(user1.getId()));
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
        if (userDAO.isFirstAdmin(user)) {
            user.setIsAdmin(true);
        } else {
            user.setIsAdmin(false);
        }
        userDAO.saveUser(user);
        return user;
    }


}
