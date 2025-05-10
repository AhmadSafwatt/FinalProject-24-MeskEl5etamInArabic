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

        return messageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found"));
    }

    public void saveMessage(Message message) {

        if (message == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message cannot be null");
        }

        message.setId(UUID.randomUUID());
        messageRepository.save(message);
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

        boolean isUpdated = false;

        if (partialMessage.getContent() != null) {
            String newContent = partialMessage.getContent().trim();
            if (!newContent.isEmpty() && !newContent.equals(existingMessage.getContent())) {
                existingMessage.setContent(newContent);
                isUpdated = true;
            } else if (newContent.isEmpty()) {
                throw new IllegalArgumentException("Content must be a non-empty string");
            }
        }

        if (partialMessage.getStatus() != null) {
            if (!partialMessage.getStatus().equals(existingMessage.getStatus())) {
                existingMessage.setStatus(partialMessage.getStatus());
                isUpdated = true;
            }
        }

        if (partialMessage.getType() != null) {
            if (!partialMessage.getType().toString().isBlank() &&
                    !partialMessage.getType().equals(existingMessage.getType())) {
                existingMessage.setType(partialMessage.getType());
                isUpdated = true;
            } else if (partialMessage.getType().toString().isBlank()) {
                throw new IllegalArgumentException("Type must be a non-empty string");
            }
        }

        if (!isUpdated) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT, "No fields were updated");
        }

        messageRepository.save(existingMessage);
    }

    public boolean isMessageSeen(UUID messageId) {
        Message message = getMessageById(messageId);

        if (message == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found");
        }

        return message.getStatus() == MessageStatus.SEEN;
    }
}
