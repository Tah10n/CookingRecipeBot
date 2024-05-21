package org.example.cooking_recipe_bot.bot.keyboards;

import org.example.cooking_recipe_bot.bot.constants.ButtonNameEnum;
import org.example.cooking_recipe_bot.bot.constants.MessageTranslator;
import org.example.cooking_recipe_bot.db.entity.Recipe;
import org.example.cooking_recipe_bot.db.entity.User;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
public class InlineKeyboardMaker {
    private final MessageTranslator messageTranslator;

    public InlineKeyboardMaker(MessageTranslator messageTranslator) {
        this.messageTranslator = messageTranslator;
    }

    public InlineKeyboardMarkup getUserKeyboard(User requester, User user) {
        if (Boolean.TRUE.equals(user.getIsAdmin())) {
            return InlineKeyboardMarkup.builder()
                    .keyboardRow(new InlineKeyboardRow(
                            InlineKeyboardButton.builder().text(messageTranslator.getMessage(ButtonNameEnum.DELETE_USER.name(), requester.getLanguage())).callbackData("delete_user_button:" + user.getId()).build(),
                            InlineKeyboardButton.builder().text(messageTranslator.getMessage(ButtonNameEnum.UNSET_ADMIN.name(), requester.getLanguage())).callbackData("unset_admin_button:" + user.getId()).build()
                    )).build();
        } else {
            return InlineKeyboardMarkup.builder()
                    .keyboardRow(new InlineKeyboardRow(
                            InlineKeyboardButton.builder().text(messageTranslator.getMessage(ButtonNameEnum.DELETE_USER.name(), requester.getLanguage())).callbackData("delete_user_button:" + user.getId()).build(),
                            InlineKeyboardButton.builder().text(messageTranslator.getMessage(ButtonNameEnum.SET_ADMIN.name(), requester.getLanguage())).callbackData("set_admin_button:" + user.getId()).build()
                    )).build();
        }

    }

    public InlineKeyboardMarkup getRecipeKeyboard(Recipe recipe, int isOpened, User user) {
        if (Boolean.TRUE.equals(user.getIsAdmin())) {
            InlineKeyboardButton openButton;
            if (isOpened == 0) {
                openButton = InlineKeyboardButton.builder().text(messageTranslator.getMessage(ButtonNameEnum.OPEN.name(), user.getLanguage())).callbackData("open_recipe_button:" + isOpened + ":" + recipe.getId()).build();
            } else {
                openButton = InlineKeyboardButton.builder().text(messageTranslator.getMessage(ButtonNameEnum.CLOSE.name(), user.getLanguage())).callbackData("open_recipe_button:" + isOpened + ":" + recipe.getId()).build();
            }


            InlineKeyboardButton deleteButton = InlineKeyboardButton.builder().text(messageTranslator.getMessage(ButtonNameEnum.DELETE_RECIPE.name(), user.getLanguage())).callbackData("delete_recipe_button:" + recipe.getId()).build();
            InlineKeyboardButton changePhotoButton = InlineKeyboardButton.builder().text(messageTranslator.getMessage(ButtonNameEnum.CHANGE_PHOTO.name(), user.getLanguage())).callbackData("change_photo_button:" + recipe.getId()).build();
            InlineKeyboardButton changeVideoButton = InlineKeyboardButton.builder().text(messageTranslator.getMessage(ButtonNameEnum.CHANGE_VIDEO.name(), user.getLanguage())).callbackData("change_video_button:" + recipe.getId()).build();
            InlineKeyboardButton editButton = InlineKeyboardButton.builder().text(messageTranslator.getMessage(ButtonNameEnum.EDIT_RECIPE.name(), user.getLanguage()))
                    .switchInlineQueryCurrentChat("/edit_recipe///" + recipe.getId() + "///" + recipe).build();

            return InlineKeyboardMarkup.builder()
                    .keyboardRow(new InlineKeyboardRow(openButton))
                    .keyboardRow(new InlineKeyboardRow(editButton, deleteButton))
                    .keyboardRow(new InlineKeyboardRow(changePhotoButton, changeVideoButton)).build();
        } else {
            InlineKeyboardButton openButton;
            if (isOpened == 0) {
                openButton = InlineKeyboardButton.builder().text(messageTranslator.getMessage(ButtonNameEnum.OPEN.name(), user.getLanguage())).callbackData("open_recipe_button:" + isOpened + ":" + recipe.getId()).build();
            } else {
                openButton = InlineKeyboardButton.builder().text(messageTranslator.getMessage(ButtonNameEnum.CLOSE.name(), user.getLanguage())).callbackData("open_recipe_button:" + isOpened + ":" + recipe.getId()).build();
            }
            return InlineKeyboardMarkup.builder()
                    .keyboardRow(new InlineKeyboardRow(openButton)).build();
        }

    }

