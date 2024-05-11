package org.example.cooking_recipe_bot.db.entity;

import lombok.Data;
import org.example.cooking_recipe_bot.bot.BotState;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "bot_state_contexts")
public class BotStateContext {
    @Id
    private String id;
    private BotState currentBotState;
    private String additionalData;


}
