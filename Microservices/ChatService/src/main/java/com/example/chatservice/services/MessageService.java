package com.example.chatservice.services;

import com.example.chatservice.models.Message;
import com.example.chatservice.repositories.MessageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import com.example.chatservice.enums.MessageStatus;

@Service
public class MessageService {

    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public List<Message> getMessages() {
        return messageRepository.findAll();
    }

    public Message getMessageById(UUID id) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message ID cannot be null");
        }
        return messageRepository.findById(id).orElse(null);
    }

    public Message saveMessage(Message message) {
        if (message == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message cannot be null");
        }
        if (message.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message ID cannot be null");
        }
        if (message.getSenderId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sender ID cannot be null");
        }
        if (message.getReceiverId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Receiver ID cannot be null");
        }
        if (message.getContent() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message content cannot be null");
        }
        return messageRepository.save(message);
    }

    public void deleteMessage(UUID id) {

        Message message = getMessageById(id);
        if (message == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found");
        }

        messageRepository.deleteById(id);
    }

    public void updateMessage(UUID id, Message partialMessage) {
        Message existingMessage = getMessageById(id);

        if (existingMessage == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found");
        }

        if (partialMessage.getContent() != null) {
            existingMessage.setContent(partialMessage.getContent());
        }

        if (partialMessage.getStatus() != null) {
            existingMessage.setStatus(partialMessage.getStatus());
        }

        if (partialMessage.getType() != null) {
            existingMessage.setType(partialMessage.getType());
        }

        saveMessage(existingMessage);
    }

    public boolean isMessageSeen(UUID messageId) {
        Message message = getMessageById(messageId);

        if (message == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found");
        }

        return message.getStatus() == MessageStatus.SEEN;
    }
}
