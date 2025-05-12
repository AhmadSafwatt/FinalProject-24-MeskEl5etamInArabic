package com.example.chatservice.services;

import com.example.chatservice.dtos.CreateMessageDTO;
import com.example.chatservice.dtos.UpdateMessageDTO;
import com.example.chatservice.enums.MessageStatus;
import com.example.chatservice.factories.MessageFactory;
import com.example.chatservice.models.Message;
import com.example.chatservice.repositories.MessageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No message with id " + id + " found"));
    }

    public Message saveMessage(CreateMessageDTO createMessageDTO) {

        if (createMessageDTO == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message cannot be null");
        }

        Message message = MessageFactory.createMessage(
                createMessageDTO.getSenderId(),
                createMessageDTO.getReceiverId(),
                createMessageDTO.getContent(),
                createMessageDTO.getType()
        );

        if (message.getId() == null) {
            message.setId(UUID.randomUUID());
        }

        return messageRepository.save(message);

    }

    public void deleteMessage(UUID id) {

        getMessageById(id);

        messageRepository.deleteById(id);
    }

    public Message updateMessage(UUID id, UpdateMessageDTO partialMessage) {
        Message existingMessage = getMessageById(id);

        boolean isUpdated = false;

        if (partialMessage.getContent() != null) {
            String newContent = partialMessage.getContent().trim();
            if (!newContent.isEmpty() && newContent.length() <= Message.MAX_CONTENT_LENGTH
                    && !newContent.equals(existingMessage.getContent())) {
                existingMessage.setContent(newContent);
                isUpdated = true;
            }
        }

        if (partialMessage.getStatus() != null && !partialMessage.getStatus().equals(existingMessage.getStatus())) {
            existingMessage.setStatus(partialMessage.getStatus());
            isUpdated = true;
        }

        if (partialMessage.getType() != null) {
            String newType = partialMessage.getType().toString();
            if (!newType.isBlank() && !partialMessage.getType().equals(existingMessage.getType())) {
                existingMessage.setType(partialMessage.getType());
                isUpdated = true;
            }
        }

        if (!isUpdated) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No valid fields to update");
        }

        return messageRepository.save(existingMessage);
    }

    public Message markMessageAsSeen(UUID messageId) {
        Message message = getMessageById(messageId);

        if (message.getStatus() == MessageStatus.SEEN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message with id " + messageId + " is already marked as seen");
        }

        message.setStatus(MessageStatus.SEEN);
        return messageRepository.save(message);
    }

    public boolean isMessageSeen(UUID messageId) {
        Message message = getMessageById(messageId);
        return message.getStatus() == MessageStatus.SEEN;
    }
}
