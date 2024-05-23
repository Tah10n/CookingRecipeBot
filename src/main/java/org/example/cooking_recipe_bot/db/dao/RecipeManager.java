package org.example.cooking_recipe_bot.db.dao;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RecipeManager {

    private final Map<String, RecipeDAO> map = new HashMap<>();

    public RecipeManager(RecipeRuDAO recipeRuDAO, RecipeEnDAO recipeEnDAO) {
        map.put("ru", recipeRuDAO);
        map.put("en", recipeEnDAO);
    }

    public RecipeDAO getRecipeDAO(String language) {
        return map.get(language);
    }
}
