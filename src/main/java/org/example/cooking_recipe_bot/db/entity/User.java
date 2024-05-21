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

    @Override
    public String toString() {
        return "id=" + id + ", " +
                "userName=" + userName +
                ", firstName=" + firstName +
                ", lastName=" + lastName +
                ", isAdmin=" + isAdmin;
    }
}
