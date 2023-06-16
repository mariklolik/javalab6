package org.example.client;

import org.example.message.Message;
import org.example.message.MessageType;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Client {
    private final ObjectOutputStream out;
    private final ObjectInputStream in;
    private final BlockingQueue<String> messageQueue;
    private final BlockingQueue<String> clientsQueue;


    public final String clientId;

    public ClientGUI gui;

    public BlockingQueue<String> getClientsQueue() {
        return clientsQueue;
    }

    public Client(Socket socket, String clientId) throws IOException {
        this.clientId = clientId;
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
        messageQueue = new LinkedBlockingQueue<>();
        clientsQueue = new LinkedBlockingQueue<>();
        sendMessage(new Message(this.clientId, MessageType.POST_LOGIN, "NEW USER JOINED - %s".formatted(this.clientId)));
        // Start a separate thread to continuously receive messages from the server
        Thread receiveThread = new Thread(() -> {
            try {
                while (true) {
                    Object obj = in.readObject();
                    if (obj instanceof Message message) {
                        if (message.getMessageType() == MessageType.USER_LIST) {
                            clientsQueue.put(message.getText());

                        } else if (message.getMessageType() == MessageType.USER_KILL) {
                            this.gui.dispose();
                            System.out.println("You been inactive for too much time");
                            System.exit(0);
                        } else {
                            messageQueue.put(message.getText());
                        }
                    }
                }
            }
            catch (IOException | ClassNotFoundException | InterruptedException e) {
                System.err.println("Unable to establish connection with server");
                this.gui.dispose();
                System.exit(0);
            }
        });
        receiveThread.start();
    }

    public BlockingQueue<String> getMessageQueue() {
        return messageQueue;
    }

    public void sendMessage(Message message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
