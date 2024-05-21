package org.example.cooking_recipe_bot.bot.keyboards;

import org.example.cooking_recipe_bot.bot.constants.ButtonNameEnum;
import org.example.cooking_recipe_bot.bot.constants.MessageTranslator;
import org.example.cooking_recipe_bot.db.entity.User;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

@Component
public class ReplyKeyboardMaker {
    private final MessageTranslator messageTranslator;

    public ReplyKeyboardMaker(MessageTranslator messageTranslator) {
        this.messageTranslator = messageTranslator;
    }

    private ReplyKeyboardMarkup createSimpleKeyboard(User user) {
        String findRandomRecipeButton = messageTranslator.getMessage(ButtonNameEnum.FIND_RANDOM_RECIPE_BUTTON.name(),user.getLanguage());
        String breakfastButton = messageTranslator.getMessage(ButtonNameEnum.BREAKFAST_BUTTON.name(),user.getLanguage());
        String lunchButton = messageTranslator.getMessage(ButtonNameEnum.LUNCH_BUTTON.name(),user.getLanguage());
        String dinnerButton = messageTranslator.getMessage(ButtonNameEnum.DINNER_BUTTON.name(),user.getLanguage());
        String fifteenMinButton = messageTranslator.getMessage(ButtonNameEnum.FIFTEEN_MIN_BUTTON.name(),user.getLanguage());
        String thirtyMinButton = messageTranslator.getMessage(ButtonNameEnum.THIRTY_MIN_BUTTON.name(),user.getLanguage());

        ReplyKeyboardMarkup replyKeyboardMarkup = ReplyKeyboardMarkup.builder()
                .keyboardRow(new KeyboardRow(
                        KeyboardButton.builder().text(findRandomRecipeButton).build()))
                .keyboardRow(new KeyboardRow(
                        KeyboardButton.builder().text(breakfastButton).build(),
                        KeyboardButton.builder().text(lunchButton).build(),
                        KeyboardButton.builder().text(dinnerButton).build()))
                .keyboardRow(new KeyboardRow(
                        KeyboardButton.builder().text(fifteenMinButton).build(),
                        KeyboardButton.builder().text(thirtyMinButton).build()))
                .build();

        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        replyKeyboardMarkup.setIsPersistent(true);

        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup getMainMenuKeyboard(User user) {
        if (Boolean.TRUE.equals(user.getIsAdmin())) {
            return createAdminKeyboard(user);
        }
        return createSimpleKeyboard(user);
    }


    private ReplyKeyboardMarkup createAdminKeyboard(User user) {
        String findRandomRecipeButton = messageTranslator.getMessage(ButtonNameEnum.FIND_RANDOM_RECIPE_BUTTON.name(),user.getLanguage());
        String addRecipeButton = messageTranslator.getMessage(ButtonNameEnum.ADD_RECIPE_BUTTON.name(),user.getLanguage());
        String usersButton = messageTranslator.getMessage(ButtonNameEnum.USERS_BUTTON.name(),user.getLanguage());
        String breakfastButton = messageTranslator.getMessage(ButtonNameEnum.BREAKFAST_BUTTON.name(),user.getLanguage());
        String lunchButton = messageTranslator.getMessage(ButtonNameEnum.LUNCH_BUTTON.name(),user.getLanguage());
        String dinnerButton = messageTranslator.getMessage(ButtonNameEnum.DINNER_BUTTON.name(),user.getLanguage());
        String fifteenMinButton = messageTranslator.getMessage(ButtonNameEnum.FIFTEEN_MIN_BUTTON.name(),user.getLanguage());
        String thirtyMinButton = messageTranslator.getMessage(ButtonNameEnum.THIRTY_MIN_BUTTON.name(),user.getLanguage());
        String sendNotificationButton = messageTranslator.getMessage(ButtonNameEnum.SEND_NOTIFICATION.name(),user.getLanguage());

        ReplyKeyboardMarkup replyKeyboardMarkup = ReplyKeyboardMarkup.builder()
                .keyboardRow(new KeyboardRow(
                        KeyboardButton.builder().text(findRandomRecipeButton).build(),
                        KeyboardButton.builder().text(addRecipeButton).build()
                        , KeyboardButton.builder().text(usersButton).build()
                ))
                .keyboardRow(new KeyboardRow(
                        KeyboardButton.builder().text(breakfastButton).build(),
                        KeyboardButton.builder().text(lunchButton).build(),
                        KeyboardButton.builder().text(dinnerButton).build()))
                .keyboardRow(new KeyboardRow(
                        KeyboardButton.builder().text(fifteenMinButton).build(),
                        KeyboardButton.builder().text(thirtyMinButton).build()))
                .keyboardRow(new KeyboardRow(
                        KeyboardButton.builder().text(sendNotificationButton).build()))
                .build();

        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        replyKeyboardMarkup.setIsPersistent(true);

        return replyKeyboardMarkup;
    }
}
