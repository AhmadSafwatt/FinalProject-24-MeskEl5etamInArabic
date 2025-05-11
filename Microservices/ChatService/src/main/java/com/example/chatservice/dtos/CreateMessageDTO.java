package com.example.chatservice.dtos;

import com.example.chatservice.enums.MessageType;
import com.example.chatservice.models.Message;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
public class CreateMessageDTO {

    private static final int MAX_CONTENT_LENGTH = Message.MAX_CONTENT_LENGTH;

    @NotNull(message = "senderId cannot be null")
    private UUID senderId;

    @NotNull(message = "receiverId cannot be null")
    private UUID receiverId;

    @NotNull
    @NotBlank
    @Size(max = MAX_CONTENT_LENGTH)
    private String content;

    @NotNull
    private MessageType type;

    public CreateMessageDTO(UUID senderId, UUID receiverId, String content, MessageType type) {
        this.content = content;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.type = type;
    }

    public static CreateMessageDTO createMessageDTO(UUID senderId, UUID receiverId, String content, MessageType type) {
        return new CreateMessageDTO(senderId, receiverId, content, type);
    }

}
