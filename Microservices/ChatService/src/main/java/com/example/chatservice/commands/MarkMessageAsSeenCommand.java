package com.example.chatservice.commands;

import com.example.chatservice.enums.MessageStatus;
import com.example.chatservice.models.Message;
import com.example.chatservice.services.MessageService;

import java.util.UUID;

public class MarkMessageAsSeenCommand implements Command<Void> {
    private final UUID messageId;
    private final MessageService messageService;

    public MarkMessageAsSeenCommand(UUID messageId, MessageService messageService) {
        this.messageId = messageId;
        this.messageService = messageService;
    }

    @Override
    public Void execute() {
        messageService.markMessageAsSeen(messageId);
        return null;
    }
}