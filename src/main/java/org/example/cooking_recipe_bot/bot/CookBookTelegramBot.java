package org.example.cooking_recipe_bot.bot;

import lombok.Data;
import org.example.cooking_recipe_bot.constants.BotMessageEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.telegram.telegrambots.webhook.starter.SpringTelegramWebhookBot;
import org.telegram.telegrambots.webhook.starter.TelegramBotsSpringWebhookApplication;

import java.util.function.Function;



public class CookBookTelegramBot extends SpringTelegramWebhookBot {


    public CookBookTelegramBot(String botPath, Function<Update, BotApiMethod<?>> updateHandler, Runnable setWebhook, Runnable deleteWebhook) {
        super(botPath, updateHandler, setWebhook, deleteWebhook);

    }

    @Override
    public void runDeleteWebhook() {
        super.runDeleteWebhook();
    }

    @Override
    public void runSetWebhook() {
        super.runSetWebhook();
    }

    @Override
    public BotApiMethod<?> consumeUpdate(Update update) {
        return super.consumeUpdate(update);
    }
}
