package org.example.server;

import org.example.message.Message;
import org.example.message.MessageType;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

    private static final int PORT = 1234;
    private static final List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("[INFO] Started at %s:%s".formatted(InetAddress.getLocalHost().getHostAddress(), PORT));
            System.out.println("Server started. Listening for incoming connections...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress().getHostAddress());

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();

                broadcastMessage(new Message("Server", MessageType.GET_USER_ADDED, "NEW USER!"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static synchronized void broadcastMessage(Message message) {
        System.out.printf("BROADCASTING [%s]: %s\n", message.getSender(), message.getText());
        message.setText(String.format("[%s]: %s", message.getSender(), message.getText()));
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }
}