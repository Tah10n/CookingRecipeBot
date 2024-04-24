package org.example.cooking_recipe_bot.bot.keyboards;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

@Component
public class InlineKeyboardMaker {
    public InlineKeyboardMarkup getUserKeyboard() {
        InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text("Удалить пользователя").callbackData("delete_user_button").build(),
                        InlineKeyboardButton.builder().text("Сделать админом").callbackData("set_admin_button").build()
                )).build();

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getUserAdminKeyboard() {
        InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text("Удалить пользователя").callbackData("delete_user_button").build(),
                        InlineKeyboardButton.builder().text("Убрать из админов").callbackData("unset_admin_button").build()
                )).build();

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getRecipeAdminKeyboard(String recipe) {
        InlineKeyboardButton deleteButton = InlineKeyboardButton.builder().text("Удалить рецепт").callbackData("delete_recipe_button").build();
        InlineKeyboardButton editButton = InlineKeyboardButton.builder().text("Редактировать").callbackData("edit_recipe_button").build();
        editButton.setSwitchInlineQueryCurrentChat(recipe);
        InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(deleteButton, editButton)).build();

        return inlineKeyboardMarkup;
    }

}
