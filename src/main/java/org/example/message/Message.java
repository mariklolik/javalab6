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

    private final String text;
    private final String from;
    private final MessageType messageType;

    public Message(String from, MessageType messageType, String text) {
        this.id = UUID.randomUUID();
        this.from = from;
        this.messageType = messageType;
        this.text = text;
    }

}
