package org.example.cooking_recipe_bot.db.dao;

import org.example.cooking_recipe_bot.db.entity.Recipe;
import org.example.cooking_recipe_bot.db.repository.RecipesRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecipeDAO {
    private RecipesRepository recipesRepository;

    public RecipeDAO(RecipesRepository recipesRepository) {
        this.recipesRepository = recipesRepository;
    }

    public List<Recipe> findRecipeByName(String name) {
        return recipesRepository.findByNameLikeIgnoreCase(name);
    }



    public Recipe saveRecipe(Recipe recipe) {
        return recipesRepository.save(recipe);

    }

    public Recipe getRandomRecipe() {
        List<Recipe> recipes = recipesRepository.findAll();
        int index = (int) (Math.random() * recipes.size());
        return recipes.get(index);
    }

    public void deleteRecipe(Recipe recipe) {
        recipesRepository.delete(recipe);
    }

    public List<Recipe> findAllRecipes() {
        return recipesRepository.findAll();
    }


    public List<Recipe> getRecipesByName(String name) {
        return recipesRepository.findRecipesByHashtagsContainsOrderByName(name);
    }

    public Recipe updateRecipe(Recipe recipe) {
        return recipesRepository.save(recipe);
    }

    public List<Recipe> findRecipesByHashtagsContains(String hashtag) {
        return recipesRepository.findRecipesByHashtagsContainsOrderByName(hashtag);

    }
}
