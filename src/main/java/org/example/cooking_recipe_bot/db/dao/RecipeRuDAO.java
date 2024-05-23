package org.example.cooking_recipe_bot.db.dao;

import org.example.cooking_recipe_bot.db.repository.RecipeRuRepository;
import org.springframework.stereotype.Service;

@Service
public class RecipeRuDAO extends RecipeDAO {

    public RecipeRuDAO(RecipeRuRepository recipeRuRepository) {
        super(recipeRuRepository);
    }
}
