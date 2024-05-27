package org.example.cooking_recipe_bot.utils;

import lombok.extern.slf4j.Slf4j;
import org.example.cooking_recipe_bot.db.entity.Recipe;
import org.example.cooking_recipe_bot.db.entity.RecipeEn;
import org.example.cooking_recipe_bot.db.entity.RecipeRu;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class RecipeParser {
    private RecipeParser() {
    }

    public static Recipe parseRecipeFromString(String recipeString, String language) throws ParseException {
        if (recipeString == null) {
            throw new ParseException("Recipe string is null", 0);
        }

        // Extract the recipe name
        String[] lines = recipeString.split("\n");
        if (lines.length == 0) {
            throw new ParseException("Recipe string has no lines", 0);
        }
        Recipe recipe;
        if(language.equals("ru")) {
            recipe = new RecipeRu();
        } else {
            recipe = new RecipeEn();
        }
        recipe.setText(recipeString);

        String name = lines[0].trim().toLowerCase().replaceAll("[.]", "");
        if (name.length() > 100) {
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
        if (ingredientsBuilder.isEmpty()) {
            log.info("where is no ingredients in added recipe");
        }
        recipe.setIngredients(ingredientsBuilder.toString().trim().toLowerCase());

        // Extract the instructions
        Pattern instructionPattern = Pattern.compile("(?<=\\n)([^•#-]+)");
        Matcher instructionMatcher = instructionPattern.matcher(recipeString);
        StringBuilder instructionsBuilder = new StringBuilder();
        while (instructionMatcher.find()) {
            String instruction = instructionMatcher.group(1).trim();
            instructionsBuilder.append(instruction).append("\n");
        }
        if (instructionsBuilder.isEmpty()) {
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
        if (hashtagsBuilder.isEmpty()) {
            log.info("where is no hashtags in added recipe");
        }
        recipe.setHashtags(hashtagsBuilder.toString().trim());

        return recipe;
    }
}
