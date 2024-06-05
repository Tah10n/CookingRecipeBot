package org.example.cooking_recipe_bot.db.dao;

import org.example.cooking_recipe_bot.bot.BotState;
import org.example.cooking_recipe_bot.db.entity.BotStateContext;
import org.example.cooking_recipe_bot.db.repository.BotStateContextRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BotStateContextDAO {
    private final BotStateContextRepository botStateContextRepository;

    @Autowired
    public BotStateContextDAO(BotStateContextRepository botStateContextRepository) {
        this.botStateContextRepository = botStateContextRepository;
    }

    public void saveBotStateContext(BotStateContext botStateContext) {
        botStateContextRepository.save(botStateContext);
    }

    public void deleteBotStateContext(BotStateContext botStateContext) {
        botStateContextRepository.delete(botStateContext);
    }

    public BotStateContext findBotStateContextById(Long id) {
        return botStateContextRepository.findById(String.valueOf(id)).orElse(null);
    }

    public void changeBotState(Long userId, BotState botState, String additionalData) {
        BotStateContext botStateContext = findBotStateContextById(userId);
        if(botStateContext == null) {
            botStateContext = new BotStateContext();
            botStateContext.setId(String.valueOf(userId));
            botStateContext.setCurrentBotState(BotState.DEFAULT);
            saveBotStateContext(botStateContext);
            return;
        }
        botStateContext.setAdditionalData(additionalData);
        botStateContext.setCurrentBotState(botState);
        saveBotStateContext(botStateContext);
    }


    public void changeBotState(Long userId, BotState botState) {
        BotStateContext botStateContext = findBotStateContextById(userId);
        if(botStateContext == null) {
            botStateContext = new BotStateContext();
            botStateContext.setId(String.valueOf(userId));
            botStateContext.setCurrentBotState(BotState.DEFAULT);
            saveBotStateContext(botStateContext);
            return;
        }
        botStateContext.setCurrentBotState(botState);
        saveBotStateContext(botStateContext);
    }
}
