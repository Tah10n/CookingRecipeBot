package org.example.cooking_recipe_bot.bot.keyboards;

import org.example.cooking_recipe_bot.bot.constants.ButtonNameEnum;
import org.example.cooking_recipe_bot.db.entity.Recipe;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
public class InlineKeyboardMaker {
    public InlineKeyboardMarkup getUserKeyboard(Long userId, boolean isAdmin) {
        if (isAdmin) {
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
        if (isAdmin) {
            InlineKeyboardButton openButton;
            if (isOpened == 0) {
                openButton = InlineKeyboardButton.builder().text("открыть").callbackData("open_recipe_button:" + isOpened + ":" + recipe.getId()).build();
            } else {
                openButton = InlineKeyboardButton.builder().text("закрыть").callbackData("open_recipe_button:" + isOpened + ":" + recipe.getId()).build();
            }


            InlineKeyboardButton deleteButton = InlineKeyboardButton.builder().text("Удалить рецепт").callbackData("delete_recipe_button:" + recipe.getId()).build();
            InlineKeyboardButton changePhotoButton = InlineKeyboardButton.builder().text("Изменить фото").callbackData("change_photo_button:" + recipe.getId()).build();
            InlineKeyboardButton changeVideoButton = InlineKeyboardButton.builder().text("Изменить видео").callbackData("change_video_button:" + recipe.getId()).build();
            InlineKeyboardButton editButton = InlineKeyboardButton.builder().text("Редактировать")
                    .switchInlineQueryCurrentChat("/edit_recipe//" + recipe.getId() + "//" + recipe.toString()).build();

            InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardMarkup.builder()
                    .keyboardRow(new InlineKeyboardRow(openButton))
                    .keyboardRow(new InlineKeyboardRow(editButton, deleteButton))
                    .keyboardRow(new InlineKeyboardRow(changePhotoButton, changeVideoButton)).build();

            return inlineKeyboardMarkup;
        } else {
            InlineKeyboardButton openButton;
            if (isOpened == 0) {
                openButton = InlineKeyboardButton.builder().text("открыть").callbackData("open_recipe_button:" + isOpened + ":" + recipe.getId()).build();
            } else {
                openButton = InlineKeyboardButton.builder().text("закрыть").callbackData("open_recipe_button:" + isOpened + ":" + recipe.getId()).build();
            }
            InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardMarkup.builder()
                    .keyboardRow(new InlineKeyboardRow(openButton)).build();

            return inlineKeyboardMarkup;
        }

    }

    public InlineKeyboardMarkup addRateButtonKeyboard(InlineKeyboardMarkup inlineKeyboardMarkup, Recipe recipe) {
        InlineKeyboardButton rateButton = InlineKeyboardButton.builder().text("оценить")
                .callbackData("rate_button:" + recipe.getId()).build();
        List<InlineKeyboardRow> keyboardRows = new ArrayList<>();
        keyboardRows.addAll(inlineKeyboardMarkup.getKeyboard());
        keyboardRows.add(new InlineKeyboardRow(rateButton));
        InlineKeyboardMarkup inlineKeyboardMarkup1 = InlineKeyboardMarkup.builder().build();
        inlineKeyboardMarkup1.setKeyboard(keyboardRows);

        return inlineKeyboardMarkup1;
    }

    public ReplyKeyboard getMoreRecipesKeyboard() {
        InlineKeyboardButton moreRecipesButton = InlineKeyboardButton.builder().text(ButtonNameEnum.MORE_RECIPES.getButtonName()).callbackData("more_recipes_button:").build();
        InlineKeyboardButton cancelButton = InlineKeyboardButton.builder().text(ButtonNameEnum.CANCEL.getButtonName()).callbackData("cancel_button:").build();

        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(cancelButton, moreRecipesButton)).build();
    }

    public ReplyKeyboard getYesOrNoForDeleteRecipeKeyboard(String recipeId) {
        InlineKeyboardButton yesButton = InlineKeyboardButton.builder().text(ButtonNameEnum.YES.getButtonName()).callbackData("yes_for_delete_recipe_button:" + recipeId).build();
        InlineKeyboardButton noButton = InlineKeyboardButton.builder().text(ButtonNameEnum.NO.getButtonName()).callbackData("no_for_delete_recipe_button:" + recipeId).build();
        InlineKeyboardMarkup inlineKeyboardMarkup = InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(yesButton, noButton)).build();

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardRow getRatingButtons(String recipeId) {
        InlineKeyboardButton oneButton = InlineKeyboardButton.builder().text("1").callbackData("rate_1:" + recipeId).build();
        InlineKeyboardButton twoButton = InlineKeyboardButton.builder().text("2").callbackData("rate_2:" + recipeId).build();
        InlineKeyboardButton threeButton = InlineKeyboardButton.builder().text("3").callbackData("rate_3:" + recipeId).build();
        InlineKeyboardButton fourButton = InlineKeyboardButton.builder().text("4").callbackData("rate_4:" + recipeId).build();
        InlineKeyboardButton fiveButton = InlineKeyboardButton.builder().text("5").callbackData("rate_5:" + recipeId).build();
        return new InlineKeyboardRow(oneButton, twoButton, threeButton, fourButton, fiveButton);
    }
}
