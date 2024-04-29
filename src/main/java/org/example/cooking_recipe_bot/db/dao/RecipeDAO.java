package org.example.cooking_recipe_bot.db.dao;

import org.example.cooking_recipe_bot.db.entity.Recipe;
import org.example.cooking_recipe_bot.db.repository.RecipesRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RecipeDAO {
    private RecipesRepository recipesRepository;

    public RecipeDAO(RecipesRepository recipesRepository) {
        this.recipesRepository = recipesRepository;
    }

    public List<Recipe> findRecipeByNameLikeIgnoreCase(String name) {
        return recipesRepository.findRecipesByNameContainsIgnoreCase(name);
    }

    public Recipe findRecipeByNameEqualsIgnoreCase(String name) {
        return recipesRepository.findRecipesByNameEqualsIgnoreCase(name);
    }

    public Recipe saveRecipe(Recipe recipe) {
        return recipesRepository.save(recipe);

    }

    public Recipe getRandomRecipe() {
        List<Recipe> recipes = recipesRepository.findAll();
        if(recipes.isEmpty()) return null;
        int index = (int) (Math.random() * recipes.size());
        return recipes.get(index);
    }

    public void deleteRecipe(String recipeId) {
        recipesRepository.deleteRecipeById(recipeId);
    }

    public List<Recipe> findAllRecipes() {
        return recipesRepository.findAll();
    }


    public Recipe updateRecipe(Recipe recipe) {
        return recipesRepository.save(recipe);
    }

    public List<Recipe> findRecipesByHashtags(String hashtag) {
        return recipesRepository.findRecipesByHashtagsContainsIgnoreCase(hashtag);

    }

    public List<Recipe> findRecipesByString(String string) {
        List<Recipe> recipes = new ArrayList<>();
        string = string.toLowerCase();
        recipesRepository.findRecipesByNameContainsIgnoreCase(string).forEach(recipes::add);
        recipesRepository.findRecipesByHashtagsContainsIgnoreCase(string).forEach(recipes::add);
        recipesRepository.findRecipesByIngredientsContainsIgnoreCase(string).forEach(recipes::add);
        return recipes;
    }

    public Recipe findRecipeById(String recipeId) {
        return recipesRepository.findRecipeById(recipeId);
    }
}
