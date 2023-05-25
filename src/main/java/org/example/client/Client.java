package org.example.client;

import org.example.message.Message;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Client {
    private final PrintWriter out;
    private final ObjectInputStream in;
    private final BlockingQueue<String> messageQueue;

    private final String clientId;

    public Client(Socket socket, String clientId) throws IOException {
        this.clientId = clientId;
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new ObjectInputStream(socket.getInputStream());
        messageQueue = new LinkedBlockingQueue<>();

        // Start a separate thread to continuously receive messages from the server
        Thread receiveThread = new Thread(() -> {
            try {
                while (true) {
                    Object obj = in.readObject();
                    if (obj instanceof Message) {
                        Message message = (Message) obj;
                        messageQueue.put("Received from server: " + message.getText());
                    }
                }
            } catch (IOException | ClassNotFoundException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        receiveThread.start();
    }

    public BlockingQueue<String> getMessageQueue() {
        return messageQueue;
    }

    public void sendMessage(String message) {
        out.println(message);
        out.flush();
    }
}
