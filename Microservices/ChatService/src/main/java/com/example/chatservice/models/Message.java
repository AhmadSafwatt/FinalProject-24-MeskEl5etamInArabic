package com.example.chatservice.models;

import com.example.chatservice.enums.ReportType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.chatservice.enums.MessageStatus;
import com.example.chatservice.enums.MessageType;

/**
 * Represents a message in the chat service.
 */

@Setter
@Getter
@Table(value = "messages")
public class Message {

    public Message() {
    }

    public Message(MessageType type) {
        this.id = UUID.randomUUID();
        this.type = type;
    }
    public Message(UUID sender, UUID receiver, String content, MessageType type) {
        this.id = UUID.randomUUID();
        this.senderId = sender;
        this.receiverId = receiver;
        this.content = content;
        this.type = type;
        this.timestamp = LocalDateTime.now();
        this.status = MessageStatus.SENT;
    }

    @PrimaryKey
    private UUID id;

    private UUID senderId;

    private UUID receiverId;

    private String content;

    private LocalDateTime timestamp;

    private MessageStatus status;
    private MessageType type;

    private boolean isReported;
    private ReportType reportType;
}