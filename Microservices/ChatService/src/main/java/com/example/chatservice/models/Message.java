package com.example.chatservice.models;

import com.example.chatservice.enums.MessageStatus;
import com.example.chatservice.enums.MessageType;
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
import org.springframework.data.cassandra.core.mapping.Indexed;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a message in the chat service.
 */

@Data
@Table(value = "messages")
@NoArgsConstructor
public class Message {

    public static final int MAX_CONTENT_LENGTH = 500;

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


    // This constructor is for testing purposes
    public Message(MessageType type, LocalDateTime timestamp, MessageStatus status, String content, boolean isReported, ReportType reportType) {
        this.id = UUID.randomUUID();
        this.senderId = UUID.randomUUID();
        this.receiverId = UUID.randomUUID();
        this.type = type;
        this.timestamp = timestamp;
        this.status = status;
        this.content = content;
        this.isReported = isReported;
        this.reportType = reportType;
    }

    @PrimaryKey
    private UUID id;

    @NotNull(message = "Sender ID cannot be null")
    private UUID senderId;

    @NotNull(message = "Receiver ID cannot be null")
    private UUID receiverId;

    @NotNull(message = "Content cannot be null")
    @NotBlank(message = "Content cannot be blank")
    @Size(max = 500, message = "Content cannot exceed 500 characters")
    @Indexed
    private String content;


    private LocalDateTime timestamp;


    @NotNull
    private MessageStatus status;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Message type cannot be null")
    private MessageType type;


    private boolean isReported;


    @Enumerated(EnumType.STRING)
    private ReportType reportType;

}