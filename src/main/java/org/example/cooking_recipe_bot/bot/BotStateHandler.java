package org.example.cooking_recipe_bot.bot;

import org.springframework.stereotype.Service;

@Service
public class BotStateHandler {
    private BotState currentBotState;

    public BotStateHandler() {
        currentBotState = BotState.DEFAULT;
    }

    public BotState getCurrentBotState() {
        return currentBotState;
    }

    public void resetCurrentBotState() {
        this.currentBotState = BotState.DEFAULT;
    }

    public void changeCurrentBotState(BotState botState) {
        this.currentBotState = botState;
    }

}
