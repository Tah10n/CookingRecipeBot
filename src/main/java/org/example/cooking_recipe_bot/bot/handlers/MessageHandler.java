package org.example.cooking_recipe_bot.bot.handlers;

import lombok.extern.slf4j.Slf4j;
import org.example.cooking_recipe_bot.bot.BotState;
import org.example.cooking_recipe_bot.bot.BotStateHandler;
import org.example.cooking_recipe_bot.bot.keyboards.InlineKeyboardMaker;
import org.example.cooking_recipe_bot.bot.keyboards.ReplyKeyboardMaker;
import org.example.cooking_recipe_bot.utils.constants.BotMessageEnum;
import org.example.cooking_recipe_bot.utils.constants.ButtonNameEnum;
import org.example.cooking_recipe_bot.db.dao.RecipeDAO;
import org.example.cooking_recipe_bot.db.dao.UserDAO;
import org.example.cooking_recipe_bot.db.entity.Recipe;
import org.example.cooking_recipe_bot.db.entity.User;
import org.example.cooking_recipe_bot.utils.RecipeParser;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Component

public class MessageHandler implements UpdateHandler {
    private final RecipeDAO recipeDAO;
    ReplyKeyboardMaker replyKeyboardMaker;
    InlineKeyboardMaker inlineKeyboardMaker;
    UserDAO userDAO;
    TelegramClient telegramClient;
    BotStateHandler botStateHandler;


    public MessageHandler(ReplyKeyboardMaker replyKeyboardMaker, InlineKeyboardMaker inlineKeyboardMaker, UserDAO userDAO, TelegramClient telegramClient, BotStateHandler botStateHandler, RecipeDAO recipeDAO) {
        this.replyKeyboardMaker = replyKeyboardMaker;
        this.inlineKeyboardMaker = inlineKeyboardMaker;
        this.userDAO = userDAO;
        this.telegramClient = telegramClient;
        this.botStateHandler = botStateHandler;
        this.recipeDAO = recipeDAO;
    }

