package com.example.chatservice.seeders;

import com.example.chatservice.enums.MessageStatus;
import com.example.chatservice.enums.MessageType;
import com.example.chatservice.enums.ReportType;
import com.example.chatservice.factories.MessageFactory;
import com.example.chatservice.models.Message;
import com.example.chatservice.repositories.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import static com.example.chatservice.factories.MessageFactory.createMessage;

@Component
public class MessageSeeder {

    private final MessageRepository messageRepository;

    @Autowired
    public MessageSeeder(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    private Message createRandomMessage() {
        UUID sender = UUID.randomUUID();
        UUID receiver = UUID.randomUUID();

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
                "Make sure to eat this dish while it's hot."
        );

        Message message = createMessage(
                sender,
                receiver,
                messages.get(new Random().nextInt(messages.size())),
                MessageType.values()[(int) (Math.random() * MessageType.values().length)]
        );

        // Rnadomly set the message as reported or not
        if (Math.random() < 0.5) {
            message.setReported(true);
            ReportType reportType = ReportType.values()[(int) (Math.random() * ReportType.values().length)];
            message.setReportType(reportType);
        }

        // Randomly set the message status
        MessageStatus status = MessageStatus.values()[(int) (Math.random() * MessageStatus.values().length)];
        message.setStatus(status);

        return message;
    }

    public void seedMessages() {
        for (int i = 0; i < 50; i++) {
            Message message = createRandomMessage();
            messageRepository.save(message);
        }
    }
}