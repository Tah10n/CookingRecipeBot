package org.example.cooking_recipe_bot.utils;

import org.example.cooking_recipe_bot.db.entity.Recipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RecipeParser {
    private static final Logger log = LoggerFactory.getLogger(RecipeParser.class);

    public static Recipe parseRecipeFromString(String recipeString) throws ParseException {
        if(recipeString == null) {
            throw new ParseException("Recipe string is null", 0);
        }

        // Extract the recipe name
        String[] lines = recipeString.split("\n");
        if (lines.length == 0) {
            throw new ParseException("Recipe string is empty", 0);
        }
        Recipe recipe = new Recipe();
        String name = lines[0].trim().toLowerCase().replaceAll("[.]", "");
        if(name.length() > 100) {
            throw new ParseException("Recipe name is too long", recipeString.lastIndexOf(name));
        }
        recipe.setName(name);

        // Extract the ingredients
        Pattern ingredientPattern = Pattern.compile("•(.+)");
        StringBuilder ingredientsBuilder = new StringBuilder();
        for (String line : lines) {
            Matcher ingredientMatcher = ingredientPattern.matcher(line);
            if (ingredientMatcher.find()) {
                String ingredient = ingredientMatcher.group(1).trim();
                ingredientsBuilder.append(ingredient).append("\n");
            }
        }
        if(ingredientsBuilder.length() == 0) {
            log.info("where is no ingredients in added recipe");
            //throw new ParseException("Recipe string has no ingredients", recipeString.lastIndexOf(name));
        }
        recipe.setIngredients(ingredientsBuilder.toString().trim().toLowerCase());

        // Extract the instructions
        Pattern instructionPattern = Pattern.compile("(?<=\\n)([^•#]+)");
        Matcher instructionMatcher = instructionPattern.matcher(recipeString);
        StringBuilder instructionsBuilder = new StringBuilder();
        while (instructionMatcher.find()) {
            String instruction = instructionMatcher.group(1).trim();
            instructionsBuilder.append(instruction).append("\n");
        }
        if(instructionsBuilder.length() == 0) {
            log.info("where is no instructions in added recipe");
            throw new ParseException("Recipe string has no instructions", recipeString.lastIndexOf(name));
        }

        recipe.setInstructions(instructionsBuilder.toString().trim().replaceAll("\\n+", "\n"));

        // Extract the hashtags
        Pattern hashtagsPattern = Pattern.compile("#([^#]+)");
        Matcher hashtagsMatcher = hashtagsPattern.matcher(recipeString);
        StringBuilder hashtagsBuilder = new StringBuilder();
        while (hashtagsMatcher.find()) {
            String hashtag = hashtagsMatcher.group(1).trim();
            hashtagsBuilder.append(hashtag).append("\n");
        }
        if(hashtagsBuilder.length() == 0) {
            //throw new ParseException("Recipe string has no hashtags", recipeString.lastIndexOf(name));
            log.info("where is no hashtags in added recipe");
        }
        recipe.setHashtags(hashtagsBuilder.toString().trim());

        return recipe;
    }
}
