package com.example.chatservice;

import com.example.chatservice.commands.MarkMessageAsSeenCommand;
import com.example.chatservice.commands.SendMessageCommand;
import com.example.chatservice.commands.UpdateMessageCommand;
import com.example.chatservice.enums.MessageStatus;
import com.example.chatservice.enums.MessageType;
import com.example.chatservice.factories.MessageFactory;
import com.example.chatservice.models.Message;
import com.example.chatservice.models.TextMessage;
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

import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

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
        void testSaveMessageEndpoint_shouldSaveMessage_whenIdIsNull() throws Exception {
            Message message = createTestMessage(MessageType.TEXT);
            message.setId(null);

            String responseContent = mockMvc.perform(
                            MockMvcRequestBuilders.post("/messages")
                                    .contentType("application/json")
                                    .content(objectMapper.writeValueAsString(message)))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            Message savedMessage = objectMapper.readValue(responseContent, Message.class);
            assertNotNull(savedMessage.getId());
        }

        @Test
        void testSaveMessageEndpoint_ShouldSaveMessageAndIgnoreId() throws Exception {
            Message message = createTestMessage(MessageType.TEXT);
            message.setId(UUID.randomUUID());

            String responseContent = mockMvc.perform(
                            MockMvcRequestBuilders.post("/messages")
                                    .contentType("application/json")
                                    .content(objectMapper.writeValueAsString(message)))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            Message savedMessage = objectMapper.readValue(responseContent, Message.class);
            assertNotEquals(message.getId(), savedMessage.getId());
        }

        @Test
        void testSaveMessageEndpoint_shouldReturnBadRequest_whenMessageIsNull() throws Exception {
            Message nullMessage = null;

            mockMvc.perform(MockMvcRequestBuilders.post("/messages")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(nullMessage)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andDo(MockMvcResultHandlers.print());
        }

        @Test
        void testSaveMessageEndpoint_shouldSaveMessage_WhenValidMessage() throws Exception {
            // Mock the repository
            MessageRepository mockMessageRepository = org.mockito.Mockito.mock(MessageRepository.class);
            messageService = new MessageService(mockMessageRepository);

            Message message = createTestMessage(MessageType.IMAGE);

            // Mock save and findById behavior
            when(mockMessageRepository.save(message)).thenReturn(message);
            when(mockMessageRepository.findById(message.getId())).thenReturn(java.util.Optional.of(message));

            mockMvc.perform(MockMvcRequestBuilders.post("/messages")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(message)))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            Message savedMessage = messageService.getMessageById(message.getId());
            assertNotNull(savedMessage);
            assertEquals(message.getId(), savedMessage.getId());
        }

        @Test
        void testSaveMessageEndpoint_shouldSaveMessage_whenMessageExists() throws Exception {
            // Mock the repository
            MessageRepository mockMessageRepository = org.mockito.Mockito.mock(MessageRepository.class);
            messageService = new MessageService(mockMessageRepository);

            Message message = createTestMessage(MessageType.TEXT);

            // Mock save and findById behavior
            when(mockMessageRepository.save(message)).thenReturn(message);
            when(mockMessageRepository.findById(message.getId())).thenReturn(java.util.Optional.of(message));

            mockMvc.perform(MockMvcRequestBuilders.post("/messages")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(message)))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andDo(MockMvcResultHandlers.print());

            Message savedMessage = messageService.getMessageById(message.getId());
            assertNotNull(savedMessage);
            assertEquals(message.getId(), savedMessage.getId());
        }

        @Test
        void testSaveMessageEndpoint_shouldReturnBadRequest_whenContentIsNull() throws Exception {
            Message invalidMessage = createTestMessage(MessageType.TEXT);
            invalidMessage.setContent(null);

            mockMvc.perform(MockMvcRequestBuilders.post("/messages")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(invalidMessage)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andDo(MockMvcResultHandlers.print());
        }

        @Test
        void testSaveMessageEndpoint_shouldReturnBadRequest_whenContentIsEmpty() throws Exception {
            Message invalidMessage = createTestMessage(MessageType.TEXT);
            invalidMessage.setContent("");

            mockMvc.perform(MockMvcRequestBuilders.post("/messages")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(invalidMessage)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andDo(MockMvcResultHandlers.print());
        }

        @Test
        void testSaveMessageEndpoint_shouldSaveMesage_whenContentWithinLimit() throws Exception {
            String validContent = "a".repeat(500); // 500 characters
            Message validMessage = createTestMessage(MessageType.TEXT);
            validMessage.setContent(validContent);

            mockMvc.perform(MockMvcRequestBuilders.post("/messages")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(validMessage)))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andDo(MockMvcResultHandlers.print());
        }

        @Test
        void testSaveMessageEndpoint_shouldReturnBadRequest_whenContentIsTooLong() throws Exception {
            String longContent = "a".repeat(501); // 501 characters
            Message invalidMessage = createTestMessage(MessageType.TEXT);
            invalidMessage.setContent(longContent);

            mockMvc.perform(MockMvcRequestBuilders.post("/messages")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(invalidMessage)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andDo(MockMvcResultHandlers.print());
        }

        @Test
        void testSaveMessageEndpoint_shouldReturnBadRequest_whenSenderIdIsNull() throws Exception {
            Message invalidMessage = createTestMessage(MessageType.TEXT);
            invalidMessage.setSenderId(null);

            mockMvc.perform(MockMvcRequestBuilders.post("/messages")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(invalidMessage)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andDo(MockMvcResultHandlers.print());
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
                    .andExpect(MockMvcResultMatchers.status().isNoContent())
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
            message.setContent("Initial content");
            message.setStatus(MessageStatus.DELIVERED);

            SendMessageCommand messageSender = new SendMessageCommand(message, messageService);
            messageSender.execute();

            TextMessage textMessage = new TextMessage();
            textMessage.setContent("Updated content");


            UpdateMessageCommand updateMessageCommand = new UpdateMessageCommand(message.getId(), textMessage, messageService);
            updateMessageCommand.execute();

            Message updatedMessage = messageService.getMessageById(message.getId());
            assertNotNull(updatedMessage);
            assertEquals(message.getId(), updatedMessage.getId());
            assertEquals("Updated content", updatedMessage.getContent());
            assertEquals(MessageStatus.DELIVERED, updatedMessage.getStatus());
        }

        @Test
        void testUpdateMessageEndpoint_shouldReturnUpdatedMessage_whenValidMessage() throws Exception {
            Message message = createTestMessage(MessageType.TEXT);
            SendMessageCommand messageSender = new SendMessageCommand(message, messageService);
            messageSender.execute();

            Message productMessage = createTestMessage(MessageType.PRODUCT);

            mockMvc.perform(MockMvcRequestBuilders.patch("/messages/" + message.getId())
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(productMessage))) // Use partialMessage here
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            Message updatedMessage = messageService.getMessageById(message.getId());
            assertEquals(MessageType.PRODUCT, updatedMessage.getType());
            assertEquals(message.getId(), updatedMessage.getId());
        }

        @Test
        void testUpdateMessageEndpoint_ShouldNotUpdateMessageType_WhenTypeIsNotChanged() throws Exception {
            Message message = createTestMessage(MessageType.TEXT);
            SendMessageCommand messageSender = new SendMessageCommand(message, messageService);
            messageSender.execute();

            Message textMessage = createTestMessage(MessageType.TEXT);
            textMessage.setContent("Updated content");

            String responseContent = mockMvc.perform(MockMvcRequestBuilders.patch("/messages/" + message.getId())
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(textMessage)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            Message updatedMessage = objectMapper.readValue(responseContent, Message.class);
            assertEquals(message.getType(), updatedMessage.getType());
            assertEquals("Updated content", updatedMessage.getContent());
            assertNotEquals(message.getContent(), updatedMessage.getContent());
        }

        @Test
        void testUpdateMessageEndpoint_shouldNotUpdateSenderId() throws Exception {
            Message message = createTestMessage(MessageType.TEXT);
            SendMessageCommand messageSender = new SendMessageCommand(message, messageService);
            messageSender.execute();

            Message imageMessage = createTestMessage(MessageType.IMAGE);

            mockMvc.perform(MockMvcRequestBuilders.patch("/messages/" + message.getId())
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(imageMessage)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            Message updatedMessage = messageService.getMessageById(message.getId());
            assertEquals(message.getSenderId(), updatedMessage.getSenderId());
            assertNotEquals(message.getType(), updatedMessage.getType());
            assertEquals(MessageType.IMAGE, updatedMessage.getType());
        }

        @Test
        void testUpdateMessageEndpoint_shouldReturnNotFound_whenMessageDoesNotExist() throws Exception {
            UUID nonExistingMessageId = UUID.randomUUID();
            Message message = createTestMessage(MessageType.TEXT);
            message.setId(nonExistingMessageId);

            mockMvc.perform(MockMvcRequestBuilders.patch("/messages/" + nonExistingMessageId)
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(message)))
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
        }

        @Test
        void testUpdateMessageEndpoint_shouldUpdateMessageTimestamp_whenUpdateSucceeds() throws Exception {
            Message message = createTestMessage(MessageType.TEXT);
            SendMessageCommand messageSender = new SendMessageCommand(message, messageService);
            messageSender.execute();

            Thread.sleep(500);

            Message updatedMessage = new TextMessage();
            updatedMessage.setContent("Updated content");

            mockMvc.perform(MockMvcRequestBuilders.patch("/messages/" + message.getId())
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(updatedMessage)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            Thread.sleep(500);
            Message retrievedMessage = messageService.getMessageById(message.getId());
            assertNotNull(retrievedMessage);
            assertNotEquals(message.getTimestamp().truncatedTo(ChronoUnit.MILLIS),
                    retrievedMessage.getTimestamp().truncatedTo(ChronoUnit.MILLIS));
            assertEquals("Updated content", retrievedMessage.getContent());
        }

        @Test
        void testUpdateMessageEndpoint_shouldNotUpdateMessageTimestamp_whenUpdateFails() throws Exception {
            Message message = createTestMessage(MessageType.TEXT);
            SendMessageCommand messageSender = new SendMessageCommand(message, messageService);
            messageSender.execute();

            Thread.sleep(500);

            // Simulate a failure in the update process
            mockMvc.perform(MockMvcRequestBuilders.patch("/messages/" + message.getId())
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(null)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            Thread.sleep(500);
            Message updatedMessage = messageService.getMessageById(message.getId());
            assertEquals(
                    message.getTimestamp().truncatedTo(ChronoUnit.MILLIS),
                    updatedMessage.getTimestamp().truncatedTo(ChronoUnit.MILLIS)
            );
        }
    }


    @Nested
    class MessageStatusTests {
        @Test
        void testUpdateMessageStatus_shouldUpdateStatus_whenMessageExists() {
            Message message = createTestMessage(MessageType.TEXT);

            SendMessageCommand messageSender = new SendMessageCommand(message, messageService);
            messageSender.execute();

            Message productMessage = createTestMessage(MessageType.PRODUCT);

			int messageCountBefore = messageService.getMessages().size();

            productMessage.setStatus(MessageStatus.DELIVERED);
            messageService.updateMessage(message.getId(), productMessage);

            Message updatedMessage = messageService.getMessageById(message.getId());
            assertNotNull(updatedMessage);
            assertEquals(MessageStatus.DELIVERED, updatedMessage.getStatus());

			// Verify that the updated message was not saved again as a new message
			assertEquals(messageCountBefore, messageService.getMessages().size());
        }

        @Test
        void testIsMessageSeenEndpoint_shouldReturnTrue_whenMessageIsSeen() throws Exception {
            Message message = createTestMessage(MessageType.TEXT);

            MarkMessageAsSeenCommand markMessageAsSeenCommand = new MarkMessageAsSeenCommand(message, messageService);
            markMessageAsSeenCommand.execute();


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