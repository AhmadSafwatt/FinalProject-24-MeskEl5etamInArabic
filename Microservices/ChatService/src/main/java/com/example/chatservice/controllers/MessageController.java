package com.example.chatservice.controllers;
import com.example.chatservice.models.Message;
import com.example.chatservice.services.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import com.example.chatservice.commands.SendMessageCommand;
import com.example.chatservice.commands.UpdateMessageCommand;
import com.example.chatservice.commands.DeleteMessageCommand;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
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
    public ResponseEntity<Message> createMessage(@RequestBody Message message) {

        if (message == null) {
            return ResponseEntity.badRequest().build();
        }

        Message existingMessage = messageService.getMessageById(message.getId());

        if (existingMessage != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        SendMessageCommand sendMessageCommand = new SendMessageCommand(message, messageService);
        sendMessageCommand.execute();
        return ResponseEntity.ok(message);
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
        return ResponseEntity.ok("Message deleted successfully");
    }

    /**
     * Update a message.
     *
     * @param id Message ID
     * @param message Message object
     * @return Updated message object
     */
    @PutMapping("/{id}")
    public ResponseEntity<Message> updateMessage(@PathVariable UUID id, @RequestBody Message message) {
        if (message == null || !id.equals(message.getId())) {
            return ResponseEntity.badRequest().build();
        }

        if (messageService.getMessageById(id) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Message updatedMessage = messageService.saveMessage(message);
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
}