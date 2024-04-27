package org.example.cooking_recipe_bot.db.dao;

import org.example.cooking_recipe_bot.bot.BotState;
import org.example.cooking_recipe_bot.db.entity.BotStateContext;
import org.example.cooking_recipe_bot.db.repository.BotStateContextRepository;
import org.springframework.stereotype.Service;

@Service
public class BotStateContextDAO {
    private BotStateContextRepository botStateContextRepository;

    public BotStateContextDAO(BotStateContextRepository botStateContextRepository) {
        this.botStateContextRepository = botStateContextRepository;
    }

    public BotStateContext saveBotStateContext(BotStateContext botStateContext) {
        return botStateContextRepository.save(botStateContext);
    }

    public void deleteBotStateContext(BotStateContext botStateContext) {
        botStateContextRepository.delete(botStateContext);
    }

    public BotStateContext findBotStateContextByUserName(String userName) {
        return botStateContextRepository.findByUserName(userName);
    }

    public void updateBotStateContext(BotStateContext botStateContext) {
        botStateContextRepository.save(botStateContext);
    }


    public void changeBotState(String userName, BotState botState, String additionalData) {
        BotStateContext botStateContext = findBotStateContextByUserName(userName);
        botStateContext.setAdditionalData(additionalData);
        botStateContext.setCurrentBotState(botState);
        saveBotStateContext(botStateContext);
    }


    public void changeBotState(String userName, BotState botState) {
        BotStateContext botStateContext = findBotStateContextByUserName(userName);
        botStateContext.setCurrentBotState(botState);
        saveBotStateContext(botStateContext);
    }
}
