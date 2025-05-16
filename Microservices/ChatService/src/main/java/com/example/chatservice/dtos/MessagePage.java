package com.example.chatservice.dtos;

import com.example.chatservice.models.Message;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class MessagePage {
    private List<Message> messages;
    private String nextPagingState;

    public MessagePage(List<Message> messages, String nextPagingState) {
        this.messages = messages;
        this.nextPagingState = nextPagingState;
    }

}
