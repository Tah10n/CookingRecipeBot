package org.example.cooking_recipe_bot.bot.keyboards;

import org.example.cooking_recipe_bot.utils.constants.ButtonNameEnum;
import org.example.cooking_recipe_bot.db.entity.User;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

@Component
public class ReplyKeyboardMaker {
    private static ReplyKeyboardMarkup createSimpleKeyboard() {

        ReplyKeyboardMarkup replyKeyboardMarkup = ReplyKeyboardMarkup.builder()
                .keyboardRow(new KeyboardRow(
                        KeyboardButton.builder().text(ButtonNameEnum.FIND_RANDOM_RECIPE_BUTTON.getButtonName()).build()))
                .keyboardRow(new KeyboardRow(
                        KeyboardButton.builder().text(ButtonNameEnum.BREAKFAST_BUTTON.getButtonName()).build(),
                        KeyboardButton.builder().text(ButtonNameEnum.LUNCH_BUTTON.getButtonName()).build(),
                        KeyboardButton.builder().text(ButtonNameEnum.DINNER_BUTTON.getButtonName()).build()))
                .keyboardRow(new KeyboardRow(
                        KeyboardButton.builder().text(ButtonNameEnum.HELP_BUTTON.getButtonName()).build())).build();

        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup getMainMenuKeyboard(User user) {
        if (user == null || !user.getIsAdmin()) {
            return createSimpleKeyboard();
        }
        return createAdminKeyboard();
    }

    private ReplyKeyboardMarkup createAdminKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = ReplyKeyboardMarkup.builder()
                .keyboardRow(new KeyboardRow(
                        KeyboardButton.builder().text(ButtonNameEnum.FIND_RANDOM_RECIPE_BUTTON.getButtonName()).build(),
                        KeyboardButton.builder().text(ButtonNameEnum.ADD_RECIPE_BUTTON.getButtonName()).build(),
                        KeyboardButton.builder().text(ButtonNameEnum.USERS_BUTTON.getButtonName()).build()))
                .keyboardRow(new KeyboardRow(
                        KeyboardButton.builder().text(ButtonNameEnum.BREAKFAST_BUTTON.getButtonName()).build(),
                        KeyboardButton.builder().text(ButtonNameEnum.LUNCH_BUTTON.getButtonName()).build(),
                        KeyboardButton.builder().text(ButtonNameEnum.DINNER_BUTTON.getButtonName()).build()))
                .keyboardRow(new KeyboardRow(
                        KeyboardButton.builder().text(ButtonNameEnum.HELP_BUTTON.getButtonName()).build())).build();

        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        return replyKeyboardMarkup;
    }
}
