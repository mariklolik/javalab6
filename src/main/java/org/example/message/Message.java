package org.example.message;

import java.io.Serializable;
import java.util.UUID;

public class Message implements Serializable {
    public UUID getId() {
        return id;
    }

    private final UUID id;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    private String text;

    public String getSender() {
        return sender;
    }

    private final String sender;

    public MessageType getMessageType() {
        return messageType;
    }

    private final MessageType messageType;

    public Message(String sender, MessageType messageType, String text) {
        this.id = UUID.randomUUID();
        this.sender = sender;
        this.messageType = messageType;
        this.text = text;
    }

}
