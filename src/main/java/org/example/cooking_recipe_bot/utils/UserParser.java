package org.example.cooking_recipe_bot.utils;

import org.example.cooking_recipe_bot.db.entity.User;

import java.util.HashMap;
import java.util.Map;

public class UserParser {
    public static User parseUserFromString(String userString) {
        userString = userString.replace("User(", "").replace(")", "");
        String[] attributes = userString.split(", ");

        Map<String, Object> attributeMap = new HashMap<>();
        for (String attribute : attributes) {
            String[] parts = attribute.split("=");
            String key = parts[0];
            String value = parts[1];

            if ("null".equals(value)) {
                attributeMap.put(key, null);
            } else if ("true".equals(value) || "false".equals(value)) {
                attributeMap.put(key, Boolean.parseBoolean(value));
            } else {
                attributeMap.put(key, value);
            }
        }
        User user = new User();
        user.setId(Long.parseLong((String) attributeMap.get("id")));
        user.setUserName((String) attributeMap.get("userName"));
        user.setFirstName((String) attributeMap.get("firstName"));
        user.setLastName((String) attributeMap.get("lastName"));
        user.setIsAdmin((Boolean) attributeMap.get("isAdmin"));
        return user;
    }
}

