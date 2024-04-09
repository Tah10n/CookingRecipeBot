package org.example.cooking_recipe_bot;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.starter.SpringWebhookBot;

public class CookBookTelegramBot extends SpringWebhookBot {
    private String webHookPath;
    private String botName;
    private String botToken;

    public CookBookTelegramBot(SetWebhook setWebhook, String botToken) {
        super(setWebhook);
        this.botToken = botToken;
    }


    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        long chatId = update.getMessage().getChatId();
        if(update.hasMessage() && update.getMessage().hasText()){
            return new SendMessage(Long.toString(chatId), update.getMessage().getText());
        }
        return new SendMessage(Long.toString(chatId), "I don't understand");
    }

    @Override
    public String getBotPath() {
        return webHookPath;
    }


    @Override
    public String getBotUsername() {
        return botName;
    }

    public void setWebHookPath(String webHookPath) {
        this.webHookPath = webHookPath;
    }

    public void setBotName(String botName) {
        this.botName = botName;
    }
}
