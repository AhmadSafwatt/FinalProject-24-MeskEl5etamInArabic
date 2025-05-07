package com.example.chatservice.commands;

import com.example.chatservice.models.Message;
import com.example.chatservice.services.MessageService;

import java.util.UUID;

public class UpdateMessageCommand implements Command {

    private final UUID messageId;
    private final Message partialMessage;
    private final MessageService messageService;

    public UpdateMessageCommand(UUID messageId, Message partialMessage, MessageService messageService) {
        this.messageId = messageId;
        this.partialMessage = partialMessage;
        this.messageService = messageService;
    }

    @Override
    public void execute() {
        messageService.updateMessage(messageId, partialMessage);
    }
}