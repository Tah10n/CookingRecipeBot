package org.example.cooking_recipe_bot.bot.exceptions;

public class RecipeSaveException extends RuntimeException {
    public RecipeSaveException(String recipeName, Exception e) {
        super("Failed to save recipe " + recipeName, e);
    }
}
