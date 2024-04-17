package org.example.cooking_recipe_bot.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "users")
public class User {
    private Long id;
    private String firstName;
    private String lastName;
    private String userName;
    private Boolean isAdmin;
}
