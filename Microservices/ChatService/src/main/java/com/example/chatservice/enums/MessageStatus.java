package com.example.chatservice.enums;

import lombok.Getter;

@Getter
public enum MessageStatus {
    SENT("sent"),
    DELIVERED("delivered"),
    SEEN("seem"),
    FAILED("failed");

    private final String status;

    MessageStatus(String status) {
        this.status = status;
    }

}
