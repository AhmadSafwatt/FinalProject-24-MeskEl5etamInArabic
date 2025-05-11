package com.example.chatservice.commands;

public interface Command<T> {
    T execute();
}