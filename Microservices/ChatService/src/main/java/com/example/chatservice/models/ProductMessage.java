package com.example.chatservice.models;

import com.example.chatservice.enums.MessageType;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.UUID;

@Table("messages")
public class ProductMessage extends Message {

    public ProductMessage(UUID sender, UUID receiver, String content) {
        super(sender, receiver, content, MessageType.PRODUCT);
    }

    public ProductMessage() {
        super(MessageType.PRODUCT);
    }
}
