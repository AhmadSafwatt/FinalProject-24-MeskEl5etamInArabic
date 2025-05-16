package com.example.chatservice.seeders;

import com.example.chatservice.enums.MessageStatus;
import com.example.chatservice.enums.MessageType;
import com.example.chatservice.enums.ReportType;
import com.example.chatservice.models.Message;
import com.example.chatservice.services.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
public class MessageSeeder {

    private final static int BATCH_SIZE = 50;

    private final MessageService messageService;

    @Autowired
    public MessageSeeder(MessageService messageService) {
        this.messageService = messageService;
    }

    private LocalDateTime getRandomTimestamp() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusDays(30);
        long startEpoch = start.toEpochSecond(java.time.ZoneOffset.UTC);
        long endEpoch = now.toEpochSecond(java.time.ZoneOffset.UTC);
        long randomEpoch = ThreadLocalRandom.current().nextLong(startEpoch, endEpoch);
        return LocalDateTime.ofEpochSecond(randomEpoch, 0, java.time.ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
    }

    private Message createRandomMessage() {

        List<String> messages = List.of(
                "Hello, how are you?",
                "Check out this image!",
                "Here's a link to the dish.",
                "Your dish will be ready in 30 minutes.",
                "Happy birthday!",
                "This dish looks delicious!",
                "Om Ahmad's molokhia is the best!",
                "I love this dish!",
                "This is a great recipe!",
                "I can't wait to try this!",
                "This is my favorite dish!",
                "I think Sumaya is serving this dish.",
                "Make sure to eat this dish while it's hot.",
                "This dish is a must-try!",
                "I can't believe how good this is!",
                "The delivery was super fast!",
                "I love the presentation of this dish!",
                "This dish is a bit spicy for my taste.",
                "I think this dish is too salty.",
                "I recommend item 4 on the menu."
        );

        // Randomly select a message content from the list
        String randomContent = messages.get(new Random().nextInt(messages.size()));

        // Randomly select a message type from the enum
        MessageType[] messageTypes = MessageType.values();
        MessageType randomMessageType = messageTypes[new Random().nextInt(messageTypes.length)];

        // Randomly select a message status from the enum
        MessageStatus[] messageStatuses = MessageStatus.values();
        MessageStatus randomMessageStatus = messageStatuses[new Random().nextInt(messageStatuses.length)];

        boolean isReported = Math.random() < 0.5; // 50% chance to be reported
        ReportType randomReportReason = null;

        if (isReported) {
            randomReportReason = ReportType.values()[new Random().nextInt(ReportType.values().length)];
        }

        // Create a random message
        return new Message(
                randomMessageType,
                getRandomTimestamp(),
                randomMessageStatus,
                randomContent,
                isReported,
                randomReportReason
        );
    }


    public void seedMessages(int numberOfMessages) {
        List<Message> batch = new java.util.ArrayList<>(BATCH_SIZE);
        List<java.util.concurrent.CompletableFuture<Void>> futures = new java.util.ArrayList<>();
        int[] seeded = {0};

        for (int i = 0; i < numberOfMessages; i++) {
            batch.add(createRandomMessage());

            if (batch.size() == BATCH_SIZE || i == numberOfMessages - 1) {
                int batchSize = batch.size();
                futures.add(
                        messageService.saveAllMessagesAsync(new java.util.ArrayList<>(batch))
                                .thenRun(() -> log.debug("Seeded {} / {} messages", seeded[0] += batchSize, numberOfMessages))
                );
                batch.clear();
            }
        }

        CompletableFuture.allOf(futures.toArray(new java.util.concurrent.CompletableFuture[0])).join();
        int total = messageService.getMessages().size();
        log.info("Seeded {} messages. Total messages in the database: {}", numberOfMessages, total);
    }
}