package com.example.chatservice.commands;

    import com.example.chatservice.models.Message;
    import com.example.chatservice.services.MessageService;

    public class SendMessageCommand implements Command {
        private final Message message;
        private final MessageService messageService;

        public SendMessageCommand(Message message, MessageService messageService) {
            this.messageService = messageService;
            this.message = message;
        }

        @Override
        public void execute() {
            messageService.saveMessage(message);
        }
    }