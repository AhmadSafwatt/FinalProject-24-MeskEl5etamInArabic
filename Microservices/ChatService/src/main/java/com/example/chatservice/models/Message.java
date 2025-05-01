package com.example.chatservice.models;

import com.example.chatservice.enums.ReportType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

import com.example.chatservice.enums.MessageStatus;
import com.example.chatservice.enums.MessageType;

/**
 * Represents a message in the chat service.
 */

@Setter
@Getter
@Table(value = "messages",keyspace = "chat_keyspace")
public class Message {
    @PrimaryKey
    private UUID id;

    private UUID sender;

    private UUID receiver;

    private String content;

    private Instant timestamp;

    private MessageStatus status;
    private MessageType type;

    private boolean isReported;
    private ReportType reportType;
}