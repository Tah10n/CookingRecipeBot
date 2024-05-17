package org.example.cooking_recipe_bot.db.entity;

import lombok.Data;
import org.telegram.telegrambots.meta.api.objects.User;

@Data
public class MyMessageEntity {

    private String type;
    private Integer offset;
    private Integer length;
    private String url;
    private User user;
    private String language;
    private String customEmojiId;
    private String text;

    public MyMessageEntity(String type, Integer offset, Integer length, String url, User user, String language, String customEmojiId, String text) {
        this.type = type;
        this.offset = offset;
        this.length = length;
        this.url = url;
        this.user = user;
        this.language = language;
        this.customEmojiId = customEmojiId;
        this.text = text;
    }
}
