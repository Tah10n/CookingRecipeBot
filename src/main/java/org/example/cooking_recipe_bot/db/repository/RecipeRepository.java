package org.example.cooking_recipe_bot.db.repository;

import org.example.cooking_recipe_bot.db.entity.Recipe;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepository<R extends Recipe> extends MongoRepository<R, String> {

    R findRecipesByNameEqualsIgnoreCase(String name);

    R findRecipeById(String recipeId);

    void deleteRecipeById(String id);

    List<R> findAll();

    R save(R recipe);
}
