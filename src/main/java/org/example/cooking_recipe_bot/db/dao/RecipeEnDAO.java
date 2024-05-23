package org.example.cooking_recipe_bot.db.dao;

import org.example.cooking_recipe_bot.db.entity.Recipe;
import org.example.cooking_recipe_bot.db.entity.RecipeEn;
import org.example.cooking_recipe_bot.db.repository.RecipeEnRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecipeEnDAO extends RecipeDAO {

    public RecipeEnDAO(RecipeEnRepository recipeEnRepository) {
        super(recipeEnRepository);
    }
}
