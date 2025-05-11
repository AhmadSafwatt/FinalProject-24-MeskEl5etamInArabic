package com.example.chatservice.dtos;

import com.example.chatservice.enums.MessageStatus;
import com.example.chatservice.enums.MessageType;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateMessageDTO {

    @Nullable
    private String content;

    @Nullable
    private MessageType type;

    @Nullable
    private MessageStatus status;
}