    @Override
    public SendMessage handle(Update update) throws TelegramApiException {

        User user = getUserFromUpdate(update);

        Message message = update.getMessage();
        long chatId = message.getChatId();

        SendMessage sendMessage = SendMessage.builder().chatId(chatId).text("").build();

        sendMessage.enableMarkdown(true);
        sendMessage.setReplyMarkup(replyKeyboardMaker.getMainMenuKeyboard(user));

        if (message.hasText()) {
            String inputText = message.getText().toLowerCase();
            if (inputText.equals("/start")) {

                botStateHandler.resetCurrentBotState();
                sendMessage.setText("Привет, " + user.getUserName() + "\n\n" + BotMessageEnum.HELP_MESSAGE.getMessage());

            } else if (inputText.equals(ButtonNameEnum.HELP_BUTTON.getButtonName().toLowerCase())) {

                sendMessage.setText(BotMessageEnum.HELP_MESSAGE.getMessage());

            } else if (inputText.equals(ButtonNameEnum.FIND_RANDOM_RECIPE_BUTTON.getButtonName().toLowerCase())) {
                //TODO добавить поиск случайного рецептов
                botStateHandler.changeCurrentBotState(BotState.DEFAULT);
                Recipe randomRecipe = recipeDAO.getRandomRecipe();
                if(randomRecipe != null){
                    if(randomRecipe.getPhoto() != null){
                        SendPhoto sendPhoto = SendPhoto.builder().chatId(chatId)
                                .photo(new InputFile(randomRecipe.getPhoto()))
                                .caption(randomRecipe.toString()).build();

                        telegramClient.execute(sendPhoto);
                    } else {
                        sendMessage.setText(randomRecipe.toString());
                    }
                } else {
                    sendMessage.setText(BotMessageEnum.RECIPE_NOT_FOUND.getMessage());
                }

            } else if (inputText.equals(ButtonNameEnum.ADD_RECIPE_BUTTON.getButtonName().toLowerCase())) {
                //TODO добавить добавление рецептов
                sendMessage.setText(BotMessageEnum.INSERT_RECIPE_MESSAGE.getMessage());
                botStateHandler.changeCurrentBotState(BotState.ADDING_RECIPE);

            } else if (inputText.equals(ButtonNameEnum.BREAKFAST_BUTTON.getButtonName().toLowerCase())) {
                //TODO find breakfast recipes
                botStateHandler.changeCurrentBotState(BotState.DEFAULT);

            } else if (inputText.equals(ButtonNameEnum.LUNCH_BUTTON.getButtonName().toLowerCase())) {
                //TODO find lunch recipes
                botStateHandler.changeCurrentBotState(BotState.DEFAULT);

            } else if (inputText.equals(ButtonNameEnum.DINNER_BUTTON.getButtonName().toLowerCase())) {
                //TODO find dinner recipes
                botStateHandler.changeCurrentBotState(BotState.DEFAULT);

            } else if (inputText.equals(ButtonNameEnum.USERS_BUTTON.getButtonName().toLowerCase())) {
                sendUsersList(update);

            } else {

                if (botStateHandler.getCurrentBotState().equals(BotState.DEFAULT)) {
                    //TODO find on inputText

                    List<Recipe> recipesByHashtagsContains = recipeDAO.findRecipesByHashtagsContains(inputText);

                    sendRecipesList(update, recipesByHashtagsContains);
                } else if (botStateHandler.getCurrentBotState().equals(BotState.ADDING_RECIPE)) {

                    Recipe recipe = RecipeParser.parseRecipeFromString(inputText);
                    if (checkIsRecipeAlreadyExists(recipe)) {
                        sendMessage.setText("Такой рецепт уже есть в базе");
                    } else {
                        Recipe savedRecipe = recipeDAO.saveRecipe(recipe);
                        sendMessage.setText("Рецепт добавлен: \n" + savedRecipe.toString());
                    }


                }
            }
        } else if (message.hasPhoto()) {

            if (botStateHandler.getCurrentBotState().equals(BotState.DEFAULT)) {
                sendMessage.setText("Отправьте ключевое слово или ингредиент для поиска рецептов");
            } else if (botStateHandler.getCurrentBotState().equals(BotState.ADDING_RECIPE)) {
                List<PhotoSize> photos = update.getMessage().getPhoto();

                String f_id = photos.stream().max(Comparator.comparing(PhotoSize::getFileSize))
                        .map(PhotoSize::getFileId)
                        .orElse("");

                Recipe recipe = RecipeParser.parseRecipeFromString(update.getMessage().getCaption());
                recipe.setPhoto(f_id);

                if (checkIsRecipeAlreadyExists(recipe)) {
                    sendMessage.setText("Такой рецепт уже есть в базе");
                    Recipe savedRecipe = recipeDAO.saveRecipe(recipe);
                    SendPhoto sendPhoto = SendPhoto.builder().chatId(chatId).photo(new InputFile(f_id)).caption(savedRecipe.toString()).build();
                    telegramClient.execute(sendPhoto);
                } else {
                    Recipe savedRecipe = recipeDAO.saveRecipe(recipe);
                    sendMessage.setText("Рецепт добавлен");
                    SendPhoto sendPhoto = SendPhoto.builder().chatId(chatId).photo(new InputFile(f_id)).caption(savedRecipe.toString()).build();
                    telegramClient.execute(sendPhoto);
                }
            }

        }


        return sendMessage;
    }

    private boolean checkIsRecipeAlreadyExists(Recipe recipe) {
        String recipeName = recipe.getName();
        if (recipeDAO.findRecipeByName(recipeName) != null) {
            return true;
        }
        return false;
    }

    private void sendRecipesList(Update update, List<Recipe> recipesByHashtagsContains) throws TelegramApiException {

        for (Recipe recipe : recipesByHashtagsContains) {
            SendMessage sendMessage = SendMessage.builder().chatId(update.getMessage().getChatId()).text(recipe.toString()).build();
            telegramClient.execute(sendMessage);
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
