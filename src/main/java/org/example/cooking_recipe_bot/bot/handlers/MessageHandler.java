package org.example.cooking_recipe_bot.bot.handlers;

import lombok.extern.slf4j.Slf4j;
import org.example.cooking_recipe_bot.bot.keyboards.ReplyKeyboardMaker;
import org.example.cooking_recipe_bot.constants.BotMessageEnum;
import org.example.cooking_recipe_bot.constants.ButtonNameEnum;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
public class MessageHandler implements UpdateHandler {
    ReplyKeyboardMaker replyKeyboardMaker;

    public MessageHandler(ReplyKeyboardMaker replyKeyboardMaker) {
        this.replyKeyboardMaker = replyKeyboardMaker;
    }

    @Override
    public SendMessage handle(Update update) {

        Message message = update.getMessage();
        long userId = message.getFrom().getId();
        long chatId = message.getChatId();

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));


        String inputText = message.getText().toLowerCase();
        if (inputText.equals("/start")) {
            sendMessage.enableMarkdown(true);
            sendMessage.setReplyMarkup(replyKeyboardMaker.getMainMenuKeyboard());
            sendMessage.setText(BotMessageEnum.HELP_MESSAGE.getMessage());
        } else if (inputText.equals(ButtonNameEnum.HELP_BUTTON.getButtonName().toLowerCase())) {
            sendMessage.enableMarkdown(true);
            sendMessage.setReplyMarkup(replyKeyboardMaker.getMainMenuKeyboard());
            sendMessage.setText(BotMessageEnum.HELP_MESSAGE.getMessage());

        } else if (inputText.equals(ButtonNameEnum.FIND_RANDOM_RECIPE_BUTTON.getButtonName().toLowerCase())) {
            //TODO добавить поиск случайного рецептов
        } else if (inputText.equals(ButtonNameEnum.ADD_RECIPE_BUTTON.getButtonName().toLowerCase())) {
            //TODO добавить добавление рецептов
        } else if (inputText.equals(ButtonNameEnum.BREAKFAST_BUTTON.getButtonName().toLowerCase())) {
            //TODO find breakfast recipes
            sendMessage.setText("Завтрак");
        } else if (inputText.equals(ButtonNameEnum.LUNCH_BUTTON.getButtonName().toLowerCase())) {
            //TODO find lunch recipes
            sendMessage.setText("Обед");
        } else if (inputText.equals(ButtonNameEnum.DINNER_BUTTON.getButtonName().toLowerCase())) {
            //TODO find dinner recipes
            sendMessage.setText("Ужин");
        } else {
            //TODO find on inputText
            sendMessage.setText("Воспользуйтесь главным меню");
        }


        return sendMessage;
    }


}
