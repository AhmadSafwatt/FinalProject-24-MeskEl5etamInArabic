package com.example.chatservice;

import com.example.chatservice.dtos.CreateMessageDTO;
import com.example.chatservice.enums.MessageType;
import com.example.chatservice.models.Message;
import com.example.chatservice.repositories.MessageRepository;
import com.example.chatservice.services.MessageService;
import com.example.chatservice.utils.JwtUtil;
import com.example.chatservice.utils.TokenUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureMockMvc
public class ChatServiceAuthTests {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private MessageService messageService;

    @Autowired
    private CassandraTemplate cassandraTemplate;

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Getter
    private static String testToken;

    @BeforeAll
    static void initToken() {
        UUID testSenderId = UUID.randomUUID();
        testToken = TokenUtil.generateTestToken(testSenderId);
    }

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        messageService = new MessageService(messageRepository, cassandraTemplate);
    }

    private CreateMessageDTO createTestCreateMessageDTO(MessageType type) {
        return CreateMessageDTO.builder()
                .receiverId(UUID.randomUUID())
                .content("Test message content")
                .type(type)
                .build();
    }


    @Test
    void testHomeEndpoint_shouldBeAccessible_withoutJWTToken() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void testSaveMessageEndpoint_shouldSaveMesage_whenContentWithinLimit() throws Exception {
        String validContent = "a".repeat(500);
        CreateMessageDTO validMessageDTO = createTestCreateMessageDTO(MessageType.TEXT);
        validMessageDTO.setContent(validContent);


        String responseContent = mockMvc.perform(MockMvcRequestBuilders.post("/messages")
                        .header("Authorization", "Bearer " + testToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(validMessageDTO)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andDo(MockMvcResultHandlers.print())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(responseContent);

        String id = jsonNode.path("id").asText();
        String userId = JwtUtil.extractUserId(testToken);

        Message savedMessage = messageService.getMessageById(UUID.fromString(id));
        assertNotNull(savedMessage);
        assertEquals(savedMessage.getSenderId().toString(), userId);

    }

    @Test
    void testSaveMessageEndpoint_shouldSaveMessage_WhenValidMessage() throws Exception {

        CreateMessageDTO message = createTestCreateMessageDTO(MessageType.IMAGE);

        String responseContent = mockMvc.perform(MockMvcRequestBuilders.post("/messages")
                        .header("Authorization", "Bearer " + testToken)
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
    void testSaveMessageEndpoint_shouldReturnUnauthorized_WhenNoToken() throws Exception {
        CreateMessageDTO message = createTestCreateMessageDTO(MessageType.PRODUCT);

        mockMvc.perform(MockMvcRequestBuilders.post("/messages")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(message)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void testSaveMessageEndpoint_shouldReturnUnauthorized_whenTokenInvalid() throws Exception {
        String invalidToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJPbmxpbmUgSldUIEJ1aWxkZXIiLCJpYXQiOjE3NDczOTE4MzQsImV4cCI6MTc3ODkyNzgzNCwiYXVkIjoid3d3LmV4YW1wbGUuY29tIiwic3ViIjoianJvY2tldEBleGFtcGxlLmNvbSIsImlkIjoiMWY3YjhjMjctMzU3Yy00OTUzLThlZTQtMTQ3NjI3ZTZiZDgzIiwidXNlcm5hbWUiOiJSb2NrZXQiLCJlbWFpbCI6Impyb2NrZXRAZXhhbXBsZS5jb20iLCJhZGRyZXNzIjoiMTIzIEUuIFRlc3QgU3QuIiwicGhvbmVOdW1iZXIiOiIyMzk0ODcyODU0NzEiLCJyb2xlIjoidW5hdXRob3JpemVkX3VzZXIifQ.KfR7-YwSTdqrhMAZSb59g0FTT91CVcnHJsZR-gjR-p0";
        CreateMessageDTO message = createTestCreateMessageDTO(MessageType.TEXT);

        mockMvc.perform(MockMvcRequestBuilders.post("/messages")
                        .header("Authorization", "Bearer " + invalidToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(message)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andDo(MockMvcResultHandlers.print());
    }

}
