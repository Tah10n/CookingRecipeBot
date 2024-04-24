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

        User user = getUserFromUpdate(update);
        BotStateContext botStateContext = botStateContextDAO.findBotStateContextByUserName(user.getUserName());
        if (botStateContext == null) {
            botStateContext = new BotStateContext();
            botStateContext.setUserName(user.getUserName());
            botStateContext.setCurrentBotState(BotState.DEFAULT);
            botStateContextDAO.saveBotStateContext(botStateContext);
        }
        Message message = update.getMessage();
        long chatId = message.getChatId();


        Map<String, Runnable> buttonActions = new HashMap<>();
        buttonActions.put("/start", getStartAction(botStateContext, chatId, user));
        buttonActions.put(ButtonNameEnum.HELP_BUTTON.getButtonName().toLowerCase(), getHelpAction(chatId, user));
        buttonActions.put(ButtonNameEnum.FIND_RANDOM_RECIPE_BUTTON.getButtonName().toLowerCase(), getFindRandomRecipeAction(botStateContext, update));
        buttonActions.put(ButtonNameEnum.ADD_RECIPE_BUTTON.getButtonName().toLowerCase(), getAddRecipeAction(chatId, botStateContext));
        buttonActions.put(ButtonNameEnum.USERS_BUTTON.getButtonName().toLowerCase(), getGetUsersAction(update));

        SendMessage sendMessage = SendMessage.builder().chatId(chatId).text("").build();

        if (message.hasText()) {
            String inputText = message.getText().toLowerCase();
            if (buttonActions.containsKey(inputText)) {
                buttonActions.get(inputText).run();
            } else {
                if (botStateContext.getCurrentBotState().equals(BotState.DEFAULT)) {

                    List<Recipe> recipesByHashtagsContains = recipeDAO.findRecipesByHashtagsContains(inputText);

                    try {
                        sendRecipesList(update, recipesByHashtagsContains);
                    } catch (TelegramApiException e) {
                        log.error(e.getMessage());
                        throw new RuntimeException(e);
                    }
                } else if (botStateContext.getCurrentBotState().equals(BotState.ADDING_RECIPE)) {

                    Recipe recipe = null;
                    try {
                        recipe = RecipeParser.parseRecipeFromString(inputText);
                    } catch (ParseException e) {
                        sendMessage.setText("Не удалось распарсить рецепт");
                        log.error(e.getMessage());
                        throw new RuntimeException(e);
                    }
                    if (checkIsRecipeAlreadyExists(recipe)) {
                        sendMessage.setText("Такой рецепт уже есть в базе");
                    } else {
                        Recipe savedRecipe = recipeDAO.saveRecipe(recipe);
                        sendMessage.setText("Рецепт добавлен: \n" + savedRecipe.toString());
                    }
                } else if (botStateContext.getCurrentBotState().equals(BotState.WAITING_FOR_EDITED_RECIPE)) {

                    Recipe recipe = null;
                    try {
                        recipe = RecipeParser.parseRecipeFromString(inputText);
                        Recipe recipeByName = recipeDAO.findRecipeByName(recipe.getName()).get(0);
                        recipeByName.setDescription(recipe.getDescription());
                        recipeByName.setIngredients(recipe.getIngredients());
                        recipeByName.setInstructions(recipe.getInstructions());
                        recipeByName.setHashtags(recipe.getHashtags());
                        recipeDAO.saveRecipe(recipeByName);
                        sendMessage.setText("Рецепт изменен: \n" + recipeByName.toString());
                    } catch (ParseException e) {
                        sendMessage.setText("Не удалось распарсить рецепт");
                        log.error(e.getMessage());
                        throw new RuntimeException(e);
                    }

                    botStateContext.setCurrentBotState(BotState.DEFAULT);
                    botStateContextDAO.saveBotStateContext(botStateContext);
                }
            }

        } else if (message.hasPhoto()) {

            if (botStateContext.getCurrentBotState().equals(BotState.DEFAULT)) {
                sendMessage.setText("Отправьте ключевое слово или ингредиент для поиска рецептов");
            } else if (botStateContext.getCurrentBotState().equals(BotState.ADDING_RECIPE)) {
                List<PhotoSize> photos = update.getMessage().getPhoto();

                String f_id = photos.stream().max(Comparator.comparing(PhotoSize::getFileSize))
                        .map(PhotoSize::getFileId)
                        .orElse("");

                Recipe recipe = null;
                try {
                    recipe = RecipeParser.parseRecipeFromString(update.getMessage().getCaption());
                } catch (ParseException e) {
                    sendMessage.setText("Не удалось распарсить рецепт");
                    log.error(e.getMessage());
                    throw new RuntimeException(e);
                }
                recipe.setPhoto(f_id);

                if (checkIsRecipeAlreadyExists(recipe)) {
                    sendMessage.setText("Такой рецепт уже есть в базе");

                } else {
                    Recipe savedRecipe = recipeDAO.saveRecipe(recipe);
                    sendMessage.setText("Рецепт добавлен");
                    SendPhoto sendPhoto = SendPhoto.builder().chatId(chatId).photo(new InputFile(f_id)).caption(savedRecipe.toString()).build();
                    try {
                        telegramClient.execute(sendPhoto);
                    } catch (TelegramApiException e) {
                        log.error(e.getMessage());
                        throw new RuntimeException(e);
                    }
                }
            }

        }

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

    private @NotNull Runnable getAddRecipeAction(long chatId, BotStateContext botStateContext) {
        return () -> {
            SendMessage sendMessage = SendMessage.builder().chatId(chatId).text(BotMessageEnum.INSERT_RECIPE_MESSAGE.getMessage()).build();
            try {
                telegramClient.execute(sendMessage);
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
                throw new RuntimeException(e);
            }
            botStateContext.setCurrentBotState(BotState.ADDING_RECIPE);
            botStateContextDAO.saveBotStateContext(botStateContext);
        };
    }

    private @NotNull Runnable getFindRandomRecipeAction(BotStateContext botStateContext, Update update) {
        return () -> {
            botStateContext.setCurrentBotState(BotState.DEFAULT);
            botStateContextDAO.saveBotStateContext(botStateContext);

            Recipe randomRecipe = recipeDAO.getRandomRecipe();
            if (randomRecipe != null) {
                try {
                    sendRecipesList(update, List.of(randomRecipe));
                } catch (TelegramApiException e) {
                    log.error(e.getMessage());
                    throw new RuntimeException(e);
                }
            } else {
                SendMessage sendMessage = SendMessage.builder().chatId(update.getMessage().getChatId()).text(BotMessageEnum.RECIPE_NOT_FOUND.getMessage()).build();
                try {
                    telegramClient.execute(sendMessage);
                } catch (TelegramApiException e) {
                    log.error(e.getMessage());
                    throw new RuntimeException(e);
                }
            }
        };
    }

    private @NotNull Runnable getHelpAction(long chatId, User user) {
        return () -> {
            try {
                telegramClient.execute(SendMessage.builder().chatId(chatId).text(BotMessageEnum.HELP_MESSAGE.getMessage()).replyMarkup(replyKeyboardMaker.getMainMenuKeyboard(user)).build());
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
                throw new RuntimeException(e);
            }
            SendMessage.builder().chatId(chatId).text(BotMessageEnum.HELP_MESSAGE.getMessage()).replyMarkup(replyKeyboardMaker.getMainMenuKeyboard(user)).build();
        };
    }

    private @NotNull Runnable getStartAction(BotStateContext botStateContext, long chatId, User user) {
        return () -> {
            botStateContext.setCurrentBotState(BotState.DEFAULT);
            botStateContextDAO.saveBotStateContext(botStateContext);

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
        if (recipeDAO.findRecipeByName(recipeName) != null && recipeDAO.findRecipeByName(recipeName).size() > 0) {
            return true;
        }
        return false;
    }

    private void sendRecipesList(Update update, List<Recipe> recipeList) throws TelegramApiException {
        Long userId = update.getMessage().getFrom().getId();
        for (Recipe recipe : recipeList) {
            if (recipe.getPhoto() == null) {
                SendMessage sendMessage = SendMessage.builder().chatId(update.getMessage().getChatId()).text(recipe.toString()).build();
                if (userDAO.findById(userId).get().getIsAdmin()==true) {
                    sendMessage.setReplyMarkup(inlineKeyboardMaker.getRecipeAdminKeyboard(recipe.toString()));
                }
                telegramClient.execute(sendMessage);
            } else {
                SendPhoto sendPhoto = SendPhoto.builder().chatId(update.getMessage().getChatId())
                        .caption(recipe.toString()).photo(new InputFile(recipe.getPhoto())).build();
                if (userDAO.findById(userId).get().getIsAdmin()==true) {
                    sendPhoto.setReplyMarkup(inlineKeyboardMaker.getRecipeAdminKeyboard(recipe.toString()));
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
                sendMessage.setReplyMarkup(inlineKeyboardMaker.getUserAdminKeyboard());
            } else {
                sendMessage.setReplyMarkup(inlineKeyboardMaker.getUserKeyboard());
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
