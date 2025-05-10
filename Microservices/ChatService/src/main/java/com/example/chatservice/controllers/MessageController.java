package com.example.chatservice.controllers;
import com.example.chatservice.commands.UpdateMessageCommand;
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
     * @param message Message object
     * @return Created message object
     */
    @PostMapping
    public ResponseEntity<Message> createMessage(@Valid @RequestBody Message message) {
        SendMessageCommand sendMessageCommand = new SendMessageCommand(message, messageService);
        sendMessageCommand.execute();
        return ResponseEntity.created(URI.create("/messages/" + message.getId())).body(message);
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
     * @param partialMessage Updated message object
     */
    @PatchMapping("/{id}")
    public ResponseEntity<Message> updateMessage(@PathVariable UUID id, @RequestBody Message partialMessage) {
        UpdateMessageCommand updateMessageCommand = new UpdateMessageCommand(id, partialMessage, messageService);
        updateMessageCommand.execute();
        Message updatedMessage = messageService.getMessageById(id);
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
        return ResponseEntity.ok(messageService.isMessageSeen(id));
    }

    @GetMapping("/seed")
    public ResponseEntity<String> seedMessages() {
        messageSeeder.seedMessages();
        return ResponseEntity.ok("Messages seeded successfully");
    }

    @GetMapping("/search")
    public ResponseEntity<List<Message>> getMessagesByContent(@RequestParam String content) {
        List<Message> messages = messageService.getMessagesByContent(content);
        return ResponseEntity.ok(messages);
    }
}