package org.example.cooking_recipe_bot.db.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "recipes")
public class Recipe {
    @Id
    @Indexed(unique=true)
    private String id;
    private String name;
    private String description;
    private String ingredients;
    private String instructions;
    private String photo;
    private String hashtags;
}
