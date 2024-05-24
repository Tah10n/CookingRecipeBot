package org.example.cooking_recipe_bot.bot;

import lombok.extern.slf4j.Slf4j;
import org.example.cooking_recipe_bot.bot.constants.BotMessageEnum;
import org.example.cooking_recipe_bot.bot.constants.ButtonNameEnum;
import org.example.cooking_recipe_bot.bot.keyboards.InlineKeyboardMaker;
import org.example.cooking_recipe_bot.bot.keyboards.ReplyKeyboardMaker;
import org.example.cooking_recipe_bot.db.dao.BotStateContextDAO;
import org.example.cooking_recipe_bot.db.dao.RecipeDAOManager;
import org.example.cooking_recipe_bot.db.dao.UserDAO;
import org.example.cooking_recipe_bot.db.entity.BotStateContext;
import org.example.cooking_recipe_bot.db.entity.Recipe;
import org.example.cooking_recipe_bot.db.entity.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ActionFactory {
    private final Map<String, Runnable> buttonActions = new HashMap<>();
    private final BotStateContextDAO botStateContextDAO;
    private final TelegramClient telegramClient;
    private final UserDAO userDAO;
    private final RecipeDAOManager recipeDAOManager;
    private final ReplyKeyboardMaker replyKeyboardMaker;
    private final InlineKeyboardMaker inlineKeyboardMaker;
    private final MessageTranslator messageTranslator;

    public ActionFactory(BotStateContextDAO botStateContextDAO, TelegramClient telegramClient, UserDAO userDAO, RecipeDAOManager recipeDAOManager, ReplyKeyboardMaker replyKeyboardMaker, InlineKeyboardMaker inlineKeyboardMaker, MessageTranslator messageTranslator) {
        this.botStateContextDAO = botStateContextDAO;
        this.telegramClient = telegramClient;
        this.userDAO = userDAO;
        this.recipeDAOManager = recipeDAOManager;
        this.replyKeyboardMaker = replyKeyboardMaker;
        this.inlineKeyboardMaker = inlineKeyboardMaker;
        this.messageTranslator = messageTranslator;
    }

    public Map<String, Runnable> createButtonActions(User user, long chatId) {

        String findRandomRecipeButton = messageTranslator.getMessage(ButtonNameEnum.FIND_RANDOM_RECIPE_BUTTON.name(), user.getLanguage()).toLowerCase();
        String addRecipeButton = messageTranslator.getMessage(ButtonNameEnum.ADD_RECIPE_BUTTON.name(), user.getLanguage()).toLowerCase();
        String usersButton = messageTranslator.getMessage(ButtonNameEnum.USERS_BUTTON.name(), user.getLanguage()).toLowerCase();
        String sendNotificationButton = messageTranslator.getMessage(ButtonNameEnum.SEND_NOTIFICATION.name(), user.getLanguage()).toLowerCase();

        buttonActions.put("/start", getStartAction(user, chatId));
        buttonActions.put("/cancel", getCancelAction(user, chatId));
        buttonActions.put("/language", getChangeLanguageAction(user, chatId));
        buttonActions.put("/help", getHelpAction(user, chatId));
        buttonActions.put(findRandomRecipeButton, getFindRandomRecipeAction(user, chatId));
        buttonActions.put(addRecipeButton, getAddRecipeAction(user, chatId));
        buttonActions.put(usersButton, getGetUsersAction(user, chatId));
        buttonActions.put(sendNotificationButton, getSendNotificationAction(user, chatId));
        return buttonActions;
    }

    private Runnable getChangeLanguageAction(User user, long chatId) {
        return () -> {
            botStateContextDAO.changeBotState(user.getId(), BotState.DEFAULT);
            SendMessage sendMessage = SendMessage.builder().chatId(chatId)
                    .text(messageTranslator.getMessage(BotMessageEnum.AVAILABLE_LANGUAGES_MESSAGE.name(), user.getLanguage())).build();
            sendMessage.setReplyMarkup(inlineKeyboardMaker.getLanguagesKeyboard(user));
            try {
                telegramClient.execute(sendMessage);
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
                log.error(Arrays.toString(e.getStackTrace()));
            }
        };
    }

    private Runnable getCancelAction(User user, long chatId) {
        return () -> {

            botStateContextDAO.changeBotState(user.getId(), BotState.DEFAULT);
            try {
                telegramClient.execute(SendMessage.builder().chatId(chatId)
                        .text(messageTranslator.getMessage(BotMessageEnum.CANCEL_MESSAGE.name(), user.getLanguage()))
                        .replyMarkup(replyKeyboardMaker.getMainMenuKeyboard(user)).build());
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
                log.error(Arrays.toString(e.getStackTrace()));
            }
        };
    }

    private @NotNull Runnable getSendNotificationAction(User user, long chatId) {
        return () -> {
            if (userIsNotAdmin(user)) return;
            SendMessage sendMessage = SendMessage.builder().chatId(chatId).text(messageTranslator.getMessage(BotMessageEnum.INSERT_NOTIFICATION_MESSAGE.name(), user.getLanguage())).build();
            try {
                telegramClient.execute(sendMessage);
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
                log.error(Arrays.toString(e.getStackTrace()));
            }

            botStateContextDAO.changeBotState(user.getId(), BotState.WAITING_FOR_NOTIFICATION);
        };
    }

    private boolean userIsNotAdmin(User user) {
        return user == null || !user.getIsAdmin();
    }

    private @NotNull Runnable getGetUsersAction(User user, long chatId) {
        return () -> {
            if (userIsNotAdmin(user)) return;
            List<User> userList = userDAO.findAllUsers();

            if (userList == null || userList.isEmpty()) {
                SendMessage sendMessage = SendMessage.builder().chatId(chatId)
                        .text(messageTranslator.getMessage(BotMessageEnum.USER_NOT_FOUND.name(), user.getLanguage()))
                        .build();
                try {
                    telegramClient.execute(sendMessage);
                } catch (TelegramApiException e) {
                    log.error(e.getMessage());
                    log.error(Arrays.toString(e.getStackTrace()));
                }
                return;
            }

            sendUsersList(user, chatId, userList);
        };
    }

    private @NotNull Runnable getAddRecipeAction(User user, long chatId) {
        return () -> {
            if (userIsNotAdmin(user)) return;

            SendMessage sendMessage = SendMessage.builder().chatId(chatId)
                    .text(messageTranslator.getMessage(BotMessageEnum.INSERT_RECIPE_MESSAGE.name(), user.getLanguage()))
                    .replyMarkup(inlineKeyboardMaker.getCancelKeyboard(user))
                    .build();
            try {
                telegramClient.execute(sendMessage);
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
                log.error(Arrays.toString(e.getStackTrace()));
            }

            botStateContextDAO.changeBotState(user.getId(), BotState.ADDING_RECIPE);
        };
    }

    private @NotNull Runnable getFindRandomRecipeAction(User user, long chatId) {
        return () -> {

            botStateContextDAO.changeBotState(user.getId(), BotState.DEFAULT);
            Recipe randomRecipe = recipeDAOManager.getRecipeDAO(user.getLanguage()).getRandomRecipe();
            if (randomRecipe != null) {
                sendRecipesList(user.getId(), chatId, List.of(randomRecipe));
            } else {
                SendMessage sendMessage = SendMessage.builder().chatId(chatId)
                        .text(messageTranslator.getMessage(BotMessageEnum.RECIPE_NOT_FOUND_MESSAGE.name(), user.getLanguage())).build();
                try {
                    telegramClient.execute(sendMessage);
                } catch (TelegramApiException e) {
                    log.error(e.getMessage());
                    log.error(Arrays.toString(e.getStackTrace()));
                }
            }
        };
    }


    private @NotNull Runnable getHelpAction(User user, long chatId) {
        return () -> {
            botStateContextDAO.changeBotState(user.getId(), BotState.DEFAULT);
            String helpMessage = messageTranslator.getMessage(BotMessageEnum.HELP_MESSAGE.name(), user.getLanguage());
            try {
                telegramClient.execute(SendMessage.builder().chatId(chatId)
                        .text(helpMessage)
                        .parseMode(ParseMode.HTML).replyMarkup(replyKeyboardMaker.getMainMenuKeyboard(user)).build());
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
                log.error(Arrays.toString(e.getStackTrace()));
            }
        };
    }

    private @NotNull Runnable getStartAction(User user, long chatId) {
        return () -> {
            botStateContextDAO.changeBotState(user.getId(), BotState.DEFAULT);
            user.setIsUnsubscribed(false);
            userDAO.saveUser(user);
            String userName;
            if (user.getUserName() != null) {
                userName = user.getUserName();
            } else {
                userName = user.getFirstName() != null ? user.getFirstName() : messageTranslator.getMessage(BotMessageEnum.UNKNOWN_USER.name(), user.getLanguage());
            }
            String helloMessage = String.format(messageTranslator.getMessage(BotMessageEnum.HELLO_MESSAGE.name(), user.getLanguage()), userName);
            try {
                telegramClient.execute(SendMessage.builder().chatId(chatId)
                        .text(helloMessage)
                        .parseMode(ParseMode.HTML).replyMarkup(replyKeyboardMaker.getMainMenuKeyboard(user)).build());
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
                log.error(Arrays.toString(e.getStackTrace()));
            }
        };
    }

    public void sendUsersList(User requester, long chatId, List<User> userList) {
        if(userList.size() > 5) {
            for (int i = 0; i < 5; i++) {
                if (userList.get(i).getUserName() != null && (userDAO.isFirstAdmin(userList.get(i).getUserName()) || userList.get(i).getId().equals(requester.getId()))) {
                    continue;
                }

                sendUser(requester,chatId,userList.get(i));
            }
            sendMoreUsersButton(requester,chatId, userList.subList(5, userList.size()));
        } else {
            for (User user : userList) {
                if (user.getUserName() != null && (userDAO.isFirstAdmin(user.getUserName()) || user.getId().equals(requester.getId()))) {
                    continue;
                }
                sendUser(requester,chatId,user);
            }
        }

    }

    private void sendMoreUsersButton(User requester, long chatId, List<User> users) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(messageTranslator.getMessage(BotMessageEnum.SEND_MORE_USERS_QUESTION_MESSAGE.name(), requester.getLanguage()))
                .replyMarkup(inlineKeyboardMaker.getMoreUsersKeyboard(requester)).build();
        try {
            telegramClient.execute(sendMessage);
            BotStateContext botStateContext = botStateContextDAO.findBotStateContextById(requester.getId());
            botStateContext.setUserList(users);
            botStateContextDAO.saveBotStateContext(botStateContext);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            log.error(Arrays.toString(e.getStackTrace()));
        }
    }

    private void sendUser(User requester, long chatId, User user) {
        SendMessage sendMessage = SendMessage.builder().chatId(chatId).text(user.toString())
                .replyMarkup(inlineKeyboardMaker.getUserKeyboard(requester, user)).build();
        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            log.error(Arrays.toString(e.getStackTrace()));
        }
    }

    public void sendRecipesList(Long userId, Long chatId, List<Recipe> recipes) {
        User user = userDAO.getUserById(userId);

        if (recipes.isEmpty()) {
            sendTextMessage(chatId, messageTranslator.getMessage(BotMessageEnum.RECIPE_NOT_FOUND_MESSAGE.name(), user.getLanguage()));
            return;
        }

        if (recipes.size() > 4) {
            for (int i = 0; i < 4; i++) {
                sendRecipe(chatId, recipes.get(i), user);
            }
            sendMoreRecipesButton(chatId, user, recipes.subList(4, recipes.size()));
        } else {
            for (Recipe recipe : recipes) {
                sendRecipe(chatId, recipe, user);
            }
        }

    }

    private void sendMoreRecipesButton(Long chatId, User user, List<Recipe> recipes) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(messageTranslator.getMessage(BotMessageEnum.SEND_MORE_RECIPES_QUESTION_MESSAGE.name(), user.getLanguage()))
                .replyMarkup(inlineKeyboardMaker.getMoreRecipesKeyboard(user)).build();
        try {
            telegramClient.execute(sendMessage);

            BotStateContext botStateContext = botStateContextDAO.findBotStateContextById(user.getId());
            botStateContext.setRecipeList(recipes);
            botStateContextDAO.saveBotStateContext(botStateContext);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            log.error(Arrays.toString(e.getStackTrace()));
        }

    }

    public void sendTextMessage(Long chatId, String message) {
        SendMessage sendMessage = SendMessage.builder().chatId(chatId).text(message).build();
        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            log.error(Arrays.toString(e.getStackTrace()));
        }
    }

    private void sendRecipe(Long chatId, Recipe recipe, User user) {
        if (recipe == null) {
            return;
        }
        if (recipe.getPhotoId() != null && !recipe.getPhotoId().isEmpty()) {
            SendPhoto sendPhoto = SendPhoto.builder()
                    .chatId(chatId)
                    .photo(new InputFile(recipe.getPhotoId()))
                    .build();
            try {
                telegramClient.execute(sendPhoto);
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
                log.error(Arrays.toString(e.getStackTrace()));
            }
        }
        sendTextRecipe(chatId, recipe, user);

    }

    private void sendTextRecipe(Long chatId, Recipe recipe, User user) {
        InlineKeyboardMarkup recipeKeyboard = inlineKeyboardMaker.getRecipeKeyboard(recipe, 0, user);

        recipeKeyboard = getRecipeKeyboardWithRateButton(recipe, user, recipeKeyboard);
        Double rating = recipe.getRating() == null ? 0 : recipe.getRating();
        String message = String.format(messageTranslator.getMessage(BotMessageEnum.RECIPE_NAME_AND_RATING_MESSAGE.name(), user.getLanguage()), recipe.getName().toUpperCase(), rating);
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(message).parseMode(ParseMode.HTML)
                .build();
        sendMessage.setReplyMarkup(recipeKeyboard);
        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            log.error(Arrays.toString(e.getStackTrace()));
        }
    }

    public InlineKeyboardMarkup getRecipeKeyboardWithRateButton(Recipe recipe, User user, InlineKeyboardMarkup recipeKeyboard) {

        if (checkUserIsNotVote(user, recipe) && recipeKeyboard.getKeyboard().get(0).get(0).getText().equals(messageTranslator.getMessage(ButtonNameEnum.CLOSE.name(), user.getLanguage()))) {
            recipeKeyboard = inlineKeyboardMaker.addRateButtonKeyboard(recipeKeyboard, recipe, user);
        }
        return recipeKeyboard;
    }

    private boolean checkUserIsNotVote(User user, Recipe recipe) {
        Long userId = user.getId();
        List<Long> votedUsersIds = recipe.getVotedUsersIds();
        return votedUsersIds == null || !votedUsersIds.contains(userId);
    }
}
