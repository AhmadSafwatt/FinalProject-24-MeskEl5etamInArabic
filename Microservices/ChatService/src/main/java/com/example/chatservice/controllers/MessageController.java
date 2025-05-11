package com.example.chatservice.controllers;
import com.example.chatservice.commands.UpdateMessageCommand;
import com.example.chatservice.dtos.CreateMessageDTO;
import com.example.chatservice.dtos.UpdateMessageDTO;
import com.example.chatservice.models.Message;
import com.example.chatservice.seeders.MessageSeeder;
import com.example.chatservice.services.MessageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import com.example.chatservice.commands.SendMessageCommand;
import com.example.chatservice.commands.DeleteMessageCommand;

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
        return ResponseEntity.ok(messageService.getMessages());
    }

    /**
     * Get a message by its ID.
     *
     * @param id Message ID
     * @return Message object
     */
    @GetMapping("/{id}")
    public ResponseEntity<Message> getMessageById(@PathVariable UUID id) {
        return ResponseEntity.ok(messageService.getMessageById(id));
    }

    /**
     * Create a new message.
     *
     * @param createMessageDTO CreateMessageDTO object
     * @return Created message object
     */
    @PostMapping
    public ResponseEntity<Message> createMessage(@Valid @RequestBody CreateMessageDTO createMessageDTO) {
        SendMessageCommand sendMessageCommand = new SendMessageCommand(createMessageDTO, messageService);
        Message createdMessage = sendMessageCommand.execute();
        return ResponseEntity.created(URI.create("/messages/" + createdMessage.getId())).body(createdMessage);
    }

    /**
     * Delete a message by its ID.
     *
     * @param id Message ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteMessage(@PathVariable UUID id) {
        DeleteMessageCommand deleteMessageCommand = new DeleteMessageCommand(id, messageService);
        deleteMessageCommand.execute();
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
        UpdateMessageCommand updateMessageCommand = new UpdateMessageCommand(id, updateMessageDTO, messageService);
        Message updatedMessage = updateMessageCommand.execute();
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
        Message message = messageService.markMessageAsSeen(id);
        return ResponseEntity.ok(message);
    }

    /**
     * Get message seen status.
     *
     * @param id Message ID
     * @return Message seen status
     */
    @GetMapping("/{id}/seen")
    public ResponseEntity<Boolean> getMessageSeenStatus(@PathVariable UUID id) {
        return ResponseEntity.ok(messageService.isMessageSeen(id));
    }

    @GetMapping("/seed")
    public ResponseEntity<String> seedMessages() {
        messageSeeder.seedMessages();
        return ResponseEntity.ok("Messages seeded successfully");
    }
}