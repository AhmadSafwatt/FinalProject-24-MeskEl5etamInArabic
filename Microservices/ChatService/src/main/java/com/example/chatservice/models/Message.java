package com.example.chatservice.models;

import com.example.chatservice.enums.ReportType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
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
        this.type = type;
    }

    @JsonCreator
    public Message(
            @JsonProperty("senderId") @NotNull UUID senderId,
            @JsonProperty("receiverId") @NotNull UUID receiverId,
            @JsonProperty("content") @NotNull String content,
            @JsonProperty("type") MessageType type) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.type = type;
        this.timestamp = LocalDateTime.now();
        this.status = MessageStatus.SENT;
    }

    @PrimaryKey
    @NotNull
    @JsonProperty(access =  JsonProperty.Access.READ_ONLY)
    private UUID id = UUID.randomUUID();

    @NotNull
    private UUID senderId;

    @NotNull
    private UUID receiverId;

    @NotNull
    private String content;

    private LocalDateTime timestamp;

    private MessageStatus status;
    private MessageType type;

    private boolean isReported;
    private ReportType reportType;
}