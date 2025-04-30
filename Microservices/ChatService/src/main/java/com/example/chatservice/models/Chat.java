package com.example.chatservice.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.util.List;
import java.util.UUID;


@Getter
@Setter
@Table(value = "chats")
public class Chat {

    @Value("${spring.cassandra.keyspace-name}")
    private String keyspaceName;

    @PrimaryKey
    private UUID id;

    private String user1;
    private String user2;

    @CassandraType(type = CassandraType.Name.LIST, typeArguments = CassandraType.Name.TEXT)
    private List<Message> messages;
}