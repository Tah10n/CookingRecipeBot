package org.example.cooking_recipe_bot.bot.keyboards;

import org.example.cooking_recipe_bot.db.entity.Recipe;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.SwitchInlineQueryChosenChat;

@Component
public class InlineKeyboardMaker {
    public InlineKeyboardMarkup getUserKeyboard(Long userId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text("Удалить пользователя").callbackData("delete_user_button:" + userId).build(),
                        InlineKeyboardButton.builder().text("Сделать админом").callbackData("set_admin_button:" + userId).build()
                )).build();

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getUserAdminKeyboard(Long userId) {
        InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder().text("Удалить пользователя").callbackData("delete_user_button:" + userId).build(),
                        InlineKeyboardButton.builder().text("Убрать из админов").callbackData("unset_admin_button:" + userId).build()
                )).build();

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getRecipeAdminKeyboard(Recipe recipe, int isOpened) {
        InlineKeyboardButton openButton = InlineKeyboardButton.builder().text(recipe.getName().toUpperCase()).callbackData("open_recipe_button:" + isOpened + ":" + recipe.getId()).build();

        InlineKeyboardButton deleteButton = InlineKeyboardButton.builder().text("Удалить рецепт").callbackData("delete_recipe_button:" + recipe.getId()).build();
        InlineKeyboardButton changePhotoButton = InlineKeyboardButton.builder().text("Изменить фото").callbackData("change_photo_button:" + recipe.getId()).build();
        InlineKeyboardButton editButton = InlineKeyboardButton.builder().text("Редактировать")
                .switchInlineQueryCurrentChat("/edit_recipe//" + recipe.getId() + "//" + recipe.toString())
                .build();

        InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(openButton))
                .keyboardRow(new InlineKeyboardRow(editButton, changePhotoButton, deleteButton)).build();

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getRecipeKeyboard(Recipe recipe, int isOpened) {
        InlineKeyboardButton openButton = InlineKeyboardButton.builder().text(recipe.getName().toUpperCase()).callbackData("open_recipe_button:" + isOpened + ":" + recipe.getId()).build();

        InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(openButton)).build();

        return inlineKeyboardMarkup;
    }

}
