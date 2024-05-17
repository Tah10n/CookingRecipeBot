package org.example.cooking_recipe_bot.utils;

import org.example.cooking_recipe_bot.db.entity.MyMessageEntity;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;

import java.util.ArrayList;
import java.util.List;

public class MessageEntityMapper {

    public static MyMessageEntity mapToMyMessageEntity(MessageEntity messageEntity, int offset) {
        return new MyMessageEntity(messageEntity.getType(), messageEntity.getOffset() - offset, messageEntity.getLength(),
                messageEntity.getUrl(), messageEntity.getUser(), messageEntity.getLanguage(),
                messageEntity.getCustomEmojiId(), messageEntity.getText());

    }

    public static MessageEntity mapToMessageEntity(MyMessageEntity myMessageEntity) {
        return MessageEntity.builder().type(myMessageEntity.getType()).offset(myMessageEntity.getOffset())
                .length(myMessageEntity.getLength()).url(myMessageEntity.getUrl()).user(myMessageEntity.getUser())
                .language(myMessageEntity.getLanguage()).customEmojiId(myMessageEntity.getCustomEmojiId())
                .text(myMessageEntity.getText()).build();
    }

    public static List<MessageEntity> mapToMessageEntities(List<MyMessageEntity> myMessageEntities) {
        List<MessageEntity> messageEntities = new ArrayList<>();
        for (MyMessageEntity myMessageEntity : myMessageEntities) {
            messageEntities.add(mapToMessageEntity(myMessageEntity));
        }
        return messageEntities;
    }

    public static List<MyMessageEntity> mapToMyMessageEntities(List<MessageEntity> messageEntities, int offset) {
        List<MyMessageEntity> myMessageEntities = new ArrayList<>();
        for (MessageEntity messageEntity : messageEntities) {

            myMessageEntities.add(mapToMyMessageEntity(messageEntity, offset));
        }
        return myMessageEntities;
    }
}
