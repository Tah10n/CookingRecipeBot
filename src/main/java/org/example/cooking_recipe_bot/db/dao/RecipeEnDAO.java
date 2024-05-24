package org.example.cooking_recipe_bot.db.dao;

import org.example.cooking_recipe_bot.db.entity.RecipeEn;
import org.example.cooking_recipe_bot.db.repository.RecipeEnRepository;
import org.springframework.stereotype.Service;

@Service
public class RecipeEnDAO extends RecipeDAO<RecipeEn> {

    public RecipeEnDAO(RecipeEnRepository recipeEnRepository) {
        super(recipeEnRepository);
    }
}
