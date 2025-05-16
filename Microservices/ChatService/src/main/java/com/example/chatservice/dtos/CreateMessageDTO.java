package com.example.chatservice.dtos;

import com.example.chatservice.enums.MessageType;
import com.example.chatservice.models.Message;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMessageDTO {

    private static final int MAX_CONTENT_LENGTH = Message.MAX_CONTENT_LENGTH;

    @NotNull
    private UUID receiverId;

    @NotNull
    @NotBlank
    @Size(max = MAX_CONTENT_LENGTH)
    private String content;

    @NotNull
    private MessageType type;
}