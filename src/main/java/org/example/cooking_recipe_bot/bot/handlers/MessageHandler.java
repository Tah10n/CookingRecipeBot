package org.example.cooking_recipe_bot.bot.handlers;

import lombok.extern.slf4j.Slf4j;
import org.example.cooking_recipe_bot.bot.CookBookTelegramBot;
import org.example.cooking_recipe_bot.bot.keyboards.InlineKeyboardMaker;
import org.example.cooking_recipe_bot.bot.keyboards.ReplyKeyboardMaker;
import org.example.cooking_recipe_bot.constants.BotMessageEnum;
import org.example.cooking_recipe_bot.constants.ButtonNameEnum;
import org.example.cooking_recipe_bot.dao.UserDAO;
import org.example.cooking_recipe_bot.entity.User;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Slf4j
@Component

public class MessageHandler implements UpdateHandler {
    ReplyKeyboardMaker replyKeyboardMaker;
    InlineKeyboardMaker inlineKeyboardMaker;
    UserDAO userDAO;



    public MessageHandler(ReplyKeyboardMaker replyKeyboardMaker, InlineKeyboardMaker inlineKeyboardMaker, UserDAO userDAO) {
        this.replyKeyboardMaker = replyKeyboardMaker;
        this.inlineKeyboardMaker = inlineKeyboardMaker;
        this.userDAO = userDAO;

    }

    @Override
    public SendMessage handle(Update update) {

        User user = getUserFromUpdate(update);

        Message message = update.getMessage();
        long chatId = message.getChatId();

        SendMessage sendMessage = SendMessage.builder().chatId(chatId).text("").build();

        sendMessage.enableMarkdown(true);
        sendMessage.setReplyMarkup(replyKeyboardMaker.getMainMenuKeyboard(user));


        String inputText = message.getText().toLowerCase();
        if (inputText.equals("/start")) {

            sendMessage.setText("Привет, " + user.getUserName() + "\n\n" + BotMessageEnum.HELP_MESSAGE.getMessage());

        } else if (inputText.equals(ButtonNameEnum.HELP_BUTTON.getButtonName().toLowerCase())) {

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
        } else if (inputText.equals(ButtonNameEnum.USERS_BUTTON.getButtonName().toLowerCase())) {
            //TODO find all users
           sendMessges(sendMessage);


        } else {
            //TODO find on inputText
            sendMessage.setText("Воспользуйтесь главным меню");
        }


        return sendMessage;
    }

    private void sendMessges(SendMessage sendMessage) {
        List<User> allUsers = userDAO.findAllUsers();

//        for (User user1 : allUsers) {
//
//            sendMessage.setText(user1.toString());
//            sendMessage.setReplyMarkup(inlineKeyboardMaker.getUserKeyboard());
//            try {
//                cookBookTelegramBot.execute(sendMessage);
//            } catch (TelegramApiException e) {
//                throw new RuntimeException(e);
//            }
//
//        }

    }

    private User getUserFromUpdate(Update update) {
        Long userId = update.getMessage().getFrom().getId();
        if (userDAO.findById(userId).isPresent()) {
            return userDAO.findById(userId).get();
        } else {
            return createUser(update);
        }
    }

    private User createUser(Update update) {
        User user = new User();
        user.setId(update.getMessage().getFrom().getId());
        user.setFirstName(update.getMessage().getFrom().getFirstName());
        user.setLastName(update.getMessage().getFrom().getLastName());
        user.setUserName(update.getMessage().getFrom().getUserName());
        if (userDAO.isFirstAdmin(user)) {
            user.setIsAdmin(true);
        } else {
            user.setIsAdmin(false);
        }
        userDAO.saveUser(user);
        return user;
    }


}
