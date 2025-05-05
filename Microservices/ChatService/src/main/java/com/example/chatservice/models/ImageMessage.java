package com.example.chatservice.models;

import com.example.chatservice.enums.MessageType;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.UUID;

@Table("messages")
public class ImageMessage extends Message {

    public ImageMessage(UUID sender, UUID receiver, String content) {
        super(sender, receiver, content, MessageType.IMAGE);
    }

    public ImageMessage() {
        super(MessageType.IMAGE);
    }
}
