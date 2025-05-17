package com.example.chatservice.enums;

import lombok.Getter;

@Getter
public enum ReportType {
    SPAM("spam"),
    ABUSE("abuse"),
    HARASSMENT("harassment"),
    INAPPROPRIATE_CONTENT("inappropriate_content"),
    OTHER("other");

    private final String type;

    ReportType(String type) {
        this.type = type;
    }
}
