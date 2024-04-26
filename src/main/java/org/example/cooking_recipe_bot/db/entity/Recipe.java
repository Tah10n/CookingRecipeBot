package org.example.cooking_recipe_bot.db.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "recipes")
public class Recipe {
    @Id
    @Indexed(unique = true)
    private String id;
    private String name;
    private String description;
    private String ingredients;
    private String instructions;
    private String photoId;
    private String hashtags;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name.toUpperCase()).append("\n");
        String[] ingredientsArray = ingredients.split("\n");
        for (String ingredient : ingredientsArray) {
            sb.append("• ").append(ingredient).append("\n");
        }
        sb.append(instructions).append("\n");
        String[] hashtagsArray = hashtags.split("\n");
        for (String hashtag : hashtagsArray) {
            sb.append("#").append(hashtag).append(" ");
        }

        return sb.toString();
    }
}
