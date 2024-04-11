package org.example.cooking_recipe_bot.bot;

import org.example.cooking_recipe_bot.constants.BotMessageEnum;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.starter.SpringWebhookBot;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class CookBookTelegramBot extends SpringWebhookBot {
    private String webHookPath;
    private String botName;
    private String botToken;
    private TelegramFacade telegramFacade;

    public CookBookTelegramBot(SetWebhook setWebhook, String botToken, TelegramFacade telegramFacade) {
        super(setWebhook);
        this.botToken = botToken;
        this.telegramFacade = telegramFacade;

    }


    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        try {
            return telegramFacade.handleUpdate(update);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return new SendMessage(update.getMessage().getChatId().toString(),
                    BotMessageEnum.EXCEPTION_ILLEGAL_MESSAGE.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return new SendMessage(update.getMessage().getChatId().toString(),
                    BotMessageEnum.EXCEPTION_WHAT_THE_FUCK.getMessage());
        }
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
