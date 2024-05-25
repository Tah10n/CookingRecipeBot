package org.example.cooking_recipe_bot.db.dao;

import org.example.cooking_recipe_bot.bot.exceptions.RecipeSaveException;
import org.example.cooking_recipe_bot.db.entity.Recipe;
import org.example.cooking_recipe_bot.db.repository.RecipeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;


public abstract class RecipeDAO<T extends Recipe> {
    private static final Logger log = LoggerFactory.getLogger(RecipeDAO.class);
    private final RecipeRepository<T> recipeRepository;
    private final Random random = new Random();
    private Map<String, Recipe> recipesCache;

    protected RecipeDAO(RecipeRepository<T> recipeRepository) {
        this.recipeRepository = recipeRepository;

    }

    @PostConstruct
    private void initializeRecipeCache() {
        if (recipesCache == null || recipesCache.isEmpty()) {
            recipesCache = fetchAllRecipes(recipeRepository);
        }
    }

    private Map<String, Recipe> fetchAllRecipes(RecipeRepository<T> recipeRepository) {
        return recipeRepository.findAll().stream()
                .collect(Collectors.toMap(Recipe::getId, recipe -> recipe));
    }


    public Recipe findRecipeByNameEqualsIgnoreCase(String name) {
        return recipeRepository.findRecipesByNameEqualsIgnoreCase(name);
    }


    @SuppressWarnings("unchecked")
    public Recipe saveRecipe(Recipe recipe) {
        if (recipe == null) {
            log.error("Recipe is null: {}", this.getClass().getSimpleName());
            return null;
        }

        try {
            T savedRecipe = recipeRepository.save((T) recipe);
            addToCache(savedRecipe);
            return savedRecipe;
        } catch (ClassCastException e) {
            log.error("Recipe class cast exception: {}", this.getClass().getSimpleName());
            throw new RecipeSaveException(recipe.getName(), e);
        } catch (Exception e) {
            throw new RecipeSaveException(recipe.getName(), e);
        }
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
            recipesCache = fetchAllRecipes(recipeRepository);
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
