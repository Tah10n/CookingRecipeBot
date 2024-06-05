package org.example.cooking_recipe_bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "org.example.cooking_recipe_bot.db.repository")
public class CookingRecipeBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(CookingRecipeBotApplication.class, args);
    }

}
