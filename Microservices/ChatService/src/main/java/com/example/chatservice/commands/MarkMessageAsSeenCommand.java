package com.example.chatservice.commands;

import com.example.chatservice.enums.MessageStatus;
import com.example.chatservice.models.Message;
import com.example.chatservice.services.MessageService;

public class MarkMessageAsSeenCommand implements Command {
    private final Message message;
    private final MessageService messageService;

    public MarkMessageAsSeenCommand(Message message, MessageService messageService) {
        this.message = message;
        this.messageService = messageService;
    }

    @Override
    public void execute() {
        if (message != null) {
            message.setStatus(MessageStatus.READ);
            messageService.saveMessage(message);
        }
    }
}