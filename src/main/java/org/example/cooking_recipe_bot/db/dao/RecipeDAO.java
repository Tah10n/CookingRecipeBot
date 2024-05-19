package org.example.cooking_recipe_bot.db.dao;

import org.example.cooking_recipe_bot.db.entity.Recipe;
import org.example.cooking_recipe_bot.db.repository.RecipesRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecipeDAO {
    private RecipesRepository recipesRepository;
    private final Random random = new Random();
    private Map<String, Recipe> recipesCache;

    public RecipeDAO(RecipesRepository recipesRepository) {
        this.recipesRepository = recipesRepository;

        recipesCache = new HashMap<>();
        recipesCache.putAll(recipesRepository.findAll().stream().collect(Collectors.toMap(Recipe::getId, recipe -> recipe)));
    }


    public Recipe findRecipeByNameEqualsIgnoreCase(String name) {
        return recipesRepository.findRecipesByNameEqualsIgnoreCase(name);
    }

    public Recipe saveRecipe(Recipe recipe) {
        updateCache(recipe);
        return recipesRepository.save(recipe);

    }

    public void deleteRecipe(String recipeId) {
        recipesCache.remove(recipeId);
        recipesRepository.deleteRecipeById(recipeId);
    }

    private void updateCache(Recipe recipe) {
        recipesCache.put(recipe.getId(), recipe);
    }

    public Recipe getRandomRecipe() {
        int randomIndex = random.nextInt(recipesCache.size());
        List<Recipe> recipes = new ArrayList<>(recipesCache.values());
        return recipes.get(randomIndex);
    }


    public List<Recipe> findRecipesByString(String string) {
        Set<Recipe> result = new HashSet<>();
        string = string.toLowerCase();
        for (Recipe recipe : recipesCache.values()) {
            if ((recipe.getText() != null && recipe.getText().toLowerCase().contains(string)) ||
                    (recipe.getName() != null && recipe.getName().toLowerCase().contains(string)) ||
                    (recipe.getHashtags() != null && recipe.getHashtags().toLowerCase().contains(string)) ||
                    (recipe.getIngredients() != null && recipe.getIngredients().toLowerCase().contains(string))) {
                result.add(recipe);
            }
        }
        return new ArrayList<>(result);
    }

    public Recipe findRecipeById(String recipeId) {
        return recipesRepository.findRecipeById(recipeId);
    }
}
