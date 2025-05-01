package com.example.chatservice.models;

import com.example.chatservice.enums.ReportType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.util.Date;
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
        this.id = UUID.randomUUID();
        this.timestamp = Date.from(Instant.now());
    }

    public Message(UUID sender, UUID receiver, String content, MessageStatus status, MessageType type) {
        this.id = UUID.randomUUID();
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.timestamp = Date.from(Instant.now());
        this.status = status;
        this.type = type;
    }

    public Message(UUID sender, UUID receiver, String content, MessageType type) {
        this.id = UUID.randomUUID();
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.timestamp = Date.from(Instant.now());
        this.type = type;
    }

    public Message(String content) {
        this.id = UUID.randomUUID();
        this.content = content;
        this.timestamp = Date.from(Instant.now());
    }
    @PrimaryKey
    private UUID id;

    private UUID sender;

    private UUID receiver;

    private String content;

    private Date timestamp;

    private MessageStatus status;
    private MessageType type;

    private boolean isReported;
    private ReportType reportType;
}