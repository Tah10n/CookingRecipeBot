package org.example.cooking_recipe_bot.bot;

import lombok.extern.slf4j.Slf4j;
import org.example.cooking_recipe_bot.bot.constants.BotMessageEnum;
import org.example.cooking_recipe_bot.bot.constants.ButtonNameEnum;
import org.example.cooking_recipe_bot.bot.keyboards.InlineKeyboardMaker;
import org.example.cooking_recipe_bot.bot.keyboards.ReplyKeyboardMaker;
import org.example.cooking_recipe_bot.db.dao.BotStateContextDAO;
import org.example.cooking_recipe_bot.db.dao.RecipeDAO;
import org.example.cooking_recipe_bot.db.dao.UserDAO;
import org.example.cooking_recipe_bot.db.entity.Recipe;
import org.example.cooking_recipe_bot.db.entity.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
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
    BotStateContextDAO botStateContextDAO;
    TelegramClient telegramClient;
    UserDAO userDAO;
    RecipeDAO recipeDAO;
    ReplyKeyboardMaker replyKeyboardMaker;
    InlineKeyboardMaker inlineKeyboardMaker;

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

    private @NotNull Runnable getGetUsersAction(Update update) {
        return () -> {
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
                    sendRecipesList(update, List.of(randomRecipe));
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

                telegramClient.execute(SendMessage.builder().chatId(chatId).text(BotMessageEnum.HELP_MESSAGE.getMessage()).replyMarkup(replyKeyboardMaker.getMainMenuKeyboard(user)).build());
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
            try {
                telegramClient.execute(SendMessage.builder().chatId(chatId).text("Привет, " + user.getUserName() + "\n\n" + BotMessageEnum.HELP_MESSAGE.getMessage()).replyMarkup(replyKeyboardMaker.getMainMenuKeyboard(user)).build());
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
                log.error(Arrays.toString(e.getStackTrace()));
            }
        };
    }

    private void sendUsersList(Update update) throws TelegramApiException {
        List<User> allUsers = userDAO.findAllUsers();

        for (User user : allUsers) {
            if (user.getUserName() != null && (userDAO.isFirstAdmin(user.getUserName()) || user.getUserName().equals(update.getMessage().getFrom().getUserName()))) {
                continue;
            }
            SendMessage sendMessage = SendMessage.builder().chatId(update.getMessage().getChatId()).text(user.toString()).build();

            sendMessage.setReplyMarkup(inlineKeyboardMaker.getUserKeyboard(user.getId(), user.getIsAdmin()));

            telegramClient.execute(sendMessage);
        }

    }

    public void sendRecipesList(Update update, List<Recipe> recipes) throws TelegramApiException {
        Long userId = update.getMessage().getFrom().getId();
        User user = userDAO.findById(userId).orElseThrow(() -> new TelegramApiException(BotMessageEnum.USER_NOT_FOUND.getMessage() + userId));

        if (recipes.isEmpty()) {
            sendMessage(update, BotMessageEnum.RECIPE_NOT_FOUND.getMessage());
            return;
        }

        for (Recipe recipe : recipes) {
            sendRecipe(update, recipe, user);
        }
    }

    private void sendRecipe(Update update, Recipe recipe, User user) {
        if (recipe.getAnimationId() != null && !recipe.getAnimationId().isEmpty()) {
            sendAnimation(update, recipe, user);
        } else if (recipe.getVideoId() != null && !recipe.getVideoId().isEmpty()) {
            sendVideo(update, recipe, user);
        } else if (recipe.getPhotoId() != null && !recipe.getPhotoId().isEmpty()) {
            sendPhoto(update, recipe, user);
        } else {
            sendMessage(update, "Рецепт:");
        }
    }

    private void sendAnimation(Update update, Recipe recipe, User user) {
        SendAnimation animation = createAnimation(update, recipe);
        animation.setReplyMarkup(inlineKeyboardMaker.getRecipeKeyboard(recipe, 0, user.getIsAdmin()));
        try {
            telegramClient.execute(animation);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            log.error(Arrays.toString(e.getStackTrace()));
        }
    }

    private void sendVideo(Update update, Recipe recipe, User user) {
        SendVideo video = createVideo(update, recipe);
        video.setReplyMarkup(inlineKeyboardMaker.getRecipeKeyboard(recipe, 0, user.getIsAdmin()));
        try {
            telegramClient.execute(video);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            log.error(Arrays.toString(e.getStackTrace()));
        }
    }

    private void sendPhoto(Update update, Recipe recipe, User user) {
        SendPhoto photo = createPhoto(update, recipe);
        photo.setReplyMarkup(inlineKeyboardMaker.getRecipeKeyboard(recipe, 0, user.getIsAdmin()));
        try {
            telegramClient.execute(photo);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            log.error(Arrays.toString(e.getStackTrace()));
        }
    }

    private SendAnimation createAnimation(Update update, Recipe recipe) {
        return SendAnimation.builder()
                .chatId(update.getMessage().getChatId())
                .animation(new InputFile(recipe.getAnimationId()))
                .build();
    }

    private SendVideo createVideo(Update update, Recipe recipe) {
        return SendVideo.builder()
                .chatId(update.getMessage().getChatId())
                .video(new InputFile(recipe.getVideoId()))
                .build();
    }

    private SendPhoto createPhoto(Update update, Recipe recipe) {
        return SendPhoto.builder()
                .chatId(update.getMessage().getChatId())
                .caption("")
                .photo(new InputFile(recipe.getPhotoId()))
                .build();
    }

    private void sendMessage(Update update, String message) {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(update.getMessage().getChatId())
                .text(message)
                .build();
        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            log.error(Arrays.toString(e.getStackTrace()));
        }
    }
}