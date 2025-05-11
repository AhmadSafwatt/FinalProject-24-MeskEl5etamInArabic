package com.example.chatservice.controllers;
import com.example.chatservice.commands.ReportMessageCommand;
import com.example.chatservice.commands.UpdateMessageCommand;
import com.example.chatservice.enums.ReportType;
import com.example.chatservice.commands.DeleteMessageCommand;
import com.example.chatservice.commands.SendMessageCommand;
import com.example.chatservice.dtos.CreateMessageDTO;
import com.example.chatservice.dtos.UpdateMessageDTO;
import com.example.chatservice.models.Message;
import com.example.chatservice.seeders.MessageSeeder;
import com.example.chatservice.services.MessageService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;
    private final MessageSeeder messageSeeder;

    @Autowired
    public MessageController(MessageService messageService, MessageSeeder messageSeeder) {
        this.messageService = messageService;
        this.messageSeeder = messageSeeder;
    }

    /**
     * Get all messages.
     *
     * @return List of messages
     */
    @GetMapping
    public ResponseEntity<List<Message>> getMessages() {
        log.info("Getting all messages from /messages endpoint");
        List<Message> messages = messageService.getMessages();
        log.info("Retrieved {} messages from /messages endpoint", messages.size());
        return ResponseEntity.ok(messages);
    }

    /**
     * Get a message by its ID.
     *
     * @param id Message ID
     * @return Message object
     */
    @GetMapping("/{id}")
    public ResponseEntity<Message> getMessageById(@PathVariable UUID id) {
        log.info("Getting message with ID {} from /messages/{} endpoint", id, id);
        Message message = messageService.getMessageById(id);
        log.info("Retrieved message with ID {} from /messages/{} endpoint", id, id);
        return ResponseEntity.ok(message);
    }

    /**
     * Create a new message.
     *
     * @param createMessageDTO CreateMessageDTO object
     * @return Created message object
     */
    @PostMapping
    public ResponseEntity<Message> createMessage(@Valid @RequestBody CreateMessageDTO createMessageDTO) {
        log.info("Creating message at /messages endpoint");
        SendMessageCommand sendMessageCommand = new SendMessageCommand(createMessageDTO, messageService);
        Message createdMessage = sendMessageCommand.execute();
        log.info("Created message at /messages endpoint {}", createdMessage);
        return ResponseEntity.created(URI.create("/messages/" + createdMessage.getId())).body(createdMessage);
    }

    /**
     * Delete a message by its ID.
     *
     * @param id Message ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteMessage(@PathVariable UUID id) {
        log.info("Deleting message with ID {} at /messages/{} endpoint", id, id);
        DeleteMessageCommand deleteMessageCommand = new DeleteMessageCommand(id, messageService);
        deleteMessageCommand.execute();
        log.info("Deleted message with ID {} at /messages/{} endpoint", id, id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Update a message.
     *
     * @param id      Message ID
     * @param updateMessageDTO UpdateMessageDTO object
     * @return Updated message object
     */
    @PatchMapping("/{id}")
    public ResponseEntity<Message> updateMessage(@PathVariable UUID id, @Valid @RequestBody UpdateMessageDTO updateMessageDTO) {
        log.info("Updating message with ID {} at /messages/{} endpoint {}", id, id, updateMessageDTO);
        UpdateMessageCommand updateMessageCommand = new UpdateMessageCommand(id, updateMessageDTO, messageService);
        Message updatedMessage = updateMessageCommand.execute();
        log.info("Updated message with ID {} at /messages/{} endpoint {}", id, id, updatedMessage);
        return ResponseEntity.ok(updatedMessage);
    }

    /**
     * Mark a message as seen.
     *
     * @param id Message ID
     * @return Updated message object
     */
    @PatchMapping("/{id}/seen")
    public ResponseEntity<Message> markMessageAsSeen(@PathVariable UUID id) {
        log.info("Marking message with ID {} as seen at /messages/{}/seen endpoint", id, id);
        Message updatedMessage = messageService.markMessageAsSeen(id);
        log.info("Marked message with ID {} as seen at /messages/{}/seen endpoint", id, id);
        return ResponseEntity.ok(updatedMessage);
    }

    /**
     * Get message seen status.
     *
     * @param id Message ID
     * @return Message seen status
     */
    @GetMapping("/{id}/seen")
    public ResponseEntity<Boolean> getMessageSeenStatus(@PathVariable UUID id) {
        log.info("Getting message seen status for ID {} at /messages/{}/seen endpoint", id, id);
        boolean seenStatus = messageService.isMessageSeen(id);
        log.info("Retrieved message seen status for ID {} at /messages/{}/seen endpoint", id, id);
        return ResponseEntity.ok(seenStatus);
    }

    @GetMapping("/seed")
    public ResponseEntity<String> seedMessages() {
        log.info("Seeding messages at /messages/seed endpoint");
        messageSeeder.seedMessages();
        log.info("Seeded messages at /messages/seed endpoint");
        return ResponseEntity.ok("Messages seeded successfully");
    }

    /**
     * Endpoint to report a message.
     *
     * @param messageId Message ID
     * @param reportType Report type
     */
    @PatchMapping("/report/{id}")
    public Message reportMessage(@PathVariable("id") UUID messageId, @RequestParam ReportType reportType) {
        ReportMessageCommand reportCommand = new ReportMessageCommand(messageId, reportType, messageService);
        reportCommand.execute();
        return messageService.getMessageById(messageId);
    }
}