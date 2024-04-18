package org.example.cooking_recipe_bot.bot.keyboards;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
public class InlineKeyboardMaker {
    public InlineKeyboardMarkup getUserKeyboard() {
        InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text("Удалить пользователя").callbackData("/delete_user").build(),
                        InlineKeyboardButton.builder().text("Сделать админом").callbackData("/set_admin").build()
                )).build();

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getUserAdminKeyboard() {
        InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text("Удалить пользователя").callbackData("/delete_user").build(),
                        InlineKeyboardButton.builder().text("Убрать из админов").callbackData("/unset_admin").build()
                )).build();

        return inlineKeyboardMarkup;
    }

}
