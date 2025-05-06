package com.example.chatservice.models;

import com.example.chatservice.enums.ReportType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.chatservice.enums.MessageStatus;
import com.example.chatservice.enums.MessageType;

/**
 * Represents a message in the chat service.
 */

@Data
@Table(value = "messages")
@NoArgsConstructor
public class Message {

    public Message(MessageType type) {
        this.type = type;
    }

    @JsonCreator
    public Message(
            @JsonProperty("senderId") @NotNull UUID senderId,
            @JsonProperty("receiverId") @NotNull UUID receiverId,
            @JsonProperty("content") @NotNull @NotBlank @Size(max = 500) String content,
            @JsonProperty("type") MessageType type) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.type = type;
        this.timestamp = LocalDateTime.now();
        this.status = MessageStatus.SENT;
    }

    @PrimaryKey
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID id = UUID.randomUUID();

    @NotNull(message = "Sender ID cannot be null")
    private UUID senderId;

    @NotNull(message = "Receiver ID cannot be null")
    private UUID receiverId;

    @NotNull(message = "Content cannot be null")
    @NotBlank(message = "Content cannot be blank")
    @Size(max = 500, message = "Content cannot exceed 500 characters")
    private String content;

    private LocalDateTime timestamp;

    private MessageStatus status;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Message type cannot be null")
    private MessageType type;

    private boolean isReported;

    @Enumerated(EnumType.STRING)
    private ReportType reportType;
}