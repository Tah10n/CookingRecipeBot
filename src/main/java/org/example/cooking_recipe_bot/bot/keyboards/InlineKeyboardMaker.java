package org.example.cooking_recipe_bot.bot.keyboards;

import org.example.cooking_recipe_bot.bot.constants.ButtonNameEnum;
import org.example.cooking_recipe_bot.db.entity.Recipe;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

@Component
public class InlineKeyboardMaker {
    public InlineKeyboardMarkup getUserKeyboard(Long userId, boolean isAdmin) {
        if(isAdmin) {
            InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardMarkup.builder()
                    .keyboardRow(new InlineKeyboardRow(
                            InlineKeyboardButton.builder().text("Удалить пользователя").callbackData("delete_user_button:" + userId).build(),
                            InlineKeyboardButton.builder().text("Убрать из админов").callbackData("unset_admin_button:" + userId).build()
                    )).build();

            return inlineKeyboardMarkup;
        } else {
            InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardMarkup.builder()
                    .keyboardRow(new InlineKeyboardRow(
                            InlineKeyboardButton.builder().text("Удалить пользователя").callbackData("delete_user_button:" + userId).build(),
                            InlineKeyboardButton.builder().text("Сделать админом").callbackData("set_admin_button:" + userId).build()
                    )).build();

            return inlineKeyboardMarkup;
        }

    }

    public InlineKeyboardMarkup getRecipeKeyboard(Recipe recipe, int isOpened, boolean isAdmin) {
        if(isAdmin) {
            InlineKeyboardButton openButton;
            if(isOpened == 0) {
                openButton = InlineKeyboardButton.builder().text("открыть").callbackData("open_recipe_button:" + isOpened + ":" + recipe.getId()).build();
            } else {
                openButton = InlineKeyboardButton.builder().text("закрыть").callbackData("open_recipe_button:" + isOpened + ":" + recipe.getId()).build();
            }


            InlineKeyboardButton deleteButton = InlineKeyboardButton.builder().text("Удалить рецепт").callbackData("delete_recipe_button:" + recipe.getId()).build();
            InlineKeyboardButton changePhotoButton = InlineKeyboardButton.builder().text("Изменить фото").callbackData("change_photo_button:" + recipe.getId()).build();
            InlineKeyboardButton changeVideoButton = InlineKeyboardButton.builder().text("Изменить видео").callbackData("change_video_button:" + recipe.getId()).build();
            InlineKeyboardButton editButton = InlineKeyboardButton.builder().text("Редактировать")
                    .switchInlineQueryCurrentChat("/edit_recipe//" + recipe.getId() + "//" + recipe.toString())
                    .build();

            InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardMarkup.builder()
                    .keyboardRow(new InlineKeyboardRow(openButton))
                    .keyboardRow(new InlineKeyboardRow(editButton, deleteButton))
                    .keyboardRow(new InlineKeyboardRow(changePhotoButton, changeVideoButton)).build();

            return inlineKeyboardMarkup;
        } else {
            InlineKeyboardButton openButton;
            if(isOpened == 0) {
                openButton = InlineKeyboardButton.builder().text("открыть").callbackData("open_recipe_button:" + isOpened + ":" + recipe.getId()).build();
            } else {
                openButton = InlineKeyboardButton.builder().text("закрыть").callbackData("open_recipe_button:" + isOpened + ":" + recipe.getId()).build();
            }
            InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardMarkup.builder()
                    .keyboardRow(new InlineKeyboardRow(openButton)).build();

            return inlineKeyboardMarkup;
        }

    }

    public ReplyKeyboard getMoreRecipesKeyboard() {
        InlineKeyboardButton moreRecipesButton = InlineKeyboardButton.builder().text(ButtonNameEnum.MORE_RECIPES.getButtonName()).callbackData("more_recipes_button:").build();
        InlineKeyboardButton cancelButton = InlineKeyboardButton.builder().text(ButtonNameEnum.CANCEL.getButtonName()).callbackData("cancel_button:").build();
        InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(cancelButton, moreRecipesButton)).build();

        return inlineKeyboardMarkup;
    }

    public ReplyKeyboard getYesOrNoForDeleteRecipeKeyboard(String recipeId) {
        InlineKeyboardButton yesButton = InlineKeyboardButton.builder().text(ButtonNameEnum.YES.getButtonName()).callbackData("yes_for_delete_recipe_button:" + recipeId).build();
        InlineKeyboardButton noButton = InlineKeyboardButton.builder().text(ButtonNameEnum.NO.getButtonName()).callbackData("no_for_delete_recipe_button:" + recipeId).build();
        InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(yesButton, noButton)).build();

        return inlineKeyboardMarkup;
    }
}
