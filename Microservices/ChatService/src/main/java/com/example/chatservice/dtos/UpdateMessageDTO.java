package com.example.chatservice.dtos;

import com.example.chatservice.enums.MessageStatus;
import com.example.chatservice.enums.MessageType;
import jakarta.annotation.Nullable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class UpdateMessageDTO {

    @Nullable
    private String content;

    @Nullable
    private MessageType type;

    @Nullable
    private MessageStatus status;


    public UpdateMessageDTO(String content, MessageType type, MessageStatus status) {
        this.content = content;
        this.status = status;
        this.type = type;
    }

    public static UpdateMessageDTO createMessageDTO(String content, MessageStatus status, MessageType type) {
        return new UpdateMessageDTO(content, type, status);
    }
}
