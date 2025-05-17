package com.example.chatservice.commands;

import com.example.chatservice.dtos.CreateMessageDTO;
import com.example.chatservice.models.Message;
import com.example.chatservice.services.MessageService;

import java.util.UUID;

public class SendMessageCommand implements Command<Message> {

    private final UUID userId;
    private final CreateMessageDTO createMessageDTO;
    private final MessageService messageService;

    public SendMessageCommand(UUID userId, CreateMessageDTO createMessageDTO, MessageService messageService) {
        this.userId = userId;
        this.messageService = messageService;
        this.createMessageDTO = createMessageDTO;
    }

    @Override
    public Message execute() {
        return messageService.createMessage(userId, createMessageDTO);
    }
}