    public InlineKeyboardMarkup addRateButtonKeyboard(InlineKeyboardMarkup inlineKeyboardMarkup, Recipe recipe, User user) {
        InlineKeyboardButton rateButton = InlineKeyboardButton.builder().text(messageTranslator.getMessage(ButtonNameEnum.RATE.name(), user.getLanguage()))
                .callbackData("rate_button:" + recipe.getId()).build();
        List<InlineKeyboardRow> keyboardRows = new ArrayList<>(inlineKeyboardMarkup.getKeyboard());
        keyboardRows.add(new InlineKeyboardRow(rateButton));
        InlineKeyboardMarkup inlineKeyboardMarkup1 = InlineKeyboardMarkup.builder().build();
        inlineKeyboardMarkup1.setKeyboard(keyboardRows);

        return inlineKeyboardMarkup1;
    }

    public ReplyKeyboard getMoreRecipesKeyboard(User user) {
        String moreRecipesButtonName = messageTranslator.getMessage(ButtonNameEnum.MORE_RECIPES.name(), user.getLanguage());
        String cancelButtonName = messageTranslator.getMessage(ButtonNameEnum.CANCEL.name(), user.getLanguage());
        InlineKeyboardButton moreRecipesButton = InlineKeyboardButton.builder().text(moreRecipesButtonName).callbackData("more_recipes_button:").build();
        InlineKeyboardButton cancelButton = InlineKeyboardButton.builder().text(cancelButtonName).callbackData("cancel_button:").build();

        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(cancelButton, moreRecipesButton)).build();
    }

    public ReplyKeyboard getYesOrNoForDeleteRecipeKeyboard(User user, String recipeId) {
        String yesButtonName = messageTranslator.getMessage(ButtonNameEnum.YES.name(), user.getLanguage());
        String noButtonName = messageTranslator.getMessage(ButtonNameEnum.NO.name(), user.getLanguage());
        InlineKeyboardButton yesButton = InlineKeyboardButton.builder().text(yesButtonName).callbackData("yes_for_delete_recipe_button:" + recipeId).build();
        InlineKeyboardButton noButton = InlineKeyboardButton.builder().text(noButtonName).callbackData("no_for_delete_recipe_button:" + recipeId).build();
        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(yesButton, noButton)).build();
    }

    public InlineKeyboardRow getRatingButtons(String recipeId) {
        InlineKeyboardButton oneButton = InlineKeyboardButton.builder().text("1").callbackData("rate_1:" + recipeId).build();
        InlineKeyboardButton twoButton = InlineKeyboardButton.builder().text("2").callbackData("rate_2:" + recipeId).build();
        InlineKeyboardButton threeButton = InlineKeyboardButton.builder().text("3").callbackData("rate_3:" + recipeId).build();
        InlineKeyboardButton fourButton = InlineKeyboardButton.builder().text("4").callbackData("rate_4:" + recipeId).build();
        InlineKeyboardButton fiveButton = InlineKeyboardButton.builder().text("5").callbackData("rate_5:" + recipeId).build();
        return new InlineKeyboardRow(oneButton, twoButton, threeButton, fourButton, fiveButton);
    }

    public InlineKeyboardMarkup getCancelKeyboard(User user) {
        String cancelButtonName = messageTranslator.getMessage(ButtonNameEnum.CANCEL.name(), user.getLanguage());
        InlineKeyboardButton cancelButton = InlineKeyboardButton.builder().text(cancelButtonName).callbackData("cancel_button:").build();
        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(cancelButton)).build();
    }

    public InlineKeyboardMarkup getLanguagesKeyboard(User user) {
        String languageRuButtonName = messageTranslator.getMessage(ButtonNameEnum.LANGUAGE_RU.name(), user.getLanguage());
        String languageEnButtonName = messageTranslator.getMessage(ButtonNameEnum.LANGUAGE_EN.name(), user.getLanguage());
        InlineKeyboardButton languageRuButton = InlineKeyboardButton.builder().text(languageRuButtonName).callbackData("language_ru_button:").build();
        InlineKeyboardButton languageEnButton = InlineKeyboardButton.builder().text(languageEnButtonName).callbackData("language_en_button:").build();
        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(languageRuButton, languageEnButton)).build();
    }
}
