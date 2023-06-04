package org.example.server;

import org.example.message.Message;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.example.message.MessageType.USER_LIST;

public class Server {

    private static final int PORT = 1234;
    private static final List<ClientHandler> clients = new ArrayList<>();

    private static final List<Message> chatHistory = new ArrayList<>();
    private static final int CHAT_HISTORY_SIZE = 10;

    public static void main(String[] args) {
        // добавить дисконнект по таймауту
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(PORT);
            System.out.printf("[INFO] Started at %s:%s%n", InetAddress.getLocalHost().getHostAddress(), PORT);
            System.out.println("Server started. Listening for incoming connections...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();

                sendChatHistory(clientHandler);

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
    public static void deleteUser(Socket clientSocket){
        for (int i = 0; i < clients.size(); i++) {
            if (Objects.equals(clients.get(i).getClientSocket(), clientSocket)){
                clients.remove(i);
                break;
            }
        }
    }
    public static synchronized void broadcastMessage(Message message) {
        System.out.printf("BROADCASTING [%s]: %s\n", message.getSender(), message.getText());
        message.setText(String.format("[%s]: %s", message.getSender(), message.getText()));
        addToChatHistory(message);
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    private static void addToChatHistory(Message message) {
        chatHistory.add(message);

        // Limit the chat history size
        if (chatHistory.size() > CHAT_HISTORY_SIZE) {
            chatHistory.remove(0);
        }
    }

    private static void sendChatHistory(ClientHandler clientHandler) {
        // Send the chat history as a series of messages to the client
        for (Message message : chatHistory) {
            clientHandler.sendMessage(message);
        }
    }

    public static synchronized void broadcastUserList() {

        List<String> names = new ArrayList<>();
        for (ClientHandler client: clients){
            names.add(client.getClientName());
        }
        Message msg = new Message("Server", USER_LIST, names.toString());
        System.out.printf("BROADCASTING USER-LIST %s%n", names);
        for (ClientHandler client : clients) {
            client.sendMessage(msg);
        }
    }
}