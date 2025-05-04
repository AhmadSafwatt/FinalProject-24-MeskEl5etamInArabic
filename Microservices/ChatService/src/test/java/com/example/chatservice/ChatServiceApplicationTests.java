package com.example.chatservice;

import com.example.chatservice.commands.MarkMessageAsSeenCommand;
import com.example.chatservice.commands.SendMessageCommand;
import com.example.chatservice.commands.UpdateMessageCommand;
import com.example.chatservice.enums.MessageStatus;
import com.example.chatservice.enums.MessageType;
import com.example.chatservice.factories.MessageFactory;
import com.example.chatservice.models.Message;
import com.example.chatservice.repositories.MessageRepository;
import com.example.chatservice.services.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
class ChatServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MessageService messageService;

    @Autowired
    private MessageRepository messageRepository;

    private ObjectMapper objectMapper;
    private UUID senderId;
    private UUID receiverId;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        senderId = UUID.randomUUID();
        receiverId = UUID.randomUUID();
        messageService = new MessageService(messageRepository);
    }

    private Message createTestMessage(MessageType type) {
        return MessageFactory.createMessage(
                senderId,
                receiverId,
                "Test message content",
                type
        );
    }

    @Test
    void contextLoads() {
	}

    @Nested
    class MessageEndpointTests {
        @Test
        void testGetMessagesEndpoint() throws Exception {
            mockMvc.perform(MockMvcRequestBuilders.get("/messages"))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andDo(MockMvcResultHandlers.print());
        }

        @Test
        void testSaveMessageEndpoint_shouldReturnSavedMessage_whenValidMessage() throws Exception {
            Message message = createTestMessage(MessageType.IMAGE);

            String responseContent = mockMvc.perform(MockMvcRequestBuilders.post("/messages")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(message)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            Message returnedMessage = objectMapper.readValue(responseContent, Message.class);
            Message savedMessage = messageService.getMessageById(returnedMessage.getId());
            assertNotNull(savedMessage);
        }
    }

    @Nested
    class MessageSaveTests {
        @Test
        void testSaveMessageService_ShouldSaveDifferentTypesOfMessages() {
            int messageCountBefore = messageService.getMessages().size();

            Message textMessage = createTestMessage(MessageType.TEXT);
            Message productMessage = createTestMessage(MessageType.PRODUCT);

            SendMessageCommand textCommand = new SendMessageCommand(textMessage, messageService);
            SendMessageCommand productCommand = new SendMessageCommand(productMessage, messageService);

            textCommand.execute();
            productCommand.execute();

            assertNotNull(messageService.getMessages());
            assertEquals(messageCountBefore + 2, messageService.getMessages().size());
        }

        @Test
        void testSaveMessageService_shouldNotSaveMessage_whenMessageIsNull() {
            assertThrows(ResponseStatusException.class, () -> messageService.saveMessage(null));
        }

        @Test
        void testSaveMessageService_shouldNotSaveMessage_whenMessageIdIsNull() {
            Message message = createTestMessage(MessageType.TEXT);
            message.setId(null);

            assertThrows(ResponseStatusException.class, () -> messageService.saveMessage(message));
        }

        @Test
        void testSaveMessageEndpoint_shouldReturnConflict_whenMessageExists() throws Exception {
            Message message = createTestMessage(MessageType.TEXT);
            SendMessageCommand messageSender = new SendMessageCommand(message, messageService);
            messageSender.execute();

            mockMvc.perform(MockMvcRequestBuilders.post("/messages")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(message)))
                    .andExpect(MockMvcResultMatchers.status().isConflict())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
        }
    }

    @Nested
    class MessageDeletionTests {
        @Test
        void testDeleteMessage_shouldDeleteMessage_whenMessageExists() throws Exception {
            Message message = createTestMessage(MessageType.TEXT);

            SendMessageCommand messageSender = new SendMessageCommand(message, messageService);
            messageSender.execute();

            Message existingMessage = messageService.getMessageById(message.getId());
            assertNotNull(existingMessage);
            assertEquals(message.getId(), existingMessage.getId());

            mockMvc.perform(MockMvcRequestBuilders.delete("/messages/" + message.getId()))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andDo(MockMvcResultHandlers.print());

            assertNull(messageService.getMessageById(message.getId()));
        }

        @Test
        void testDeleteMessage_shouldReturnNotFound_whenMessageDoesNotExist() throws Exception {
            UUID nonExistingMessageId = UUID.randomUUID();

            mockMvc.perform(MockMvcRequestBuilders.delete("/messages/delete/" + nonExistingMessageId))
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andDo(MockMvcResultHandlers.print());
        }
    }


    @Nested
    class MessageUpdateTests {
        @Test
        void testUpdateMessage_shouldUpdateMessage_whenMessageExists() {
            Message message = createTestMessage(MessageType.TEXT);
            SendMessageCommand messageSender = new SendMessageCommand(message, messageService);
            messageSender.execute();


            UpdateMessageCommand updateMessageCommand = new UpdateMessageCommand(message, messageService);
            message.setContent("Updated content");
            updateMessageCommand.execute();

            Message updatedMessage = messageService.getMessageById(message.getId());
            assertNotNull(updatedMessage);
            assertEquals(message.getId(), updatedMessage.getId());
            assertEquals("Updated content", updatedMessage.getContent());
        }

        @Test
        void testUpdateMessageEndpoint_shouldReturnUpdatedMessage_whenValidMessage() throws Exception {
            Message message = createTestMessage(MessageType.TEXT);
            SendMessageCommand messageSender = new SendMessageCommand(message, messageService);
            messageSender.execute();

            message.setContent("Updated content");

            String responseContent = mockMvc.perform(MockMvcRequestBuilders.put("/messages/" + message.getId())
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(message)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            Message updatedMessage = objectMapper.readValue(responseContent, Message.class);
            assertEquals("Updated content", updatedMessage.getContent());
        }

        @Test
        void testUpdateMessageEndpoint_shouldReturnNotFound_whenMessageDoesNotExist() throws Exception {
            UUID nonExistingMessageId = UUID.randomUUID();
            Message message = createTestMessage(MessageType.TEXT);
            message.setId(nonExistingMessageId);

            mockMvc.perform(MockMvcRequestBuilders.put("/messages/" + nonExistingMessageId)
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(message)))
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
        }
    }


    @Nested
    class MessageStatusTests {
        @Test
        void testUpdateMessageStatus_shouldUpdateStatus_whenMessageExists() {
            Message message = createTestMessage(MessageType.TEXT);

            SendMessageCommand messageSender = new SendMessageCommand(message, messageService);
            messageSender.execute();

			int messageCountBefore = messageService.getMessages().size();

            message.setStatus(MessageStatus.SEEN);
            messageService.saveMessage(message);

            Message updatedMessage = messageService.getMessageById(message.getId());
            assertNotNull(updatedMessage);
            assertEquals(MessageStatus.SEEN, updatedMessage.getStatus());

			// Verify that the updated message was not saved again as a new message
			assertEquals(messageCountBefore, messageService.getMessages().size());
        }

        @Test
        void testIsMessageSeenEndpoint_shouldReturnTrue_whenMessageIsSeen() throws Exception {
            Message message = createTestMessage(MessageType.TEXT);

            MarkMessageAsSeenCommand markMessageAsSeenCommand = new MarkMessageAsSeenCommand(message, messageService);
            markMessageAsSeenCommand.execute();

            SendMessageCommand messageSender = new SendMessageCommand(message, messageService);
            messageSender.execute();

            mockMvc.perform(MockMvcRequestBuilders.get("/messages/" + message.getId() + "/seen"))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().string("true"))
                    .andDo(MockMvcResultHandlers.print());
        }

        @Test
        void testIsMessageSeenEndpoint_shouldReturnFalse_whenMessageIsNotSeen() throws Exception {
            Message message = createTestMessage(MessageType.TEXT);

            SendMessageCommand messageSender = new SendMessageCommand(message, messageService);
            messageSender.execute();


            mockMvc.perform(MockMvcRequestBuilders.get("/messages/" + message.getId() + "/seen"))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().string("false"))
                    .andDo(MockMvcResultHandlers.print());
        }

        @Test
        void testIsMessageSeenEndpoint_shouldReturnNotFound_whenMessageDoesNotExist() throws Exception {
            UUID nonExistingMessageId = UUID.randomUUID();

            mockMvc.perform(MockMvcRequestBuilders.get("/messages/" + nonExistingMessageId + "/seen"))
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andDo(MockMvcResultHandlers.print());
        }
    }
}