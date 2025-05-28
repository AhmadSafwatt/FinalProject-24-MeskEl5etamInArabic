package com.example.chatservice.commands;

import com.example.chatservice.dtos.UpdateMessageDTO;
import com.example.chatservice.models.Message;
import com.example.chatservice.services.MessageService;

import java.util.UUID;

public class UpdateMessageCommand implements Command<Message> {

    private final UUID messageId;
    private final UpdateMessageDTO updateMessageDTO;
    private final MessageService messageService;

    public UpdateMessageCommand(UUID messageId, UpdateMessageDTO updateMessageDTO, MessageService messageService) {
        this.messageId = messageId;
        this.updateMessageDTO = updateMessageDTO;
        this.messageService = messageService;
    }

    @Override
    public Message execute() {
        return messageService.updateMessage(messageId, updateMessageDTO);
    }
}