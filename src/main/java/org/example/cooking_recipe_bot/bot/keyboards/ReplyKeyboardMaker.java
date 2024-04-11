package org.example.cooking_recipe_bot.bot.keyboards;

import org.example.cooking_recipe_bot.constants.ButtonNameEnum;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
public class ReplyKeyboardMaker {
    public ReplyKeyboardMarkup getMainMenuKeyboard() {
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton(ButtonNameEnum.FIND_RANDOM_RECIPE_BUTTON.getButtonName()));
        row1.add(new KeyboardButton(ButtonNameEnum.ADD_RECIPE_BUTTON.getButtonName()));


        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton(ButtonNameEnum.BREAKFAST_BUTTON.getButtonName()));
        row2.add(new KeyboardButton(ButtonNameEnum.LUNCH_BUTTON.getButtonName()));
        row2.add(new KeyboardButton(ButtonNameEnum.DINNER_BUTTON.getButtonName()));

        KeyboardRow row3 = new KeyboardRow();
        row3.add(new KeyboardButton(ButtonNameEnum.HELP_BUTTON.getButtonName()));

        List<KeyboardRow> keyboard = new ArrayList<>();
        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);

        final ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setKeyboard(keyboard);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        return replyKeyboardMarkup;
    }
}
