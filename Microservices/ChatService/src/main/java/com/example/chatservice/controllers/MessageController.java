package com.example.chatservice.controllers;

import com.example.chatservice.commands.ReportMessageCommand;
import com.example.chatservice.dtos.CreateMessageDTO;
import com.example.chatservice.dtos.MessagePage;
import com.example.chatservice.dtos.UpdateMessageDTO;
import com.example.chatservice.enums.ReportType;
import com.example.chatservice.models.Message;
import com.example.chatservice.seeders.MessageSeeder;
import com.example.chatservice.services.MessageService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.query.CassandraPageRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
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
     * @return messages: field contains a list of messages,
     *         pagingState: field contains the next page state for pagination
     */

    @GetMapping
    public ResponseEntity<MessagePage> getMessages(
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String pagingState
    ) {
        log.info("Getting all messages from /messages endpoint");

        Pageable pageable = PageRequest.of(0, size);

        if (pagingState != null && !pagingState.isEmpty()) {
            try {
                ByteBuffer decoded = ByteBuffer.wrap(Base64.getDecoder().decode(pagingState));
                pageable = CassandraPageRequest.of(pageable, decoded);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(null);
            }
        }

        Slice<Message> slice = messageService.getMessages(pageable);

        String nextPageState = null;
        if (slice.hasNext() && slice.getPageable() instanceof CassandraPageRequest cassandraPageRequest) {
            ByteBuffer stateBuffer = cassandraPageRequest.getPagingState();
            if (stateBuffer != null) {
                byte[] bytes = new byte[stateBuffer.remaining()];
                stateBuffer.duplicate().get(bytes);
                nextPageState = URLEncoder.encode(Base64.getEncoder().encodeToString(bytes), StandardCharsets.UTF_8);
            }
        }

        MessagePage response = new MessagePage(slice.getContent(), nextPageState);

        log.info("Retrieved {} messages", slice.getNumberOfElements());

        return ResponseEntity.ok(response);
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
     * @return The created message object
     */
    @PostMapping
    public ResponseEntity<Message> createMessage(@Valid @RequestBody CreateMessageDTO createMessageDTO) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) authentication.getDetails();

        log.info("Creating message at /messages endpoint");
        Message createdMessage = messageService.createMessageEntrypoint(
                UUID.fromString(userId),
                createMessageDTO
        );
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

        messageService.deleteMessageByIdEntrypoint(id);

        log.info("Deleted message with ID {} at /messages/{} endpoint", id, id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Update a message.
     *
     * @param id      Message ID
     * @param updateMessageDTO UpdateMessageDTO object
     * @return The updated message object
     */
    @PatchMapping("/{id}")
    public ResponseEntity<Message> updateMessage(@PathVariable UUID id, @Valid @RequestBody UpdateMessageDTO updateMessageDTO) {
        log.info("Updating message with ID {} at /messages/{} endpoint {}", id, id, updateMessageDTO);

        Message updatedMessage = messageService.updateMessageEntrypoint(id, updateMessageDTO);
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

    /**
     * Seeds the database with a specified number of messages.
     *
     * @param count The number of messages to seed
     */

    @GetMapping("/seed")
    public ResponseEntity<String> seedMessages(@RequestParam(defaultValue = "50") int count) {
        log.info("Seeding messages at /messages/seed endpoint with count={}", count);
        long startTime = System.currentTimeMillis();
        messageSeeder.seedMessages(count);
        log.info("Seeded messages at /messages/seed endpoint");
        long endTime = System.currentTimeMillis();
        return ResponseEntity.ok("Seeded " + count + " messages in " + (endTime - startTime) + " ms");
    }

    /**
     * Clear all messages.
     */
    @DeleteMapping
    public ResponseEntity<String> clearMessages() {
        log.info("Deleting all messages at /messages endpoint");
        messageService.deleteAllMessages();
        log.info("Deleted all messages at /messages endpoint");
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Message>> getMessagesByContent(@RequestParam String content) {

        List<Message> messages = messageService.searchMessagesByContent(content);
        return ResponseEntity.ok(messages);
    }

    /**
     * Endpoint to report a message.
     *
     * @param messageId Message ID
     * @param reportType Report type
     */
    @PatchMapping("/report/{id}")
    public ResponseEntity<?> reportMessage(@PathVariable("id") UUID messageId, @RequestParam(required = false) ReportType reportType) {

        if (reportType == null) {
            return ResponseEntity
                    .badRequest()
                    .body("reportType parameter is required and must be a valid value.");
        }

        Message message = messageService.getMessageById(messageId);
        if (message == null) {
            return ResponseEntity
                    .status(404)
                    .body("Message with ID " + messageId + " not found.");
        }

        ReportMessageCommand reportCommand = new ReportMessageCommand(messageId, reportType, messageService);
        reportCommand.execute();

        return ResponseEntity.ok(messageService.getMessageById(messageId));
    }


}