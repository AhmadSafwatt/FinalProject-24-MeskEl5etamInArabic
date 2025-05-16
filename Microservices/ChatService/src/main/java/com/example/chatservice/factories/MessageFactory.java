package com.example.chatservice.factories;

import com.example.chatservice.enums.MessageType;
import com.example.chatservice.models.Message;

import java.lang.reflect.Constructor;
import java.util.UUID;

public class MessageFactory {

    public static Message createMessage(UUID senderId, UUID receiverId, String content, MessageType type) {
        String className = "com.example.chatservice.models." +
                capitalize(type.name().toLowerCase()) + "Message";

        try {
            Class<? extends Message> messageClass = Class.forName(className).asSubclass(Message.class);
            Constructor<? extends Message> constructor = messageClass.getConstructor(UUID.class, UUID.class, String.class);
            return constructor.newInstance(senderId, receiverId, content);
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate message of type: " + type, e);
        }
    }

    private static String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

}
