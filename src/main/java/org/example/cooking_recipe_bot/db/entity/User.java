package org.example.cooking_recipe_bot.db.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "users")
public class User {
    @Id
    private Long id;
    private String firstName;
    private String lastName;
    private String userName;
    private Boolean isAdmin;
    private Long chatId;
    private Boolean isUnsubscribed;
    private String language;

    public String getLanguage() {
        if(language == null || language.isEmpty()) {
            return "en";
        } else {
            return language;
        }
    }

    @Override
    public String toString() {
        return "id=" + id + ", " +
                "userName=" + userName +
                ", firstName=" + firstName +
                ", lastName=" + lastName +
                ", isAdmin=" + isAdmin;
    }
}
