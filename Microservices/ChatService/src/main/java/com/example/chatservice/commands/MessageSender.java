package com.example.chatservice.commands;

import com.example.chatservice.commands.Command;

public class MessageSender {
    public void send(Command command) {
        command.execute();
    }
}