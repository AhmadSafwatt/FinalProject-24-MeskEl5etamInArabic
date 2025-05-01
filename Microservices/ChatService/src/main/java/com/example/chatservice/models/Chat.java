package com.example.chatservice.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.List;
import java.util.UUID;

/**
 * Represents a chat between two users.
 * A chat contains a list of messages exchanged between the users.
 */


@Getter
@Setter
@Table(value = "chats")
public class Chat {

    @PrimaryKey
    private UUID id;

    private String user1;
    private String user2;

    @CassandraType(type = CassandraType.Name.LIST, typeArguments = CassandraType.Name.TEXT)
    private List<Message> messages;
}