package com.example.chatservice.models;

import com.example.chatservice.enums.MessageType;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.UUID;

@Table("messages")
public class TextMessage extends Message {

    public TextMessage(UUID sender, UUID receiver, String content) {
        super(sender, receiver, content, MessageType.TEXT);
    }

    public TextMessage() {
        super(MessageType.TEXT);
    }
}
