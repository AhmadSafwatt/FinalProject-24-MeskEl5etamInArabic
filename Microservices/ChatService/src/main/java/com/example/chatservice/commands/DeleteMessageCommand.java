package com.example.chatservice.commands;

import com.example.chatservice.services.MessageService;

import java.util.UUID;

public class DeleteMessageCommand implements Command<Void> {

    private final UUID messageId;
    private final MessageService messageService;

    public DeleteMessageCommand(UUID messageId, MessageService messageService) {
        this.messageId = messageId;
        this.messageService = messageService;
    }

    @Override
    public Void execute() {
        messageService.deleteMessageById(messageId);
        return null;
    }
}