package org.example.cooking_recipe_bot.bot;

import lombok.extern.slf4j.Slf4j;
import org.example.cooking_recipe_bot.bot.constants.BotMessageEnum;
import org.example.cooking_recipe_bot.bot.constants.ButtonNameEnum;
import org.example.cooking_recipe_bot.bot.keyboards.InlineKeyboardMaker;
import org.example.cooking_recipe_bot.bot.keyboards.ReplyKeyboardMaker;
import org.example.cooking_recipe_bot.db.dao.BotStateContextDAO;
import org.example.cooking_recipe_bot.db.dao.RecipeDAO;
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
import org.telegram.telegrambots.meta.api.objects.Update;
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
    private final RecipeDAO recipeDAO;
    private final ReplyKeyboardMaker replyKeyboardMaker;
    private final InlineKeyboardMaker inlineKeyboardMaker;

    public ActionFactory(BotStateContextDAO botStateContextDAO, TelegramClient telegramClient, UserDAO userDAO, RecipeDAO recipeDAO, ReplyKeyboardMaker replyKeyboardMaker, InlineKeyboardMaker inlineKeyboardMaker) {
        this.botStateContextDAO = botStateContextDAO;
        this.telegramClient = telegramClient;
        this.userDAO = userDAO;
        this.recipeDAO = recipeDAO;
        this.replyKeyboardMaker = replyKeyboardMaker;
        this.inlineKeyboardMaker = inlineKeyboardMaker;

    }

    public Map<String, Runnable> createButtonActions(Update update, User user) {

        buttonActions.put("/start", getStartAction(update, user));
        buttonActions.put("/cancel", getCancelAction(update, user));
        buttonActions.put(ButtonNameEnum.HELP_BUTTON.getButtonName().toLowerCase(), getHelpAction(update, user));
        buttonActions.put(ButtonNameEnum.FIND_RANDOM_RECIPE_BUTTON.getButtonName().toLowerCase(), getFindRandomRecipeAction(update));
        buttonActions.put(ButtonNameEnum.ADD_RECIPE_BUTTON.getButtonName().toLowerCase(), getAddRecipeAction(update));
        buttonActions.put(ButtonNameEnum.USERS_BUTTON.getButtonName().toLowerCase(), getGetUsersAction(update));
        buttonActions.put(ButtonNameEnum.SEND_NOTIFICATION.getButtonName().toLowerCase(), getSendNotificationAction(update));
        return buttonActions;
    }

    private Runnable getCancelAction(Update update, User user) {
        return () -> {
            botStateContextDAO.changeBotState(user.getId(), BotState.DEFAULT);
            long chatId = update.getMessage().getChatId();
            try {
                telegramClient.execute(SendMessage.builder().chatId(chatId).text(BotMessageEnum.CANCEL_MESSAGE.getMessage()).replyMarkup(replyKeyboardMaker.getMainMenuKeyboard(user)).build());
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
                log.error(Arrays.toString(e.getStackTrace()));
            }
        };
    }

    private @NotNull Runnable getSendNotificationAction(Update update) {
        return () -> {
            if (checkUserIsNotAdmin(update)) return;

            Long chatId = update.getMessage().getChatId();
            SendMessage sendMessage = SendMessage.builder().chatId(chatId).text(BotMessageEnum.INSERT_NOTIFICATION_MESSAGE.getMessage()).build();
            try {
                telegramClient.execute(sendMessage);
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
                log.error(Arrays.toString(e.getStackTrace()));
            }

            botStateContextDAO.changeBotState(update.getMessage().getFrom().getId(), BotState.WAITING_FOR_NOTIFICATION);
        };
    }

    private boolean checkUserIsNotAdmin(Update update) {
        User user = userDAO.findById(update.getMessage().getFrom().getId()).orElse(null);
        return user == null || !user.getIsAdmin();
    }

    private @NotNull Runnable getGetUsersAction(Update update) {
        return () -> {
            if (checkUserIsNotAdmin(update)) return;

            try {
                sendUsersList(update);
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
                log.error(Arrays.toString(e.getStackTrace()));
            }
        };
    }

    private @NotNull Runnable getAddRecipeAction(Update update) {
        return () -> {
            if (checkUserIsNotAdmin(update)) return;

            Long chatId = update.getMessage().getChatId();
            SendMessage sendMessage = SendMessage.builder().chatId(chatId).text(BotMessageEnum.INSERT_RECIPE_MESSAGE.getMessage()).build();
            try {
                telegramClient.execute(sendMessage);
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
                log.error(Arrays.toString(e.getStackTrace()));
            }

            botStateContextDAO.changeBotState(update.getMessage().getFrom().getId(), BotState.ADDING_RECIPE);
        };
    }

    private @NotNull Runnable getFindRandomRecipeAction(Update update) {
        return () -> {

            botStateContextDAO.changeBotState(update.getMessage().getFrom().getId(), BotState.DEFAULT);
            Recipe randomRecipe = recipeDAO.getRandomRecipe();
            if (randomRecipe != null) {
                try {
                    Long userId = update.getMessage().getFrom().getId();
                    Long chatId = update.getMessage().getChatId();
                    sendRecipesList(userId, chatId, List.of(randomRecipe));
                } catch (TelegramApiException e) {
                    log.error(e.getMessage());
                    log.error(Arrays.toString(e.getStackTrace()));
                }
            } else {
                SendMessage sendMessage = SendMessage.builder().chatId(update.getMessage().getChatId()).text(BotMessageEnum.RECIPE_NOT_FOUND.getMessage()).build();
                try {
                    telegramClient.execute(sendMessage);
                } catch (TelegramApiException e) {
                    log.error(e.getMessage());
                    log.error(Arrays.toString(e.getStackTrace()));
                }
            }
        };
    }

    private @NotNull Runnable getHelpAction(Update update, User user) {
        return () -> {
            long chatId = update.getMessage().getChatId();

            try {
                telegramClient.execute(SendMessage.builder().chatId(chatId).text(BotMessageEnum.HELP_MESSAGE.getMessage()).parseMode(ParseMode.HTML).replyMarkup(replyKeyboardMaker.getMainMenuKeyboard(user)).build());
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
                log.error(Arrays.toString(e.getStackTrace()));
            }
            SendMessage.builder().chatId(chatId).text(BotMessageEnum.HELP_MESSAGE.getMessage()).replyMarkup(replyKeyboardMaker.getMainMenuKeyboard(user)).build();
        };
    }

    private @NotNull Runnable getStartAction(Update update, User user) {
        return () -> {
            botStateContextDAO.changeBotState(user.getId(), BotState.DEFAULT);
            long chatId = update.getMessage().getChatId();
            user.setIsUnsubscribed(false);
            userDAO.saveUser(user);
            String userName;
            if (user.getUserName() != null) {
                userName = user.getUserName();
            } else {
                userName = user.getFirstName() != null ? user.getFirstName() : "друг";
            }
            try {
                telegramClient.execute(SendMessage.builder().chatId(chatId).text("Привет, " + userName + "\n\n" + BotMessageEnum.HELP_MESSAGE.getMessage())
                        .parseMode(ParseMode.HTML).replyMarkup(replyKeyboardMaker.getMainMenuKeyboard(user)).build());
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
                log.error(Arrays.toString(e.getStackTrace()));
            }
        };
    }

    private void sendUsersList(Update update) throws TelegramApiException {
        List<User> allUsers = userDAO.findAllUsers();

        for (User user : allUsers) {
            if (user.getUserName() != null && (userDAO.isFirstAdmin(user.getUserName()) || user.getId().equals(update.getMessage().getFrom().getId()))) {
                continue;
            }
            SendMessage sendMessage = SendMessage.builder().chatId(update.getMessage().getChatId()).text(user.toString()).build();

            sendMessage.setReplyMarkup(inlineKeyboardMaker.getUserKeyboard(user.getId(), user.getIsAdmin()));

            telegramClient.execute(sendMessage);
        }

    }

    public void sendRecipesList(Long userId, Long chatId, List<Recipe> recipes) throws TelegramApiException {
        User user = userDAO.findById(userId).orElseThrow(() -> new TelegramApiException(BotMessageEnum.USER_NOT_FOUND.getMessage() + userId));

        if (recipes.isEmpty()) {
            sendTextMessage(chatId, BotMessageEnum.RECIPE_NOT_FOUND.getMessage());
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
                .text("Продолжить показывать рецепты? \n" +
                        "(или отправьте новый запрос)")
                .replyMarkup(inlineKeyboardMaker.getMoreRecipesKeyboard()).build();
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
        InlineKeyboardMarkup recipeKeyboard = inlineKeyboardMaker.getRecipeKeyboard(recipe, 0, user.getIsAdmin());

        recipeKeyboard = getRecipeKeyboardWithRateButton(recipe, user, recipeKeyboard);
        Double rating = recipe.getRating() == null ? 0 : recipe.getRating();

        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text("<b>" + recipe.getName().toUpperCase() + "</b> \nРейтинг: " + String.format("%.2f", rating)).parseMode(ParseMode.HTML)
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

        if (checkUserIsNotVote(user, recipe) && recipeKeyboard.getKeyboard().get(0).get(0).getText().equals("закрыть")) {
            recipeKeyboard = inlineKeyboardMaker.addRateButtonKeyboard(recipeKeyboard, recipe);
        }
        return recipeKeyboard;
    }

    private boolean checkUserIsNotVote(User user, Recipe recipe) {
        Long userId = user.getId();
        List<Long> votedUsersIds = recipe.getVotedUsersIds();
        return votedUsersIds == null || !votedUsersIds.contains(userId);
    }
}
