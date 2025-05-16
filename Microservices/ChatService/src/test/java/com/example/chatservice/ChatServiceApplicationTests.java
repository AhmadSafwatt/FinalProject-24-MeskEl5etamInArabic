package com.example.chatservice;

import com.example.chatservice.commands.MarkMessageAsSeenCommand;
import com.example.chatservice.commands.SendMessageCommand;
import com.example.chatservice.commands.UpdateMessageCommand;
import com.example.chatservice.dtos.CreateMessageDTO;
import com.example.chatservice.dtos.UpdateMessageDTO;
import com.example.chatservice.enums.MessageStatus;
import com.example.chatservice.enums.MessageType;
import com.example.chatservice.models.Message;
import com.example.chatservice.repositories.MessageRepository;
import com.example.chatservice.services.MessageService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.server.ResponseStatusException;

import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@EnableAutoConfiguration
class ChatServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MessageService messageService;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private CassandraTemplate cassandraTemplate;

    private ObjectMapper objectMapper;
    private UUID senderId;
    private UUID receiverId;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        senderId = UUID.randomUUID();
        receiverId = UUID.randomUUID();
        messageService = new MessageService(messageRepository, cassandraTemplate);
    }

    private CreateMessageDTO createTestCreateMessageDTO(MessageType type) {
        return CreateMessageDTO.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .content("Test content")
                .type(type)
                .build();
    }

    private UpdateMessageDTO createTestUpdateMessageDTO() {
        return UpdateMessageDTO.builder().build();
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
        void testGetMessageByIdEndpoint_shouldReturnMessage_whenMessageExists() throws Exception {
            CreateMessageDTO createMessageDTO = createTestCreateMessageDTO(MessageType.TEXT);
            SendMessageCommand messageSender = new SendMessageCommand(createMessageDTO, messageService);
            Message message = messageSender.execute();

            String responseContent = mockMvc.perform(MockMvcRequestBuilders.get("/messages/" + message.getId()))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andDo(MockMvcResultHandlers.print())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            JsonNode responseJson = objectMapper.readTree(responseContent);
            assertEquals(message.getId().toString(), responseJson.get("id").asText());
            assertEquals(message.getSenderId().toString(), responseJson.get("senderId").asText());
            assertEquals(message.getReceiverId().toString(), responseJson.get("receiverId").asText());
            assertEquals(message.getContent(), responseJson.get("content").asText());
            assertEquals(message.getType().toString(), responseJson.get("type").asText());
            assertEquals(message.getStatus().toString(), responseJson.get("status").asText());
            assertNotNull(responseJson.get("timestamp").asText());
        }

        @Test
        void testGetMessageByIdEndpoint_shouldReturnNotFound_whenMessageDoesNotExist() throws Exception {
            UUID nonExistingMessageId = UUID.randomUUID();

            mockMvc.perform(MockMvcRequestBuilders.get("/messages/" + nonExistingMessageId))
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andDo(MockMvcResultHandlers.print());
        }
    }

    @Nested
    class MessageSaveTests {
        @Test
        void testSaveMessageService_ShouldSaveDifferentTypesOfMessages() {
            int messageCountBefore = messageService.getMessages().size();

            CreateMessageDTO textMessageDTO = createTestCreateMessageDTO(MessageType.TEXT);
            SendMessageCommand messageSender = new SendMessageCommand(textMessageDTO, messageService);
            Message textMessage = messageSender.execute();



            CreateMessageDTO productMessageDTO = createTestCreateMessageDTO(MessageType.PRODUCT);
            SendMessageCommand productMessageSender = new SendMessageCommand(productMessageDTO, messageService);
            Message productMessage = productMessageSender.execute();


            assertNotNull(messageService.getMessages());
            assertNotNull(messageService.getMessageById(textMessage.getId()));
            assertNotNull(messageService.getMessageById(productMessage.getId()));
            assertEquals(messageCountBefore + 2, messageService.getMessages().size());
        }

        @Test
        void testSaveMessageEndpoint_shouldReturnBadRequest_whenMessageIsNull() throws Exception {
            CreateMessageDTO nullMessage = null;

            mockMvc.perform(MockMvcRequestBuilders.post("/messages")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(nullMessage)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andDo(MockMvcResultHandlers.print());
        }

        @Test
        void testSaveMessageEndpoint_shouldSaveMessage_WhenValidMessage() throws Exception {

            CreateMessageDTO message = createTestCreateMessageDTO(MessageType.TEXT);

            String responseContent = mockMvc.perform(MockMvcRequestBuilders.post("/messages")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(message)))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            JsonNode jsonNode = objectMapper.readTree(responseContent);
            String id = jsonNode.path("id").asText();

            Message savedMessage = messageService.getMessageById(UUID.fromString(id));
            assertNotNull(savedMessage);
        }

        @Test
        void testSaveMessageEndpoint_shouldReturnBadRequest_whenContentIsNull() throws Exception {
            CreateMessageDTO invalidMessage = createTestCreateMessageDTO(MessageType.TEXT);
            invalidMessage.setContent(null);

            mockMvc.perform(MockMvcRequestBuilders.post("/messages")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(invalidMessage)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andDo(MockMvcResultHandlers.print());
        }

        @Test
        void testSaveMessageEndpoint_shouldReturnBadRequest_whenContentIsEmpty() throws Exception {
            CreateMessageDTO invalidMessage = createTestCreateMessageDTO(MessageType.TEXT);
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
            CreateMessageDTO validMessageDTO = createTestCreateMessageDTO(MessageType.TEXT);
            validMessageDTO.setContent(validContent);

            mockMvc.perform(MockMvcRequestBuilders.post("/messages")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(validMessageDTO)))
                    .andExpect(MockMvcResultMatchers.status().isCreated())
                    .andDo(MockMvcResultHandlers.print());
        }

        @Test
        void testSaveMessageEndpoint_shouldReturnBadRequest_whenContentIsTooLong() throws Exception {
            String longContent = "a".repeat(501); // 501 characters
            CreateMessageDTO invalidMessage = createTestCreateMessageDTO(MessageType.TEXT);
            invalidMessage.setContent(longContent);

            mockMvc.perform(MockMvcRequestBuilders.post("/messages")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(invalidMessage)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andDo(MockMvcResultHandlers.print());
        }

        @Test
        void testSaveMessageEndpoint_shouldReturnBadRequest_whenSenderIdIsNull() throws Exception {
            CreateMessageDTO invalidMessage = createTestCreateMessageDTO(MessageType.TEXT);
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
            CreateMessageDTO createMessageDTO = createTestCreateMessageDTO(MessageType.TEXT);

            SendMessageCommand messageSender = new SendMessageCommand(createMessageDTO, messageService);
            Message message = messageSender.execute();

            Message existingMessage = messageService.getMessageById(message.getId());
            assertNotNull(existingMessage);
            assertEquals(message.getId(), existingMessage.getId());

            mockMvc.perform(MockMvcRequestBuilders.delete("/messages/" + message.getId()))
                    .andExpect(MockMvcResultMatchers.status().isNoContent())
                    .andDo(MockMvcResultHandlers.print());

            assertThrows(ResponseStatusException.class, () -> messageService.getMessageById(message.getId()));
        }

        @Test
        void testDeleteMessage_shouldReturnNotFound_whenMessageDoesNotExist() throws Exception {
            UUID nonExistingMessageId = UUID.randomUUID();

            mockMvc.perform(MockMvcRequestBuilders.delete("/messages/" + nonExistingMessageId))
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andDo(MockMvcResultHandlers.print());
        }

//        @Test
//        void testQueryMessageByContentEndpoint_shouldReturnMessages_whenContentMatches() throws Exception {
//            CreateMessageDTO createMessageDTO = createTestCreateMessageDTO(MessageType.TEXT);
//            createMessageDTO.setContent("Query");
//
//            SendMessageCommand messageSender = new SendMessageCommand(createMessageDTO, messageService);
//            messageSender.execute();
//
//            mockMvc.perform(MockMvcRequestBuilders.get("/messages/search/query?content=Query"))
//                    .andExpect(MockMvcResultMatchers.status().isOk())
//                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].content").value("Query"))
//                    .andDo(MockMvcResultHandlers.print());
//        }
    }


    @Nested
    class MessageUpdateTests {
        @Test
        void testUpdateMessage_shouldUpdateMessage_whenMessageExists() {
            UpdateMessageDTO updateMessageDTO = createTestUpdateMessageDTO();
            updateMessageDTO.setContent("Updated content");
            updateMessageDTO.setStatus(MessageStatus.DELIVERED);

            CreateMessageDTO createMessageDTO = createTestCreateMessageDTO(MessageType.TEXT);
            SendMessageCommand messageSender = new SendMessageCommand(createMessageDTO, messageService);
            Message message = messageSender.execute();

            UpdateMessageCommand updateMessageCommand = new UpdateMessageCommand(message.getId(), updateMessageDTO, messageService);
            Message updatedMessage = updateMessageCommand.execute();
            assertNotNull(updatedMessage);
            assertEquals(message.getId(), updatedMessage.getId());
            assertEquals("Updated content", updatedMessage.getContent());
            assertEquals(MessageStatus.DELIVERED, updatedMessage.getStatus());
        }

        @Test
        void testUpdateMessageEndpoint_shouldReturnUpdatedMessage_whenValidMessage() throws Exception {
            CreateMessageDTO createMessageDTO = createTestCreateMessageDTO(MessageType.TEXT);
            SendMessageCommand messageSender = new SendMessageCommand(createMessageDTO, messageService);
            Message message = messageSender.execute();

            UpdateMessageDTO productMessageDTO = createTestUpdateMessageDTO();
            productMessageDTO.setType(MessageType.PRODUCT);

            mockMvc.perform(MockMvcRequestBuilders.patch("/messages/" + message.getId())
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(productMessageDTO))) // Use partialMessage here
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
            CreateMessageDTO createMessageDTO = createTestCreateMessageDTO(MessageType.TEXT);
            SendMessageCommand messageSender = new SendMessageCommand(createMessageDTO, messageService);
            Message message = messageSender.execute();

            UpdateMessageDTO textMessageDTO = createTestUpdateMessageDTO();
            textMessageDTO.setContent("Updated content");
            textMessageDTO.setType(MessageType.TEXT);

          
            int messageCountBefore = messageService.getMessages().size();

          
            String responseContent = mockMvc.perform(MockMvcRequestBuilders.patch("/messages/" + message.getId())
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(textMessageDTO)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            Message responseMessage = objectMapper.readValue(responseContent, Message.class);

            assertEquals(message.getId(), responseMessage.getId());
            assertEquals(textMessageDTO.getType(), responseMessage.getType());
            assertEquals(textMessageDTO.getContent(), responseMessage.getContent());
            assertEquals(messageCountBefore, messageService.getMessages().size());

        }

        @Test
        void testUpdateMessageEndpoint_shouldNotUpdateSenderId() throws Exception {
            CreateMessageDTO createMessageDTO = createTestCreateMessageDTO(MessageType.TEXT);
            SendMessageCommand messageSender = new SendMessageCommand(createMessageDTO, messageService);
            Message message = messageSender.execute();

            UpdateMessageDTO productMessageDTO = createTestUpdateMessageDTO();
            productMessageDTO.setType(MessageType.IMAGE);

            mockMvc.perform(MockMvcRequestBuilders.patch("/messages/" + message.getId())
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(productMessageDTO)))
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

            CreateMessageDTO createMessageDTO = createTestCreateMessageDTO(MessageType.TEXT);

            mockMvc.perform(MockMvcRequestBuilders.patch("/messages/" + nonExistingMessageId)
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(createMessageDTO)))
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();
        }

        @Test
        void testUpdateMessageEndpoint_shouldNotUpdateMessageTimestamp_whenUpdateSucceeds() throws Exception {

            CreateMessageDTO createMessageDTO = createTestCreateMessageDTO(MessageType.TEXT);
            SendMessageCommand messageSender = new SendMessageCommand(createMessageDTO, messageService);
            Message message = messageSender.execute();

            Thread.sleep(10);

            UpdateMessageDTO productMessageDTO = createTestUpdateMessageDTO();
            productMessageDTO.setContent("Updated content");

            mockMvc.perform(MockMvcRequestBuilders.patch("/messages/" + message.getId())
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(productMessageDTO)))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            Thread.sleep(10);
            Message retrievedMessage = messageService.getMessageById(message.getId());
            assertNotNull(retrievedMessage);
            assertEquals(message.getTimestamp().truncatedTo(ChronoUnit.MILLIS),
                    retrievedMessage.getTimestamp().truncatedTo(ChronoUnit.MILLIS));
            assertEquals("Updated content", retrievedMessage.getContent());
        }

        @Test
        void testUpdateMessageEndpoint_shouldNotUpdateMessageTimestamp_whenUpdateFails() throws Exception {
            CreateMessageDTO createMessageDTO = createTestCreateMessageDTO(MessageType.TEXT);
            SendMessageCommand messageSender = new SendMessageCommand(createMessageDTO, messageService);
            Message message = messageSender.execute();

            Thread.sleep(10);

            // Simulate a failure in the update process
            mockMvc.perform(MockMvcRequestBuilders.patch("/messages/" + message.getId())
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(null)))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            Thread.sleep(10);
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

            CreateMessageDTO createMessageDTO = createTestCreateMessageDTO(MessageType.TEXT);
            SendMessageCommand messageSender = new SendMessageCommand(createMessageDTO, messageService);
            Message message = messageSender.execute();


            UpdateMessageDTO productMessage = createTestUpdateMessageDTO();

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
            CreateMessageDTO createMessageDTO = createTestCreateMessageDTO(MessageType.TEXT);
            SendMessageCommand messageSender = new SendMessageCommand(createMessageDTO, messageService);
            Message message = messageSender.execute();

            MarkMessageAsSeenCommand markMessageAsSeenCommand = new MarkMessageAsSeenCommand(message.getId(), messageService);
            markMessageAsSeenCommand.execute();


            mockMvc.perform(MockMvcRequestBuilders.get("/messages/" + message.getId() + "/seen"))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.content().string("true"))
                    .andDo(MockMvcResultHandlers.print());
        }

        @Test
        void testIsMessageSeenEndpoint_shouldReturnFalse_whenMessageIsNotSeen() throws Exception {
            CreateMessageDTO createMessageDTO = createTestCreateMessageDTO(MessageType.TEXT);
            SendMessageCommand messageSender = new SendMessageCommand(createMessageDTO, messageService);
            Message message = messageSender.execute();

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

        @Test
        void testMarkMessageAsSeenEndpoint_shouldReturnOkWhenMessageNotSeen_andMarkItAsSeen() throws Exception {
            CreateMessageDTO createMessageDTO = createTestCreateMessageDTO(MessageType.TEXT);
            SendMessageCommand messageSender = new SendMessageCommand(createMessageDTO, messageService);
            Message message = messageSender.execute();

            mockMvc.perform(MockMvcRequestBuilders.patch("/messages/" + message.getId() + "/seen"))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andDo(MockMvcResultHandlers.print());
        }

        @Test
        void testMarkMessageAsSeenEndpoint_shouldReturnBadRequest_WhenMessageAlreadySeen() throws Exception {
            CreateMessageDTO createMessageDTO = createTestCreateMessageDTO(MessageType.TEXT);
            SendMessageCommand messageSender = new SendMessageCommand(createMessageDTO, messageService);
            Message message = messageSender.execute();

            MarkMessageAsSeenCommand markMessageAsSeenCommand = new MarkMessageAsSeenCommand(message.getId(), messageService);
            markMessageAsSeenCommand.execute();

            mockMvc.perform(MockMvcRequestBuilders.patch("/messages/" + message.getId() + "/seen"))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andDo(MockMvcResultHandlers.print());
        }

    }
}