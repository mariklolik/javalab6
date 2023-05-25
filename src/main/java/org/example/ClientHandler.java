package org.example;

import org.example.message.Message;
import org.example.message.MessageType;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;
    private String clientName;

    public ClientHandler(Socket socket) throws IOException {
        this.clientSocket = socket;
        out = new ObjectOutputStream(clientSocket.getOutputStream());
        in = new ObjectInputStream(clientSocket.getInputStream());
    }

    @Override
    public void run() {
        try {
            Message firstMessage = (Message) in.readObject();
            System.out.printf("First message received from client: %s\n", firstMessage.getText());
            clientName = firstMessage.getSender();
            System.out.printf("Client connected: %s\n", clientName);
            Server.broadcastMessage(firstMessage);

            while (true) {
                Message message = (Message) in.readObject();
                System.out.printf("Message received from client: %s %s\n", clientName, message.getText());
                Server.broadcastMessage(message);
            }
        } catch (SocketException e) {
            System.err.println("Client disconnected: " + e.getMessage());
            Message message = new Message("Server", MessageType.POST_LOGOUT, "User logout: " + clientName);
            Server.broadcastMessage(message);
            try {
                closeResources();
            } catch (IOException ex) {
                System.out.println();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                closeResources();
            } catch (IOException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    public void sendMessage(Message message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }

    private void closeResources() throws IOException {
        out.close();
        in.close();
        clientSocket.close();
    }
}
