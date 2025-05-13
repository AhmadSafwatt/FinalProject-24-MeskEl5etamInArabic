package com.example.chatservice.seeders;

import com.example.chatservice.dtos.CreateMessageDTO;
import com.example.chatservice.dtos.UpdateMessageDTO;
import com.example.chatservice.enums.MessageStatus;
import com.example.chatservice.enums.MessageType;
import com.example.chatservice.models.Message;
import com.example.chatservice.services.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;
import java.util.UUID;

@Component
public class MessageSeeder {

    private final MessageService messageService;

    @Autowired
    public MessageSeeder(MessageService messageService) {
        this.messageService = messageService;
    }

    private CreateMessageDTO createRandomCreateMessageDTO() {
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


        return new CreateMessageDTO(
                sender,
                receiver,
                messages.get(new Random().nextInt(messages.size())),
                MessageType.values()[new Random().nextInt(MessageType.values().length)]
        );
    }


    public void seedMessages(int numberOfMessages) {
        for (int i = 0; i < numberOfMessages; i++) {
            CreateMessageDTO createMessageDTO = createRandomCreateMessageDTO();
            Message message = messageService.saveMessage(createMessageDTO);

            // Set half of the messages to a random status other than SENT
            if (Math.random() < 0.5) {
                UpdateMessageDTO updateMessageDTO = new UpdateMessageDTO();
                MessageStatus[] statuses = MessageStatus.values();
                MessageStatus randomStatus = statuses[new Random().nextInt(statuses.length - 1) + 1]; // Excludes SENT
                updateMessageDTO.setStatus(randomStatus);
                messageService.updateMessage(message.getId(), updateMessageDTO);
            }
        }
    }
}