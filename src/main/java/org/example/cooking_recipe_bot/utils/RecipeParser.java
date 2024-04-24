package org.example.cooking_recipe_bot.utils;

import org.example.cooking_recipe_bot.db.entity.Recipe;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RecipeParser {
    public static Recipe parseRecipeFromString(String recipeString) throws ParseException {
        Recipe recipe = new Recipe();

        // Extract the recipe name
        String name = recipeString.split("\n")[0].trim();
        recipe.setName(name);

        // Extract the ingredients
        Pattern ingredientPattern = Pattern.compile("• (.+)");
        Matcher ingredientMatcher = ingredientPattern.matcher(recipeString);
        StringBuilder ingredientsBuilder = new StringBuilder();
        while (ingredientMatcher.find()) {
            String ingredient = ingredientMatcher.group(1).trim();
            ingredientsBuilder.append(ingredient).append("\n");
        }
        recipe.setIngredients(ingredientsBuilder.toString().trim());

        // Extract the instructions
        Pattern instructionPattern = Pattern.compile("(?<=\\n)([^•#]+)");
        Matcher instructionMatcher = instructionPattern.matcher(recipeString);
        StringBuilder instructionsBuilder = new StringBuilder();
        while (instructionMatcher.find()) {
            String instruction = instructionMatcher.group(1).trim();
            instructionsBuilder.append(instruction).append("\n");
        }
        recipe.setInstructions(instructionsBuilder.toString().trim());

        // Extract the hashtags
        Pattern hashtagsPattern = Pattern.compile("#([^#]+)");
        Matcher hashtagsMatcher = hashtagsPattern.matcher(recipeString);
        StringBuilder hashtagsBuilder = new StringBuilder();
        while (hashtagsMatcher.find()) {
            String hashtag = hashtagsMatcher.group(1).trim();
            hashtagsBuilder.append(hashtag).append("\n");
        }
        recipe.setHashtags(hashtagsBuilder.toString().trim());

        return recipe;

    }
}
