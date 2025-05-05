package com.example.chatservice.controllers;

import com.example.chatservice.commands.*;
import com.example.chatservice.enums.ReportType;
import com.example.chatservice.models.Message;
import com.example.chatservice.services.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
     * Endpoint to get all messages.
     *
     * @return List of messages
     */
    @GetMapping("/")
    public List<Message> getMessages() {
        return messageService.getMessages();
    }

    /**
     * Endpoint to get a message by its ID.
     *
     * @param id Message ID
     * @return Message object
     */
    @GetMapping("/{id}")
    public Message getMessageById(UUID id) {
        return messageService.getMessageById(id);
    }

    /**
     * Endpoint to save a new message.
     *
     * @param message Message object
     * @return Saved message object
     */
    @PostMapping("/save")
    public Message saveMessage(Message message) {
        SendMessageCommand sendMessageCommand = new SendMessageCommand(message, messageService);
        sendMessageCommand.execute();
        return message;
    }

    /**
     * Endpoint to delete a message by its ID.
     *
     * @param id Message ID
     */
    @DeleteMapping("/delete/{id}")
    public void deleteMessage(@PathVariable UUID id) {
        DeleteMessageCommand deleteMessageCommand = new DeleteMessageCommand(id, messageService);
        deleteMessageCommand.execute();
    }


    /**
     * Endpoint to update a message.
     *
     * @param message Message object
     * @return Updated message object
     */

    @PutMapping("/update")
    public Message updateMessage(@RequestBody Message message) {
        UpdateMessageCommand updateMessageCommand = new UpdateMessageCommand(message, messageService);
        updateMessageCommand.execute();
        return message;
    }

    /**
     * Endpoint to check if a message has been seen.
     *
     * @param messageId Message ID
     * @return true if the message is seen, false otherwise
     */

    @GetMapping("/seen/{messageId}")
    public boolean isMessageSeen(@PathVariable UUID messageId) {
        return messageService.isMessageSeen(messageId);
    }

    /**
     * Endpoint to report a message.
     *
     * @param messageId Message ID
     * @param reportType Report type
     */
    @PatchMapping("/report/{id}")
    public Message reportMessage(@PathVariable UUID messageId, @RequestParam ReportType reportType) {
        ReportMessageCommand reportCommand = new ReportMessageCommand(messageId, reportType, messageService);
        reportCommand.execute();
        return messageService.getMessageById(messageId);
    }

}
