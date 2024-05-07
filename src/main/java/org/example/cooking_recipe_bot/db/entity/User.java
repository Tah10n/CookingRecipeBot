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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("id=").append(id).append(", ");
        sb.append("userName=").append(userName);
        sb.append(", firstName=").append(firstName);
        sb.append(", lastName=").append(lastName);
        sb.append(", isAdmin=").append(isAdmin);

        return sb.toString();
    }
}
