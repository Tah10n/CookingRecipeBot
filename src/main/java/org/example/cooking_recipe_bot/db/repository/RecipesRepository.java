package org.example.cooking_recipe_bot.db.repository;

import org.example.cooking_recipe_bot.db.entity.Recipe;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipesRepository extends MongoRepository<Recipe, String> {
    List<Recipe> findRecipesByNameContainsIgnoreCase(String name);
    List<Recipe> findRecipesByHashtagsContainsIgnoreCase(String hashtag);
    List<Recipe> findRecipesByIngredientsContainsIgnoreCase(String ingredient);

    Recipe findRecipesByNameEqualsIgnoreCase(String name);

    void deleteRecipeById(String id);

    Recipe findRecipeById(String recipeId);


}
