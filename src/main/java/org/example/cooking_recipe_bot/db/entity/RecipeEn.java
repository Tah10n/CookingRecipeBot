package org.example.cooking_recipe_bot.db.entity;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "recipeEn")
public class RecipeEn extends Recipe {
}
