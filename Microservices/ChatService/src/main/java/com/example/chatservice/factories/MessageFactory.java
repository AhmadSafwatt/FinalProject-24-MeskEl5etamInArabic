package com.example.chatservice.factories;

import com.example.chatservice.enums.MessageType;
import com.example.chatservice.models.ImageMessage;
import com.example.chatservice.models.ProductMessage;
import com.example.chatservice.models.TextMessage;
import com.example.chatservice.models.Message;

import java.util.UUID;

public class MessageFactory {
    public static Message createMessage(UUID senderId, UUID receiverId, String content, MessageType type) {
        switch (type) {
            case TEXT:
                return new TextMessage(senderId, receiverId, content);
            case IMAGE:
                return new ImageMessage(senderId, receiverId, content);
            case PRODUCT:
                return new ProductMessage(senderId, receiverId, content);
            default:
                throw new IllegalArgumentException("Unsupported message type: " + type);
        }
    }
}