package org.example.cooking_recipe_bot.utils;

import org.example.cooking_recipe_bot.db.entity.User;

import java.util.HashMap;
import java.util.Map;

public class UserParser {
    public static User parseUserFromString(String userString) {
        userString = userString.replace("User(", "").replace(")", "");
        String[] attributes = userString.split(", ");

        Map<String, Object> userMap = new HashMap<>();
        for (String attribute : attributes) {
            String[] parts = attribute.split("=");
            String key = parts[0];
            String value = parts[1];

            if ("null".equals(value)) {
                userMap.put(key, null);
            } else if ("true".equals(value) || "false".equals(value)) {
                userMap.put(key, Boolean.parseBoolean(value));
            } else {
                userMap.put(key, value);
            }
        }
        User user = new User();
        user.setId(Long.parseLong((String) userMap.get("id")));
        user.setUserName((String) userMap.get("userName"));
        user.setFirstName((String) userMap.get("firstName"));
        user.setLastName((String) userMap.get("lastName"));
        user.setIsAdmin((Boolean) userMap.get("isAdmin"));
        return user;
    }
}

