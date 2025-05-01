package com.example.chatservice;

import com.example.chatservice.models.Message;
import com.example.chatservice.repositories.MessageRepository;
import com.example.chatservice.services.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

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

	@Test
	void contextLoads() {
	}

	@Test
	void testGetMessages() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/messages/"))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andDo(MockMvcResultHandlers.print());
	}

	@Test
	void testSaveMessage_shouldReturnSavedMessage_whenValidMessage() throws Exception {
		// Arrange
		Message message = new Message();
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

		// Act: Perform the request and extract the response
		String responseContent = mockMvc.perform(MockMvcRequestBuilders.post("/messages/save")
						.contentType("application/json")
						.content(objectMapper.writeValueAsString(message)))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		// Deserialize the response to a Message object
		Message returnedMessage = objectMapper.readValue(responseContent, Message.class);

		// Assert: Use the returned ID to fetch the message from the database
		Message savedMessage = messageService.getMessageById(returnedMessage.getId());
		assertNotNull(savedMessage);
	}

	@Test
	void testSaveMessage_shouldNotSaveMessage_whenMessageIsNull() throws IllegalArgumentException {

		assertThrows(IllegalArgumentException.class, () -> {
			messageService.saveMessage(null);
		});
	}

	@Test
	void testDeleteMessage_shouldDeleteMessage_whenMessageExists() throws Exception {
		// Arrange: Create and save a message
		Message message = new Message();
		messageRepository.save(message);

		// Assert: Check that the message exists in the database before deletion
		Message existingMessage = messageService.getMessageById(message.getId());
		assertNotNull(existingMessage);
		assertEquals(message.getId(), existingMessage.getId());


		// Act: Perform the delete request
		mockMvc.perform(MockMvcRequestBuilders.delete("/messages/delete/" + message.getId()))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andDo(MockMvcResultHandlers.print());

		// Assert: Check that the message no longer exists in the database
		Message deletedMessage = messageService.getMessageById(message.getId());
		assertNull(deletedMessage);
	}
}
