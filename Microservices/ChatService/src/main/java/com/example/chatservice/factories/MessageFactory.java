package com.example.chatservice.factories;

import com.example.chatservice.enums.MessageType;
import com.example.chatservice.models.Message;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MessageFactory {

    private static final Map<MessageType, Class<? extends Message>> messageTypeClassMap = new HashMap<>();

    static {
        messageTypeClassMap.put(MessageType.TEXT, getClassByName("com.example.chatservice.models.TextMessage"));
        messageTypeClassMap.put(MessageType.IMAGE, getClassByName("com.example.chatservice.models.ImageMessage"));
        messageTypeClassMap.put(MessageType.PRODUCT, getClassByName("com.example.chatservice.models.ProductMessage"));
    }

    private static Class<? extends Message> getClassByName(String className) {
        try {
            return Class.forName(className).asSubclass(Message.class);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Message class not found: " + className, e);
        }
    }

    public static Message createMessage(UUID senderId, UUID receiverId, String content, MessageType type) {
        Class<? extends Message> messageClass = messageTypeClassMap.get(type);

        if (messageClass == null) {
            throw new IllegalArgumentException("Unsupported message type: " + type);
        }

        try {
            Constructor<? extends Message> constructor = messageClass.getConstructor(UUID.class, UUID.class, String.class);
            return constructor.newInstance(senderId, receiverId, content);
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate message of type: " + type, e);
        }
    }

//    public static void main (String[]args){
//        UUID senderId = UUID.randomUUID();
//        UUID receiverId = UUID.randomUUID();
//        String content = "Hello, this is a test message!";
//        MessageType type = MessageType.IMAGE;
//
//        Message message = createMessage(senderId, receiverId, content, type);
//        System.out.println("Message is a: " + message.message());
//    }
}
