package com.example.chatservice.commands;

import com.example.chatservice.enums.ReportType;
import com.example.chatservice.models.Message;
import com.example.chatservice.services.MessageService;

import java.util.UUID;

public class ReportMessageCommand implements Command {

    private final UUID messageId;
    private final ReportType reportType;
    private final MessageService messageService;

    public ReportMessageCommand(UUID messageId, ReportType reportType, MessageService messageService) {
        this.messageId = messageId;
        this.reportType = reportType;
        this.messageService = messageService;
    }

    @Override
    public Void execute() {
        Message message = messageService.getMessageById(messageId);
        if (message != null) {
            messageService.reportMessage(messageId, reportType);
        } else {
            throw new IllegalArgumentException("Message not found");
        }
        return null;
    }
}
