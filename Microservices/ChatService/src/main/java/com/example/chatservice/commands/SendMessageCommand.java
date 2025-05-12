package com.example.chatservice.commands;

import com.example.chatservice.dtos.CreateMessageDTO;
import com.example.chatservice.models.Message;
import com.example.chatservice.services.MessageService;

public class SendMessageCommand implements Command<Message> {

    private final CreateMessageDTO createMessageDTO;
    private final MessageService messageService;

    public SendMessageCommand(CreateMessageDTO createMessageDTO, MessageService messageService) {
        this.messageService = messageService;
        this.createMessageDTO = createMessageDTO;
    }

    @Override
    public Message execute() {
        return messageService.saveMessage(createMessageDTO);
    }
}