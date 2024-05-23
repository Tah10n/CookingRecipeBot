package org.example.cooking_recipe_bot.db.dao;

import org.example.cooking_recipe_bot.db.entity.Recipe;
import org.example.cooking_recipe_bot.db.repository.RecipeRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public abstract class RecipeDAO {
    private final RecipeRepository recipeRepository;
    private final Random random = new Random();
    private Map<String, Recipe> recipesCache;

    protected RecipeDAO(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
        initializeRecipeCache(recipeRepository);
    }

    private void initializeRecipeCache(RecipeRepository recipeRepo) {
        if (recipesCache == null || recipesCache.isEmpty()) {
            recipesCache = fetchAllRecipes(recipeRepo);
        }
    }

    private Map<String, Recipe> fetchAllRecipes(RecipeRepository recipeRepo) {
        return (Map<String, Recipe>) recipeRepo.findAll().stream()
                .collect(Collectors.toMap(Recipe::getId, recipe -> recipe));
    }


    public Recipe findRecipeByNameEqualsIgnoreCase(String name) {
        return recipeRepository.findRecipesByNameEqualsIgnoreCase(name);
    }


    public Recipe saveRecipe(Recipe recipe) {
        addToCache(recipe);
        return recipeRepository.save(recipe);

    }

    public void deleteRecipe(String recipeId) {
        recipesCache.remove(recipeId);
        recipeRepository.deleteRecipeById(recipeId);
    }

    private void addToCache(Recipe recipe) {
        recipesCache.put(recipe.getId(), recipe);
    }

    public Recipe getRandomRecipe() {
        if (recipesCache == null || recipesCache.isEmpty()) {
            initializeRecipeCache(recipeRepository);
            if(recipesCache == null || recipesCache.isEmpty()) {
                return null;
            }
        }

        List<String> keys = new ArrayList<>(recipesCache.keySet());
        int randomIndex = random.nextInt(keys.size());
        return recipesCache.get(keys.get(randomIndex));
    }


    public List<Recipe> findRecipesByString(String string) {
        Set<String> result = new HashSet<>();
        string = string.toLowerCase();
        for (Recipe recipe : recipesCache.values()) {
            if ((recipe.getText() != null && recipe.getText().toLowerCase().contains(string)) ||
                (recipe.getName() != null && recipe.getName().toLowerCase().contains(string)) ||
                (recipe.getHashtags() != null && recipe.getHashtags().toLowerCase().contains(string)) ||
                (recipe.getIngredients() != null && recipe.getIngredients().toLowerCase().contains(string))) {
                result.add(recipe.getId());
            }
        }
        return result.stream().map(recipesCache::get).toList();
    }

    public Recipe findRecipeById(String recipeId) {
        return recipeRepository.findRecipeById(recipeId);
    }
}
