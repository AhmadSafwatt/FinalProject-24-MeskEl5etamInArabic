package com.example.chatservice.services;

import com.example.chatservice.clients.ProductClient;
import com.example.chatservice.commands.DeleteMessageCommand;
import com.example.chatservice.commands.SendMessageCommand;
import com.example.chatservice.commands.UpdateMessageCommand;
import com.example.chatservice.dtos.CreateMessageDTO;
import com.example.chatservice.dtos.UpdateMessageDTO;
import com.example.chatservice.enums.MessageStatus;
import com.example.chatservice.enums.MessageType;
import com.example.chatservice.enums.ReportType;
import com.example.chatservice.factories.MessageFactory;
import com.example.chatservice.models.Message;
import com.example.chatservice.repositories.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final CassandraTemplate cassandraTemplate;

    private final ProductClient productClient;

    @Autowired
    public MessageService(MessageRepository messageRepository, CassandraTemplate cassandraTemplate, ProductClient productClient) {
        this.productClient = productClient;
        this.messageRepository = messageRepository;
        this.cassandraTemplate = cassandraTemplate;
    }

    public Slice<Message> getMessages(Pageable pageable) {
        return messageRepository.findAll(pageable);
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

    public Message createMessageEntrypoint(UUID userId, CreateMessageDTO createMessageDTO) {
        SendMessageCommand sendMessageCommand = new SendMessageCommand(
                userId,
                createMessageDTO,
                this
        );

        if (createMessageDTO.getType() == MessageType.PRODUCT) {
            String productId = createMessageDTO.getContent().trim();

            String productContent = productClient.getProductById(productId);

            createMessageDTO.setContent(productContent);
        }

        return sendMessageCommand.execute();
    }

    public Message createMessage(UUID userId, CreateMessageDTO createMessageDTO) {

        Message message = MessageFactory.createMessage(
                userId,
                createMessageDTO.getReceiverId(),
                createMessageDTO.getContent(),
                createMessageDTO.getType()
        );

        if (message.getId() == null) {
            message.setId(UUID.randomUUID());
        }

        return messageRepository.save(message);
    }

    @Async
    public CompletableFuture<Void> saveAllMessagesAsync(List<Message> messages) {
        saveAllMessages(messages);
        return CompletableFuture.completedFuture(null);
    }

    private void saveAllMessages(List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message list cannot be null or empty");
        }

        messageRepository.saveAll(messages);
    }

    public void deleteMessageByIdEntrypoint(UUID id) {
        getMessageById(id);
        DeleteMessageCommand deleteMessageCommand = new DeleteMessageCommand(id, this);
        deleteMessageCommand.execute();
    }

    public void deleteMessageById(UUID id) {
        messageRepository.deleteById(id);
    }

    public Message updateMessageEntrypoint(UUID id, UpdateMessageDTO partialMessage) {
        UpdateMessageCommand updateMessageCommand = new UpdateMessageCommand(id, partialMessage, this);
        return updateMessageCommand.execute();
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
        return getMessageById(messageId).getStatus() == MessageStatus.SEEN;
    }

    public void deleteAllMessages() {
        int messageCount = (int) messageRepository.count();
        if (messageCount == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No messages to delete");
        }
        cassandraTemplate.truncate(Message.class);
    }

    public List<Message> searchMessagesByContent(String searchString) {
        List<Message> allMessages = messageRepository.findAll();

        return allMessages.stream()
                .filter(message -> message.getContent() != null && message.getContent().toLowerCase().contains(searchString.toLowerCase()))
                .collect(Collectors.toList());
    }

    public Message reportMessage(UUID messageId, ReportType reportType) {
        Message message = getMessageById(messageId);

        message.setReported(true);
        message.setReportType(reportType);

        return messageRepository.save(message);
    }

}
