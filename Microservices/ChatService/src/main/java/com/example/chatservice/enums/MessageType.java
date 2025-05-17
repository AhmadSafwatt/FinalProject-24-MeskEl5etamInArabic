package com.example.chatservice.enums;

import lombok.Getter;

@Getter
public enum MessageType {
    TEXT("text"),
    IMAGE("image"),
    PRODUCT("product");

    private final String type;

    MessageType(String type) {
        this.type = type;
    }

}
