package org.example.cooking_recipe_bot.db.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;

import java.util.List;

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
    private String thumbnailId;
    private String photoId;
    private String animationId;
    private String videoId;
    private String hashtags;
    private Double rating;
    private List<Long> votedUsersIds;
    private List<MyMessageEntity> messageEntities;

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
